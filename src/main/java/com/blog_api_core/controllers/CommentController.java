package com.blog_api_core.controllers;

import com.blog_api_core.models.Comment;
import com.blog_api_core.models.User;
import com.blog_api_core.payload.CommentPayload;
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
@RequestMapping("/v1/blog")
public class CommentController {
    private final CommentService commentService;
    private final UserRepository userRepository;
    public CommentController(CommentService commentService, UserRepository userRepository) {
        this.commentService = commentService;
        this.userRepository = userRepository;
    }
    /**
     * Endpoint for adding a comment to a post.
     *
     * This method allows a user to add a comment on a specific post by providing the post ID and comment content.
     * It retrieves the currently logged-in user, associates the comment with that user, and saves it.
     *
     * @param post_id The ID of the post to which the comment will be added
     * @param comment The comment object containing the content to be posted
     * @return A ResponseEntity containing the status of the operation and the saved comment data
     *
     * @throws NotFoundException if the user is not logged in or the post does not exist
     */
    @PostMapping("/comments/{post_id}")
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
    /**
     * Endpoint for retrieving comments for a specific post.
     *
     * This method retrieves all the comments associated with a particular post, based on the provided post ID.
     * It returns the list of comments in the response, along with the status of the operation.
     *
     * @param post_id The ID of the post whose comments are to be fetched
     * @return A ResponseEntity containing the status of the operation and the list of comments for the post
     *
     * @throws NotFoundException if the post does not exist or if no comments are found for the given post
     */
    @GetMapping("/posts/comments/{post_id}")
    public ResponseEntity<Map<String, Object>> getComments(@PathVariable Long post_id) {
        Map<String, Object> response = new LinkedHashMap<>();
        List<CommentPayload> comments = commentService.findAllCommentsForPost(post_id);

        response.put("status", true);
        response.put("comments", comments);
        return ResponseEntity.ok(response);
    }
}
