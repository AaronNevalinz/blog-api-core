package com.blog_api_core.controllers;

import com.blog_api_core.exceptions.NotFoundException;
import com.blog_api_core.models.Post;
import com.blog_api_core.models.Profile;
import com.blog_api_core.models.User;
import com.blog_api_core.payload.ProfileSummary;
import com.blog_api_core.repository.UserRepository;
import com.blog_api_core.services.ProfileService;
import com.blog_api_core.utils.S3FileStorageUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/profile")
public class ProfileController {
    private final UserRepository userRepository;
    private final S3FileStorageUtils s3FileStorageUtils;
    private final ProfileService profileService;

    public ProfileController(UserRepository userRepository, S3FileStorageUtils s3FileStorageUtils, ProfileService profileService) {
        this.userRepository = userRepository;
        this.s3FileStorageUtils = s3FileStorageUtils;
        this.profileService = profileService;
    }

    @PostMapping(value = "/set-profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> setProfile(@RequestPart("file") MultipartFile file, @RequestPart("profile") Profile profile) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() ->new NotFoundException("User not found"));

        Profile userProfile = new Profile();
        userProfile.setUser(user);
        userProfile.setBio(profile.getBio());
        userProfile.setDisplayName(profile.getDisplayName());
        //        upload the post image too
        if(file != null && !file.isEmpty()) {
            String filePath = s3FileStorageUtils.uploadProfilePic(file);
            userProfile.setImgUrl(filePath);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        try{
            Profile savedProfile = profileService.saveProfile(userProfile);

            response.put("status", "true");
            response.put("result", savedProfile);
            response.put("username", username);
        } catch (NotFoundException e) {
            throw new NotFoundException("Profile not saved");
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByUsername(username);

        Map<String, Object> response = new LinkedHashMap<>();
        if (user.isPresent()) {
            response.put("status", true);
            response.put("user", user.get());
        } else {
            response.put("status", false);
            response.put("message", "User not found");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-user/profile/")
    public ResponseEntity<Map<String, Object>> getLoggedInUserProfile() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        ProfileSummary profileSummary = profileService.getUserProfile(user.get().getId());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", true);
        response.put("result", profileSummary);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-user/profile/{username}")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        ProfileSummary profileSummary = profileService.getUserProfile(user.get().getId());
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", true);
        response.put("result", profileSummary);
        return ResponseEntity.ok(response);
    }
}
