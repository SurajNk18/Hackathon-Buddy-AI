package com.hackathonbuddy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences")
@Data
@Builder
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 100)
    private String preferredRole; // e.g. "Backend Developer", "ML Developer"

    @Column(columnDefinition = "TEXT")
    private String preferredDomains; // comma-separated: "AI/ML,FinTech,HealthTech"

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 200)
    private String location;

    @Column(length = 500)
    private String profilePhotoUrl;

    private LocalDateTime updatedAt;

    public UserPreference() {
    }

    public UserPreference(Long id, User user, String preferredRole, String preferredDomains,
                          String bio, String location, String profilePhotoUrl, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.preferredRole = preferredRole;
        this.preferredDomains = preferredDomains;
        this.bio = bio;
        this.location = location;
        this.profilePhotoUrl = profilePhotoUrl;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        updatedAt = LocalDateTime.now();
    }
}
