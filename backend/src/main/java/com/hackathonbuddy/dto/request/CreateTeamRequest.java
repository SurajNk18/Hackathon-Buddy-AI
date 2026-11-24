package com.hackathonbuddy.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
public class CreateTeamRequest {
    @NotBlank
    private String name;
    private String description;
    private Long hackathonId;

    public CreateTeamRequest() {
    }

    public CreateTeamRequest(String name, String description, Long hackathonId) {
        this.name = name;
        this.description = description;
        this.hackathonId = hackathonId;
    }
}
