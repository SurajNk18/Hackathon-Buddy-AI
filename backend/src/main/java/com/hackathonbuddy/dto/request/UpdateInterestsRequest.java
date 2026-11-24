package com.hackathonbuddy.dto.request;

import lombok.*;
import java.util.List;

@Data
@Builder
public class UpdateInterestsRequest {
    private List<String> interests;

    public UpdateInterestsRequest() {
    }

    public UpdateInterestsRequest(List<String> interests) {
        this.interests = interests;
    }
}
