package com.sapo.mock.clothing.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.sapo.mock.clothing.entity.Category;
import com.sapo.mock.clothing.entity.Product;

import java.util.List;

import org.springframework.data.domain.Pageable;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
	boolean existsByCategory_Id(Integer categoryId);

	boolean existsByName(String name);

	@Modifying
	@Query("UPDATE Product p SET p.category = :newCategory WHERE p.category.id = :oldCategoryId")
	void updateCategoryForProducts(@Param("oldCategoryId") Integer oldCategoryId, @Param("newCategory") Category newCategory);

	/**
	 * [AI Recommendation Fallback] Top sản phẩm bán chạy nhất.
	 * Dùng khi sản phẩm mới chưa có lịch sử Apriori hoặc ít rules.
	 */
	@Query(value = """
			SELECT pv.product_id
			FROM order_line_item oli
			JOIN product_variant pv ON oli.variant_id = pv.id
			JOIN product p ON pv.product_id = p.id
			WHERE p.is_deleted = false
			GROUP BY pv.product_id
			ORDER BY SUM(oli.quantity) DESC
			""", nativeQuery = true)
	List<Integer> findTopSellingProductIds(Pageable pageable);
}
