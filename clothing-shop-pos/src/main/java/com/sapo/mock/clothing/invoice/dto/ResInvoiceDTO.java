package com.sapo.mock.clothing.invoice.dto;

import com.sapo.mock.clothing.util.constant.InvoiceStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Builder
public class ResInvoiceDTO {
    private Integer id;
    private String code;

    private Integer customerId;
    private String customerName;

    private Integer warehouseId;
    private String warehouseName;

    private Integer createdById;
    private String createdByUsername;

    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal changeAmount;

    private InvoiceStatus status;
    private String note;

    private Instant createdAt;
    private Instant updatedAt;

    private List<ResInvoiceItemDTO> items;

    @Getter
    @Setter
    @Builder
    public static class ResInvoiceItemDTO {
        private Integer id;
        private Integer productId;
        private String productName;
        private String productSku;
        private int quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
