package com.blog_api_core.services;

import com.blog_api_core.models.Profile;
import com.blog_api_core.models.User;
import com.blog_api_core.payload.ProfileSummary;
import com.blog_api_core.payload.UserSpecification;
import com.blog_api_core.repository.ProfileRepository;
import com.blog_api_core.repository.UserRepository;
import com.blog_api_core.utils.S3FileStorageUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ProfileService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final S3FileStorageUtils s3FileStorageUtils;

    public ProfileService(UserRepository userRepository, ProfileRepository profileRepository, S3FileStorageUtils s3FileStorageUtils) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.s3FileStorageUtils = s3FileStorageUtils;
    }

    public Profile saveProfile(Profile profile) {
        return profileRepository.save(profile);
    }

    public List<ProfileSummary> searchUser(String searchTerm){
        return profileRepository.getAllMatchedResults(searchTerm);
    }

    public ProfileSummary getUserProfile(Long userId) {
        return profileRepository.findUserProfile(userId);
    }
}
