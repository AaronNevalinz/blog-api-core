package com.blog_api_core.controllers;

import com.blog_api_core.models.User;
import com.blog_api_core.repository.UserRepository;
import com.blog_api_core.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@CrossOrigin
@RequestMapping("api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping(value = "/register")
    public ResponseEntity<Map<String, Object>> registerUser(@RequestBody User user) {
        Map<String, Object> response = new HashMap<>();
        User savedUser = authService.register(user);
        response.put("status", true);
        response.put("result", savedUser);

        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<Map<String, Object>> loginUser(@RequestBody User user) {
        String token = authService.login(user);
        User databaseUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", true);
        response.put("user", databaseUser);
        response.put("token", token);
        return ResponseEntity.ok(response);
    }
}
