package com.sapo.mock.clothing.user.dto.response;

import com.sapo.mock.clothing.entity.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
public class RoleResponse {
    private Integer id;
    private String name;
    private String description;
    private boolean isSystem;
    private Set<String> permissions;

    public static RoleResponse fromEntity(Role role) {
        RoleResponse res = new RoleResponse();
        res.setId(role.getId());
        res.setName(role.getName());
        res.setDescription(role.getDescription());
        res.setSystem(role.isSystem());
        if (role.getPermissions() != null) {
            res.setPermissions(role.getPermissions().stream().map(Enum::name).collect(Collectors.toSet()));
        }
        return res;
    }
}
