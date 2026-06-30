package com.sapo.mock.clothing.recommendation.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.sapo.mock.clothing.recommendation.service.AprioriService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Cronjob 2h sáng mỗi ngày: quét đơn cũ → tính Apriori → cập nhật rules.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationScheduler {

	private final AprioriService aprioriService;

	@Scheduled(cron = "${ai.recommendation.cron:0 0 2 * * ?}")
	public void rebuildRecommendationRules() {
		log.info(">>> [AI RECOMMENDATION] Bắt đầu tính toán lại luật gợi ý...");
		try {
			int rulesCreated = aprioriService.computeAndSaveRules();
			log.info(">>> [AI RECOMMENDATION] Hoàn thành. Đã tạo {} luật gợi ý.", rulesCreated);
		} catch (Exception e) {
			log.error(">>> [AI RECOMMENDATION] Lỗi khi tính toán: {}", e.getMessage(), e);
		}
	}
}
