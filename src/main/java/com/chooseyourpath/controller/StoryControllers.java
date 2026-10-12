package com.chooseyourpath.controller;

import com.chooseyourpath.dto.ResponseDtos;
import com.chooseyourpath.dto.StoryDtos;
import com.chooseyourpath.service.StoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Stories / Scenes / Choices")
public class StoryControllers {

    private final StoryService storyService;
    private final com.chooseyourpath.repository.SceneRepository sceneRepo;

    public StoryControllers(StoryService storyService, com.chooseyourpath.repository.SceneRepository sceneRepo) {
        this.storyService = storyService;
        this.sceneRepo = sceneRepo;
    }

    private static String me() {
        return org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication().getName();
    }

    // ---------- public catalog ----------

    @GetMapping("/stories")
    @Operation(summary = "Browse published stories (search, filter, sort, paginate)")
    public ResponseEntity<Page<ResponseDtos.StoryResponse>> catalog(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "newest") String sort) {
        return ResponseEntity.ok(storyService.catalog(search, genre, page, size, sort));
    }

    @GetMapping("/stories/{id}")
    @Operation(summary = "Story detail (increments reads)")
    public ResponseEntity<ResponseDtos.StoryResponse> story(@PathVariable Long id) {
        return ResponseEntity.ok(storyService.getStory(id));
    }

    @GetMapping("/stories/{storyId}/scenes")
    @Operation(summary = "All scenes of a story")
    public ResponseEntity<List<ResponseDtos.SceneResponse>> scenes(@PathVariable Long storyId) {
        return ResponseEntity.ok(storyService.getScenes(storyId));
    }

    @GetMapping("/scenes/{sceneId}")
    @Operation(summary = "Scene with its choices")
    public ResponseEntity<Map<String, Object>> scene(@PathVariable Long sceneId) {
        com.chooseyourpath.entity.Scene s = sceneRepo.findById(sceneId)
                .orElseThrow(() -> new IllegalArgumentException("Scene not found."));
        return ResponseEntity.ok(Map.of(
                "scene", ResponseDtos.SceneResponse.of(s),
                "choices", storyService.getChoices(sceneId)));
    }

    @GetMapping("/scenes/{sceneId}/choices")
    @Operation(summary = "Choices of a scene")
    public ResponseEntity<List<ResponseDtos.ChoiceResponse>> choices(@PathVariable Long sceneId) {
        return ResponseEntity.ok(storyService.getChoices(sceneId));
    }

    @GetMapping("/stories/{storyId}/styles")
    @Operation(summary = "Visual styles offered by a story")
    public ResponseEntity<List<ResponseDtos.StyleResponse>> styles(@PathVariable Long storyId) {
        return ResponseEntity.ok(storyService.getStyles(storyId));
    }

    // ---------- writer: stories ----------

    @GetMapping("/writer/stories")
    @PreAuthorize("hasRole('WRITER')")
    @Operation(summary = "Writer: my stories")
    public ResponseEntity<List<ResponseDtos.StoryResponse>> myStories() {
        return ResponseEntity.ok(storyService.myStories(me()));
    }

    @PostMapping("/writer/stories")
    @PreAuthorize("hasRole('WRITER')")
    @Operation(summary = "Writer: create story")
    public ResponseEntity<?> create(@Valid @RequestBody StoryDtos.StoryRequest body) {
        return ResponseEntity.ok(Map.of("id", storyService.createStory(me(), body)));
    }
    @PutMapping("/writer/stories/{id}")
    @PreAuthorize("hasRole('WRITER')")
    @Operation(summary = "Writer: update story")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody StoryDtos.StoryRequest body) {
        storyService.updateStory(me(), id, body);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/writer/stories/{id}")
    @PreAuthorize("hasRole('WRITER')")
    @Operation(summary = "Writer: delete story")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        storyService.deleteStory(me(), id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PutMapping("/writer/stories/{id}/submit")
    @PreAuthorize("hasRole('WRITER')")
    @Operation(summary = "Writer: submit story for review")
    public ResponseEntity<?> submit(@PathVariable Long id) {
        storyService.submitForReview(me(), id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @PutMapping("/writer/stories/{id}/unpublish")
    @PreAuthorize("hasRole('WRITER')")
    @Operation(summary = "Writer: unpublish story")
    public ResponseEntity<?> unpublish(@PathVariable Long id) {
        storyService.unpublish(me(), id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // ---------- writer: scenes ----------

    @PostMapping("/writer/stories/{storyId}/scenes")
    @PreAuthorize("hasRole('WRITER')")
    @Operation(summary = "Writer: add scene")
    public ResponseEntity<?> addScene(@PathVariable Long storyId, @Valid @RequestBody StoryDtos.SceneRequest body) {
        return ResponseEntity.ok(Map.of("id", storyService.createScene(me(), storyId, body)));
    }

    @PutMapping("/writer/scenes/{id}")
    @PreAuthorize("hasRole('WRITER')")
    @Operation(summary = "Writer: update scene")
    public ResponseEntity<?> updateScene(@PathVariable Long id, @Valid @RequestBody StoryDtos.SceneRequest body) {
        storyService.updateScene(me(), id, body);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/writer/scenes/{id}")
    @PreAuthorize("hasRole('WRITER')")
    @Operation(summary = "Writer: delete scene")
    public ResponseEntity<?> deleteScene(@PathVariable Long id) {
        storyService.deleteScene(me(), id);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    // ---------- writer: choices ----------

    @PostMapping("/writer/scenes/{sceneId}/choices")
    @PreAuthorize("hasRole('WRITER')")
    @Operation(summary = "Writer: add choice")
    public ResponseEntity<?> addChoice(@PathVariable Long sceneId, @Valid @RequestBody StoryDtos.ChoiceRequest body) {
        return ResponseEntity.ok(Map.of("id", storyService.createChoice(me(), sceneId, body)));
    }

    @PutMapping("/writer/choices/{id}")
    @PreAuthorize("hasRole('WRITER')")
    @Operation(summary = "Writer: update choice")
    public ResponseEntity<?> updateChoice(@PathVariable Long id, @Valid @RequestBody StoryDtos.ChoiceRequest body) {
        storyService.updateChoice(me(), id, body);
        return ResponseEntity.ok(Map.of("ok", true));
    }

    @DeleteMapping("/writer/choices/{id}")
    @PreAuthorize("hasRole('WRITER')")
    @Operation(summary = "Writer: delete choice")
    public ResponseEntity<?> deleteChoice(@PathVariable Long id) {
        storyService.deleteChoice(me(), id);
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
