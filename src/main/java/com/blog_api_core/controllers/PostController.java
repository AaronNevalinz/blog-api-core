package com.blog_api_core.controllers;

import com.blog_api_core.models.Post;
import com.blog_api_core.models.Topic;
import com.blog_api_core.payload.PostRequest;
import com.blog_api_core.services.PostService;
import com.blog_api_core.services.TopicService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/blog")
public class PostController {
    private final PostService postService;
    private final TopicService topicService;

    public PostController(PostService postService, TopicService topicService) {
        this.postService = postService;
        this.topicService = topicService;
    }


    @PostMapping("/add-post")
    public ResponseEntity<Map<String, Object>> addPost(@Valid @RequestBody PostRequest postRequest) {
        Post post = postRequest.getPost();
        List<Topic> topics = postRequest.getTopics();


        // Link each topic to the post
        if(topics != null && !topics.isEmpty()) {
            for (Topic topic : topics) {
                topic.setPost(post); // Set the post_id in each topic (ManyToOne relation)
            }
            post.setTopics(topics);
        }

        Post savedPost = postService.savePost(post);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "true");
        response.put("result", savedPost);

        return ResponseEntity.ok(response);


//        Post savedPost = postService.savePost(post);
//        post.setTopics(topics);
//        Map<String, Object> response = new LinkedHashMap<>();
//        response.put("status", "true");
//        response.put("result", savedPost);
//        return ResponseEntity.ok(response);
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
