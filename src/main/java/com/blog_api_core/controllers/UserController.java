package com.blog_api_core.controllers;

import com.blog_api_core.models.Profile;
import com.blog_api_core.models.User;
import com.blog_api_core.repository.ProfileRepository;
import com.blog_api_core.repository.UserRepository;
import jakarta.persistence.Entity;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class UserController {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    public UserController(UserRepository userRepository, ProfileRepository profileRepository) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
    }

    @PostMapping("/update-profile")
    public ResponseEntity<Map<String, Object>> updateProfile(Profile profile) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByUsername(username);

        user.get().setProfile(profile);


        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");

        return ResponseEntity.ok(response);
    }

}
