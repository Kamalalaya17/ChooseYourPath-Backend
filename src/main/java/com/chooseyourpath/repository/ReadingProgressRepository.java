package com.chooseyourpath.repository;

import com.chooseyourpath.entity.ReadingProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReadingProgressRepository extends JpaRepository<ReadingProgress, Long> {
    Optional<ReadingProgress> findByUserIdAndStoryId(Long userId, Long storyId);
    List<ReadingProgress> findByUserIdOrderByUpdatedAtDesc(Long userId);
    List<ReadingProgress> findByStoryId(Long storyId);
}
