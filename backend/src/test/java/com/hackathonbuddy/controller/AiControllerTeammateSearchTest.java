package com.hackathonbuddy.controller;

import com.hackathonbuddy.client.AiServiceClient;
import com.hackathonbuddy.dto.response.ApiResponse;
import com.hackathonbuddy.dto.response.TeammateMatchResponse;
import com.hackathonbuddy.entity.Role;
import com.hackathonbuddy.entity.Skill;
import com.hackathonbuddy.entity.User;
import com.hackathonbuddy.entity.UserSkill;
import com.hackathonbuddy.repository.*;
import com.hackathonbuddy.service.ActivityService;
import com.hackathonbuddy.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class AiControllerTeammateSearchTest {

    private AiServiceClient aiServiceClient;
    private UserRepository userRepository;
    private UserSkillRepository userSkillRepository;
    private UserInterestRepository userInterestRepository;
    private UserExperienceRepository userExperienceRepository;
    private UserPreferenceRepository userPreferenceRepository;
    private ProjectIdeaRepository projectIdeaRepository;
    private ActivityService activityService;
    private NotificationService notificationService;
    private TeamRepository teamRepository;
    private AiController aiController;

    private User requestingUser;
    private User studentDocker;
    private User studentR;
    private User studentJava;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        aiServiceClient = Mockito.mock(AiServiceClient.class);
        userRepository = Mockito.mock(UserRepository.class);
        userSkillRepository = Mockito.mock(UserSkillRepository.class);
        userInterestRepository = Mockito.mock(UserInterestRepository.class);
        userExperienceRepository = Mockito.mock(UserExperienceRepository.class);
        userPreferenceRepository = Mockito.mock(UserPreferenceRepository.class);
        projectIdeaRepository = Mockito.mock(ProjectIdeaRepository.class);
        activityService = Mockito.mock(ActivityService.class);
        notificationService = Mockito.mock(NotificationService.class);
        teamRepository = Mockito.mock(TeamRepository.class);

        aiController = new AiController(
                aiServiceClient,
                userRepository,
                userSkillRepository,
                userInterestRepository,
                userExperienceRepository,
                userPreferenceRepository,
                projectIdeaRepository,
                activityService,
                notificationService,
                teamRepository
        );

        Role studentRole = Role.builder().id(1L).name("ROLE_STUDENT").build();

        requestingUser = User.builder()
                .id(1L)
                .email("alice@test.com")
                .firstName("Alice")
                .lastName("Smith")
                .role(studentRole)
                .isActive(true)
                .build();

        studentDocker = User.builder()
                .id(2L)
                .email("bob@test.com")
                .firstName("Bob")
                .lastName("Docker")
                .role(studentRole)
                .isActive(true)
                .build();

        studentR = User.builder()
                .id(3L)
                .email("charlie@test.com")
                .firstName("Charlie")
                .lastName("RLang")
                .role(studentRole)
                .isActive(true)
                .build();

        studentJava = User.builder()
                .id(4L)
                .email("david@test.com")
                .firstName("David")
                .lastName("Java")
                .role(studentRole)
                .isActive(true)
                .build();

        userDetails = Mockito.mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("alice@test.com");
        when(userRepository.findByEmail("alice@test.com")).thenReturn(Optional.of(requestingUser));

        Skill skillDocker = Skill.builder().id(101L).name("Docker").build();
        Skill skillR = Skill.builder().id(102L).name("R").build();
        Skill skillJava = Skill.builder().id(103L).name("Java").build();

        when(userSkillRepository.findByUser(studentDocker)).thenReturn(List.of(
                UserSkill.builder().id(1L).user(studentDocker).skill(skillDocker).build()
        ));
        when(userSkillRepository.findByUser(studentR)).thenReturn(List.of(
                UserSkill.builder().id(2L).user(studentR).skill(skillR).build()
        ));
        when(userSkillRepository.findByUser(studentJava)).thenReturn(List.of(
                UserSkill.builder().id(3L).user(studentJava).skill(skillJava).build()
        ));
        when(userSkillRepository.findByUser(requestingUser)).thenReturn(List.of());

        when(userRepository.findAll(any(Sort.class))).thenReturn(List.of(requestingUser, studentDocker, studentR, studentJava));
    }

    @Test
    void testSkillsMatchStrictness() {
        // Valid exact / normalized matches
        assertTrue(AiController.skillsMatch("React", "react"));
        assertTrue(AiController.skillsMatch("React.js", "React"));
        assertTrue(AiController.skillsMatch("Node.js", "Node"));
        assertTrue(AiController.skillsMatch("Go", "Golang"));
        assertTrue(AiController.skillsMatch("C++", "C++"));
        assertTrue(AiController.skillsMatch("C#", "C#"));

        // Substrings that must NOT match
        assertFalse(AiController.skillsMatch("R", "Docker"));
        assertFalse(AiController.skillsMatch("C", "React"));
        assertFalse(AiController.skillsMatch("Java", "JavaScript"));
        assertFalse(AiController.skillsMatch("JavaScript", "Java"));
        assertFalse(AiController.skillsMatch("Go", "Algo"));
        assertFalse(AiController.skillsMatch("C", "C++"));
    }

    @Test
    void testFindTeammatesReturnsOnlyStudentsWithSearchedSkill() {
        Map<String, Object> request = Map.of("skills", "Docker");

        // Simulate ML service returning the eligible candidate Bob
        when(aiServiceClient.findCompatibleTeammates(anyMap(), anyList(), anyLong())).thenAnswer(invocation -> {
            List<Map<String, Object>> candidates = invocation.getArgument(1);
            assertEquals(1, candidates.size());
            assertEquals(2L, candidates.get(0).get("userId"));

            return List.of(Map.of(
                    "userId", 2L,
                    "name", "Bob Docker",
                    "skills", List.of("Docker"),
                    "compatibilityScore", 92.5,
                    "commonSkills", List.of("Docker"),
                    "matchReason", "Strong Docker match"
            ));
        });

        ResponseEntity<ApiResponse<List<TeammateMatchResponse>>> response = aiController.findTeammates(null, request, userDetails);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        List<TeammateMatchResponse> matches = response.getBody().getData();
        assertEquals(1, matches.size());
        assertEquals(2L, matches.get(0).getUserId());
        assertEquals("Bob Docker", matches.get(0).getName());
    }

    @Test
    void testFindTeammatesEmptyWhenNoStudentMatches() {
        Map<String, Object> request = Map.of("skills", "Rust");

        ResponseEntity<ApiResponse<List<TeammateMatchResponse>>> response = aiController.findTeammates(null, request, userDetails);

        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        List<TeammateMatchResponse> matches = response.getBody().getData();
        assertTrue(matches.isEmpty());
        assertEquals("Teammate matching complete — 0 matches found", response.getBody().getMessage());
    }

    @Test
    void testFindTeammatesConsistentResults() {
        Map<String, Object> request = Map.of("skills", "Docker");

        when(aiServiceClient.findCompatibleTeammates(anyMap(), anyList(), anyLong())).thenReturn(List.of(
                Map.of("userId", 2L, "name", "Bob Docker", "skills", List.of("Docker"), "compatibilityScore", 92.0)
        ));

        ResponseEntity<ApiResponse<List<TeammateMatchResponse>>> res1 = aiController.findTeammates(null, request, userDetails);
        ResponseEntity<ApiResponse<List<TeammateMatchResponse>>> res2 = aiController.findTeammates(null, request, userDetails);

        assertEquals(res1.getBody().getData().size(), res2.getBody().getData().size());
        assertEquals(res1.getBody().getData().get(0).getUserId(), res2.getBody().getData().get(0).getUserId());
    }

    @Test
    void testProfileUpdateReflectedInTeammateSearch() {
        Map<String, Object> request = Map.of("skills", "Rust");

        // Before update: no one has Rust
        ResponseEntity<ApiResponse<List<TeammateMatchResponse>>> resBefore = aiController.findTeammates(null, request, userDetails);
        assertTrue(resBefore.getBody().getData().isEmpty());

        // Update profile: David adds Rust
        Skill skillRust = Skill.builder().id(104L).name("Rust").build();
        when(userSkillRepository.findByUser(studentJava)).thenReturn(List.of(
                UserSkill.builder().id(3L).user(studentJava).skill(Skill.builder().name("Java").build()).build(),
                UserSkill.builder().id(4L).user(studentJava).skill(skillRust).build()
        ));

        when(aiServiceClient.findCompatibleTeammates(anyMap(), anyList(), anyLong())).thenAnswer(invocation -> {
            List<Map<String, Object>> candidates = invocation.getArgument(1);
            assertEquals(1, candidates.size());
            assertEquals(4L, candidates.get(0).get("userId"));

            return List.of(Map.of(
                    "userId", 4L,
                    "name", "David Java",
                    "skills", List.of("Java", "Rust"),
                    "compatibilityScore", 88.0
            ));
        });

        // After update: David is found
        ResponseEntity<ApiResponse<List<TeammateMatchResponse>>> resAfter = aiController.findTeammates(null, request, userDetails);
        assertEquals(1, resAfter.getBody().getData().size());
        assertEquals(4L, resAfter.getBody().getData().get(0).getUserId());
    }
}
