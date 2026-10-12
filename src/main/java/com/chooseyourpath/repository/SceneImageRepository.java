package com.chooseyourpath.repository;

import com.chooseyourpath.entity.SceneImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SceneImageRepository extends JpaRepository<SceneImage, Long> {
    List<SceneImage> findBySceneId(Long sceneId);
}
