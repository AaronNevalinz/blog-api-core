package com.blog_api_core.controllers;

import com.blog_api_core.services.PostService;
import org.springframework.stereotype.Controller;

@Controller
public class PostController {
    private final PostService postService;
    public PostController(PostService postService) {
        this.postService = postService;
    }



}
