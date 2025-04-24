package com.blog_api_core.payload;

import java.time.LocalDateTime;

public interface PostSummary {
    Long getPostId();
    String getUsername();
    String getUserImgUrl();
    String getTitle();
    String getContent();
    String getPostImg();
    int getLikeCount();
    Integer getLikedByUser();
    LocalDateTime getCreatedAt();
}
