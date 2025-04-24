package com.blog_api_core.payload;

public interface SinglePost {
    Long getId();
    String getTitle();
    String getContent();
    String getImgUrl();
    int getLikesCount();
    int getCommentsCount();
    String getUsername();
    String getUserImgUrl();
    String getDisplayName();
}
