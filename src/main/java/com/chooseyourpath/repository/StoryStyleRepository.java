package com.chooseyourpath.repository;

import com.chooseyourpath.entity.StoryStyle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoryStyleRepository extends JpaRepository<StoryStyle, Long> {
    List<StoryStyle> findByStoryId(Long storyId);
}
