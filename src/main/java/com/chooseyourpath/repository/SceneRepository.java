package com.chooseyourpath.repository;

import com.chooseyourpath.entity.Scene;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SceneRepository extends JpaRepository<Scene, Long> {
    List<Scene> findByStoryIdOrderBySceneOrderAsc(Long storyId);
    long countByStoryIdAndIsEndingTrue(Long storyId);
    long countByStoryId(Long storyId);
}
