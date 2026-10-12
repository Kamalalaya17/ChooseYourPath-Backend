package com.chooseyourpath.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "path_progress", indexes = @Index(name = "idx_path_user_story", columnList = "user_id, story_id"))
public class PathProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "story_id", nullable = false)
    private Story story;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "choice_id", nullable = false)
    private Choice choice;

    @Column(name = "path_name", nullable = false, length = 120)
    private String pathName;

    @Column(name = "discovered_at", nullable = false)
    private LocalDateTime discoveredAt = LocalDateTime.now();

    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Story getStory() { return story; }
    public void setStory(Story story) { this.story = story; }
    public Choice getChoice() { return choice; }
    public void setChoice(Choice choice) { this.choice = choice; }
    public String getPathName() { return pathName; }
    public void setPathName(String pathName) { this.pathName = pathName; }
    public LocalDateTime getDiscoveredAt() { return discoveredAt; }
    public void setDiscoveredAt(LocalDateTime discoveredAt) { this.discoveredAt = discoveredAt; }
}
