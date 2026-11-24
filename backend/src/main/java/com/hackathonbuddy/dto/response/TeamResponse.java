package com.hackathonbuddy.dto.response;

import lombok.*;
import java.util.List;

@Data
@Builder
public class TeamResponse {
    private Long id;
    private String name;
    private String description;
    private TeamMemberResponse leader;
    private List<TeamMemberResponse> members;
    private Integer skillCoveragePercent;
    private List<String> missingSkills;
    private Boolean isActive;

    public TeamResponse() {
    }

    public TeamResponse(Long id, String name, String description, TeamMemberResponse leader,
                        List<TeamMemberResponse> members, Integer skillCoveragePercent,
                        List<String> missingSkills, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.leader = leader;
        this.members = members;
        this.skillCoveragePercent = skillCoveragePercent;
        this.missingSkills = missingSkills;
        this.isActive = isActive;
    }
}
