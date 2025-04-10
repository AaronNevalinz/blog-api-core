package com.blog_api_core.controllers;

import com.blog_api_core.models.Role;
import com.blog_api_core.models.User;
import com.blog_api_core.repository.RoleRepository;
import com.blog_api_core.repository.UserRepository;
import com.blog_api_core.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public AuthController(AuthService authService, UserRepository userRepository, RoleRepository roleRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @PostMapping(value = "/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody User user) {
//        if(userRepository.existsByUsername(user.getUsername())) {
//            throw new IOException()
//        }

        Set<Role> roles = new HashSet<>();
        Role userRole;
        if(userRepository.count() == 0) {
            userRole = roleRepository.findByName("ROLE_ADMIN");
            roles.add(userRole);
        }else{
            userRole = roleRepository.findByName("ROLE_USER");
            roles.add(userRole);
        }
        roles.add(userRole);
        user.setRoles(roles);

        Map<String, Object> response = new HashMap<>();
        User savedUser = authService.register(user);

        response.put("Status", true);
        response.put("result", savedUser);

        return ResponseEntity.ok(response);
    }
}
