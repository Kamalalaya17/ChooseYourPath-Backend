package com.chooseyourpath.repository;

import com.chooseyourpath.entity.PathProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PathProgressRepository extends JpaRepository<PathProgress, Long> {
    List<PathProgress> findByUserIdAndStoryId(Long userId, Long storyId);
    List<PathProgress> findByUserId(Long userId);
    List<PathProgress> findByStoryId(Long storyId);
    boolean existsByUserIdAndChoiceId(Long userId, Long choiceId);
}
