package com.blog_api_core.payload;

public interface PostSummary {
    String getUsername();
    String getTitle();
    String getContent();
    String getPostImg();
    int getLikeCount();
}
