package com.hackathonbuddy.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String primaryRole;
    private String location;
    private String bio;
    private String githubUrl;
    private String linkedinUrl;
    private String techSkills;
    private String projectDomains;
    private List<String> skills;
    private List<String> domains;
    private Boolean isActive;
    private Boolean isAdmin;
    private Boolean profileComplete;
    private LocalDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(Long id, String firstName, String lastName, String fullName,
                        String email, String phone, String role, String primaryRole,
                        String location, String bio, String githubUrl, String linkedinUrl,
                        String techSkills, String projectDomains, List<String> skills,
                        List<String> domains, Boolean isActive, Boolean isAdmin,
                        Boolean profileComplete, LocalDateTime createdAt) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.primaryRole = primaryRole;
        this.location = location;
        this.bio = bio;
        this.githubUrl = githubUrl;
        this.linkedinUrl = linkedinUrl;
        this.techSkills = techSkills;
        this.projectDomains = projectDomains;
        this.skills = skills;
        this.domains = domains;
        this.isActive = isActive;
        this.isAdmin = isAdmin;
        this.profileComplete = profileComplete;
        this.createdAt = createdAt;
    }
}
