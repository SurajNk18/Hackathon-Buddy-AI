package com.hackathonbuddy.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterRequest {

    private String fullName;

    private String firstName;
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6)
    private String password;

    private String phone;
    private String primaryRole;
    private String techSkills;
    private String projectDomains;
    private String githubUrl;
    private String location;

    public RegisterRequest() {
    }

    public RegisterRequest(String fullName, String firstName, String lastName, String email,
                           String password, String phone, String primaryRole, String techSkills,
                           String projectDomains, String githubUrl, String location) {
        this.fullName = fullName;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.primaryRole = primaryRole;
        this.techSkills = techSkills;
        this.projectDomains = projectDomains;
        this.githubUrl = githubUrl;
        this.location = location;
    }
}
