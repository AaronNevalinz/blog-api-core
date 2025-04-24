package com.blog_api_core.repository;

import com.blog_api_core.models.BookMark;
import com.blog_api_core.models.Post;
import com.blog_api_core.models.User;
import com.blog_api_core.payload.BookMarkPayload;
import com.blog_api_core.payload.LikesPayload;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookMarkRepository extends JpaRepository<BookMark, Long> {
    boolean existsByUserAndPost(User user, Post post);
    Optional<BookMark> findByUserAndPost(User user, Post post);


    @Query(value = "SELECT br.post_id as postId, br.user_id as userId FROM user_bookmarks br WHERE br.post_id = :postId", nativeQuery = true)
    List<BookMarkPayload> findBookMarkByPost(@Param("postId") Long postId);
}
