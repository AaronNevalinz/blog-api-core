package com.blog_api_core.controllers;

import com.blog_api_core.models.Comment;
import com.blog_api_core.models.User;
import com.blog_api_core.repository.UserRepository;
import com.blog_api_core.services.CommentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@CrossOrigin
@RequestMapping("blog")
public class CommentController {
    private final CommentService commentService;
    private final UserRepository userRepository;
    public CommentController(CommentService commentService, UserRepository userRepository) {
        this.commentService = commentService;
        this.userRepository = userRepository;
    }

    @PostMapping("/add-comment/{post_id}")
    public ResponseEntity<Map<String, Object>> addComment(@PathVariable Long post_id, @RequestBody Comment comment) {
        // get the currently logged-in user
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        // find the user by username
        Optional<User> user = userRepository.findByUsername(username);
        // set the user to the comment
        comment.setUser(user.get());
        Map<String, Object> response = new LinkedHashMap<>();
        Comment savedComment = commentService.saveComment(post_id, comment);
        response.put("status", true);
        response.put("comment", savedComment);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post/comments/{post_id}")
    public ResponseEntity<Map<String, Object>> getComments(@PathVariable Long post_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        List<Comment> comments = commentService.findAllCommentsForPost(post_id);

        response.put("status", true);
        response.put("comments", comments);
        return ResponseEntity.ok(response);
    }
}
