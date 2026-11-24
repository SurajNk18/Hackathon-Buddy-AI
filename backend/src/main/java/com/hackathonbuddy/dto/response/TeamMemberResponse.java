package com.hackathonbuddy.dto.response;

import lombok.*;
import java.util.List;

@Data
@Builder
public class TeamMemberResponse {
    private Long id;
    private Long userId;
    private String name;
    private String email;
    private String role;
    private String status;
    private Boolean isCurrentUser;
    private List<String> skills;

    public TeamMemberResponse() {
    }

    public TeamMemberResponse(Long id, Long userId, String name, String email,
                              String role, String status, Boolean isCurrentUser) {
        this(id, userId, name, email, role, status, isCurrentUser, null);
    }

    public TeamMemberResponse(Long id, Long userId, String name, String email,
                              String role, String status, Boolean isCurrentUser, List<String> skills) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
        this.status = status;
        this.isCurrentUser = isCurrentUser;
        this.skills = skills;
    }
}
