package com.hackathonbuddy.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HackathonRegistrationRequest {
    private String hackathonTitle;
    private String hackathonCategory;
    private String participationType; // INDIVIDUAL, TEAM
    private String teamName;
    private String role;
    private String experienceLevel;
    private String githubUrl;
    private String motivation;
    private Boolean agreeTerms;

    public HackathonRegistrationRequest() {
    }

    public HackathonRegistrationRequest(String hackathonTitle, String hackathonCategory,
                                        String participationType, String teamName, String role,
                                        String experienceLevel, String githubUrl, String motivation,
                                        Boolean agreeTerms) {
        this.hackathonTitle = hackathonTitle;
        this.hackathonCategory = hackathonCategory;
        this.participationType = participationType;
        this.teamName = teamName;
        this.role = role;
        this.experienceLevel = experienceLevel;
        this.githubUrl = githubUrl;
        this.motivation = motivation;
        this.agreeTerms = agreeTerms;
    }
}
