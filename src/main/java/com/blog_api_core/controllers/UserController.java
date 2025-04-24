package com.blog_api_core.controllers;

import com.blog_api_core.models.Profile;
import com.blog_api_core.models.User;
import com.blog_api_core.payload.ProfileSummary;
import com.blog_api_core.repository.UserRepository;
import com.blog_api_core.services.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class UserController {
    private final UserRepository userRepository;
    private final ProfileService profileService;

    public UserController(UserRepository userRepository, ProfileService profileService) {
        this.userRepository = userRepository;
        this.profileService = profileService;
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

    @PostMapping("/bookmark")
    public ResponseEntity<Map<String, Object>> bookmark() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByUsername(username);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("user", user.get());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/delete-user")
    public ResponseEntity<Map<String, Object>> deleteUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByUsername(username);

        user.get().setIs_deleted(true);
        userRepository.save(user.get());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("result", user);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-users")
    public ResponseEntity<Map<String, Object>> getUsers() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("result", userRepository.findAllActive());

        return ResponseEntity.ok(response);
    }
    @GetMapping("/get-inactive-users")
    public ResponseEntity<Map<String, Object>> getInactiveUsers() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "success");
        response.put("result", userRepository.findAllInActive());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchUser(@RequestParam(required = false) String searchTerm) {
        Map<String, Object> response = new LinkedHashMap<>();
        List<ProfileSummary> users = profileService.searchUser(searchTerm);
        response.put("status", "success");
        response.put("result", users);

        return ResponseEntity.ok(response);
    }
}
