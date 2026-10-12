package com.chooseyourpath.repository;

import com.chooseyourpath.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByStoryIdOrderByCreatedAtDesc(Long storyId);
    List<Comment> findAllByOrderByCreatedAtDesc();
}
