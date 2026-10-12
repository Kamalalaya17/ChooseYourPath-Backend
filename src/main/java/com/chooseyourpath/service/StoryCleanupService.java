package com.chooseyourpath.service;

import com.chooseyourpath.entity.Scene;
import com.chooseyourpath.entity.Story;
import com.chooseyourpath.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * FK-safe story deletion. The JPA graph has no cascades from Story to its
 * children, so every dependent row must be removed explicitly in
 * child-first order, or MySQL rejects the delete.
 */
@Service
public class StoryCleanupService {

    private final StoryRepository stories;
    private final SceneRepository scenes;
    private final ChoiceRepository choices;
    private final StoryStyleRepository styles;
    private final SceneImageRepository sceneImages;
    private final CommentRepository comments;
    private final ReadingProgressRepository progress;
    private final PathProgressRepository paths;
    private final FavoriteRepository favorites;

    public StoryCleanupService(StoryRepository stories, SceneRepository scenes,
                               ChoiceRepository choices, StoryStyleRepository styles,
                               SceneImageRepository sceneImages, CommentRepository comments,
                               ReadingProgressRepository progress, PathProgressRepository paths,
                               FavoriteRepository favorites) {
        this.stories = stories;
        this.scenes = scenes;
        this.choices = choices;
        this.styles = styles;
        this.sceneImages = sceneImages;
        this.comments = comments;
        this.progress = progress;
        this.paths = paths;
        this.favorites = favorites;
    }

    /** Deletes a story and every row that references it or its scenes. */
    @Transactional
    public void deleteStoryDeep(Long storyId) {
        List<Scene> sceneList = scenes.findByStoryIdOrderBySceneOrderAsc(storyId);
        List<Long> sceneIds = sceneList.stream().map(Scene::getId).toList();

        // Choices referencing the story's scenes, both directions.
        choices.deleteAll(choices.findByScene_Story_Id(storyId));
        for (Scene sc : sceneList) {
            choices.deleteAll(choices.findBySceneId(sc.getId()));
        }
        choices.flush();

        for (Scene sc : sceneList) {
            sceneImages.deleteAll(sceneImages.findBySceneId(sc.getId()));
        }
        sceneImages.flush();

        progress.deleteAll(progress.findByStoryId(storyId)); // see repo addition below
        progress.flush();

        paths.deleteAll(paths.findByStoryId(storyId));
        paths.flush();

        favorites.deleteAll(favorites.findByStoryId(storyId)); // see repo addition below
        favorites.flush();

        comments.deleteAll(comments.findByStoryIdOrderByCreatedAtDesc(storyId));
        comments.flush();

        styles.deleteAll(styles.findByStoryId(storyId));
        styles.flush();

        choices.deleteAll(choices.findByNextSceneIdIn(sceneIds));
        choices.flush();

        scenes.deleteAll(sceneList);
        scenes.flush();

        stories.deleteById(storyId);
        stories.flush();
    }

    /** Deletes all stories authored by a user (used when deleting that user). */
    @Transactional
    public void deleteAllStoriesOf(Long authorId) {
        for (Story s : stories.findByCreatorId(authorId)) {
            deleteStoryDeep(s.getId());
        }
    }
}
