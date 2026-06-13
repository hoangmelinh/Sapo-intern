package com.sapo.mock.clothing.crm.service;

import com.sapo.mock.clothing.crm.dto.request.CustomerCreateRequest;
import com.sapo.mock.clothing.crm.dto.request.CustomerUpdateRequest;
import com.sapo.mock.clothing.crm.dto.response.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    // Search ACTIVE customers by name or phone number.
    Page<CustomerResponse> searchCustomers(String keyword, Pageable pageable);

    // Retrieve customer details by ID.
    CustomerResponse getCustomerById(Integer id);

    // Handle customer creation.
    CustomerResponse createCustomer(CustomerCreateRequest request);

    // Update customer details.
    CustomerResponse updateCustomer(Integer id, CustomerUpdateRequest request);

    // Soft delete a customer.
    void deactivateCustomer(Integer id);}