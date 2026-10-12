package com.chooseyourpath.controller;

import com.chooseyourpath.dto.ResponseDtos;
import com.chooseyourpath.dto.StoryDtos;
import com.chooseyourpath.service.ReadingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Progress / Favorites / Comments")
public class ReaderControllers {

    private final ReadingService reading;

    public ReaderControllers(ReadingService reading) {
        this.reading = reading;
    }

    private static String me() {
        return org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }

    // ---------- progress ----------

    @GetMapping("/reader/progress")
    @PreAuthorize("hasRole('READER')")
    @Operation(summary = "Reader: full reading history")
    public ResponseEntity<List<ReadingService.HistoryItem>> history() {
        return ResponseEntity.ok(reading.history(me()));
    }

    @PostMapping("/reader/progress")
    @PreAuthorize("hasRole('READER')")
    @Operation(summary = "Reader: save progress")
    public ResponseEntity<ResponseDtos.ProgressResponse> save(@RequestBody Map<String, Long> body) {
        return ResponseEntity.ok(reading.saveProgress(me(), body.get("storyId"), body.get("sceneId")));
    }

    @PutMapping("/reader/progress")
    @PreAuthorize("hasRole('READER')")
    @Operation(summary = "Reader: update progress (same as save)")
    public ResponseEntity<ResponseDtos.ProgressResponse> update(@RequestBody Map<String, Long> body) {
        return ResponseEntity.ok(reading.saveProgress(me(), body.get("storyId"), body.get("sceneId")));
    }

    @GetMapping("/reader/progress/{storyId}")
    @PreAuthorize("hasRole('READER')")
    @Operation(summary = "Reader: progress for one story")
    public ResponseEntity<ResponseDtos.ProgressResponse> progress(@PathVariable Long storyId) {
        return ResponseEntity.ok(reading.getProgress(me(), storyId));
    }

    // ---------- paths / perspectives ----------

    @GetMapping("/reader/stories/{storyId}/paths")
    @Operation(summary = "Discovered perspectives for a story (works signed-in or anonymous)")
    public ResponseEntity<ResponseDtos.PathsResponse> paths(@PathVariable Long storyId) {
        return ResponseEntity.ok(reading.storyPaths(me(), storyId));
    }

    @PostMapping("/reader/stories/{storyId}/paths")
    @PreAuthorize("hasRole('READER')")
    @Operation(summary = "Record a discovered path (returns isNew)")
    public ResponseEntity<Map<String, Object>> discover(@PathVariable Long storyId,
                                                        @RequestBody Map<String, Long> body) {
        boolean isNew = reading.discoverPath(me(), storyId, body.get("choiceId"));
        return ResponseEntity.ok(Map.of("isNew", isNew));
    }

    // ---------- favorites ----------

    @GetMapping("/reader/favorites")
    @PreAuthorize("hasRole('READER')")
    @Operation(summary = "Reader: my favorites")
    public ResponseEntity<List<ReadingService.FavoriteStory>> favorites() {
        return ResponseEntity.ok(reading.myFavorites(me()));
    }

    @PostMapping("/reader/favorites/{storyId}")
    @PreAuthorize("hasRole('READER')")
    @Operation(summary = "Reader: toggle favorite")
    public ResponseEntity<Map<String, Object>> toggleFavorite(@PathVariable Long storyId) {
        return ResponseEntity.ok(Map.of("favorited", reading.toggleFavorite(me(), storyId)));
    }

    @GetMapping("/reader/favorites/{storyId}")
    @PreAuthorize("hasRole('READER')")
    @Operation(summary = "Reader: is story a favorite")
    public ResponseEntity<Map<String, Object>> isFavorite(@PathVariable Long storyId) {
        return ResponseEntity.ok(Map.of("favorited", reading.isFavorite(me(), storyId)));
    }

    // ---------- comments ----------

    @GetMapping("/stories/{storyId}/comments")
    @Operation(summary = "Comments of a story (public)")
    public ResponseEntity<List<ResponseDtos.CommentResponse>> comments(@PathVariable Long storyId) {
        return ResponseEntity.ok(reading.listComments(storyId));
    }

    @PostMapping("/reader/stories/{storyId}/comments")
    @PreAuthorize("hasRole('READER')")
    @Operation(summary = "Reader: add comment")
    public ResponseEntity<ResponseDtos.CommentResponse> addComment(@PathVariable Long storyId,
                                                                   @Valid @RequestBody StoryDtos.CommentRequest body) {
        return ResponseEntity.ok(reading.addComment(me(), storyId, body.commentText));
    }

    @DeleteMapping("/reader/comments/{id}")
    @Operation(summary = "Delete own comment (admins may delete any)")
    public ResponseEntity<?> deleteComment(@PathVariable Long id) {
        reading.deleteComment(me(), id);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
