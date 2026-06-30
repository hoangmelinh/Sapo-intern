package com.sapo.mock.clothing.recommendation.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.sapo.mock.clothing.entity.RecommendationRule;

@Repository
public interface RecommendationRuleRepository extends JpaRepository<RecommendationRule, Integer> {

	// Lấy gợi ý cho nhiều SP (cả giỏ hàng), sắp theo confidence giảm dần
	List<RecommendationRule> findByAntecedentProductIdInOrderByConfidenceDesc(
			List<Integer> antecedentProductIds, Pageable pageable);

	// Xóa sạch rules cũ — CHỈ gọi bên trong atomic swap transaction
	@Modifying
	@Query("DELETE FROM RecommendationRule r")
	void deleteAllRules();
}
