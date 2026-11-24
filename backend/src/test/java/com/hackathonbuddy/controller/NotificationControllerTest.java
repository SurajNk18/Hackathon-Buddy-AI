package com.hackathonbuddy.controller;

import com.hackathonbuddy.dto.response.ApiResponse;
import com.hackathonbuddy.dto.response.NotificationResponse;
import com.hackathonbuddy.entity.User;
import com.hackathonbuddy.repository.UserRepository;
import com.hackathonbuddy.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    private NotificationService notificationService;
    private UserRepository userRepository;
    private NotificationController notificationController;

    private User testUser;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        userRepository = mock(UserRepository.class);
        notificationController = new NotificationController(notificationService, userRepository);

        testUser = User.builder()
                .id(1L)
                .email("sanika@test.com")
                .firstName("Sanika")
                .lastName("Patil")
                .build();

        userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("sanika@test.com");
        when(userRepository.findByEmail("sanika@test.com")).thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("GET /api/notifications - Should return all user notifications")
    void testGetNotifications_Success() {
        NotificationResponse n1 = NotificationResponse.builder()
                .id(1L).title("Team Invitation").message("You were invited to Team Alpha")
                .type("team").isRead(false).unread(true)
                .createdAt(LocalDateTime.now())
                .build();
        NotificationResponse n2 = NotificationResponse.builder()
                .id(2L).title("Hackathon Starting").message("Global AI Hack starts tomorrow")
                .type("hackathon").isRead(true).unread(false)
                .createdAt(LocalDateTime.now().minusHours(2))
                .build();

        when(notificationService.getNotifications(testUser)).thenReturn(List.of(n1, n2));

        ResponseEntity<ApiResponse<List<NotificationResponse>>> response =
                notificationController.getNotifications(userDetails);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals(2, response.getBody().getData().size());
        assertEquals("Team Invitation", response.getBody().getData().get(0).getTitle());
    }

    @Test
    @DisplayName("POST /api/notifications - Should create a notification")
    void testCreateNotification_Success() {
        Map<String, String> payload = Map.of(
                "title", "Project Update",
                "message", "New task assigned to you",
                "type", "project"
        );

        ResponseEntity<ApiResponse<Void>> response =
                notificationController.createNotification(payload, userDetails);

        assertTrue(response.getBody().isSuccess());
        assertEquals("Notification created successfully", response.getBody().getMessage());
        verify(notificationService).createNotification(
                eq(testUser), eq("Project Update"), eq("New task assigned to you"),
                eq("project"), isNull(), isNull(), isNull()
        );
    }

    @Test
    @DisplayName("GET /api/notifications/unread-count - Should return unread count")
    void testGetUnreadCount() {
        when(notificationService.getUnreadCount(testUser)).thenReturn(5L);

        ResponseEntity<ApiResponse<Long>> response =
                notificationController.getUnreadCount(userDetails);

        assertTrue(response.getBody().isSuccess());
        assertEquals(5L, response.getBody().getData());
    }

    @Test
    @DisplayName("PUT /api/notifications/mark-all-read - Should mark all as read")
    void testMarkAllRead() {
        ResponseEntity<ApiResponse<Void>> response =
                notificationController.markAllRead(userDetails);

        assertTrue(response.getBody().isSuccess());
        verify(notificationService).markAllAsRead(testUser);
    }

    @Test
    @DisplayName("PUT /api/notifications/{id}/mark-read - Should mark single notification as read")
    void testMarkSingleRead() {
        ResponseEntity<ApiResponse<Void>> response =
                notificationController.markRead(42L);

        assertTrue(response.getBody().isSuccess());
        verify(notificationService).markAsRead(42L);
    }

    @Test
    @DisplayName("DELETE /api/notifications/{id} - Should delete notification")
    void testDeleteNotification() {
        ResponseEntity<ApiResponse<Void>> response =
                notificationController.deleteNotification(42L);

        assertTrue(response.getBody().isSuccess());
        verify(notificationService).deleteNotification(42L);
    }

    @Test
    @DisplayName("POST /api/notifications/invite - Should send team invitation")
    void testSendInvitation() {
        Map<String, Object> payload = Map.of(
                "receiverId", 5L,
                "teamId", 10L,
                "role", "Backend Developer"
        );

        ResponseEntity<ApiResponse<Void>> response =
                notificationController.sendInvitation(payload, userDetails);

        assertTrue(response.getBody().isSuccess());
        assertEquals("Team invitation sent successfully", response.getBody().getMessage());
        verify(notificationService).sendTeamInvitation(testUser, 5L, 10L, "Backend Developer");
    }

    @Test
    @DisplayName("POST /api/notifications/join-request - Should send join request")
    void testSendJoinRequest() {
        Map<String, Object> payload = Map.of("teamId", 10L);

        ResponseEntity<ApiResponse<Void>> response =
                notificationController.sendJoinRequest(payload, userDetails);

        assertTrue(response.getBody().isSuccess());
        verify(notificationService).sendJoinRequest(testUser, 10L);
    }

    @Test
    @DisplayName("POST /api/notifications/{id}/respond - Should respond to notification")
    void testRespondToNotification() {
        Map<String, String> payload = Map.of("action", "ACCEPT");

        ResponseEntity<ApiResponse<Void>> response =
                notificationController.respondToNotification(1L, payload, userDetails);

        assertTrue(response.getBody().isSuccess());
        verify(notificationService).respondToNotification(1L, "ACCEPT", testUser);
    }

    @Test
    @DisplayName("DELETE /api/notifications - Should clear all notifications")
    void testClearAll() {
        ResponseEntity<ApiResponse<Void>> response =
                notificationController.clearAll(userDetails);

        assertTrue(response.getBody().isSuccess());
        verify(notificationService).clearAllNotifications(testUser);
    }
}
