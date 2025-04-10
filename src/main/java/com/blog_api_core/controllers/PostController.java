package com.blog_api_core.controllers;

import com.blog_api_core.models.Post;
import com.blog_api_core.models.Topic;
import com.blog_api_core.payload.PostRequest;
import com.blog_api_core.services.PostService;
import com.blog_api_core.services.TopicService;
import com.blog_api_core.utils.S3FileStorageUtils;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Controller
@RequestMapping("/blog")
public class PostController {
    private final PostService postService;
    private final TopicService topicService;
    private final S3FileStorageUtils s3FileStorageUtils;

    public PostController(PostService postService, TopicService topicService, S3FileStorageUtils s3FileStorageUtils) {
        this.postService = postService;
        this.topicService = topicService;
        this.s3FileStorageUtils = s3FileStorageUtils;
    }

    @PostMapping(value="/add-post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> addPost(
            @RequestPart("post") @Valid Post post,
            @RequestPart("image") MultipartFile file) {

        List<Topic> topics = post.getTopics();
//        check if topics already exist or create new ones
        List<Topic> existingTopics = new ArrayList<>();
        // Link each topic to the post
        if(topics != null && !topics.isEmpty()) {
            for (Topic topic : topics) {
//                checking if topic exists by name
                Optional<Topic> existingTopic = topicService.getTopicByName(topic.getName());

                if(existingTopic.isPresent()) {
                    existingTopics.add(existingTopic.get());
                }else{
                    Topic newTopic = new Topic();
                    newTopic.setName(topic.getName());
                    existingTopics.add(topicService.saveTopic(newTopic));
                }
//                topic.setPost(post); // Set the post_id in each topic (ManyToOne relation)
            }
            post.setTopics(existingTopics);
        }

//        upload the post image too
        if(file != null && !file.isEmpty()) {
            String filePath = s3FileStorageUtils.uploadPostImage(file);
            post.setImgUrl(filePath);
        }

        Post savedPost = postService.savePost(post);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "true");
        response.put("result", savedPost);

        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/posts")
    public ResponseEntity<Map<String, Object>> getAllPosts(){
        Map<String, Object> response = new LinkedHashMap<>();
        List<Post> posts = postService.getAllPosts();

        response.put("status", true);
        response.put("result", posts);
        return ResponseEntity.ok(response);
    }
}
