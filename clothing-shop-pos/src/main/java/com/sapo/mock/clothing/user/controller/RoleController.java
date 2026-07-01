package com.sapo.mock.clothing.user.controller;

import com.sapo.mock.clothing.common.dto.response.RestResponse;
import com.sapo.mock.clothing.user.dto.request.RoleRequest;
import com.sapo.mock.clothing.user.dto.response.RoleResponse;
import com.sapo.mock.clothing.user.service.RoleService;
import com.sapo.mock.clothing.util.annotation.ApiMessage;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @ApiMessage("Lấy danh sách vai trò thành công")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'MANAGE_ROLE', 'MANAGE_USER')")
    public ResponseEntity<RestResponse<List<RoleResponse>>> getAllRoles() {
        return ResponseEntity.ok(new RestResponse<>(200, null, "Thành công", roleService.getAllRoles()));
    }

    @GetMapping("/{id}")
    @ApiMessage("Lấy chi tiết vai trò thành công")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'MANAGE_ROLE', 'MANAGE_USER')")
    public ResponseEntity<RestResponse<RoleResponse>> getRoleById(@PathVariable Integer id) {
        return ResponseEntity.ok(new RestResponse<>(200, null, "Thành công", roleService.getRoleById(id)));
    }

    @PostMapping
    @ApiMessage("Tạo vai trò thành công")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'MANAGE_ROLE')")
    public ResponseEntity<RestResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        RoleResponse res = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new RestResponse<>(201, null, "Thành công", res));
    }

    @PutMapping("/{id}")
    @ApiMessage("Cập nhật vai trò thành công")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'MANAGE_ROLE')")
    public ResponseEntity<RestResponse<RoleResponse>> updateRole(@PathVariable Integer id, @Valid @RequestBody RoleRequest request) {
        return ResponseEntity.ok(new RestResponse<>(200, null, "Thành công", roleService.updateRole(id, request)));
    }

    @DeleteMapping("/{id}")
    @ApiMessage("Xóa vai trò thành công")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'MANAGE_ROLE')")
    public ResponseEntity<RestResponse<Void>> deleteRole(@PathVariable Integer id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(new RestResponse<>(200, null, "Thành công", null));
    }
}
