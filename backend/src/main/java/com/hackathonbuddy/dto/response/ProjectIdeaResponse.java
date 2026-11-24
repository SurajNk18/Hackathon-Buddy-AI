package com.hackathonbuddy.dto.response;

import lombok.*;
import java.util.List;

@Data
@Builder
public class ProjectIdeaResponse {
    private Long id;
    private String title;
    private String description;
    private String techStack;
    private String problemStatement;
    private String category;
    private Integer aiConfidenceScore;
    private String generatedAt;

    public ProjectIdeaResponse() {
    }

    public ProjectIdeaResponse(Long id, String title, String description, String techStack,
                               String problemStatement, String category,
                               Integer aiConfidenceScore, String generatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.techStack = techStack;
        this.problemStatement = problemStatement;
        this.category = category;
        this.aiConfidenceScore = aiConfidenceScore;
        this.generatedAt = generatedAt;
    }
}
