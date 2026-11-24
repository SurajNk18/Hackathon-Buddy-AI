package com.hackathonbuddy.controller;

import com.hackathonbuddy.dto.request.CreateTeamRequest;
import com.hackathonbuddy.dto.response.ApiResponse;
import com.hackathonbuddy.dto.response.TeamMemberResponse;
import com.hackathonbuddy.dto.response.TeamResponse;
import com.hackathonbuddy.entity.User;
import com.hackathonbuddy.repository.UserRepository;
import com.hackathonbuddy.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TeamControllerTest {

    private TeamService teamService;
    private UserRepository userRepository;
    private TeamController teamController;

    private User testUser;
    private UserDetails userDetails;
    private TeamResponse sampleTeam;

    @BeforeEach
    void setUp() {
        teamService = mock(TeamService.class);
        userRepository = mock(UserRepository.class);
        teamController = new TeamController(teamService, userRepository);

        testUser = User.builder()
                .id(1L)
                .email("sanika@test.com")
                .firstName("Sanika")
                .lastName("Patil")
                .build();

        userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("sanika@test.com");
        when(userRepository.findByEmail("sanika@test.com")).thenReturn(Optional.of(testUser));

        TeamMemberResponse leader = TeamMemberResponse.builder()
                .userId(1L).name("Sanika Patil").email("sanika@test.com")
                .role("Leader").status("ACCEPTED")
                .build();

        sampleTeam = TeamResponse.builder()
                .id(10L)
                .name("Innovators")
                .description("AI/ML focused hackathon team")
                .leader(leader)
                .members(List.of(leader))
                .skillCoveragePercent(75)
                .isActive(true)
                .build();
    }

    @Test
    @DisplayName("GET /api/teams/my-team - Should retrieve user's primary team")
    void testGetMyTeam() {
        when(teamService.getMyTeam(testUser)).thenReturn(sampleTeam);

        ResponseEntity<ApiResponse<TeamResponse>> response =
                teamController.getMyTeam(userDetails);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Innovators", response.getBody().getData().getName());
        assertEquals(75, response.getBody().getData().getSkillCoveragePercent());
    }

    @Test
    @DisplayName("GET /api/teams - Should retrieve all user's teams")
    void testGetAllMyTeams() {
        TeamResponse team2 = TeamResponse.builder()
                .id(11L).name("Web Warriors").isActive(true).build();

        when(teamService.getAllMyTeams(testUser)).thenReturn(List.of(sampleTeam, team2));

        ResponseEntity<ApiResponse<List<TeamResponse>>> response =
                teamController.getAllMyTeams(userDetails);

        assertTrue(response.getBody().isSuccess());
        assertEquals(2, response.getBody().getData().size());
    }

    @Test
    @DisplayName("POST /api/teams - Should create team successfully")
    void testCreateTeam() {
        CreateTeamRequest request = new CreateTeamRequest("AI Wizards", "Deep learning team", null);

        TeamResponse createdTeam = TeamResponse.builder()
                .id(12L).name("AI Wizards").description("Deep learning team").isActive(true).build();
        when(teamService.createTeam(any(CreateTeamRequest.class), eq(testUser))).thenReturn(createdTeam);

        ResponseEntity<ApiResponse<TeamResponse>> response =
                teamController.createTeam(request, userDetails);

        assertTrue(response.getBody().isSuccess());
        assertEquals("Team created successfully", response.getBody().getMessage());
        assertEquals("AI Wizards", response.getBody().getData().getName());
    }

    @Test
    @DisplayName("PUT /api/teams/{teamId} - Should update team details")
    void testUpdateTeam() {
        CreateTeamRequest request = new CreateTeamRequest("Innovators v2", "Updated description", null);

        TeamResponse updated = TeamResponse.builder()
                .id(10L).name("Innovators v2").description("Updated description").isActive(true).build();
        when(teamService.updateTeam(eq(10L), any(CreateTeamRequest.class), eq(testUser))).thenReturn(updated);

        ResponseEntity<ApiResponse<TeamResponse>> response =
                teamController.updateTeam(10L, request, userDetails);

        assertTrue(response.getBody().isSuccess());
        assertEquals("Team updated successfully", response.getBody().getMessage());
        assertEquals("Innovators v2", response.getBody().getData().getName());
    }

    @Test
    @DisplayName("POST /api/teams/{teamId}/members - Should add member via request body")
    void testAddMember_FromBody() {
        TeamResponse teamWithNewMember = TeamResponse.builder()
                .id(10L).name("Innovators").members(List.of(
                        TeamMemberResponse.builder().userId(1L).name("Sanika Patil").role("Leader").build(),
                        TeamMemberResponse.builder().userId(2L).name("Bob Dev").role("Backend Developer").build()
                )).isActive(true).build();

        when(teamService.addMember(eq(10L), eq(2L), eq("Bob Dev"), eq("bob@test.com"),
                eq("Backend Developer"), eq(testUser))).thenReturn(teamWithNewMember);

        Map<String, Object> body = Map.of(
                "userId", 2L,
                "name", "Bob Dev",
                "email", "bob@test.com",
                "role", "Backend Developer"
        );

        ResponseEntity<ApiResponse<TeamResponse>> response =
                teamController.addMember(10L, null, null, null, null, body, userDetails);

        assertTrue(response.getBody().isSuccess());
        assertEquals(2, response.getBody().getData().getMembers().size());
    }

    @Test
    @DisplayName("DELETE /api/teams/{teamId}/members/{userId} - Should remove member")
    void testRemoveMember() {
        TeamResponse teamAfterRemoval = TeamResponse.builder()
                .id(10L).name("Innovators").members(List.of(
                        TeamMemberResponse.builder().userId(1L).name("Sanika Patil").role("Leader").build()
                )).isActive(true).build();

        when(teamService.removeMember(10L, 2L, testUser)).thenReturn(teamAfterRemoval);

        ResponseEntity<ApiResponse<TeamResponse>> response =
                teamController.removeMember(10L, 2L, userDetails);

        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().getMembers().size());
    }

    @Test
    @DisplayName("PUT /api/teams/{teamId}/members/{userId}/role - Should update member role")
    void testUpdateMemberRole() {
        TeamResponse teamWithUpdatedRole = TeamResponse.builder()
                .id(10L).name("Innovators").members(List.of(
                        TeamMemberResponse.builder().userId(2L).name("Bob Dev").role("Tech Lead").build()
                )).isActive(true).build();

        when(teamService.updateMemberRole(10L, 2L, "Tech Lead", testUser))
                .thenReturn(teamWithUpdatedRole);

        ResponseEntity<ApiResponse<TeamResponse>> response =
                teamController.updateMemberRole(10L, 2L, "Tech Lead", userDetails);

        assertTrue(response.getBody().isSuccess());
        assertEquals("Tech Lead", response.getBody().getData().getMembers().get(0).getRole());
    }

    @Test
    @DisplayName("GET /api/teams/{teamId}/skill-gap - Should return skill gap analysis")
    void testGetSkillGap() {
        TeamResponse teamWithGap = TeamResponse.builder()
                .id(10L).name("Innovators")
                .skillCoveragePercent(60)
                .missingSkills(List.of("Docker", "Kubernetes", "AWS"))
                .isActive(true).build();

        when(teamService.getSkillGap(10L, testUser)).thenReturn(teamWithGap);

        ResponseEntity<ApiResponse<TeamResponse>> response =
                teamController.getSkillGap(10L, userDetails);

        assertTrue(response.getBody().isSuccess());
        assertEquals(60, response.getBody().getData().getSkillCoveragePercent());
        assertEquals(3, response.getBody().getData().getMissingSkills().size());
        assertTrue(response.getBody().getData().getMissingSkills().contains("Docker"));
    }
}
