package com.hackathonbuddy.dto.request;

import lombok.Data;

@Data
public class CreateTaskRequest {
    private Long projectId;
    private String title;
    private String description;
    private String assignedTo;
    private Long assignedToUserId;
    private String assignedToEmail;
    private String priority;
}
