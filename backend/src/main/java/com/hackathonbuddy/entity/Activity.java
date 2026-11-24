package com.hackathonbuddy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activities")
@Data
@Builder
public class Activity {

    public Activity() {
    }

    public Activity(Long id, User user, String type, String message, String referenceId, String referenceType, LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.type = type;
        this.message = message;
        this.referenceId = referenceId;
        this.referenceType = referenceType;
        this.createdAt = createdAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Types: REGISTRATION, TEAM, MATCH, IDEA, MESSAGE
    @Column(nullable = false)
    private String type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private String referenceId;   // ID of related entity (hackathon, team, etc.)
    private String referenceType; // HACKATHON, TEAM, etc.

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
