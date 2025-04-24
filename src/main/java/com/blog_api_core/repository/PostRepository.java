package com.blog_api_core.repository;

import com.blog_api_core.models.Post;
import com.blog_api_core.payload.PostSummary;
import com.blog_api_core.payload.SinglePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // maps the post to the user who created it
    @Query(value = "SELECT u.username AS username, p.id as postId, pr.img_url as userImgUrl, p.title AS title, p.created_at as createdAt, p.content AS content, p.img_url AS postImg, COUNT(l.user_id) AS likeCount, MAX(CASE WHEN l.user_id = :userId THEN 1 ELSE 0 END) AS likedByUser FROM post p JOIN app_user u ON p.user_id = u.id JOIN profile pr ON p.user_id = pr.user_id LEFT JOIN likes l ON l.post_id = p.id GROUP BY p.id, pr.img_url, u.username, p.title, p.content, p.img_url, p.created_at", nativeQuery = true)
    List<PostSummary> findPostSummaries(@Param("userId") Long userId);

    //    get post according to Bookmark id
    @Query(value = "SELECT u.username AS username, p.id as postId, pr.img_url as userImgUrl, p.title AS title, p.created_at as createdAt, p.content AS content, p.img_url AS postImg, COUNT(l.user_id) AS likeCount FROM post p JOIN app_user u ON p.user_id = u.id JOIN profile pr ON p.user_id = pr.user_id   LEFT JOIN likes l ON l.post_id = p.id JOIN user_bookmarks bm ON bm.post_id = p.id WHERE bm.user_id = ?1 GROUP BY p.id, u.username, p.title, p.content, pr.img_url, p.img_url, p.created_at", nativeQuery = true)
    List<PostSummary> findPostSummariesByBookMarksId(Long userId);


    // maps the post to the user who created it and paginates the results
    @Query(value = "SELECT u.username AS username, p.id as postId, p.title AS title, pr.img_url as userImgUrl, p.content AS content, p.created_at As createdAt, p.img_url AS postImg, COUNT(l.user_id) AS likeCount FROM post p JOIN app_user u ON p.user_id = u.id  JOIN profile pr ON p.user_id = pr.user_id LEFT JOIN likes l ON l.post_id = p.id GROUP BY p.id, u.username, pr.img_url, p.title, p.content, p.img_url, p.created_at", countQuery = "SELECT COUNT(*) FROM post", nativeQuery = true)
    Page<PostSummary> findPostSummariesWithPagination(Pageable pageable);

//    get post according to post topic
    @Query(value = "SELECT u.username AS username, p.id as postId, pr.img_url as userImgUrl, p.title AS title, p.content AS content, p.created_at AS createdAt, p.img_url AS postImg, COUNT(l.user_id) AS likeCount FROM post p JOIN app_user u ON p.user_id = u.id JOIN profile pr ON p.user_id = pr.user_id   LEFT JOIN likes l ON l.post_id = p.id JOIN post_topics pt ON pt.post_id = p.id WHERE pt.topic_id = ?1 GROUP BY p.id, u.username, p.title, p.content, pr.img_url, p.img_url, p.created_at", nativeQuery = true)
    List<PostSummary> findPostSummariesByTopicId(Long topicId);


    @Query(value = "SELECT u.username AS username, p.id as postId, p.title AS title, p.created_at as createdAt, pr.img_url as userImgUrl, p.content AS content, p.img_url AS postImg, COUNT(l.user_id) AS likeCount FROM post p JOIN app_user u ON p.user_id = u.id JOIN profile pr ON p.user_id = pr.user_id  LEFT JOIN likes l ON l.post_id = p.id WHERE u.id = ?1 GROUP BY p.id, u.username, p.title, p.content, pr.img_url, p.img_url, p.created_at", countQuery = "SELECT COUNT(*) FROM post", nativeQuery = true)
    List<PostSummary> findPostsByUserId(Long userId);

    // get post according to username
    @Query(value = "SELECT u.username AS username, p.id as postId, p.title AS title, p.created_at AS createdAt, pr.img_url as userImgUrl, p.content AS content, p.img_url AS postImg, COUNT(l.user_id) AS likeCount FROM post p JOIN app_user u ON p.user_id = u.id JOIN profile pr ON pr.user_id = p.user_id LEFT JOIN likes l ON l.post_id = p.id WHERE u.username = :username GROUP BY p.id, u.username, p.title, p.content, pr.img_url, p.img_url, p.created_at ", countQuery = "SELECT COUNT(*) FROM post", nativeQuery = true)
    List<PostSummary> findPostsByUsername(@Param("username") String username);


    @Query(value = "SELECT p.id as id, u.username AS username, pr.img_url as userImgUrl, pr.display_name AS displayName, p.title as title, p.created_at as createdAt, p.content as content, p.img_url as imgUrl, COUNT(DISTINCT l.id) as likesCount, COUNT(DISTINCT c.id) as commentsCount FROM post p JOIN app_user u ON u.id = p.user_id JOIN profile pr ON pr.user_id = p.user_id LEFT JOIN likes l ON p.id = l.post_id LEFT JOIN comment c ON c.post_id = p.id WHERE p.id = :postId GROUP BY u.username, p.created_at, p.id, p.title, p.content, p.img_url, pr.img_url, pr.display_name", nativeQuery = true)
    SinglePost findSinglePostById(@Param("postId") Long postId);
    
}
