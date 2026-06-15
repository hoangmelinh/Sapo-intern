package com.sapo.mock.clothing.invoice.service;

import com.sapo.mock.clothing.entity.*;
import com.sapo.mock.clothing.exception.BadRequestException;
import com.sapo.mock.clothing.exception.ResourceNotFoundException;
import com.sapo.mock.clothing.invoice.dto.ReqCreateInvoiceDTO;
import com.sapo.mock.clothing.invoice.dto.ResInvoiceDTO;
import com.sapo.mock.clothing.invoice.repository.InvoiceItemRepository;
import com.sapo.mock.clothing.invoice.repository.InvoiceRepository;
import com.sapo.mock.clothing.product.repository.ProductRepository;
import com.sapo.mock.clothing.user.repository.UserRepository;
import com.sapo.mock.clothing.customer.repository.CustomerRepository;
import com.sapo.mock.clothing.warehouse.repository.warehouseRepository;
import com.sapo.mock.clothing.util.constant.InvoiceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class InvoiceServiceTest {

    @Mock
    private InvoiceRepository invoiceRepository;
    @Mock
    private InvoiceItemRepository invoiceItemRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private warehouseRepository warehouseRepository;
    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private InvoiceService invoiceService;

    private User mockUser;
    private Warehouse mockWarehouse;
    private Customer mockCustomer;
    private Product mockProduct;
    private ReqCreateInvoiceDTO mockReqDto;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1);
        mockUser.setUsername("testuser");

        mockWarehouse = new Warehouse();
        mockWarehouse.setId(1);
        mockWarehouse.setName("Kho Trung Tâm");

        mockCustomer = new Customer();
        mockCustomer.setId(1);
        mockCustomer.setFullName("Nguyễn Văn A");

        mockProduct = new Product();
        mockProduct.setId(1);
        mockProduct.setName("Áo thun");
        mockProduct.setSku("AT-01");
        mockProduct.setSalePrice(new BigDecimal("100000"));

        mockReqDto = new ReqCreateInvoiceDTO();
        mockReqDto.setCustomerId(1);
        mockReqDto.setWarehouseId(1);
        mockReqDto.setNote("Test invoice");
        mockReqDto.setPaidAmount(new BigDecimal("200000"));

        ReqCreateInvoiceDTO.InvoiceItemDTO itemDto = new ReqCreateInvoiceDTO.InvoiceItemDTO();
        itemDto.setProductId(1);
        itemDto.setQuantity(2);

        mockReqDto.setItems(Collections.singletonList(itemDto));
    }

    @Test
    void createInvoice_Success() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(mockUser);
        when(warehouseRepository.findById(1)).thenReturn(Optional.of(mockWarehouse));
        when(customerRepository.findById(1)).thenReturn(Optional.of(mockCustomer));
        when(productRepository.findById(1)).thenReturn(Optional.of(mockProduct));

        when(invoiceRepository.countByCreatedAtAfter(any())).thenReturn(0L);

        Invoice savedInvoice = new Invoice();
        savedInvoice.setId(100);
        savedInvoice.setCode("HD-20230101-001");
        savedInvoice.setCustomerId(1);
        savedInvoice.setCustomerName("Nguyễn Văn A");
        savedInvoice.setWarehouseId(1);
        savedInvoice.setWarehouseName("Kho Trung Tâm");
        savedInvoice.setCreatedBy(1);
        savedInvoice.setCreatedByUsername("testuser");
        savedInvoice.setTotalAmount(new BigDecimal("200000"));
        savedInvoice.setPaidAmount(new BigDecimal("200000"));
        savedInvoice.setChangeAmount(BigDecimal.ZERO);
        savedInvoice.setStatus(InvoiceStatus.COMPLETED);

        when(invoiceRepository.save(any(Invoice.class))).thenReturn(savedInvoice);

        // Act
        ResInvoiceDTO result = invoiceService.createInvoice(mockReqDto, "testuser");

        // Assert
        assertNotNull(result);
        assertEquals(100, result.getId());
        assertEquals("Nguyễn Văn A", result.getCustomerName());
        assertEquals("testuser", result.getCreatedByUsername());
        assertEquals(new BigDecimal("200000"), result.getTotalAmount());

        verify(invoiceRepository, times(1)).save(any(Invoice.class));
        verify(invoiceItemRepository, times(1)).saveAll(anyList());
    }

    @Test
    void createInvoice_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> {
            invoiceService.createInvoice(mockReqDto, "unknown");
        });
    }

    @Test
    void createInvoice_InsufficientPaidAmount_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(mockUser);
        when(warehouseRepository.findById(1)).thenReturn(Optional.of(mockWarehouse));
        when(customerRepository.findById(1)).thenReturn(Optional.of(mockCustomer));
        when(productRepository.findById(1)).thenReturn(Optional.of(mockProduct));

        // Khách đưa 50k, nhưng tổng tiền là 200k (100k x 2)
        mockReqDto.setPaidAmount(new BigDecimal("50000"));

        // Act & Assert
        assertThrows(BadRequestException.class, () -> {
            invoiceService.createInvoice(mockReqDto, "testuser");
        });
    }
}
