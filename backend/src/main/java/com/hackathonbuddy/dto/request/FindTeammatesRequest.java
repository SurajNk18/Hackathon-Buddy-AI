package com.hackathonbuddy.dto.request;

import lombok.*;

@Data
@Builder
public class FindTeammatesRequest {
    private Long hackathonId;
    private Long teamId;

    public FindTeammatesRequest() {
    }

    public FindTeammatesRequest(Long hackathonId, Long teamId) {
        this.hackathonId = hackathonId;
        this.teamId = teamId;
    }
}
