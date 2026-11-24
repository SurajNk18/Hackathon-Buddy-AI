package com.hackathonbuddy.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HackathonResponse {
    private Object id;
    private String title;
    private String description;
    private String category;
    private String organizer;
    private String platform;
    private String startDate;
    private String endDate;
    private String registrationDeadline;
    private String prizePool;
    private String location;
    private String mode;
    private Integer maxTeamSize;
    private Integer minTeamSize;
    private String imageUrl;
    private String websiteUrl;
    private String url;
    private Boolean isActive;
    private Integer matchScore;
    private Boolean isRegistered;

    // Frontend-compatible fields
    private String date;
    private String deadline;
    private String prize;
    private Integer participants;
    private String duration;
    private Integer match;
    private String status;
    private String level;
    private String color;
    private String icon;
    private String type;
}
