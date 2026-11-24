package com.hackathonbuddy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectTaskResponse {
    private Long id;
    private Long projectId;
    private String title;
    private String description;
    private String assignedTo;
    private Long assignedToUserId;
    private String assignedToEmail;
    private String priority;
    private String status;
}
