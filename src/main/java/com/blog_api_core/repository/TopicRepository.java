package com.blog_api_core.repository;

import com.blog_api_core.models.Comment;
import com.blog_api_core.models.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Integer> {
}
