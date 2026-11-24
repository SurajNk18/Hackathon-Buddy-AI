package com.hackathonbuddy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    private String type; // hackathon, team, project, message, skill

    @Builder.Default
    private Boolean isRead = false;

    private String icon;
    private String action;
    private String route;
    private String actionUrl;

    private Long targetTeamId;
    private Long targetUserId;
    private String responseStatus; // PENDING, ACCEPTED, REJECTED

    private LocalDateTime createdAt;

    public Notification() {
    }

    public Notification(Long id, User user, String title, String message, String type,
                        Boolean isRead, String icon, String action, String route, String actionUrl,
                        Long targetTeamId, Long targetUserId, String responseStatus,
                        LocalDateTime createdAt) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.icon = icon;
        this.action = action;
        this.route = route;
        this.actionUrl = actionUrl;
        this.targetTeamId = targetTeamId;
        this.targetUserId = targetUserId;
        this.responseStatus = responseStatus;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
