package com.blog_api_core.services;

import com.blog_api_core.models.Comment;
import com.blog_api_core.models.Post;
import com.blog_api_core.payload.CommentPayload;
import com.blog_api_core.repository.CommentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostService postService;
    public CommentService(CommentRepository commentRepository, PostService postService) {
        this.postService = postService;
        this.commentRepository = commentRepository;
    }

    public Comment saveComment(Long post_id, Comment comment) {
        Post post = postService.getPostById(post_id);
        comment.setPost(post);
        return commentRepository.save(comment);
    }

    public List<CommentPayload> findAllCommentsForPost(Long post_id) {
        Post post = postService.getPostById(post_id);
        if (post == null) {
            return null;
        }
        return commentRepository.findByPostId(post_id);
    }

}
