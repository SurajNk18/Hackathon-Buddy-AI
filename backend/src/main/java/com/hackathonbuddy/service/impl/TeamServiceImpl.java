package com.hackathonbuddy.service.impl;

import com.hackathonbuddy.dto.request.CreateTeamRequest;
import com.hackathonbuddy.dto.response.TeamMemberResponse;
import com.hackathonbuddy.dto.response.TeamResponse;
import com.hackathonbuddy.entity.*;
import com.hackathonbuddy.repository.*;
import com.hackathonbuddy.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamServiceImpl implements TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final HackathonRepository hackathonRepository;
    private final RoleRepository roleRepository;
    private final UserSkillRepository userSkillRepository;
    private final PasswordEncoder passwordEncoder;
    private final com.hackathonbuddy.service.NotificationService notificationService;

    @Override
    public TeamResponse getMyTeam(User user) {
        if (user == null) return null;
        return teamRepository.findFirstTeamForUser(user)
                .map(team -> toResponse(team, user))
                .orElseGet(() -> {
                    CreateTeamRequest defaultReq = new CreateTeamRequest();
                    defaultReq.setName("CodeCrafters");
                    defaultReq.setDescription("Active Hackathon Squad");
                    return createTeam(defaultReq, user);
                });
    }

    private String getUserDisplayName(User user) {
        if (user == null) return "Squad Member";
        String first = user.getFirstName() != null ? user.getFirstName().trim() : "";
        String last = user.getLastName() != null ? user.getLastName().trim() : "";
        String full = (first + " " + last).trim();
        if (!full.isEmpty() && !"null".equalsIgnoreCase(full) && !"null null".equalsIgnoreCase(full)) {
            return full;
        }
        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail();
        }
        return "Squad Member";
    }

    @Override
    public List<TeamResponse> getAllMyTeams(User user) {
        if (user == null) return List.of();
        List<Team> teams = teamRepository.findTeamsByMember(user);
        if (teams.isEmpty()) {
            TeamResponse myTeam = getMyTeam(user);
            if (myTeam != null) {
                return List.of(myTeam);
            }
        }
        return teams.stream()
                .map(team -> toResponse(team, user))
                .collect(Collectors.toList());
    }

    @Override
    public TeamResponse createTeam(CreateTeamRequest request, User user) {
        if (user == null) {
            throw new RuntimeException("User must be authenticated");
        }

        // Prevent duplicate team creation if the user already belongs to a team
        Optional<Team> existing = teamRepository.findFirstTeamForUser(user);
        if (existing.isPresent()) {
            Team team = existing.get();
            boolean updated = false;
            if (request.getName() != null && !request.getName().isBlank()) {
                team.setName(request.getName().trim());
                updated = true;
            }
            if (request.getDescription() != null && !request.getDescription().isBlank()) {
                team.setDescription(request.getDescription().trim());
                updated = true;
            }
            if (request.getHackathonId() != null) {
                hackathonRepository.findById(request.getHackathonId()).ifPresent(team::setHackathon);
                updated = true;
            }
            if (updated) {
                team = teamRepository.save(team);
            }
            return toResponse(team, user);
        }

        String teamName = (request.getName() != null && !request.getName().isBlank())
                ? request.getName().trim()
                : "CodeCrafters";
        String description = (request.getDescription() != null && !request.getDescription().isBlank())
                ? request.getDescription().trim()
                : "Active Hackathon Squad";

        Team team = Team.builder()
                .name(teamName)
                .description(description)
                .leader(user)
                .skillCoveragePercent(85)
                .isActive(true)
                .build();

        if (request.getHackathonId() != null) {
            hackathonRepository.findById(request.getHackathonId())
                    .ifPresent(team::setHackathon);
        }

        team = teamRepository.save(team);

        // Auto-add leader as first member
        TeamMember leaderMember = TeamMember.builder()
                .team(team)
                .user(user)
                .role(user.getPrimaryRole() != null ? user.getPrimaryRole() : "Team Leader")
                .status("ACCEPTED")
                .build();
        teamMemberRepository.save(leaderMember);

        // Notify admins of new team creation
        try {
            notificationService.notifyAdmins("New Squad Created",
                    "Squad '" + team.getName() + "' was created by " + getUserDisplayName(user), "team_created");
        } catch (Exception ignored) {}

        return toResponse(team, user);
    }

    @Override
    public TeamResponse updateTeam(Long teamId, CreateTeamRequest request, User user) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        
        if (request.getName() != null && !request.getName().isBlank()) {
            team.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            team.setDescription(request.getDescription().trim());
        }
        
        Team saved = teamRepository.save(team);
        return toResponse(saved, user);
    }

    @Override
    public TeamResponse addMember(Long teamId, Long userId, String role, User currentUser) {
        return addMember(teamId, userId, null, null, role, currentUser);
    }

    @Override
    public TeamResponse addMember(Long teamId, Long userId, String name, String email, String role, User currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        User newMember = resolveUser(userId, name, email, role);
        if (newMember == null) {
            throw new RuntimeException("Candidate user not found in registered users");
        }

        if (newMember.getId().equals(currentUser.getId())) {
            throw new RuntimeException("You cannot invite yourself to your own squad");
        }

        String memberRole = (role != null && !role.isBlank()) ? role : (newMember.getPrimaryRole() != null ? newMember.getPrimaryRole() : "Member");

        // Send team invitation notification to candidate (creates/updates TeamMember with status PENDING and posts squad chat announcement)
        notificationService.sendTeamInvitation(currentUser, newMember.getId(), team.getId(), memberRole);

        return toResponse(team, currentUser);
    }

    private User resolveUser(Long userId, String name, String email, String role) {
        if (userId != null) {
            Optional<User> byId = userRepository.findById(userId);
            if (byId.isPresent()) return byId.get();
        }
        if (email != null && !email.isBlank()) {
            Optional<User> byEmail = userRepository.findByEmail(email.trim().toLowerCase());
            if (byEmail.isPresent()) return byEmail.get();
        }
        if (name != null && !name.isBlank()) {
            String cleanName = name.trim();
            List<User> all = userRepository.findAll();
            for (User u : all) {
                if (cleanName.equalsIgnoreCase(getUserDisplayName(u))) {
                    return u;
                }
            }
            for (User u : all) {
                String full = (u.getFirstName() != null ? u.getFirstName() : "") + " " + (u.getLastName() != null ? u.getLastName() : "");
                if (cleanName.equalsIgnoreCase(full.trim())) {
                    return u;
                }
            }
            for (User u : all) {
                if (u.getEmail() != null && u.getEmail().toLowerCase().startsWith(cleanName.toLowerCase())) {
                    return u;
                }
            }
            for (User u : all) {
                if (u.getFirstName() != null && !u.getFirstName().isBlank() &&
                        cleanName.toLowerCase().contains(u.getFirstName().toLowerCase().trim())) {
                    return u;
                }
            }
        }
        return null;
    }

    @Override
    public TeamResponse removeMember(Long teamId, Long userId, User currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        Optional<TeamMember> tmById = teamMemberRepository.findById(userId);
        if (tmById.isPresent() && tmById.get().getTeam().getId().equals(team.getId())) {
            User memberUser = tmById.get().getUser();
            teamMemberRepository.delete(tmById.get());
            try {
                notificationService.createNotification(memberUser, "Removed from Squad",
                        "You were removed from squad " + team.getName(), "team", "ℹ️", "View Teams", "/teams");
            } catch (Exception ignored) {}
        } else {
            userRepository.findById(userId).ifPresent(memberUser -> {
                List<TeamMember> tms = teamMemberRepository.findAllByTeamAndUser(team, memberUser);
                if (!tms.isEmpty()) {
                    teamMemberRepository.deleteAll(tms);
                    try {
                        notificationService.createNotification(memberUser, "Removed from Squad",
                                "You were removed from squad " + team.getName(), "team", "ℹ️", "View Teams", "/teams");
                    } catch (Exception ignored) {}
                }
            });
        }

        return toResponse(team, currentUser);
    }

    @Override
    public TeamResponse updateMemberRole(Long teamId, Long userId, String newRole, User currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        User memberUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        teamMemberRepository.findByTeamAndUser(team, memberUser).ifPresent(tm -> {
            tm.setRole(newRole);
            teamMemberRepository.save(tm);
            // Notify affected member
            try {
                notificationService.createNotification(memberUser, "Role Updated",
                        "Your role in squad " + team.getName() + " was changed to " + newRole, "team", "👤", "View Team", "/teams");
            } catch (Exception ignored) {}
        });

        return toResponse(team, currentUser);
    }

    @Override
    public TeamResponse getSkillGap(Long teamId, User currentUser) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        return toResponse(team, currentUser);
    }

    private List<String> extractUserSkills(User u) {
        if (u == null) return List.of("React", "JavaScript");
        List<String> skills = userSkillRepository.findByUser(u).stream()
                .map(us -> us.getSkill() != null ? us.getSkill().getName() : "")
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
        if (!skills.isEmpty()) return skills;

        String role = (u.getPrimaryRole() != null ? u.getPrimaryRole() : "").toLowerCase();
        if (role.contains("devops")) {
            return List.of("AWS", "Docker", "Kubernetes", "Linux", "CI/CD");
        } else if (role.contains("designer") || role.contains("ui")) {
            return List.of("Figma", "UI/UX", "Prototyping", "Design Systems");
        } else if (role.contains("ml") || role.contains("ai") || role.contains("data")) {
            return List.of("Python", "TensorFlow", "Pandas", "NLP", "PyTorch");
        } else if (role.contains("backend")) {
            return List.of("Node.js", "Express", "PostgreSQL", "Spring Boot", "MySQL");
        } else {
            return List.of("React", "JavaScript", "TypeScript", "HTML/CSS");
        }
    }

    private TeamResponse toResponse(Team team, User currentUser) {
        List<TeamMember> members = teamMemberRepository.findByTeam(team);
        boolean isLeader = currentUser != null && team.getLeader() != null && currentUser.getId().equals(team.getLeader().getId());

        List<TeamMemberResponse> memberResponses = members.stream()
                .filter(m -> {
                    String status = m.getStatus() != null ? m.getStatus().toUpperCase() : "ACCEPTED";
                    if ("REJECTED".equals(status) || "DECLINED".equals(status)) {
                        return false;
                    }
                    if ("ACCEPTED".equals(status)) {
                        return true;
                    }
                    if ("PENDING".equals(status)) {
                        // Pending members are visible to the team leader and to that pending user themselves
                        if (isLeader) return true;
                        if (currentUser != null && m.getUser() != null && m.getUser().getId().equals(currentUser.getId())) return true;
                        // Other teammates who have accepted only see members who have accepted!
                        return false;
                    }
                    return true;
                })
                .map(m -> {
                    String fullName = getUserDisplayName(m.getUser());
                    List<String> memberSkills = extractUserSkills(m.getUser());
                    return TeamMemberResponse.builder()
                            .id(m.getId())
                            .userId(m.getUser().getId())
                            .name(fullName)
                            .email(m.getUser().getEmail())
                            .role(m.getRole())
                            .status(m.getStatus() != null ? m.getStatus() : "ACCEPTED")
                            .isCurrentUser(currentUser != null && m.getUser().getId().equals(currentUser.getId()))
                            .skills(memberSkills)
                            .build();
                })
                .collect(Collectors.toList());

        if (team.getLeader() != null) {
            boolean leaderInMembers = memberResponses.stream()
                    .anyMatch(m -> m.getUserId().equals(team.getLeader().getId()));
            if (!leaderInMembers) {
                User leaderUser = team.getLeader();
                String fullName = getUserDisplayName(leaderUser);
                List<String> leaderSkills = extractUserSkills(leaderUser);
                TeamMemberResponse leaderResp = TeamMemberResponse.builder()
                        .id(null)
                        .userId(leaderUser.getId())
                        .name(fullName)
                        .email(leaderUser.getEmail())
                        .role(leaderUser.getPrimaryRole() != null ? leaderUser.getPrimaryRole() : "Team Leader")
                        .status("ACCEPTED")
                        .isCurrentUser(currentUser != null && leaderUser.getId().equals(currentUser.getId()))
                        .skills(leaderSkills)
                        .build();
                memberResponses.add(0, leaderResp);
            }
        }

        TeamMemberResponse leader = memberResponses.stream()
                .filter(m -> team.getLeader() != null && m.getUserId().equals(team.getLeader().getId()))
                .findFirst()
                .orElse(null);

        // Calculate missing skills based on ACCEPTED squad members
        List<String> requiredSkills = Arrays.asList("UI/UX", "Machine Learning", "AWS", "Docker", "React", "Node.js");
        List<String> coveredSkills = memberResponses.stream()
                .filter(m -> "ACCEPTED".equalsIgnoreCase(m.getStatus()) && m.getSkills() != null)
                .flatMap(m -> m.getSkills().stream())
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        List<String> missingSkills = requiredSkills.stream()
                .filter(req -> !coveredSkills.contains(req.toLowerCase()))
                .collect(Collectors.toList());

        return TeamResponse.builder()
                .id(team.getId())
                .name(team.getName())
                .description(team.getDescription())
                .leader(leader)
                .members(memberResponses)
                .skillCoveragePercent(team.getSkillCoveragePercent() != null ? team.getSkillCoveragePercent() : 82)
                .missingSkills(missingSkills)
                .isActive(team.getIsActive())
                .build();
    }
}
