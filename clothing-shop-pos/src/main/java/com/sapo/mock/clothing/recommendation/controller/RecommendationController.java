package com.sapo.mock.clothing.recommendation.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sapo.mock.clothing.recommendation.dto.RecommendationResponse;
import com.sapo.mock.clothing.recommendation.service.AprioriService;
import com.sapo.mock.clothing.recommendation.service.RecommendationService;
import com.sapo.mock.clothing.util.annotation.ApiMessage;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'VIEW_ORDER', 'CREATE_ORDER', 'VIEW_PRODUCT')")
public class RecommendationController {

	private final RecommendationService recommendationService;
	private final AprioriService aprioriService;

	// GET /api/recommendations?productIds=105,210&limit=5
	@GetMapping
	@ApiMessage("Lấy danh sách sản phẩm gợi ý AI thành công")
	public ResponseEntity<List<RecommendationResponse>> getRecommendations(
			@RequestParam List<Integer> productIds,
			@RequestParam(defaultValue = "5") int limit) {
		return ResponseEntity.ok(recommendationService.getRecommendations(productIds, limit));
	}

	// POST /api/recommendations/rebuild — Admin trigger tính lại rules
	@PostMapping("/rebuild")
	@PreAuthorize("hasAuthority('ROLE_ADMIN')")
	@ApiMessage("Tính toán lại luật gợi ý AI thành công")
	public ResponseEntity<Map<String, Object>> rebuildRules() {
		int count = aprioriService.computeAndSaveRules();
		return ResponseEntity.ok(Map.of(
				"rulesCreated", count,
				"message", "Đã tính toán lại " + count + " luật gợi ý sản phẩm"));
	}
}
