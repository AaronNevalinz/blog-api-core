package com.blog_api_core.services;

import com.blog_api_core.models.Like;
import com.blog_api_core.payload.LikesPayload;
import com.blog_api_core.repository.LikeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LikeService {
    private final LikeRepository likeRepository;
    public LikeService(LikeRepository likeRepository) {
        this.likeRepository = likeRepository;
    }
    public List<LikesPayload> getAllLikesOfPost(Long postId) {
        return likeRepository.findLikesByPost(postId);
    }
}
