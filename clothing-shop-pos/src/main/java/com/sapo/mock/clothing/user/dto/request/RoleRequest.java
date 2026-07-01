package com.sapo.mock.clothing.user.dto.request;

import com.sapo.mock.clothing.util.constant.PermissionEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class RoleRequest {
    @NotBlank(message = "Tên vai trò không được để trống")
    private String name;
    
    private String description;
    
    private Set<PermissionEnum> permissions;
}
