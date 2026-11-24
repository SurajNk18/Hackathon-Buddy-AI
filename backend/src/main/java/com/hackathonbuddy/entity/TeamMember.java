package com.hackathonbuddy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "team_members")
@Data
@Builder
public class TeamMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String role; // Backend Developer, UI/UX Designer, etc.

    private String status; // PENDING, ACCEPTED, REJECTED

    private LocalDateTime joinedAt;

    public TeamMember() {
    }

    public TeamMember(Long id, Team team, User user, String role, String status, LocalDateTime joinedAt) {
        this.id = id;
        this.team = team;
        this.user = user;
        this.role = role;
        this.status = status;
        this.joinedAt = joinedAt;
    }

    @PrePersist
    protected void onCreate() {
        joinedAt = LocalDateTime.now();
        if (status == null) status = "ACCEPTED";
    }
}
