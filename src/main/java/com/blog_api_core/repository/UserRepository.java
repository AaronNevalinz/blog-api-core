package com.blog_api_core.repository;

import com.blog_api_core.models.Like;
import com.blog_api_core.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE u.is_deleted = false")
    List<User> findAllActive();
//    searching
}
