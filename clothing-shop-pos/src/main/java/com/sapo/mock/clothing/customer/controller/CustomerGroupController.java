package com.sapo.mock.clothing.customer.controller;

import com.sapo.mock.clothing.common.dto.response.RestResponse;
import com.sapo.mock.clothing.customer.dto.event.OrderCompletedEvent;
import com.sapo.mock.clothing.customer.dto.request.groupcustomer.MockOrderRequest;
import com.sapo.mock.clothing.customer.dto.response.CustomerGroupResponse;
import com.sapo.mock.clothing.customer.dto.response.CustomerResponse;
import com.sapo.mock.clothing.customer.service.CustomerGroupService;
import com.sapo.mock.clothing.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crm/customer-groups")
@CrossOrigin(origins = "*")
public class CustomerGroupController {

    @Autowired
    private CustomerGroupService groupService;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private ApplicationEventPublisher eventPublisher;



    /**
     * API: Lấy TẤT CẢ các nhóm khách hàng hiện có (Không phân trang)
     * Endpoint: GET /api/crm/customer-groups/all
     */
    @GetMapping("")
    public ResponseEntity<RestResponse<Page<CustomerGroupResponse>>> getGroupsWithPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());

        Page<CustomerGroupResponse> result = groupService.getGroupsWithPage(pageable);

        RestResponse<Page<CustomerGroupResponse>> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setError(null);
        response.setMessage("Lấy danh sách phân trang nhóm khách hàng thành công");
        response.setData(result);

        return ResponseEntity.ok(response);
    }

    /**
     * API 1: Xem danh sách phân trang và tìm kiếm nhóm khách hàng
     * Endpoint: GET /api/crm/customer-groups/search?keyword=Vùng A
     */
    @GetMapping("/search")
    public ResponseEntity<RestResponse<Page<CustomerGroupResponse>>> searchGroups(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<CustomerGroupResponse> result = groupService.searchGroups(keyword, pageable);

        RestResponse<Page<CustomerGroupResponse>> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setError(null);
        response.setMessage("Tra cứu danh sách nhóm khách hàng thành công");
        response.setData(result);

        return ResponseEntity.ok(response);
    }

    /**
     * API: Xem chi tiết thông tin cấu hình một nhóm khách hàng
     * Endpoint: GET /api/crm/customer-groups/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<RestResponse<CustomerGroupResponse>> getGroupDetail(@PathVariable Integer id) {

        // Gọi service lấy dữ liệu chi tiết nhóm
        CustomerGroupResponse result = groupService.getGroupById(id);

        RestResponse<CustomerGroupResponse> response = new RestResponse<>(
                HttpStatus.OK.value(),
                null,
                "Lấy chi tiết nhóm khách hàng thành công",
                result
        );

        return ResponseEntity.ok(response);
    }





    /**
     * API: Lấy danh sách + Tìm kiếm thành viên trong từng nhóm cụ thể
     * URL TEST POSTMAN:
     * - Chỉ lọc theo nhóm: GET http://localhost:8080/api/v1/crm/customers/1/members?page=0&size=10
     * - Tìm kiếm trong nhóm: GET http://localhost:8080/api/v1/crm/customers/1/members?keyword=Đinh&page=0&size=10
     */
    @GetMapping("/{groupId}/members")
    public ResponseEntity<RestResponse<Page<CustomerResponse>>> getGroupMembers(
            @PathVariable Integer groupId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Sắp xếp danh sách khách hàng mới gia nhập hạng lên đầu tiên
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        // Truyền thêm param keyword vào service
        Page<CustomerResponse> result = customerService.getCustomersByGroupId(groupId, keyword, pageable);

        RestResponse<Page<CustomerResponse>> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.OK.value());
        response.setError(null);
        response.setMessage("Lấy danh sách và tìm kiếm thành viên của nhóm thành công");
        response.setData(result);

        return ResponseEntity.ok(response);
    }




    /**
     * API Giả lập đơn hàng hoàn thành phục vụ test luồng tự động CRM
     * Endpoint: POST /api/crm/complete
     */
    @PostMapping("/complete")
    public ResponseEntity<String> mockCompleteOrder(@RequestBody MockOrderRequest request) {

        // Phát sự kiện ngầm vào hệ thống Spring Context
        eventPublisher.publishEvent(new OrderCompletedEvent(
                request.getCustomerId(),
                request.getOrderAmount()
        ));

        return ResponseEntity.ok("Giả lập hoàn thành đơn hàng thành công! Đã phát sự kiện nhảy hạng tự động.");
    }


}