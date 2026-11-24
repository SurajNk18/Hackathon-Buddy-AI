package com.hackathonbuddy.dto.response;

import lombok.*;
import java.util.List;

@Data
@Builder
public class ProfileResponse {
    private Long userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String role;
    private Boolean profileComplete;

    // Preferences
    private String bio;
    private String location;
    private String profilePhotoUrl;
    private String preferredRole;
    private List<String> preferredDomains;

    // Skills
    private List<SkillEntry> skills;

    // Interests
    private List<String> interests;

    // Experience
    private ExperienceEntry experience;

    public ProfileResponse() {
    }

    public ProfileResponse(Long userId, String firstName, String lastName, String email,
                           String phone, String role, Boolean profileComplete,
                           String bio, String location, String profilePhotoUrl,
                           String preferredRole, List<String> preferredDomains,
                           List<SkillEntry> skills, List<String> interests,
                           ExperienceEntry experience) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.profileComplete = profileComplete;
        this.bio = bio;
        this.location = location;
        this.profilePhotoUrl = profilePhotoUrl;
        this.preferredRole = preferredRole;
        this.preferredDomains = preferredDomains;
        this.skills = skills;
        this.interests = interests;
        this.experience = experience;
    }

    @Data
    @Builder
    public static class SkillEntry {
        private Long skillId;
        private String name;
        private String category;
        private Integer proficiencyLevel; // 1=Beginner, 2=Intermediate, 3=Advanced
        private String proficiencyLabel;

        public SkillEntry() {
        }

        public SkillEntry(Long skillId, String name, String category,
                          Integer proficiencyLevel, String proficiencyLabel) {
            this.skillId = skillId;
            this.name = name;
            this.category = category;
            this.proficiencyLevel = proficiencyLevel;
            this.proficiencyLabel = proficiencyLabel;
        }
    }

    @Data
    @Builder
    public static class ExperienceEntry {
        private Integer yearsOfExperience;
        private Integer projectCount;
        private Boolean hasInternship;
        private String certifications;

        public ExperienceEntry() {
        }

        public ExperienceEntry(Integer yearsOfExperience, Integer projectCount,
                               Boolean hasInternship, String certifications) {
            this.yearsOfExperience = yearsOfExperience;
            this.projectCount = projectCount;
            this.hasInternship = hasInternship;
            this.certifications = certifications;
        }
    }
}
