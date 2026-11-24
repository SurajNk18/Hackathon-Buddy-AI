package com.hackathonbuddy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_interests")
@Data
@Builder
public class UserInterest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String interest; // e.g. "Web Development", "AI/ML", "Cloud Computing"

    private LocalDateTime addedAt;

    public UserInterest() {
    }

    public UserInterest(Long id, User user, String interest, LocalDateTime addedAt) {
        this.id = id;
        this.user = user;
        this.interest = interest;
        this.addedAt = addedAt;
    }

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}
