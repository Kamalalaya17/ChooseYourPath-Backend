package com.chooseyourpath.repository;

import com.chooseyourpath.entity.Choice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {

    List<Choice> findBySceneId(Long sceneId);

    List<Choice> findByScene_Story_Id(Long storyId);

    List<Choice> findByNextSceneId(Long nextSceneId);

    List<Choice> findByNextSceneIdIn(java.util.Collection<Long> nextSceneIds);
}