package com.sapo.mock.clothing.recommendation.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sapo.mock.clothing.entity.Product;
import com.sapo.mock.clothing.entity.ProductVariant;
import com.sapo.mock.clothing.entity.RecommendationRule;
import com.sapo.mock.clothing.product.repository.ProductRepository;
import com.sapo.mock.clothing.recommendation.dto.RecommendationResponse;
import com.sapo.mock.clothing.recommendation.repository.RecommendationRuleRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Trả gợi ý sản phẩm real-time cho POS.
 * Chỉ SELECT từ bảng pre-computed nên rất nhanh.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

	private final RecommendationRuleRepository recommendationRuleRepository;
	private final ProductRepository productRepository;

	/**
	 * Lấy gợi ý dựa trên toàn bộ giỏ hàng hiện tại.
	 * Nếu ít kết quả (<3) thì bổ sung thêm top sản phẩm bán chạy.
	 */
	@Transactional(readOnly = true)
	public List<RecommendationResponse> getRecommendations(List<Integer> cartProductIds, int limit) {
		if (cartProductIds == null || cartProductIds.isEmpty()) {
			return getFallbackRecommendations(List.of(), limit);
		}

		// Query rules, lấy dư để sau khi lọc vẫn đủ
		List<RecommendationRule> rules = recommendationRuleRepository
				.findByAntecedentProductIdInOrderByConfidenceDesc(
						cartProductIds, PageRequest.of(0, limit * 5));

		// Bỏ SP đã có trong giỏ, group theo consequent lấy MAX confidence
		Map<Integer, Double> bestConfidence = new LinkedHashMap<>();
		for (RecommendationRule rule : rules) {
			Integer consequentId = rule.getConsequentProductId();
			if (cartProductIds.contains(consequentId)) {
				continue;
			}
			bestConfidence.merge(consequentId, rule.getConfidence(), Math::max);
		}

		// Top N theo confidence
		List<Integer> topProductIds = bestConfidence.entrySet().stream()
				.sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
				.limit(limit)
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());

		// Lấy thông tin SP, lọc bỏ đã xóa / hết hàng
		List<RecommendationResponse> result = new ArrayList<>();
		if (!topProductIds.isEmpty()) {
			List<Product> products = productRepository.findAllById(topProductIds);

			for (Product product : products) {
				if (Boolean.TRUE.equals(product.getIsDeleted())) {
					continue;
				}

				int totalStock = 0;
				BigDecimal minPrice = null;
				for (ProductVariant variant : product.getVariants()) {
					totalStock += variant.getQuantity();
					if (minPrice == null || variant.getSalePrice().compareTo(minPrice) < 0) {
						minPrice = variant.getSalePrice();
					}
				}

				if (totalStock <= 0) {
					continue;
				}

				result.add(RecommendationResponse.builder()
						.productId(product.getId())
						.productName(product.getName())
						.categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
						.imageUrls(product.getImageUrls())
						.minPrice(minPrice)
						.totalStock(totalStock)
						.confidence(bestConfidence.get(product.getId()))
						.build());
			}

			// findAllById không giữ thứ tự nên sort lại
			result.sort(Comparator.comparingDouble(RecommendationResponse::getConfidence).reversed());
		}

		// Fallback nếu ít kết quả
		if (result.size() < 3) {
			List<Integer> excludeIds = new ArrayList<>(cartProductIds);
			excludeIds.addAll(result.stream()
					.map(RecommendationResponse::getProductId)
					.collect(Collectors.toList()));

			List<RecommendationResponse> fallback = getFallbackRecommendations(excludeIds, limit - result.size());
			result.addAll(fallback);
		}

		return result.stream().limit(limit).collect(Collectors.toList());
	}

	/**
	 * Fallback: gợi ý top SP bán chạy nhất khi chưa có đủ Apriori rules.
	 */
	private List<RecommendationResponse> getFallbackRecommendations(List<Integer> excludeIds, int limit) {
		if (limit <= 0) {
			return List.of();
		}

		List<Integer> topSellingIds = productRepository.findTopSellingProductIds(
				PageRequest.of(0, limit + excludeIds.size()));

		List<Integer> filteredIds = topSellingIds.stream()
				.filter(id -> !excludeIds.contains(id))
				.limit(limit)
				.collect(Collectors.toList());

		if (filteredIds.isEmpty()) {
			return List.of();
		}

		List<Product> products = productRepository.findAllById(filteredIds);
		List<RecommendationResponse> result = new ArrayList<>();

		for (Product product : products) {
			if (Boolean.TRUE.equals(product.getIsDeleted())) {
				continue;
			}

			int totalStock = 0;
			BigDecimal minPrice = null;
			for (ProductVariant variant : product.getVariants()) {
				totalStock += variant.getQuantity();
				if (minPrice == null || variant.getSalePrice().compareTo(minPrice) < 0) {
					minPrice = variant.getSalePrice();
				}
			}

			if (totalStock <= 0) {
				continue;
			}

			result.add(RecommendationResponse.builder()
					.productId(product.getId())
					.productName(product.getName())
					.categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
					.imageUrls(product.getImageUrls())
					.minPrice(minPrice)
					.totalStock(totalStock)
					.confidence(null)
					.build());
		}

		return result;
	}
}
