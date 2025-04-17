package com.blog_api_core.controllers;

import com.blog_api_core.models.Topic;
import com.blog_api_core.services.TopicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/topics")
public class TopicController {
    private final TopicService topicService;
    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }
    @GetMapping()
    public ResponseEntity<Map<String, Object>> getTopics() {
        List<Topic> topics = topicService.getAllTopics();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status",true);
        response.put("topics", topics);
        return ResponseEntity.ok(response);
    }
}
