package com.blog_api_core.services;

import com.blog_api_core.models.Profile;
import com.blog_api_core.models.User;
import com.blog_api_core.repository.ProfileRepository;
import com.blog_api_core.repository.UserRepository;
import com.blog_api_core.utils.S3FileStorageUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
}
