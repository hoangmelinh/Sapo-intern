package com.sapo.mock.clothing.customer.dto.response;

import com.sapo.mock.clothing.util.constant.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class OrderHistoryResponse {
    private Integer id;
    private String orderNumber;
    private Integer customerId;
    private String customerName;
    private Integer createdById;
    private String createdByUsername;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal changeAmount;
    private OrderStatus status;
    private String note;
    private Instant createdAt;
    private Instant updatedAt;
    private boolean printed;
    private List<ItemInfo> items;

    @Data
    public static class ItemInfo {
        private Integer id;
        private Integer variantId;
        private String productName;
        private String productSku;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}