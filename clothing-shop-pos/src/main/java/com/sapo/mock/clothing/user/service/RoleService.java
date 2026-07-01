package com.sapo.mock.clothing.user.service;

import com.sapo.mock.clothing.entity.Role;
import com.sapo.mock.clothing.exception.BadRequestException;
import com.sapo.mock.clothing.exception.ResourceNotFoundException;
import com.sapo.mock.clothing.user.dto.request.RoleRequest;
import com.sapo.mock.clothing.user.dto.response.RoleResponse;
import com.sapo.mock.clothing.user.repository.RoleRepository;
import com.sapo.mock.clothing.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(RoleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public RoleResponse getRoleById(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò ID: " + id));
        return RoleResponse.fromEntity(role);
    }

    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new BadRequestException("Tên vai trò đã tồn tại.");
        }
        Role role = new Role();
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setSystem(false);
        role.setPermissions(request.getPermissions());
        return RoleResponse.fromEntity(roleRepository.save(role));
    }

    @Transactional
    public RoleResponse updateRole(Integer id, RoleRequest request) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò ID: " + id));

        if (role.isSystem()) {
            throw new BadRequestException("Không thể sửa vai trò hệ thống.");
        }

        role.setName(request.getName());
        role.setDescription(request.getDescription());
        role.setPermissions(request.getPermissions());
        return RoleResponse.fromEntity(roleRepository.save(role));
    }

    @Transactional
    public void deleteRole(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy vai trò ID: " + id));

        if (role.isSystem()) {
            throw new BadRequestException("Không thể xóa vai trò hệ thống.");
        }
        
        if (userRepository.existsByRoleId(id)) {
            throw new BadRequestException("Không thể xóa vai trò này vì đang có nhân viên thuộc vai trò. Vui lòng chuyển nhân viên sang vai trò khác trước!");
        }
        
        roleRepository.delete(role);
    }
}
