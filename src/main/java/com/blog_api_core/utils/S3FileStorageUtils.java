package com.blog_api_core.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
public class S3FileStorageUtils {
    private final AmazonS3 s3Client;
    public S3FileStorageUtils(AmazonS3 s3Client) {
        this.s3Client = s3Client;
    }
    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public String uploadPostImage(MultipartFile file) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        try{
            String fileName = "blog-api-core/post-images/" + username + file.getOriginalFilename();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            s3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);
            return s3Client.getUrl(bucketName, fileName).toString();
        }catch (IOException e){
            return "Error occurred while uploading image to S3. " + e.getMessage();
        }
    }

    public String uploadProfilePic(MultipartFile file) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        try{
            String fileName = "blog-api-core/profile-pics/" + username + file.getOriginalFilename();
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            s3Client.putObject(bucketName, fileName, file.getInputStream(), metadata);
            return s3Client.getUrl(bucketName, fileName).toString();
        }catch (IOException e){
            return "Error occurred while uploading image to S3. " + e.getMessage();
        }
    }
}
