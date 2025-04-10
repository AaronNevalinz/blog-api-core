package com.blog_api_core.payload;

import com.blog_api_core.models.Post;
import com.blog_api_core.models.Topic;

import java.util.List;

public class PostRequest {
    private Post post;
    private List<Topic> topics;

    public Post getPost() {
        return post;
    }

    public void setPost(Post post) {
        this.post = post;
    }

    public List<Topic> getTopics() {
        return topics;
    }

    public void setTopics(List<Topic> topics) {
        this.topics = topics;
    }
}
