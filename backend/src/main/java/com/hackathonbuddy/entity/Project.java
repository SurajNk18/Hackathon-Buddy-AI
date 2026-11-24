package com.hackathonbuddy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "projects")
@Data
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String domain;

    @Builder.Default
    private String status = "Active";

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String technologies;

    @Builder.Default
    private Integer members = 1;

    @Builder.Default
    private Integer progress = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private LocalDateTime createdAt;

    public Project() {
    }

    public Project(Long id, String name, String domain, String status, String description,
                   String technologies, Integer members, Integer progress, User user, Team team,
                   LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.domain = domain;
        this.status = status;
        this.description = description;
        this.technologies = technologies;
        this.members = members;
        this.progress = progress;
        this.user = user;
        this.team = team;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "Active";
        }
        if (members == null) {
            members = 1;
        }
        if (progress == null) {
            progress = 0;
        }
    }
}
