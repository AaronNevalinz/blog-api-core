package com.blog_api_core.services;

import com.blog_api_core.models.Post;
import com.blog_api_core.repository.PostRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {
    private final PostRepository postRepository;
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post savePost(Post post){
        return postRepository.save(post);
    }
    public Post getPostById(Long id){
        return postRepository.findById(id).orElse(null);
    }
    public List<Post> getAllPosts(){
        return postRepository.findAll();
    }
    public void deletePost(Post post){
        postRepository.delete(post);
    }
}
