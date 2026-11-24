package com.hackathonbuddy.dto.response;

import lombok.*;
import java.util.List;

@Data
@Builder
public class TeammateMatchResponse {
    private Long userId;
    private String name;
    private String email;
    private String profilePhotoUrl;
    private String preferredRole;
    private String bio;
    private Integer yearsOfExperience;
    private Integer projectCount;
    private List<String> skills;
    private List<String> interests;
    private Double compatibilityScore; // 0-100
    private List<String> commonSkills;
    private List<String> complementarySkills;
    private String matchReason;

    public TeammateMatchResponse() {
    }

    public TeammateMatchResponse(Long userId, String name, String email, String profilePhotoUrl,
                                 String preferredRole, String bio, Integer yearsOfExperience,
                                 Integer projectCount, List<String> skills, List<String> interests,
                                 Double compatibilityScore, List<String> commonSkills,
                                 List<String> complementarySkills, String matchReason) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.profilePhotoUrl = profilePhotoUrl;
        this.preferredRole = preferredRole;
        this.bio = bio;
        this.yearsOfExperience = yearsOfExperience;
        this.projectCount = projectCount;
        this.skills = skills;
        this.interests = interests;
        this.compatibilityScore = compatibilityScore;
        this.commonSkills = commonSkills;
        this.complementarySkills = complementarySkills;
        this.matchReason = matchReason;
    }
}
