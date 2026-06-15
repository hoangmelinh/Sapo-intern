//package com.sapo.mock.clothing.specification;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.springframework.data.jpa.domain.Specification;
//
//import com.sapo.mock.clothing.entity.Product;
//
//import jakarta.persistence.criteria.Predicate;
//
//public class ProductSpecification {
//	public static Specification<Product> build(String search, String productName, String sku, String category,
//			Boolean isDeleted) {
//
//		return (root, query, cb) -> {
//			List<Predicate> predicates = new ArrayList<>();
//			if (search != null && !search.trim().isEmpty()) {
//				String pattern = "%" + search.trim().toLowerCase() + "%";
//
//				Predicate nameLike = cb.like(cb.lower(root.get("name")), pattern);
//				Predicate skuLike = cb.like(cb.lower(root.get("sku")), pattern);
//
//				predicates.add(cb.or(nameLike, skuLike));
//
//			}
//
//			if (productName != null && !productName.isBlank()) {
//				predicates.add(cb.like(cb.lower(root.get("name")), "%" + productName.toLowerCase().trim() + "%"));
//
//			}
//
//			if (sku != null && !sku.isBlank()) {
//				predicates.add(cb.like(cb.lower(root.get("sku")), "%" + sku.toLowerCase().trim() + "%"));
//			}
//			if (isDeleted != null) {
//				predicates.add(cb.equal(root.get("isDeleted"), isDeleted));
//			}
//
//			return cb.and(predicates.toArray(new Predicate[0]));
//		};
//
//	}
//}
package com.sapo.mock.clothing.specification;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.sapo.mock.clothing.entity.Product;
import com.sapo.mock.clothing.entity.ProductVariant; // Import Entity mới

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

public class ProductSpecification {
	public static Specification<Product> build(String search, String productName, String sku, Integer categoryId,
			Boolean isDeleted) {

		return (root, query, cb) -> {
			// QUAN TRỌNG: Loại bỏ các kết quả lặp lại (duplicate) khi dùng phép JOIN
			// OneToMany
			query.distinct(true);

			List<Predicate> predicates = new ArrayList<>();

			// Khởi tạo Join (Chỉ Join 1 lần duy nhất nếu có điều kiện tìm kiếm liên quan
			// đến Variant)
			Join<Product, ProductVariant> variantJoin = null;
			boolean needVariantJoin = (search != null && !search.trim().isEmpty()) || (sku != null && !sku.isBlank());

			if (needVariantJoin) {
				// Dùng LEFT JOIN để tránh làm rớt các Product chưa kịp khởi tạo Variant
				variantJoin = root.join("variants", JoinType.LEFT);
			}

			// 1. Lọc theo chuỗi tìm kiếm chung (Tên Sản phẩm HOẶC Sku Biến thể)
			if (search != null && !search.trim().isEmpty()) {
				String pattern = "%" + search.trim().toLowerCase() + "%";

				Predicate nameLike = cb.like(cb.lower(root.get("name")), pattern);
				Predicate skuLike = cb.like(cb.lower(variantJoin.get("sku")), pattern); // Truy xuất sku từ bảng Join

				predicates.add(cb.or(nameLike, skuLike));
			}

			// 2. Lọc chính xác theo tên sản phẩm
			if (productName != null && !productName.isBlank()) {
				predicates.add(cb.like(cb.lower(root.get("name")), "%" + productName.toLowerCase().trim() + "%"));
			}

			// 3. Lọc chính xác theo SKU
			if (sku != null && !sku.isBlank()) {
				predicates.add(cb.like(cb.lower(variantJoin.get("sku")), "%" + sku.toLowerCase().trim() + "%"));
			}

			// 4. Lọc theo Category (Bạn khai báo trên tham số nhưng chưa dùng trong hàm cũ)
			if (categoryId != null) {
				predicates.add(cb.equal(root.get("category").get("id"), categoryId));
			}

			// 5. Lọc trạng thái xóa
			if (isDeleted != null) {
				predicates.add(cb.equal(root.get("isDeleted"), isDeleted));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}