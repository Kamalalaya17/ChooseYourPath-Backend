package com.chooseyourpath.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "story_styles", indexes = @Index(name = "idx_styles_story", columnList = "story_id"))
public class StoryStyle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @Column(name = "style_name", nullable = false, length = 60)
    private String styleName;

    @Column(name = "style_description", length = 300)
    private String styleDescription;

    public Long getId() { return id; }
    public Story getStory() { return story; }
    public void setStory(Story story) { this.story = story; }
    public String getStyleName() { return styleName; }
    public void setStyleName(String styleName) { this.styleName = styleName; }
    public String getStyleDescription() { return styleDescription; }
    public void setStyleDescription(String styleDescription) { this.styleDescription = styleDescription; }
}
