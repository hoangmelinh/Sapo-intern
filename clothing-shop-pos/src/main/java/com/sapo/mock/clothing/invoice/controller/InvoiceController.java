package com.sapo.mock.clothing.invoice.controller;

import com.sapo.mock.clothing.invoice.dto.ReqCreateInvoiceDTO;
import com.sapo.mock.clothing.invoice.dto.ResInvoiceDTO;
import com.sapo.mock.clothing.exception.IdInvalidException;
import com.sapo.mock.clothing.invoice.service.InvoiceService;
import com.sapo.mock.clothing.util.SecurityUtil;
import com.sapo.mock.clothing.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @ApiMessage("Tạo mới hóa đơn bán hàng thành công")
    public ResponseEntity<ResInvoiceDTO> createInvoice(@Valid @RequestBody ReqCreateInvoiceDTO dto) throws IdInvalidException {
        String username = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("Vui lòng đăng nhập"));
        ResInvoiceDTO newInvoice = invoiceService.createInvoice(dto, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(newInvoice);
    }
}