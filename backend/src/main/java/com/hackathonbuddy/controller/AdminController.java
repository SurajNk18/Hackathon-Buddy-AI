package com.hackathonbuddy.controller;

import com.hackathonbuddy.dto.response.ApiResponse;
import com.hackathonbuddy.dto.response.UserResponse;
import com.hackathonbuddy.entity.User;
import com.hackathonbuddy.repository.HackathonRepository;
import com.hackathonbuddy.repository.UserRepository;
import com.hackathonbuddy.repository.RegistrationRepository;
import com.hackathonbuddy.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.persistence.EntityManager;
import org.springframework.transaction.annotation.Transactional;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final HackathonRepository hackathonRepository;
    private final RegistrationRepository registrationRepository;
    private final TeamRepository teamRepository;
    private final EntityManager entityManager;

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<UserResponse>>builder()
                .success(true).message("Users retrieved").data(users).build());
    }

    @PutMapping("/users/{id}/toggle-status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        boolean currentActive = Boolean.TRUE.equals(user.getIsActive());
        user.setIsActive(!currentActive);
        user = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true).message("User status toggled").data(toUserResponse(user)).build());
    }

    @DeleteMapping("/users/{id}")
    @Transactional
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        try { entityManager.createNativeQuery("DELETE FROM activities WHERE user_id = :id").setParameter("id", id).executeUpdate(); } catch (Exception ignored) {}
        try { entityManager.createNativeQuery("DELETE FROM notifications WHERE user_id = :id OR target_user_id = :id").setParameter("id", id).executeUpdate(); } catch (Exception ignored) {}
        try { entityManager.createNativeQuery("DELETE FROM user_skills WHERE user_id = :id").setParameter("id", id).executeUpdate(); } catch (Exception ignored) {}
        try { entityManager.createNativeQuery("DELETE FROM user_interests WHERE user_id = :id").setParameter("id", id).executeUpdate(); } catch (Exception ignored) {}
        try { entityManager.createNativeQuery("DELETE FROM user_experiences WHERE user_id = :id").setParameter("id", id).executeUpdate(); } catch (Exception ignored) {}
        try { entityManager.createNativeQuery("DELETE FROM user_preferences WHERE user_id = :id").setParameter("id", id).executeUpdate(); } catch (Exception ignored) {}
        try { entityManager.createNativeQuery("DELETE FROM project_ideas WHERE user_id = :id").setParameter("id", id).executeUpdate(); } catch (Exception ignored) {}
        try { entityManager.createNativeQuery("DELETE FROM registrations WHERE user_id = :id").setParameter("id", id).executeUpdate(); } catch (Exception ignored) {}
        try { entityManager.createNativeQuery("DELETE FROM team_members WHERE user_id = :id").setParameter("id", id).executeUpdate(); } catch (Exception ignored) {}
        try { entityManager.createNativeQuery("UPDATE teams SET leader_id = NULL WHERE leader_id = :id").setParameter("id", id).executeUpdate(); } catch (Exception ignored) {}
        try {
            @SuppressWarnings("unchecked")
            List<?> projectIds = entityManager.createNativeQuery("SELECT id FROM projects WHERE user_id = :id").setParameter("id", id).getResultList();
            if (projectIds != null && !projectIds.isEmpty()) {
                entityManager.createNativeQuery("DELETE FROM project_tasks WHERE project_id IN (:ids)").setParameter("ids", projectIds).executeUpdate();
            }
        } catch (Exception ignored) {}
        try { entityManager.createNativeQuery("DELETE FROM projects WHERE user_id = :id").setParameter("id", id).executeUpdate(); } catch (Exception ignored) {}
        try { entityManager.createNativeQuery("UPDATE chat_messages SET sender_id = NULL WHERE sender_id = :id").setParameter("id", id).executeUpdate(); } catch (Exception ignored) {}

        userRepository.delete(user);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("User deleted").build());
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = Map.of(
                "totalUsers", userRepository.count(),
                "totalHackathons", hackathonRepository.count(),
                "totalRegistrations", registrationRepository.count(),
                "totalTeams", teamRepository.count()
        );
        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true).message("Admin stats retrieved").data(stats).build());
    }

    private UserResponse toUserResponse(User user) {
        boolean isAdmin = user.getRole() != null && "ADMIN".equalsIgnoreCase(user.getRole().getName());
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole() != null ? user.getRole().getName() : "STUDENT")
                .primaryRole(user.getPrimaryRole())
                .location(user.getLocation())
                .bio(user.getBio())
                .isActive(user.getIsActive())
                .isAdmin(isAdmin)
                .profileComplete(user.getProfileComplete())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
