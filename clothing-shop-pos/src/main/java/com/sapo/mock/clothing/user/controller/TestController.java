package com.sapo.mock.clothing.user.controller;

import com.sapo.mock.clothing.user.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final RoleRepository roleRepository;
    
    @GetMapping("/api/public/test-roles")
    public Object get() {
        return roleRepository.findAll();
    }
}
