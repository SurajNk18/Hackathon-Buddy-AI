package com.hackathonbuddy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_skills")
@Data
@Builder
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    private Integer proficiencyLevel; // 1-5 (Beginner to Expert)

    private LocalDateTime addedAt;

    public UserSkill() {
    }

    public UserSkill(Long id, User user, Skill skill, Integer proficiencyLevel, LocalDateTime addedAt) {
        this.id = id;
        this.user = user;
        this.skill = skill;
        this.proficiencyLevel = proficiencyLevel;
        this.addedAt = addedAt;
    }

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}
