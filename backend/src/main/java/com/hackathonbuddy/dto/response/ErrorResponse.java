package com.hackathonbuddy.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponse {
    @Builder.Default
    private boolean success = false;
    private String message;
    private Object errors;
    private LocalDateTime timestamp;
    private String path;

    public ErrorResponse() {
    }

    public ErrorResponse(boolean success, String message, Object errors,
                         LocalDateTime timestamp, String path) {
        this.success = success;
        this.message = message;
        this.errors = errors;
        this.timestamp = timestamp;
        this.path = path;
    }
}
