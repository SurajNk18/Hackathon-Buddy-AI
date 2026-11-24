package com.hackathonbuddy.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
public class ActivityResponse {
    private Long id;
    private String type;
    private String message;
    private String time; // Human-readable e.g. "2 hours ago"
    private LocalDateTime createdAt;

    public ActivityResponse() {
    }

    public ActivityResponse(Long id, String type, String message, String time, LocalDateTime createdAt) {
        this.id = id;
        this.type = type;
        this.message = message;
        this.time = time;
        this.createdAt = createdAt;
    }
}
