package com.chooseyourpath.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "scene_images", indexes = @Index(name = "idx_scene_images_scene", columnList = "scene_id"))
public class SceneImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scene_id", nullable = false)
    private Scene scene;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "style_id", nullable = false)
    private StoryStyle style;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    public Long getId() { return id; }
    public Scene getScene() { return scene; }
    public void setScene(Scene scene) { this.scene = scene; }
    public StoryStyle getStyle() { return style; }
    public void setStyle(StoryStyle style) { this.style = style; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
