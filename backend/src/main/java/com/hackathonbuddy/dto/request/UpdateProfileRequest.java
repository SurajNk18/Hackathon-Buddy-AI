package com.hackathonbuddy.dto.request;

import lombok.*;
import java.util.List;

@Data
@Builder
public class UpdateProfileRequest {
    private String bio;
    private String location;
    private String profilePhotoUrl;
    private String preferredRole;
    private List<String> preferredDomains;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String bio, String location, String profilePhotoUrl,
                                String preferredRole, List<String> preferredDomains) {
        this.bio = bio;
        this.location = location;
        this.profilePhotoUrl = profilePhotoUrl;
        this.preferredRole = preferredRole;
        this.preferredDomains = preferredDomains;
    }
}
