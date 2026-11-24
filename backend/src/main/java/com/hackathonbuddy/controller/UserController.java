package com.hackathonbuddy.controller;

import com.hackathonbuddy.dto.response.ApiResponse;
import com.hackathonbuddy.dto.response.TeammateMatchResponse;
import com.hackathonbuddy.dto.response.UserResponse;
import com.hackathonbuddy.entity.*;
import com.hackathonbuddy.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final UserExperienceRepository userExperienceRepository;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        UserResponse response = UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true).message("Profile retrieved").data(response).build());
    }

    @GetMapping("/candidates")
    public ResponseEntity<ApiResponse<List<TeammateMatchResponse>>> getCandidates(
            @RequestParam(required = false) String skill,
            @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userDetails != null ? userRepository.findByEmail(userDetails.getUsername()).orElse(null) : null;
        List<User> users = userRepository.findAll();

        String targetSkill = (skill != null && !skill.isBlank()) ? skill.trim() : null;

        Map<Long, List<String>> userSkillsMap = new HashMap<>();
        try {
            userSkillRepository.findAllWithSkills().forEach(us -> {
                if (us.getUser() != null && us.getUser().getId() != null && us.getSkill() != null && us.getSkill().getName() != null) {
                    userSkillsMap.computeIfAbsent(us.getUser().getId(), k -> new ArrayList<>()).add(us.getSkill().getName());
                }
            });
        } catch (Exception ignored) {}

        Map<Long, UserPreference> userPrefMap = new HashMap<>();
        try {
            userPreferenceRepository.findAll().forEach(up -> {
                if (up.getUser() != null && up.getUser().getId() != null) {
                    userPrefMap.put(up.getUser().getId(), up);
                }
            });
        } catch (Exception ignored) {}

        Map<Long, UserExperience> userExpMap = new HashMap<>();
        try {
            userExperienceRepository.findAll().forEach(ue -> {
                if (ue.getUser() != null && ue.getUser().getId() != null) {
                    userExpMap.put(ue.getUser().getId(), ue);
                }
            });
        } catch (Exception ignored) {}

        // Only show users with STUDENT role in teammate matching (exclude admins, organizers, developer admins, companies)
        List<TeammateMatchResponse> candidates = users.stream()
                .filter(u -> currentUser == null || !u.getId().equals(currentUser.getId()))
                .filter(u -> {
                    if (u.getRole() == null || u.getRole().getName() == null) {
                        return false;
                    }
                    String r = u.getRole().getName().trim().toUpperCase().replace("ROLE_", "");
                    return "STUDENT".equals(r);
                })
                .filter(u -> Boolean.TRUE.equals(u.getIsActive()))
                .filter(u -> {
                    if (targetSkill == null) return true;
                    List<String> uSkills = userSkillsMap.getOrDefault(u.getId(), Collections.emptyList());
                    return uSkills.stream().anyMatch(s -> AiController.skillsMatch(s, targetSkill));
                })
                .map(u -> {
                    List<String> skills = userSkillsMap.getOrDefault(u.getId(), Collections.emptyList());
                    UserPreference pref = userPrefMap.get(u.getId());
                    UserExperience exp = userExpMap.get(u.getId());

                    String role = (pref != null && pref.getPreferredRole() != null && !pref.getPreferredRole().isBlank())
                            ? pref.getPreferredRole() : u.getPrimaryRole();
                    if (role == null || role.isBlank()) role = "Full Stack Developer";

                    String bio = (pref != null && pref.getBio() != null && !pref.getBio().isBlank())
                            ? pref.getBio() : u.getBio();
                    if (bio == null || bio.isBlank()) bio = "Passionate student developer.";

                    String name = (u.getLastName() != null && !u.getLastName().isBlank())
                            ? (u.getFirstName() != null ? u.getFirstName() + " " + u.getLastName() : u.getLastName())
                            : (u.getFirstName() != null ? u.getFirstName() : u.getEmail());

                    return TeammateMatchResponse.builder()
                            .userId(u.getId())
                            .name(name)
                            .email(u.getEmail())
                            .preferredRole(role)
                            .bio(bio)
                            .yearsOfExperience(exp != null && exp.getYearsOfExperience() != null ? exp.getYearsOfExperience() : 2)
                            .projectCount(exp != null && exp.getProjectCount() != null ? exp.getProjectCount() : 3)
                            .skills(skills)
                            .compatibilityScore(92.0)
                            .matchReason("Registered platform candidate")
                            .build();
                })
                .sorted(Comparator.comparing(TeammateMatchResponse::getUserId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.<List<TeammateMatchResponse>>builder()
                .success(true)
                .message("Candidates retrieved")
                .data(candidates)
                .build());
    }
}
