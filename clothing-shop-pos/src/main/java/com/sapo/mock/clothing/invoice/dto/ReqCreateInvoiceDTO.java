package com.sapo.mock.clothing.invoice.dto;

import java.math.BigDecimal;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreateInvoiceDTO {
    @NotNull(message = "Customer ID không được để trống")
    private Integer customerId;

    @NotNull(message = "Warehouse ID không được để trống")
    private Integer warehouseId;

    @NotNull(message = "Số tiền khách trả không được để trống")
    private BigDecimal paidAmount;

    private String note;
    private List<InvoiceItemDTO> items;

    @Getter
    @Setter
    public static class InvoiceItemDTO {
        private Integer variantId;
        private Integer quantity;
    }
}
