package com.hackathonbuddy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "hackathons")
@Data
@Builder
public class Hackathon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private String category;

    private String organizer;

    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate registrationDeadline;

    private String prizePool;
    private String location;
    private String mode; // ONLINE, OFFLINE, HYBRID

    private Integer maxTeamSize;
    private Integer minTeamSize;

    private String imageUrl;
    private String websiteUrl;

    private String duration;
    private String icon;
    private String color;
    private String type;
    private String level;

    @Builder.Default
    private String status = "Open";

    @Builder.Default
    private Integer participantCount = 0;

    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Hackathon() {
    }

    public Hackathon(Long id, String title, String description, String category, String organizer,
                     LocalDate startDate, LocalDate endDate, LocalDate registrationDeadline,
                     String prizePool, String location, String mode,
                     Integer maxTeamSize, Integer minTeamSize,
                     String imageUrl, String websiteUrl,
                     String duration, String icon, String color, String type, String level,
                     String status, Integer participantCount, Boolean isActive,
                     LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.organizer = organizer;
        this.startDate = startDate;
        this.endDate = endDate;
        this.registrationDeadline = registrationDeadline;
        this.prizePool = prizePool;
        this.location = location;
        this.mode = mode;
        this.maxTeamSize = maxTeamSize;
        this.minTeamSize = minTeamSize;
        this.imageUrl = imageUrl;
        this.websiteUrl = websiteUrl;
        this.duration = duration;
        this.icon = icon;
        this.color = color;
        this.type = type;
        this.level = level;
        this.status = status;
        this.participantCount = participantCount;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
