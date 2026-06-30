package com.sapo.mock.clothing.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Lưu kết quả thuật toán Apriori: "Khách mua SP A → gợi ý mua thêm SP B".
 * Bảng này được rebuild mỗi đêm bởi cronjob (atomic swap).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "recommendation_rule",
		uniqueConstraints = @UniqueConstraint(columnNames = { "antecedent_product_id", "consequent_product_id" }))
public class RecommendationRule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "antecedent_product_id", nullable = false)
	private Integer antecedentProductId; // SP khách đang chọn (A)

	@Column(name = "consequent_product_id", nullable = false)
	private Integer consequentProductId; // SP gợi ý mua kèm (B)

	@Column(nullable = false)
	private Double confidence; // VD: 0.75 = 75% khách mua A cũng mua B

	@Column(nullable = false)
	private Double lift; // > 1.0 = mua A thực sự tăng khả năng mua B

	@Column(name = "support_count", nullable = false)
	private Integer supportCount; // Số đơn chứa cả A và B

	@Column(name = "updated_at")
	private Instant updatedAt;

	@PrePersist
	public void prePersist() {
		this.updatedAt = Instant.now();
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = Instant.now();
	}
}
