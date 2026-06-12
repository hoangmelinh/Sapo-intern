package com.sapo.mock.clothing.crm.entity;

import com.sapo.mock.clothing.product.entity.Product;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "return_issue_item")
public class ReturnIssueItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "return_ticket_id", nullable = false)
    private ReturnTicket returnTicket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Snapshot tại thời điểm đổi hàng
    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "product_sku", nullable = false, length = 50)
    private String productSku;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
    private BigDecimal unitPrice;

    // subtotal = quantity * unit_price — tính trong DB (STORED), ánh xạ read-only
    @Column(nullable = false, precision = 15, scale = 2, insertable = false, updatable = false)
    private BigDecimal subtotal;
}
