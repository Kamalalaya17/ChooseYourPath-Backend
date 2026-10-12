package com.chooseyourpath.service;

import com.chooseyourpath.dto.ResponseDtos;
import com.chooseyourpath.entity.*;
import com.chooseyourpath.model.Role;
import com.chooseyourpath.model.StoryStatus;
import com.chooseyourpath.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private final UserRepository users;
    private final StoryRepository stories;
    private final SceneRepository scenes;
    private final ChoiceRepository choices;
    private final CommentRepository comments;
    private final ReadingProgressRepository progress;
    private final PathProgressRepository paths;
    private final FavoriteRepository favorites;
    private final StoryCleanupService cleanup;

    public AdminService(UserRepository users, StoryRepository stories, SceneRepository scenes,
                        ChoiceRepository choices, CommentRepository comments,
                        ReadingProgressRepository progress, PathProgressRepository paths,
                        FavoriteRepository favorites, StoryCleanupService cleanup) {
        this.users = users;
        this.stories = stories;
        this.scenes = scenes;
        this.choices = choices;
        this.comments = comments;
        this.progress = progress;
        this.paths = paths;
        this.favorites = favorites;
        this.cleanup = cleanup;
    }

    // ---------- stats ----------

    public Long userIdOf(String username) {
        return users.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found.")).getId();
    }

    @Transactional(readOnly = true)
    public ResponseDtos.StatsResponse stats() {
        long readers = users.count();
        return new ResponseDtos.StatsResponse(
                users.findByRole(Role.READER).size(),
                users.findByRole(Role.WRITER).size(),
                stories.count(),
                stories.findByStatus(StoryStatus.PUBLISHED).size(),
                stories.findByStatus(StoryStatus.PENDING).size(),
                stories.findByStatus(StoryStatus.DRAFT).size(),
                stories.findByStatus(StoryStatus.REJECTED).size(),
                comments.count(),
                stories.findAll().stream().mapToLong(Story::getReads).sum());
    }

    // ---------- users ----------

    @Transactional(readOnly = true)
    public List<ResponseDtos.UserResponse> listUsers(Role role) {
        List<User> list = role == null ? users.findAll() : users.findByRole(role);
        return list.stream().map(ResponseDtos.UserResponse::of).collect(Collectors.toList());
    }

    @Transactional
    public void setRole(Long userId, String role) {
        User u = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));
        Role parsed;
        try {
            parsed = Role.valueOf(role.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Unknown role.");
        }
        if (u.getRole() == Role.ADMIN) throw new IllegalStateException("Admin accounts cannot be changed.");
        if (parsed == Role.ADMIN) throw new IllegalArgumentException("A user cannot be promoted to ADMIN here.");
        u.setRole(parsed);
        users.save(u);
    }

    @Transactional
    public void deleteUser(Long actingUserId, Long userId) {
        User acting = users.findById(actingUserId).orElseThrow();
        if (acting.getId().equals(userId)) throw new IllegalStateException("You cannot delete your own account.");
        User target = users.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (target.getRole() == Role.ADMIN) throw new IllegalStateException("Admin accounts cannot be deleted.");
        // FK-safe: remove the user's stories (deep), then their personal rows.
        cleanup.deleteAllStoriesOf(userId);
        progress.findByUserIdOrderByUpdatedAtDesc(userId).forEach(progress::delete);
        paths.findByUserId(userId).forEach(paths::delete);
        favorites.findByUserIdOrderByCreatedAtDesc(userId).forEach(favorites::delete);
        users.delete(target);
    }

    // ---------- stories ----------

    public record AdminStory(Long id, String title, String description, String narrative,
                             String authorName, String status, String genre, String theme,
                             String writingStyle, long sceneCount, long endingCount, long reads) {}

    @Transactional(readOnly = true)
    public List<AdminStory> listStories(String status) {
        List<Story> list = (status == null || status.isBlank())
                ? stories.findAll()
                : stories.findByStatus(StoryStatus.valueOf(status.toUpperCase()));
        return list.stream()
                .map(s -> new AdminStory(s.getId(), s.getTitle(), s.getDescription(), s.getNarrative(),
                        s.getCreator().getUsername(), s.getStatus().name(), s.getGenre(), s.getTheme(),
                        s.getWritingStyle(), scenes.countByStoryId(s.getId()),
                        scenes.countByStoryIdAndIsEndingTrue(s.getId()), s.getReads()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public java.util.Map<String, Object> reviewStory(Long id) {
        Story story = stories.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Story not found."));

        ResponseDtos.StoryResponse storyResponse = ResponseDtos.StoryResponse.of(
                story,
                scenes.countByStoryId(id),
                scenes.countByStoryIdAndIsEndingTrue(id));

        List<java.util.Map<String, Object>> reviewedScenes =
                scenes.findByStoryIdOrderBySceneOrderAsc(id).stream()
                        .map(scene -> {
                            java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
                            item.put("scene", ResponseDtos.SceneResponse.of(scene));
                            item.put("choices", choices.findBySceneId(scene.getId()).stream()
                                    .map(ResponseDtos.ChoiceResponse::of)
                                    .collect(Collectors.toList()));
                            return item;
                        })
                        .collect(Collectors.toList());

        java.util.Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("story", storyResponse);
        result.put("scenes", reviewedScenes);
        return result;
    }

    @Transactional
    public void approveStory(Long id) {
        Story s = stories.findById(id).orElseThrow(() -> new IllegalArgumentException("Story not found."));
        s.setStatus(StoryStatus.PUBLISHED);
        stories.save(s);
    }

    @Transactional
    public void rejectStory(Long id) {
        Story s = stories.findById(id).orElseThrow(() -> new IllegalArgumentException("Story not found."));
        s.setStatus(StoryStatus.REJECTED);
        stories.save(s);
    }

    @Transactional
    public void deleteStory(Long id) {
        cleanup.deleteStoryDeep(id);
    }

    // ---------- comments ----------

    @Transactional(readOnly = true)
    public List<ResponseDtos.CommentResponse> allComments() {
        return comments.findAllByOrderByCreatedAtDesc().stream()
                .map(ResponseDtos.CommentResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId) {
        comments.deleteById(commentId);
    }
}
