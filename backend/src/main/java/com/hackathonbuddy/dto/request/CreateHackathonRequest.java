package com.hackathonbuddy.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateHackathonRequest {
    private String title;
    private String description;
    private String category;
    private String date;
    private String deadline;
    private String prize;
    private Integer participants;
    private String location;
    private String duration;
    private String status;
    private String level;

    public CreateHackathonRequest() {
    }

    public CreateHackathonRequest(String title, String description, String category, String date,
                                  String deadline, String prize, Integer participants, String location,
                                  String duration, String status, String level) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.date = date;
        this.deadline = deadline;
        this.prize = prize;
        this.participants = participants;
        this.location = location;
        this.duration = duration;
        this.status = status;
        this.level = level;
    }
}
