package com.hackathonbuddy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_ideas")
@Data
@Builder
public class ProjectIdea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String techStack;

    @Column(columnDefinition = "TEXT")
    private String problemStatement;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User generatedFor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hackathon_id")
    private Hackathon hackathon;

    private String category;
    private Integer aiConfidenceScore;

    private LocalDateTime generatedAt;

    public ProjectIdea() {
    }

    public ProjectIdea(Long id, String title, String description, String techStack,
                       String problemStatement, User generatedFor, Hackathon hackathon,
                       String category, Integer aiConfidenceScore, LocalDateTime generatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.techStack = techStack;
        this.problemStatement = problemStatement;
        this.generatedFor = generatedFor;
        this.hackathon = hackathon;
        this.category = category;
        this.aiConfidenceScore = aiConfidenceScore;
        this.generatedAt = generatedAt;
    }

    @PrePersist
    protected void onCreate() {
        generatedAt = LocalDateTime.now();
    }
}
