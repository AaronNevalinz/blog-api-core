package com.blog_api_core.services;

import com.blog_api_core.models.Post;
import com.blog_api_core.models.User;
import com.blog_api_core.payload.PostSummary;
import com.blog_api_core.repository.PostRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post savePost(User user, Post post){
        post.setUser(user);
        return postRepository.save(post);
    }
    public Post getPostById(Long id){
        return postRepository.findById(id).orElse(null);
    }
    public List<Post> getAllPosts(){
        return postRepository.findAll();
    }

    public List<PostSummary> getAllPostSummaries(){
        return postRepository.findPostSummaries();
    }

    public Page<PostSummary> getPaginatedPostSummaries(int offSet, int pageSize) {
        Pageable pageable = PageRequest.of(offSet, pageSize);
        return postRepository.findPostSummariesWithPagination(pageable);
    }

    public void deletePost(Post post){
        postRepository.delete(post);
    }
}
