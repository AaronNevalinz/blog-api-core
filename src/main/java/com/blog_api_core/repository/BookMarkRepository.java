package com.blog_api_core.repository;

import com.blog_api_core.models.BookMark;
import com.blog_api_core.models.Post;
import com.blog_api_core.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookMarkRepository extends JpaRepository<BookMark, Long> {
    boolean existsByUserAndPost(User user, Post post);
    Optional<BookMark> findByUserAndPost(User user, Post post);
}
