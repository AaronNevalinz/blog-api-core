package com.blog_api_core.repository;

import com.blog_api_core.models.Comment;
import com.blog_api_core.payload.CommentPayload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query(value = "SELECT c.comment as comment, p.img_url as imgUrl, u.username as username FROM comment c JOIN profile p ON p.user_id = c.user_id JOIN app_user u ON u.id = c.user_id WHERE c.post_id = :postId", nativeQuery = true)
    List<CommentPayload> findByPostId(@Param("postId") Long post_id);
}
