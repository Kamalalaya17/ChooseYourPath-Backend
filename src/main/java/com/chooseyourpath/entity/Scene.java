package com.chooseyourpath.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "scenes", indexes = @Index(name = "idx_scenes_story", columnList = "story_id"))
public class Scene {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String narration;

    @Column(name = "scene_description", columnDefinition = "TEXT")
    private String sceneDescription;

    @Column(name = "illustration_description", columnDefinition = "TEXT")
    private String illustrationDescription;

    @Column(name = "scene_order", nullable = false)
    private int sceneOrder;

    @Column(name = "is_ending", nullable = false)
    private boolean isEnding = false;

    @Column(name = "ending_label", length = 60)
    private String endingLabel;

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Story getStory() { return story; }
    public void setStory(Story story) { this.story = story; }
    public String getNarration() { return narration; }
    public void setNarration(String narration) { this.narration = narration; }
    public String getSceneDescription() { return sceneDescription; }
    public void setSceneDescription(String sceneDescription) { this.sceneDescription = sceneDescription; }
    public String getIllustrationDescription() { return illustrationDescription; }
    public void setIllustrationDescription(String illustrationDescription) { this.illustrationDescription = illustrationDescription; }
    public int getSceneOrder() { return sceneOrder; }
    public void setSceneOrder(int sceneOrder) { this.sceneOrder = sceneOrder; }
    public boolean isEnding() { return isEnding; }
    public void setEnding(boolean ending) { isEnding = ending; }
    public String getEndingLabel() { return endingLabel; }
    public void setEndingLabel(String endingLabel) { this.endingLabel = endingLabel; }
}
