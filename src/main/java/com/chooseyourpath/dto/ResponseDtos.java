package com.chooseyourpath.dto;

import com.chooseyourpath.entity.*;
import com.chooseyourpath.model.StoryStatus;
import com.chooseyourpath.model.Role;
import java.time.LocalDateTime;
import java.util.List;

public class ResponseDtos {

    public record UserResponse(Long id, String username, String bio, Role role, LocalDateTime createdAt) {
        public static UserResponse of(User u) {
            return new UserResponse(u.getId(), u.getUsername(), u.getBio(), u.getRole(), u.getCreatedAt());
        }
    }

    public record StoryResponse(
            Long id, String title, String description, String genre, String theme,
            String writingStyle, String narrative, String coverStyle, StoryStatus status,
            String authorName, long reads, long sceneCount, long endingCount,
            LocalDateTime createdAt) {
        public static StoryResponse of(Story s, long sceneCount, long endingCount) {
            return new StoryResponse(s.getId(), s.getTitle(), s.getDescription(), s.getGenre(),
                    s.getTheme(), s.getWritingStyle(), s.getNarrative(), s.getCoverStyle(),
                    s.getStatus(), s.getCreator().getUsername(), s.getReads(),
                    sceneCount, endingCount, s.getCreatedAt());
        }
    }

    public record SceneResponse(Long id, Long storyId, int sceneOrder, String title, String narration,
                                String sceneDescription, String illustrationDescription, boolean isEnding, String endingLabel) {
        public static SceneResponse of(Scene s) {
            return new SceneResponse(s.getId(), s.getStory().getId(), s.getSceneOrder(), s.getTitle(),
                    s.getNarration(), s.getSceneDescription(), s.getIllustrationDescription(), s.isEnding(), s.getEndingLabel());
        }
    }

    public record ChoiceResponse(Long id, Long sceneId, String choiceText, Long nextSceneId,
                                 String pathName, String consequence) {
        public static ChoiceResponse of(Choice c) {
            return new ChoiceResponse(c.getId(), c.getScene().getId(), c.getChoiceText(),
                    c.getNextScene() != null ? c.getNextScene().getId() : null,
                    c.getPathName(), c.getConsequence());
        }
    }

    public record StyleResponse(Long id, String styleName, String styleDescription) {
        public static StyleResponse of(StoryStyle s) {
            return new StyleResponse(s.getId(), s.getStyleName(), s.getStyleDescription());
        }
    }

    public record CommentResponse(Long id, Long storyId, String authorName, String commentText, LocalDateTime createdAt) {
        public static CommentResponse of(Comment c) {
            return new CommentResponse(c.getId(), c.getStory().getId(), c.getUser().getUsername(),
                    c.getCommentText(), c.getCreatedAt());
        }
    }

    public record ProgressResponse(Long storyId, Long currentSceneId, int progressPercentage, LocalDateTime updatedAt) {
        public static ProgressResponse of(ReadingProgress p) {
            return new ProgressResponse(p.getStory().getId(), p.getCurrentScene().getId(),
                    p.getProgressPercentage(), p.getUpdatedAt());
        }
    }

    public record PathsResponse(List<PathDto> paths, int total, int discovered) {
        public record PathDto(String pathName, boolean discovered) {}
    }

    public record StatsResponse(long readers, long writers, long stories, long published, long pending,
                                long drafts, long rejected, long comments, long reads) {}
}
