package com.sapo.mock.clothing.recommendation.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO nhẹ cho API gợi ý sản phẩm (không dùng ProductResponse vì quá nặng).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {

	private Integer productId;
	private String productName;
	private String categoryName;
	private List<String> imageUrls;
	private BigDecimal minPrice;
	private Integer totalStock;
	private Double confidence;
}
