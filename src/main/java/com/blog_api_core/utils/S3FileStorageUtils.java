package com.blog_api_core.utils;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.beans.factory.annotation.Value;
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
        try{
            String fileName = "/blog-api-core/post-images" + file.getOriginalFilename();
            s3Client.putObject(bucketName, fileName, file.getInputStream(), null);
            return s3Client.getUrl(bucketName, fileName).toString();
        }catch (IOException e){
            return "Error occurred while uploading image to S3. " + e.getMessage();
        }
    }
}
