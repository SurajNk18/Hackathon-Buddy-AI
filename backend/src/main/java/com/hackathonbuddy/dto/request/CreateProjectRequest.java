package com.hackathonbuddy.dto.request;

import lombok.Data;

@Data
public class CreateProjectRequest {
    private String name;
    private String domain;
    private String description;
    private Object technologies;
    private Integer members;
}
