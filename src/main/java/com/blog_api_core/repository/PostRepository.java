package com.blog_api_core.repository;

import com.blog_api_core.models.Post;
import com.blog_api_core.payload.PostSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query(value = "SELECT u.username AS username, p.title AS title, p.content AS content, p.img_url AS postImg, COUNT(l.user_id) AS likeCount FROM post p JOIN app_user u ON p.user_id = u.id LEFT JOIN likes l ON l.post_id = p.id GROUP BY p.id, u.username, p.title, p.content, p.img_url", nativeQuery = true)
    List<PostSummary> findPostSummaries();


    @Query(value = "SELECT u.username AS username, p.title AS title, p.content AS content, p.img_url AS postImg, COUNT(l.user_id) AS likeCount FROM post p JOIN app_user u ON p.user_id = u.id LEFT JOIN likes l ON l.post_id = p.id GROUP BY p.id, u.username, p.title, p.content, p.img_url", countQuery = "SELECT COUNT(*) FROM post", nativeQuery = true)
    Page<PostSummary> findPostSummariesWithPagination(Pageable pageable);
}
