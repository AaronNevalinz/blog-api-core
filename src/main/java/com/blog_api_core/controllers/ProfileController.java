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
import java.util.Objects;
import java.util.Optional;

@RestController
@CrossOrigin
@RequestMapping("/v1/blog")
public class ProfileController {
    private final UserRepository userRepository;
    private final S3FileStorageUtils s3FileStorageUtils;
    private final ProfileService profileService;

    public ProfileController(UserRepository userRepository, S3FileStorageUtils s3FileStorageUtils, ProfileService profileService) {
        this.userRepository = userRepository;
        this.s3FileStorageUtils = s3FileStorageUtils;
        this.profileService = profileService;
    }
    /**
     * Endpoint for updating the user's profile with optional image upload.
     *
     * This method allows the currently authenticated user to update their profile information, including their
     * bio and display name. Additionally, an optional profile image can be uploaded, which will be stored in an S3 bucket.
     * If the image is uploaded, the URL of the image will be saved in the user's profile.
     *
     * @param file The profile image to be uploaded (optional)
     * @param profile The profile information containing bio and display name
     * @return A ResponseEntity containing the status of the operation, the updated profile, and the username of the authenticated user
     *
     * @throws NotFoundException if the user is not found or if the profile cannot be saved
     */
    @PostMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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
//    /**
//     * Endpoint to retrieve the currently authenticated user's details.
//     *
//     * This method fetches the details of the user that is currently authenticated, based on the username
//     * provided in the security context. It returns the user's information if found, otherwise, it returns
//     * an error message stating that the user was not found.
//     *
//     * @return A ResponseEntity containing the status of the operation, and the user details if found.
//     *         If the user is not found, a message is returned indicating the user could not be found.
//     */
//    @GetMapping("/profile")
//    public ResponseEntity<Map<String, Object>> getCurrentUser() {
//        String username = SecurityContextHolder.getContext().getAuthentication().getName();
//        Optional<User> user = userRepository.findByUsername(username);
//
//        Map<String, Object> response = new LinkedHashMap<>();
//        if (user.isPresent()) {
//            response.put("status", true);
//            response.put("user", user.get());
//        } else {
//            response.put("status", false);
//            response.put("message", "User not found");
//        }
//
//        return ResponseEntity.ok(response);
//    }
    /**
     * Endpoint to retrieve the profile details of the currently authenticated user.
     *
     * This method retrieves the profile information of the logged-in user, based on the username
     * stored in the security context. If the user is not found, a `UsernameNotFoundException` is thrown.
     * If the user is found, the user's profile details are returned in the response.
     *
     * @return A ResponseEntity containing the status of the operation and the user's profile summary
     *         if found. If the user does not exist, an exception is thrown.
     */
    @GetMapping("/profile")
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
    /**
     * Endpoint to retrieve the profile details of a specific user by username.
     *
     * This method retrieves the profile information of the user identified by the provided
     * username. If the user is not found, a `UsernameNotFoundException` is thrown.
     * If the user is found, the user's profile details are returned in the response.
     *
     * @param username The username of the user whose profile is to be fetched.
     * @return A ResponseEntity containing the status of the operation and the user's profile summary
     *         if found. If the user does not exist, an exception is thrown.
     */
    @GetMapping("/profile/{username}")
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
    /**
     * Endpoint to update the profile details of the currently logged-in user.
     *
     * This method allows the logged-in user to update their profile. The user's profile is
     * identified by the provided profile ID. If a new profile picture is provided, it is
     * uploaded and the profile's image URL is updated. The method also allows updating the
     * bio and display name of the profile.
     * If the user attempts to update a profile that does not belong to them, an authorization
     * error is returned.
     *
     * @param profileId The ID of the profile to update.
     * @param profile The profile object containing the new data to update.
     * @param file The new profile image file, if any.
     * @return A ResponseEntity containing the status and the updated profile, or an error message
     *         if unauthorized.
     */
    @PutMapping(value = "/update-profile/{profileId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> updateProfile(@PathVariable Long profileId, @RequestPart Profile profile, @RequestPart(value = "file", required = false)  MultipartFile file) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("User not found"));
        Profile existingProfile = profileService.getProfileById(profileId);
        Map<String, Object> response = new LinkedHashMap<>();

        if(file != null && !file.isEmpty()) {
            String filePath = s3FileStorageUtils.uploadProfilePic(file);
            existingProfile.setImgUrl(filePath);
        }


        if(!Objects.equals(user.getId(), existingProfile.getUser().getId())){
            response.put("status", false);
            response.put("message", "You are not authorized to update this profile");
        } else {
            if(profile.getBio()!= null) existingProfile.setBio(profile.getBio());
            if(profile.getDisplayName()!= null) existingProfile.setDisplayName(profile.getDisplayName());
            Profile updatedProfile = profileService.saveProfile(existingProfile);
            response.put("status", true);
            response.put("result", updatedProfile);
        }
        return ResponseEntity.ok(response);
    }
}
