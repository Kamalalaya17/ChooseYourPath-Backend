package com.chooseyourpath.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "choices", indexes = @Index(name = "idx_choices_scene", columnList = "scene_id"))
public class Choice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "scene_id", nullable = false)
    private Scene scene;

    @Column(name = "choice_text", nullable = false, length = 300)
    private String choiceText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_scene_id")
    private Scene nextScene;

    @Column(name = "path_name", nullable = false, length = 120)
    private String pathName;

    @Column(columnDefinition = "TEXT")
    private String consequence;

    public Long getId() { return id; }
    public Scene getScene() { return scene; }
    public void setScene(Scene scene) { this.scene = scene; }
    public String getChoiceText() { return choiceText; }
    public void setChoiceText(String choiceText) { this.choiceText = choiceText; }
    public Scene getNextScene() { return nextScene; }
    public void setNextScene(Scene nextScene) { this.nextScene = nextScene; }
    public String getPathName() { return pathName; }
    public void setPathName(String pathName) { this.pathName = pathName; }
    public String getConsequence() { return consequence; }
    public void setConsequence(String consequence) { this.consequence = consequence; }
}
