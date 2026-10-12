package com.chooseyourpath.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class StoryDtos {

    public static class StoryRequest {
        @NotBlank
        @Size(min = 3, max = 120)
        public String title;
        @NotBlank
        @Size(min = 10, max = 2000)
        public String description;
        @NotBlank
        public String genre;
        public String theme;
        public String writingStyle;
        @NotBlank
        public String narrative;
        public String coverStyle;
        public java.util.List<String> styles;
    }

    public static class SceneRequest {
        @NotBlank
        public String title;
        @NotBlank
        public String narration;
        public String sceneDescription;
        public String illustrationDescription;
        public boolean isEnding;
        public String endingLabel;
    }

    public static class ChoiceRequest {
        @NotBlank
        public String choiceText;
        public Long nextSceneId;
        @NotBlank
        public String pathName;
        public String consequence;
    }

    public static class CommentRequest {
        @NotBlank
        @Size(max = 1000)
        public String commentText;
    }
}
