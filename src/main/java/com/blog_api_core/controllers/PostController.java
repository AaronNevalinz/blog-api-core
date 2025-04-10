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

import java.util.*;

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
