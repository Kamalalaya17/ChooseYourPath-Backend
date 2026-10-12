package com.chooseyourpath.service;

import com.chooseyourpath.dto.ResponseDtos;
import com.chooseyourpath.dto.StoryDtos;
import com.chooseyourpath.entity.*;
import com.chooseyourpath.model.Role;
import com.chooseyourpath.model.StoryStatus;
import com.chooseyourpath.repository.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StoryService {

    private final StoryRepository stories;
    private final SceneRepository scenes;
    private final ChoiceRepository choices;
    private final StoryStyleRepository styles;
    private final SceneImageRepository sceneImages;
    private final UserRepository users;
    private final StoryCleanupService cleanup;

    public StoryService(StoryRepository stories, SceneRepository scenes, ChoiceRepository choices,
                        StoryStyleRepository styles, SceneImageRepository sceneImages, UserRepository users,
                        StoryCleanupService cleanup) {
        this.stories = stories;
        this.scenes = scenes;
        this.choices = choices;
        this.styles = styles;
        this.sceneImages = sceneImages;
        this.users = users;
        this.cleanup = cleanup;
    }

    // ---------- catalog ----------

    @Cacheable(value = "stories", key = "#search + '|' + #genre + '|' + #page + '|' + #size + '|' + #sort")
    @Transactional(readOnly = true)
    public Page<ResponseDtos.StoryResponse> catalog(String search, String genre, int page, int size, String sort) {
        PageRequest pr = PageRequest.of(page, size, resolveSort(sort));
        Page<Story> result;
        if (search != null && !search.isBlank()) {
            result = stories.searchPublished(search.trim(), pr);
        } else if (genre != null && !genre.isBlank() && !"all".equalsIgnoreCase(genre)) {
            result = stories.findByStatusAndGenreIgnoreCase(StoryStatus.PUBLISHED, genre, pr);
        } else {
            result = stories.findByStatus(StoryStatus.PUBLISHED, pr);
        }
        return result.map(s -> ResponseDtos.StoryResponse.of(s,
                scenes.countByStoryId(s.getId()), scenes.countByStoryIdAndIsEndingTrue(s.getId())));
    }

    private Sort resolveSort(String sort) {
        return switch (sort == null ? "newest" : sort) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "title" -> Sort.by(Sort.Direction.ASC, "title");
            case "popular" -> Sort.by(Sort.Direction.DESC, "reads");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    @Transactional
    public ResponseDtos.StoryResponse getStory(Long id) {
        Story s = stories.findById(id).orElseThrow(() -> new IllegalArgumentException("Story not found."));
        if (s.getReads() < Long.MAX_VALUE) s.setReads(s.getReads() + 1);
        stories.save(s);
        return ResponseDtos.StoryResponse.of(s, scenes.countByStoryId(id), scenes.countByStoryIdAndIsEndingTrue(id));
    }

    @Cacheable(value = "stories", key = "'meta:' + #storyId")
    @Transactional(readOnly = true)
    public Map<String, Long> storyMeta(Long storyId) {
        return Map.of(
                "sceneCount", scenes.countByStoryId(storyId),
                "endingCount", scenes.countByStoryIdAndIsEndingTrue(storyId));
    }

    // ---------- writer ----------

    private Story ownedStory(String username, Long storyId) {
        User writer = users.findByUsername(username).orElseThrow();
        Story s = stories.findById(storyId).orElseThrow(() -> new IllegalArgumentException("Story not found."));
        if (!s.getCreator().getId().equals(writer.getId())) {
            throw new SecurityException("You can only manage your own stories.");
        }
        return s;
    }

    @Transactional(readOnly = true)
    public List<ResponseDtos.StoryResponse> myStories(String username) {
        User writer = users.findByUsername(username).orElseThrow();
        return stories.findByCreator(writer).stream()
                .map(s -> ResponseDtos.StoryResponse.of(s, scenes.countByStoryId(s.getId()),
                        scenes.countByStoryIdAndIsEndingTrue(s.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public Long createStory(String username, StoryDtos.StoryRequest req) {
        User writer = users.findByUsername(username).orElseThrow();
        Story s = new Story();
        s.setTitle(req.title.trim());
        s.setDescription(req.description.trim());
        s.setGenre(req.genre.trim());
        s.setTheme(req.theme == null ? "" : req.theme.trim());
        s.setWritingStyle(req.writingStyle == null ? "" : req.writingStyle.trim());
        s.setNarrative(req.narrative.trim());
        s.setCoverStyle(req.coverStyle == null ? "Classic Storybook" : req.coverStyle);
        s.setStatus(StoryStatus.DRAFT);
        s.setCreator(writer);
        stories.save(s);
        if (req.styles != null) {
            for (String name : req.styles) {
                StoryStyle st = new StoryStyle();
                st.setStory(s);
                st.setStyleName(name);
                styles.save(st);
            }
        }
        return s.getId();
    }

    @Transactional
    public void updateStory(String username, Long id, StoryDtos.StoryRequest req) {
        Story s = ownedStory(username, id);
        s.setTitle(req.title.trim());
        s.setDescription(req.description.trim());
        s.setGenre(req.genre.trim());
        s.setTheme(req.theme == null ? "" : req.theme.trim());
        s.setWritingStyle(req.writingStyle == null ? "" : req.writingStyle.trim());
        s.setNarrative(req.narrative.trim());
        if (req.coverStyle != null) s.setCoverStyle(req.coverStyle);
        s.setUpdatedAt(java.time.LocalDateTime.now());
        stories.save(s);
    }

    @Transactional
    public void deleteStory(String username, Long id) {
        ownedStory(username, id);
        cleanup.deleteStoryDeep(id);
    }

    @Transactional
    public void submitForReview(String username, Long id) {
        Story s = ownedStory(username, id);
        if (scenes.countByStoryIdAndIsEndingTrue(id) < 1) {
            throw new IllegalStateException("Add at least one ending scene before submitting.");
        }
        s.setStatus(StoryStatus.PENDING);
        stories.save(s);
    }

    @Transactional
    public void unpublish(String username, Long id) {
        Story s = ownedStory(username, id);
        s.setStatus(StoryStatus.DRAFT);
        stories.save(s);
    }

    // ---------- scenes ----------

    @Transactional(readOnly = true)
    public List<ResponseDtos.SceneResponse> getScenes(Long storyId) {
        return scenes.findByStoryIdOrderBySceneOrderAsc(storyId).stream()
                .map(ResponseDtos.SceneResponse::of).collect(Collectors.toList());
    }

    @Transactional
    public Long createScene(String username, Long storyId, StoryDtos.SceneRequest req) {
        Story story = ownedStory(username, storyId);
        Scene sc = new Scene();
        sc.setStory(story);
        sc.setTitle(req.title.trim());
        sc.setNarration(req.narration.trim());
        sc.setSceneDescription(req.sceneDescription == null ? "" : req.sceneDescription.trim());
        sc.setIllustrationDescription(req.illustrationDescription == null ? "" : req.illustrationDescription.trim());
        sc.setEnding(req.isEnding);
        sc.setEndingLabel(req.isEnding && req.endingLabel != null ? req.endingLabel.trim() : null);
        sc.setSceneOrder(scenes.findByStoryIdOrderBySceneOrderAsc(storyId).size() + 1);
        Scene saved = scenes.save(sc);
        stories.save(story);
        return saved.getId();
    }

    @Transactional
    public void updateScene(String username, Long sceneId, StoryDtos.SceneRequest req) {
        Scene sc = scenes.findById(sceneId).orElseThrow(() -> new IllegalArgumentException("Scene not found."));
        ownedStory(username, sc.getStory().getId());
        sc.setTitle(req.title.trim());
        sc.setNarration(req.narration.trim());
        sc.setSceneDescription(req.sceneDescription == null ? "" : req.sceneDescription.trim());
        sc.setIllustrationDescription(req.illustrationDescription == null ? "" : req.illustrationDescription.trim());
        sc.setEnding(req.isEnding);
        sc.setEndingLabel(req.isEnding && req.endingLabel != null ? req.endingLabel.trim() : null);
        scenes.save(sc);
    }

    @Transactional
    public void deleteScene(String username, Long sceneId) {
        Scene sc = scenes.findById(sceneId).orElseThrow(() -> new IllegalArgumentException("Scene not found."));
        ownedStory(username, sc.getStory().getId());
        scenes.delete(sc);
    }

    // ---------- choices ----------

    @Transactional(readOnly = true)
    public List<ResponseDtos.ChoiceResponse> getChoices(Long sceneId) {
        return choices.findBySceneId(sceneId).stream().map(ResponseDtos.ChoiceResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public Long createChoice(String username, Long sceneId, StoryDtos.ChoiceRequest req) {
        Scene scene = scenes.findById(sceneId).orElseThrow(() -> new IllegalArgumentException("Scene not found."));
        ownedStory(username, scene.getStory().getId());
        if (scene.isEnding()) throw new IllegalStateException("Ending scenes cannot have outgoing choices.");
        if (req.nextSceneId == null) throw new IllegalArgumentException("Every choice must lead to a next scene.");
        Scene next = scenes.findById(req.nextSceneId)
                .orElseThrow(() -> new IllegalArgumentException("Next scene not found."));
        Choice c = new Choice();
        c.setScene(scene);
        c.setChoiceText(req.choiceText.trim());
        c.setNextScene(next);
        c.setPathName(req.pathName == null || req.pathName.isBlank() ? "Unnamed Path" : req.pathName.trim());
        c.setConsequence(req.consequence == null ? "" : req.consequence.trim());
        Choice saved = choices.save(c);
        return saved.getId();
    }

    @Transactional
    public void updateChoice(String username, Long choiceId, StoryDtos.ChoiceRequest req) {
        Choice c = choices.findById(choiceId).orElseThrow(() -> new IllegalArgumentException("Choice not found."));
        ownedStory(username, c.getScene().getStory().getId());
        if (req.nextSceneId == null) throw new IllegalArgumentException("Every choice must lead to a next scene.");
        c.setChoiceText(req.choiceText.trim());
        c.setNextScene(scenes.findById(req.nextSceneId).orElseThrow());
        c.setPathName(req.pathName == null || req.pathName.isBlank() ? "Unnamed Path" : req.pathName.trim());
        c.setConsequence(req.consequence == null ? "" : req.consequence.trim());
        choices.save(c);
    }

    @Transactional
    public void deleteChoice(String username, Long choiceId) {
        Choice c = choices.findById(choiceId).orElseThrow(() -> new IllegalArgumentException("Choice not found."));
        ownedStory(username, c.getScene().getStory().getId());
        choices.delete(c);
    }

    public List<ResponseDtos.StyleResponse> getStyles(Long storyId) {
        return styles.findByStoryId(storyId).stream().map(ResponseDtos.StyleResponse::of)
                .collect(Collectors.toList());
    }
}
