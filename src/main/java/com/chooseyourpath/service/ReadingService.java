package com.chooseyourpath.service;

import com.chooseyourpath.dto.ResponseDtos;
import com.chooseyourpath.entity.*;
import com.chooseyourpath.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ReadingService {

    private final UserRepository users;
    private final StoryRepository stories;
    private final SceneRepository scenes;
    private final ChoiceRepository choices;
    private final ReadingProgressRepository progress;
    private final PathProgressRepository paths;
    private final FavoriteRepository favorites;
    private final CommentRepository comments;

    public ReadingService(UserRepository users, StoryRepository stories, SceneRepository scenes,
                          ChoiceRepository choices, ReadingProgressRepository progress,
                          PathProgressRepository paths, FavoriteRepository favorites,
                          CommentRepository comments) {
        this.users = users;
        this.stories = stories;
        this.scenes = scenes;
        this.choices = choices;
        this.progress = progress;
        this.paths = paths;
        this.favorites = favorites;
        this.comments = comments;
    }

    private User user(String username) {
        return users.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found."));
    }

    // ---------- progress ----------

    @Transactional
    public ResponseDtos.ProgressResponse saveProgress(String username, Long storyId, Long sceneId) {
        User u = user(username);
        Story story = stories.findById(storyId).orElseThrow(() -> new IllegalArgumentException("Story not found."));
        Scene scene = scenes.findById(sceneId).orElseThrow(() -> new IllegalArgumentException("Scene not found."));

        int pct = scene.isEnding()
                ? 100
                : Math.max(5, Math.min(95, (int) Math.round(scene.getSceneOrder() * 90.0 / Math.max(1, story.getScenes().size()))));

        ReadingProgress p = progress.findByUserIdAndStoryId(u.getId(), storyId).orElse(null);
        if (p == null) {
            p = new ReadingProgress();
            p.setUser(u);
            p.setStory(story);
            story.setReads(story.getReads() + 1);
        }
        p.setCurrentScene(scene);
        p.setProgressPercentage(pct);
        p.setUpdatedAt(java.time.LocalDateTime.now());
        progress.save(p);
        return ResponseDtos.ProgressResponse.of(p);
    }

    @Transactional(readOnly = true)
    public ResponseDtos.ProgressResponse getProgress(String username, Long storyId) {
        User u = user(username);
        return progress.findByUserIdAndStoryId(u.getId(), storyId)
                .map(ResponseDtos.ProgressResponse::of).orElse(null);
    }

    public record HistoryItem(Long storyId, String title, String genre, Long currentSceneId,
                              int progressPercentage, java.time.LocalDateTime updatedAt) {}

    @Transactional(readOnly = true)
    public List<HistoryItem> history(String username) {
        User u = user(username);
        return progress.findByUserIdOrderByUpdatedAtDesc(u.getId()).stream()
                .map(p -> new HistoryItem(p.getStory().getId(), p.getStory().getTitle(),
                        p.getStory().getGenre(), p.getCurrentScene().getId(),
                        p.getProgressPercentage(), p.getUpdatedAt()))
                .collect(Collectors.toList());
    }

    // ---------- paths / perspectives ----------

    @Transactional
    public boolean discoverPath(String username, Long storyId, Long choiceId) {
        User u = user(username);
        Choice choice = choices.findById(choiceId).orElseThrow(() -> new IllegalArgumentException("Choice not found."));
        if (paths.existsByUserIdAndChoiceId(u.getId(), choiceId)) return false;
        PathProgress pp = new PathProgress();
        pp.setUser(u);
        pp.setStory(stories.findById(storyId).orElseThrow());
        pp.setChoice(choice);
        pp.setPathName(choice.getPathName());
        paths.save(pp);
        return true;
    }

    @Transactional(readOnly = true)
    public ResponseDtos.PathsResponse storyPaths(String username, Long storyId) {
       List<Choice> storyChoices = choices.findByScene_Story_Id(storyId);

        // Distinct paths by name; a path is discovered if ANY of its choices was taken.
        Map<String, Boolean> byPath = new LinkedHashMap<>();
        for (Choice c : storyChoices) {
            byPath.putIfAbsent(c.getPathName(), false);
        }
        if (username != null) {
            User u = users.findByUsername(username).orElse(null);
            if (u != null) {
                Set<String> discovered = paths.findByUserIdAndStoryId(u.getId(), storyId).stream()
                        .map(PathProgress::getPathName)
                        .collect(Collectors.toSet());
                byPath.replaceAll((k, v) -> discovered.contains(k));
            }
        }
        List<ResponseDtos.PathsResponse.PathDto> list = byPath.entrySet().stream()
                .map(e -> new ResponseDtos.PathsResponse.PathDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        long discoveredCount = list.stream().filter(ResponseDtos.PathsResponse.PathDto::discovered).count();
        return new ResponseDtos.PathsResponse(list, list.size(), (int) discoveredCount);
    }

    // ---------- favorites ----------

    @Transactional
    public boolean toggleFavorite(String username, Long storyId) {
        User u = user(username);
        Story story = stories.findById(storyId).orElseThrow(() -> new IllegalArgumentException("Story not found."));
        var existing = favorites.findByUserIdAndStoryId(u.getId(), storyId);
        if (existing.isPresent()) {
            favorites.delete(existing.get());
            return false;
        }
        Favorite f = new Favorite();
        f.setUser(u);
        f.setStory(story);
        favorites.save(f);
        return true;
    }

    @Transactional(readOnly = true)
    public boolean isFavorite(String username, Long storyId) {
        User u = user(username);
        return favorites.findByUserIdAndStoryId(u.getId(), storyId).isPresent();
    }

    public record FavoriteStory(Long id, String title, String description, String genre, String coverStyle) {}

    @Transactional(readOnly = true)
    public List<FavoriteStory> myFavorites(String username) {
        User u = user(username);
        return favorites.findByUserIdOrderByCreatedAtDesc(u.getId()).stream()
                .map(f -> new FavoriteStory(f.getStory().getId(), f.getStory().getTitle(),
                        f.getStory().getDescription(), f.getStory().getGenre(), f.getStory().getCoverStyle()))
                .collect(Collectors.toList());
    }

    // ---------- comments ----------

    @Transactional(readOnly = true)
    public List<ResponseDtos.CommentResponse> listComments(Long storyId) {
        return comments.findByStoryIdOrderByCreatedAtDesc(storyId).stream()
                .map(ResponseDtos.CommentResponse::of).collect(Collectors.toList());
    }

    @Transactional
    public ResponseDtos.CommentResponse addComment(String username, Long storyId, String text) {
        User u = user(username);
        Story story = stories.findById(storyId).orElseThrow(() -> new IllegalArgumentException("Story not found."));
        Comment c = new Comment();
        c.setStory(story);
        c.setUser(u);
        c.setCommentText(text.trim());
        return ResponseDtos.CommentResponse.of(comments.save(c));
    }

    @Transactional
    public void deleteComment(String username, Long commentId) {
        Comment c = comments.findById(commentId).orElseThrow(() -> new IllegalArgumentException("Comment not found."));
        User u = user(username);
        if (!c.getUser().getId().equals(u.getId()) && u.getRole() != RoleHelper.admin()) {
            throw new SecurityException("You can only delete your own comments.");
        }
        comments.delete(c);
    }

    static final class RoleHelper {
        static com.chooseyourpath.model.Role admin() {
            return com.chooseyourpath.model.Role.ADMIN;
        }
    }
}
