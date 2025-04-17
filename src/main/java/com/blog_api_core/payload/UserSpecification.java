package com.blog_api_core.payload;

import com.blog_api_core.models.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    public static Specification<User>getUserByUsername(String username){
        return (root, query, criteriaBuilder) ->
                (username == null || username.trim().isEmpty()) ? null : criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), "%" + username.toLowerCase() + "%");
    }
}
