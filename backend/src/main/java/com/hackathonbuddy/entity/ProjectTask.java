package com.hackathonbuddy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "project_tasks")
@Data
@Builder
public class ProjectTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String assignedTo;

    private Long assignedToUserId;

    private String assignedToEmail;

    @Builder.Default
    private String priority = "Medium";

    @Builder.Default
    private String status = "To Do";

    private LocalDateTime createdAt;

    public ProjectTask() {
    }

    public ProjectTask(Long id, Project project, String title, String description,
                       String assignedTo, Long assignedToUserId, String assignedToEmail,
                       String priority, String status, LocalDateTime createdAt) {
        this.id = id;
        this.project = project;
        this.title = title;
        this.description = description;
        this.assignedTo = assignedTo;
        this.assignedToUserId = assignedToUserId;
        this.assignedToEmail = assignedToEmail;
        this.priority = priority;
        this.status = status;
        this.createdAt = createdAt;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (priority == null) {
            priority = "Medium";
        }
        if (status == null) {
            status = "To Do";
        }
    }
}
