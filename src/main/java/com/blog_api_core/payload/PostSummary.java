package com.blog_api_core.payload;

public interface PostSummary {
    Long getPostId();
    String getUsername();
    String getTitle();
    String getContent();
    String getPostImg();
    int getLikeCount();
}
