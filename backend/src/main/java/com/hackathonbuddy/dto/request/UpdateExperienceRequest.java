package com.hackathonbuddy.dto.request;

import lombok.*;

@Data
@Builder
public class UpdateExperienceRequest {
    private Integer yearsOfExperience;
    private Integer projectCount;
    private Boolean hasInternship;
    private String certifications;

    public UpdateExperienceRequest() {
    }

    public UpdateExperienceRequest(Integer yearsOfExperience, Integer projectCount,
                                   Boolean hasInternship, String certifications) {
        this.yearsOfExperience = yearsOfExperience;
        this.projectCount = projectCount;
        this.hasInternship = hasInternship;
        this.certifications = certifications;
    }
}
