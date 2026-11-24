package com.hackathonbuddy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_experiences")
@Data
@Builder
public class UserExperience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Builder.Default
    private Integer yearsOfExperience = 0;

    @Builder.Default
    private Integer projectCount = 0;

    @Builder.Default
    private Boolean hasInternship = false;

    @Column(columnDefinition = "TEXT")
    private String certifications; // comma-separated or JSON

    private LocalDateTime updatedAt;

    public UserExperience() {
    }

    public UserExperience(Long id, User user, Integer yearsOfExperience, Integer projectCount,
                          Boolean hasInternship, String certifications, LocalDateTime updatedAt) {
        this.id = id;
        this.user = user;
        this.yearsOfExperience = yearsOfExperience;
        this.projectCount = projectCount;
        this.hasInternship = hasInternship;
        this.certifications = certifications;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        updatedAt = LocalDateTime.now();
    }
}
