package com.sapo.mock.clothing.customer.service.impl;


import com.sapo.mock.clothing.customer.dto.response.CustomerGroupResponse;
import com.sapo.mock.clothing.customer.dto.response.CustomerResponse;
import com.sapo.mock.clothing.customer.repository.CustomerGroupRepository;
import com.sapo.mock.clothing.customer.repository.CustomerRepository;
import com.sapo.mock.clothing.customer.repository.CustomerVoucherRepository;
import com.sapo.mock.clothing.customer.service.CustomerGroupService;
import com.sapo.mock.clothing.entity.Customer;
import com.sapo.mock.clothing.entity.CustomerGroup;
import com.sapo.mock.clothing.entity.CustomerVoucher;
import com.sapo.mock.clothing.util.constant.CustomerStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerGroupServiceImpl implements CustomerGroupService {

    @Autowired
    private CustomerGroupRepository groupRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerGroupRepository customerGroupRepository;

    @Autowired
    private CustomerVoucherRepository customerVoucherRepository;


    // Retrieve all active customer groups.
    @Override
    public Page<CustomerGroupResponse> getGroupsWithPage(Pageable pageable) {
        return customerGroupRepository.findAllActiveGroups(pageable);
    }



    //  tìm kiếm nhóm ACTIVE
    @Override
    public Page<CustomerGroupResponse> searchGroups(String keyword, Pageable pageable) {
        String cleanKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return customerGroupRepository.searchGroups(cleanKeyword, pageable);
    }

    @Override
    public CustomerGroupResponse getGroupById(Integer id) {
        return customerGroupRepository.findGroupDetailById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhóm khách hàng với ID: " + id));
    }




    // Hàm phụ trợ convert từ Entity sang Response DTO gọn gàng
    private CustomerResponse convertToResponse(Customer customer) {
        CustomerResponse res = new CustomerResponse();
        res.setId(customer.getId());
        res.setFullName(customer.getFullName());
        res.setPhone(customer.getPhone());
        res.setDateOfBirth(customer.getDateOfBirth());
        res.setGender(customer.getGender());
        res.setAddress(customer.getAddress());
        res.setNote(customer.getNote());
        res.setStatus(customer.getStatus());
        res.setCreatedAt(customer.getCreatedAt());
        res.setRewardPoints(customer.getRewardPoints());

        // Bọc thông tin nhóm đi kèm để Frontend lấy được tên nhóm hiển thị lên màn hình
        if (customer.getCustomerGroup() != null) {
            CustomerResponse.GroupInfo groupInfo = new CustomerResponse.GroupInfo();
            groupInfo.setId(customer.getCustomerGroup().getId());
            groupInfo.setName(customer.getCustomerGroup().getName());
            groupInfo.setCode(customer.getCustomerGroup().getCode());
            res.setCustomerGroup(groupInfo);
        }

        // Fetch voucher — có thì set, không có thì để null
        List<CustomerVoucher> vouchers =
                customerVoucherRepository.findByCustomerIdOrderByReceivedAtDesc(customer.getId());
        if (vouchers != null && !vouchers.isEmpty()) {
            List<CustomerResponse.VoucherInfo> voucherInfos = vouchers.stream().map(cv -> {
                CustomerResponse.VoucherInfo vi = new CustomerResponse.VoucherInfo();
                vi.setId(cv.getId());
                vi.setVoucherCode(cv.getVoucher().getCode());
                vi.setVoucherName(cv.getVoucher().getName());
                vi.setDiscountAmount(cv.getVoucher().getDiscountAmount());
                vi.setMinOrderValue(cv.getVoucher().getMinOrderValue());
                vi.setStatus(cv.getStatus());
                vi.setReceivedAt(cv.getReceivedAt());
                vi.setExpiredAt(cv.getExpiredAt());
                vi.setUsedAt(cv.getUsedAt());
                return vi;
            }).collect(Collectors.toList());
            res.setVouchers(voucherInfos);
        }
        // Không có voucher → giữ null

        return res;
    }


}
