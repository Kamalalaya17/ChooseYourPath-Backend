package com.chooseyourpath.entity;
import com.chooseyourpath.model.StoryStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stories", indexes = {
    @Index(name = "idx_stories_status", columnList = "status"),
    @Index(name = "idx_stories_creator", columnList = "creator_id")
})
public class Story {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false, length = 60)
    private String genre;

    @Column(length = 120)
    private String theme;

    @Column(name = "writing_style", length = 120)
    private String writingStyle;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String narrative;

    @Column(name = "cover_style", length = 60)
    private String coverStyle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private StoryStatus status = StoryStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    // `reads` is a reserved keyword in MySQL. Use a safe physical column name.
    @Column(name = "read_count", nullable = false)
    private long reads = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "story", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sceneOrder ASC")
    private List<Scene> scenes = new ArrayList<>();

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public String getWritingStyle() { return writingStyle; }
    public void setWritingStyle(String writingStyle) { this.writingStyle = writingStyle; }
    public String getNarrative() { return narrative; }
    public void setNarrative(String narrative) { this.narrative = narrative; }
    public String getCoverStyle() { return coverStyle; }
    public void setCoverStyle(String coverStyle) { this.coverStyle = coverStyle; }
    public StoryStatus getStatus() { return status; }
    public void setStatus(StoryStatus status) { this.status = status; }
    public User getCreator() { return creator; }
    public void setCreator(User creator) { this.creator = creator; }
    public long getReads() { return reads; }
    public void setReads(long reads) { this.reads = reads; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<Scene> getScenes() { return scenes; }
    public void setScenes(List<Scene> scenes) { this.scenes = scenes; }
}
