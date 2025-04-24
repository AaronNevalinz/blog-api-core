package com.blog_api_core.payload;

public interface ProfileSummary {
    Long getUserId();
    String getUsername();
    String getDisplayName();
    String getBio();
    String getImgUrl();

}
