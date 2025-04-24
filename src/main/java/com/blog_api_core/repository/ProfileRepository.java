package com.blog_api_core.repository;

import com.blog_api_core.models.Profile;
import com.blog_api_core.payload.ProfileSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    @Query(value = "SELECT u.username as username, u.id as userId, pr.display_name as displayName, pr.bio as bio, pr.img_url as imgUrl\n" +
            "FROM app_user u JOIN profile pr ON u.id = pr.user_id WHERE u.id = :userId", nativeQuery = true)
    ProfileSummary findUserProfile(@Param("userId") Long userId);

    @Query(value = "SELECT u.username as username, u.id as userId, pr.display_name as displayName, pr.bio as bio, pr.img_url as imgUrl FROM app_user u JOIN profile pr ON u.id = pr.user_id WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(pr.display_name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))", nativeQuery = true)
    List<ProfileSummary> getAllMatchedResults( @Param("searchTerm") String searchTerm);
}
