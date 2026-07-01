package com.sapo.mock.clothing.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class VoucherRequest {
    @NotBlank(message = "Tên voucher không được để trống")
    private String name;

    @NotBlank(message = "Mã code không được để trống")
    private String code;

    @NotNull(message = "Mức giảm giá không được để trống")
    private BigDecimal discountAmount;

    private BigDecimal minOrderValue;
}
