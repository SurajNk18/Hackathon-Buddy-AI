package com.hackathonbuddy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "registrations")
@Data
@Builder
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "hackathon_id", nullable = false)
    private Hackathon hackathon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    private String status; // REGISTERED, WITHDRAWN, COMPLETED

    private String teamName;

    private String role;

    @Column(length = 1000)
    private String motivation;

    private LocalDateTime registeredAt;

    public Registration() {
    }

    public Registration(Long id, User user, Hackathon hackathon, Team team, String status,
                        String teamName, String role, String motivation, LocalDateTime registeredAt) {
        this.id = id;
        this.user = user;
        this.hackathon = hackathon;
        this.team = team;
        this.status = status;
        this.teamName = teamName;
        this.role = role;
        this.motivation = motivation;
        this.registeredAt = registeredAt;
    }

    @PrePersist
    protected void onCreate() {
        registeredAt = LocalDateTime.now();
        if (status == null) status = "REGISTERED";
    }
}
