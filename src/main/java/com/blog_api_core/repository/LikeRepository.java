package com.blog_api_core.repository;

import com.blog_api_core.models.Like;
import com.blog_api_core.models.Post;
import com.blog_api_core.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByUserAndPost(User user, Post post);
    Optional<Like> findByUserAndPost(User user, Post post);
}
