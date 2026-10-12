package com.chooseyourpath.repository;

import com.chooseyourpath.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    Optional<Favorite> findByUserIdAndStoryId(Long userId, Long storyId);
    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Favorite> findByStoryId(Long storyId);
}
