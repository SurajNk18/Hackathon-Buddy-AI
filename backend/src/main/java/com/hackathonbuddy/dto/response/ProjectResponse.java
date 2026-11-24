package com.hackathonbuddy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectResponse {
    private Long id;
    private String name;
    private String domain;
    private String status;
    private String description;
    private List<String> technologies;
    private Integer members;
    private Integer progress;
    private String createdAt;
    private Long teamId;
    private String teamName;
    private Long userId;
}
