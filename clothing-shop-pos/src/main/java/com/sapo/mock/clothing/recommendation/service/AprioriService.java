package com.sapo.mock.clothing.recommendation.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sapo.mock.clothing.entity.RecommendationRule;
import com.sapo.mock.clothing.order.repository.OrderLineItemRepository;
import com.sapo.mock.clothing.recommendation.repository.RecommendationRuleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Thuật toán Apriori - Phân tích giỏ hàng (Market Basket Analysis).
 * Pha 1: Query dữ liệu → Pha 2: Tính toán trong memory → Pha 3: Atomic swap vào DB.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AprioriService {

	private final OrderLineItemRepository orderLineItemRepository;
	private final RecommendationRuleRepository recommendationRuleRepository;

	@Value("${ai.recommendation.min-support:0.02}")
	private double minSupport;

	@Value("${ai.recommendation.min-confidence:0.3}")
	private double minConfidence;

	@Value("${ai.recommendation.min-lift:1.0}")
	private double minLift;

	@Value("${ai.recommendation.data-months:6}")
	private int dataMonths;

	/**
	 * Chạy toàn bộ pipeline Apriori và lưu kết quả.
	 * Gọi bởi Cronjob hoặc API manual trigger.
	 */
	@Transactional
	public int computeAndSaveRules() {

		// --- Pha 1: Thu thập dữ liệu (không nằm trong transaction) ---

		Instant cutoffDate = Instant.now().minus(dataMonths * 30L, ChronoUnit.DAYS);
		log.info(">>> [APRIORI] Pha 1: Query đơn hàng từ {} trở lại...", cutoffDate);

		List<Object[]> rawPairs = orderLineItemRepository.findOrderProductPairsSince(cutoffDate);

		if (rawPairs.isEmpty()) {
			log.warn(">>> [APRIORI] Không có dữ liệu đơn hàng nào trong {} tháng gần nhất. Bỏ qua.", dataMonths);
			return 0;
		}

		// Group theo orderId → tạo các "giỏ hàng" = Set<productId>
		Map<Integer, Set<Integer>> transactions = new HashMap<>();
		for (Object[] row : rawPairs) {
			Integer orderId = ((Number) row[0]).intValue();
			Integer productId = ((Number) row[1]).intValue();
			transactions.computeIfAbsent(orderId, k -> new HashSet<>()).add(productId);
		}

		// Giỏ chỉ có 1 item thì không tạo được rule
		List<Set<Integer>> validBaskets = transactions.values().stream()
				.filter(basket -> basket.size() >= 2)
				.collect(Collectors.toList());

		int totalTransactions = validBaskets.size();
		log.info(">>> [APRIORI] Pha 1 xong: {} đơn, {} giỏ hợp lệ (>=2 SP)",
				transactions.size(), totalTransactions);

		if (totalTransactions < 5) {
			log.warn(">>> [APRIORI] Quá ít giỏ hàng hợp lệ ({}). Bỏ qua.", totalTransactions);
			return 0;
		}

		// --- Pha 2: Tính toán Apriori (trong memory, chưa ghi DB) ---

		log.info(">>> [APRIORI] Pha 2: Tính toán Apriori...");

		// Đếm support: mỗi product xuất hiện trong bao nhiêu giỏ
		Map<Integer, Integer> itemSupportCount = new HashMap<>();
		for (Set<Integer> basket : validBaskets) {
			for (Integer productId : basket) {
				itemSupportCount.merge(productId, 1, Integer::sum);
			}
		}

		// Lọc frequent items: support(A) = countA / totalTransactions >= minSupport
		Set<Integer> frequentItems = itemSupportCount.entrySet().stream()
				.filter(entry -> (double) entry.getValue() / totalTransactions >= minSupport)
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());

		log.info(">>> [APRIORI] {} sản phẩm frequent (support >= {}%)",
				frequentItems.size(), minSupport * 100);

		if (frequentItems.size() < 2) {
			log.warn(">>> [APRIORI] Không đủ frequent items để tạo rules. Bỏ qua.");
			return 0;
		}

		// Đếm co-occurrence: bao nhiêu giỏ chứa cả A lẫn B
		Map<Long, Integer> pairSupportCount = new HashMap<>();
		for (Set<Integer> basket : validBaskets) {
			List<Integer> frequentInBasket = basket.stream()
					.filter(frequentItems::contains)
					.collect(Collectors.toList());

			// Ordered pairs vì A→B ≠ B→A
			for (int i = 0; i < frequentInBasket.size(); i++) {
				for (int j = 0; j < frequentInBasket.size(); j++) {
					if (i != j) {
						long pairKey = encodePair(frequentInBasket.get(i), frequentInBasket.get(j));
						pairSupportCount.merge(pairKey, 1, Integer::sum);
					}
				}
			}
		}

		// Tính Confidence + Lift, lọc theo ngưỡng
		List<RecommendationRule> newRules = new ArrayList<>();

		for (Map.Entry<Long, Integer> entry : pairSupportCount.entrySet()) {
			int[] pair = decodePair(entry.getKey());
			int antecedentId = pair[0];
			int consequentId = pair[1];
			int countAB = entry.getValue();

			int countA = itemSupportCount.get(antecedentId);
			int countB = itemSupportCount.get(consequentId);

			// Confidence(A→B) = countAB / countA
			double confidence = (double) countAB / countA;

			// Lift(A→B) = (countAB * total) / (countA * countB) — dùng tỷ lệ, không dùng số tuyệt đối
			double lift = ((double) countAB * totalTransactions) / ((double) countA * countB);

			if (confidence >= minConfidence && lift >= minLift) {
				newRules.add(RecommendationRule.builder()
						.antecedentProductId(antecedentId)
						.consequentProductId(consequentId)
						.confidence(confidence)
						.lift(lift)
						.supportCount(countAB)
						.build());
			}
		}

		log.info(">>> [APRIORI] Pha 2 xong: {} luật thỏa mãn (confidence >= {}%, lift >= {})",
				newRules.size(), minConfidence * 100, minLift);

		// --- Pha 3: Atomic swap — delete cũ + saveAll mới trong 1 transaction ---

		if (!newRules.isEmpty()) {
			atomicSwapRules(newRules);
			log.info(">>> [APRIORI] Pha 3 xong: Atomic swap {} rules thành công.", newRules.size());
		} else {
			log.info(">>> [APRIORI] Không có rules mới. Giữ nguyên rules cũ.");
		}

		return newRules.size();
	}

	/**
	 * Xóa rules cũ + ghi rules mới trong cùng 1 transaction.
	 * Đảm bảo API online không bao giờ thấy bảng trống.
	 */
	@Transactional
	public void atomicSwapRules(List<RecommendationRule> newRules) {
		recommendationRuleRepository.deleteAllRules();
		recommendationRuleRepository.saveAll(newRules);
	}

	// Encode cặp (A, B) thành 1 long key cho HashMap — A→B ≠ B→A
	private long encodePair(int a, int b) {
		return ((long) a << 32) | (b & 0xFFFFFFFFL);
	}

	private int[] decodePair(long key) {
		return new int[] { (int) (key >> 32), (int) key };
	}
}
