package com.hackathonbuddy.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String title;
    private String message;
    private String type;
    private Boolean isRead;
    private Boolean unread;
    private String icon;
    private String action;
    private String route;
    private String actionUrl;
    private Long targetTeamId;
    private Long targetUserId;
    private String responseStatus;
    private String time;
    private String date;
    private LocalDateTime createdAt;

    public NotificationResponse() {
    }

    public NotificationResponse(Long id, String title, String message, String type,
                                Boolean isRead, Boolean unread, String icon, String action,
                                String route, String actionUrl, Long targetTeamId,
                                Long targetUserId, String responseStatus, String time,
                                String date, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.unread = unread;
        this.icon = icon;
        this.action = action;
        this.route = route;
        this.actionUrl = actionUrl;
        this.targetTeamId = targetTeamId;
        this.targetUserId = targetUserId;
        this.responseStatus = responseStatus;
        this.time = time;
        this.date = date;
        this.createdAt = createdAt;
    }
}
