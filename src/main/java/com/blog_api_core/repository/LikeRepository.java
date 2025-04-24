package com.blog_api_core.repository;

import com.blog_api_core.models.Like;
import com.blog_api_core.models.Post;
import com.blog_api_core.models.User;
import com.blog_api_core.payload.LikesPayload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {
    boolean existsByUserAndPost(User user, Post post);
    Optional<Like> findByUserAndPost(User user, Post post);

    @Query(value = "SELECT l.post_id as postId, l.user_id as userId FROM likes l WHERE l.post_id = :postId", nativeQuery = true)
    List<LikesPayload> findLikesByPost(@Param("postId") Long postId);
}
