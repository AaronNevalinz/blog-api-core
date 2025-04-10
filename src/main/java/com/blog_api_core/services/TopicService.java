package com.blog_api_core.services;

import com.blog_api_core.models.Comment;
import com.blog_api_core.models.Post;
import com.blog_api_core.models.Topic;
import com.blog_api_core.repository.TopicRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TopicService {
    private final TopicRepository topicRepository;
    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    public Topic saveTopic(Topic topic) {
        return topicRepository.save(topic);
    }
    public Optional<Topic> getTopicByName(String name) {
        return topicRepository.findByName(name);
    }
}
