package com.chooseyourpath.repository;
import com.chooseyourpath.entity.User;
import com.chooseyourpath.entity.Story;
import com.chooseyourpath.model.StoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StoryRepository extends JpaRepository<Story, Long> {
    List<Story> findByCreatorId(Long creatorId);

    Page<Story> findByStatus(StoryStatus status, Pageable pageable);

    @Query("""
        SELECT s FROM Story s WHERE s.status = 'PUBLISHED'
        AND (LOWER(s.title) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(s.description) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(s.genre) LIKE LOWER(CONCAT('%', :q, '%'))
          OR LOWER(s.creator.username) LIKE LOWER(CONCAT('%', :q, '%')))
        """)
    Page<Story> searchPublished(@Param("q") String q, Pageable pageable);

    Page<Story> findByStatusAndGenreIgnoreCase(StoryStatus status, String genre, Pageable pageable);

    Page<Story> findByCreator(User creator, Pageable pageable);

    List<Story> findByCreator(User creator);

    List<Story> findByStatus(StoryStatus status);
}
