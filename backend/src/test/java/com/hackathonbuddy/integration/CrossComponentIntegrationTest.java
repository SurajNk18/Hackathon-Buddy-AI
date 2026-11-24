package com.hackathonbuddy.integration;

import com.hackathonbuddy.client.AiServiceClient;
import com.hackathonbuddy.controller.*;
import com.hackathonbuddy.dto.request.CreateProjectRequest;
import com.hackathonbuddy.dto.request.CreateTaskRequest;
import com.hackathonbuddy.dto.response.*;
import com.hackathonbuddy.entity.*;
import com.hackathonbuddy.model.Hackathon;
import com.hackathonbuddy.provider.HackathonProvider;
import com.hackathonbuddy.repository.*;
import com.hackathonbuddy.service.*;
import org.junit.jupiter.api.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests that verify cross-component interactions.
 * These tests wire together multiple controllers/services to validate
 * complete user workflows (project creation → task assignment → progress tracking,
 * hackathon search with aggregation, teammate search → notification flow).
 */
@DisplayName("Integration Tests — Cross-Component Workflows")
class CrossComponentIntegrationTest {

    // ─── Shared Mocks ───────────────────────────────────────────
    private UserRepository userRepository;
    private User testUser;
    private UserDetails userDetails;

    @BeforeEach
    void setUpCommon() {
        userRepository = mock(UserRepository.class);
        Role studentRole = Role.builder().id(1L).name("ROLE_STUDENT").build();
        testUser = User.builder()
                .id(1L).firstName("Sanika").lastName("Patil")
                .email("sanika@test.com").password("hashed")
                .role(studentRole).isActive(true).profileComplete(true)
                .primaryRole("Full Stack Developer")
                .build();

        userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("sanika@test.com");
        when(userRepository.findByEmail("sanika@test.com")).thenReturn(Optional.of(testUser));
    }

    // ═════════════════════════════════════════════════════════════
    // TEST 1: Project → Task → Progress Update Workflow
    // ═════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Project Lifecycle Integration")
    class ProjectLifecycleTest {

        private ProjectController projectController;
        private ProjectRepository projectRepository;
        private ProjectTaskRepository projectTaskRepository;
        private ProjectIdeaRepository projectIdeaRepository;
        private TeamRepository teamRepository;
        private ActivityService activityService;

        private Project savedProject;
        private List<ProjectTask> taskStore;

        @BeforeEach
        void setUp() {
            projectRepository = mock(ProjectRepository.class);
            projectTaskRepository = mock(ProjectTaskRepository.class);
            projectIdeaRepository = mock(ProjectIdeaRepository.class);
            teamRepository = mock(TeamRepository.class);
            activityService = mock(ActivityService.class);

            projectController = new ProjectController(
                    projectIdeaRepository, projectRepository, projectTaskRepository,
                    userRepository, teamRepository, activityService
            );

            taskStore = new ArrayList<>();

            // No team for simplicity
            when(teamRepository.findTeamsByMember(testUser)).thenReturn(List.of());
            when(teamRepository.findFirstTeamForUser(testUser)).thenReturn(Optional.empty());
        }

        @Test
        @DisplayName("Full workflow: Create project → Add tasks → Update status → Verify progress")
        void testFullProjectWorkflow() {
            // ── Step 1: Create a project ──
            savedProject = Project.builder()
                    .id(100L).name("AI Chatbot").domain("AI / ML")
                    .status("Active").technologies("Python, React")
                    .members(1).progress(0).user(testUser)
                    .createdAt(LocalDateTime.now())
                    .build();

            when(projectRepository.save(any(Project.class))).thenReturn(savedProject);
            when(projectRepository.findByUserOrderByCreatedAtDesc(testUser))
                    .thenReturn(List.of(savedProject));

            CreateProjectRequest createReq = new CreateProjectRequest();
            createReq.setName("AI Chatbot");
            createReq.setDomain("AI / ML");
            createReq.setTechnologies(List.of("Python", "React"));

            ResponseEntity<ApiResponse<ProjectResponse>> createRes =
                    projectController.createProject(createReq, userDetails);

            assertTrue(createRes.getBody().isSuccess());
            assertEquals("AI Chatbot", createRes.getBody().getData().getName());

            // ── Step 2: Add tasks to the project ──
            when(projectRepository.findById(100L)).thenReturn(Optional.of(savedProject));

            ProjectTask task1 = ProjectTask.builder()
                    .id(1L).project(savedProject).title("Set up environment")
                    .assignedTo("Sanika").assignedToUserId(1L)
                    .assignedToEmail("sanika@test.com")
                    .priority("High").status("To Do")
                    .createdAt(LocalDateTime.now())
                    .build();
            ProjectTask task2 = ProjectTask.builder()
                    .id(2L).project(savedProject).title("Build NLP model")
                    .assignedTo("Sanika").assignedToUserId(1L)
                    .assignedToEmail("sanika@test.com")
                    .priority("High").status("To Do")
                    .createdAt(LocalDateTime.now())
                    .build();
            ProjectTask task3 = ProjectTask.builder()
                    .id(3L).project(savedProject).title("Design frontend")
                    .assignedTo("Sanika").assignedToUserId(1L)
                    .assignedToEmail("sanika@test.com")
                    .priority("Medium").status("To Do")
                    .createdAt(LocalDateTime.now())
                    .build();

            taskStore.addAll(List.of(task1, task2, task3));

            when(projectTaskRepository.save(any(ProjectTask.class))).thenAnswer(inv -> inv.getArgument(0));
            when(projectTaskRepository.findByProjectIdOrderByIdAsc(100L)).thenReturn(taskStore);
            when(projectTaskRepository.findByProjectInOrderByIdAsc(anyList())).thenReturn(taskStore);

            // Verify tasks were stored
            ResponseEntity<ApiResponse<List<ProjectTaskResponse>>> tasksRes =
                    projectController.getTasks(userDetails);

            assertEquals(3, tasksRes.getBody().getData().size());

            // ── Step 3: Complete 1 of 3 tasks → progress should be 33% ──
            task1.setStatus("Completed");
            when(projectTaskRepository.findById(1L)).thenReturn(Optional.of(task1));
            when(projectRepository.save(any(Project.class))).thenAnswer(inv -> {
                Project p = inv.getArgument(0);
                savedProject.setProgress(p.getProgress());
                savedProject.setStatus(p.getStatus());
                return savedProject;
            });

            ResponseEntity<ApiResponse<ProjectTaskResponse>> updateRes =
                    projectController.updateTaskStatus(1L, "Completed", userDetails);

            assertTrue(updateRes.getBody().isSuccess());
            assertEquals(33, savedProject.getProgress());
            assertEquals("Active", savedProject.getStatus());

            // ── Step 4: Complete all tasks → progress = 100%, status = Completed ──
            task2.setStatus("Completed");
            task3.setStatus("Completed");
            when(projectTaskRepository.findById(2L)).thenReturn(Optional.of(task2));

            projectController.updateTaskStatus(2L, "Completed", userDetails);

            when(projectTaskRepository.findById(3L)).thenReturn(Optional.of(task3));
            projectController.updateTaskStatus(3L, "Completed", userDetails);

            assertEquals(100, savedProject.getProgress());
            assertEquals("Completed", savedProject.getStatus());
        }
    }

    // ═════════════════════════════════════════════════════════════
    // TEST 2: Hackathon Search → Filter → Pagination Workflow
    // ═════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Hackathon Aggregation Integration")
    class HackathonAggregationTest {

        private HackathonController hackathonController;

        @BeforeEach
        void setUp() {
            HackathonProvider devpostProvider = new HackathonProvider() {
                @Override
                public String getProviderName() { return "Devpost"; }

                @Override
                public List<Hackathon> fetchHackathons() {
                    return List.of(
                            Hackathon.builder().id("dp-1").title("AI Global Hack")
                                    .platform("Devpost").deadline("2026-11-01")
                                    .status("Open").mode("Online")
                                    .tags(List.of("AI", "Python")).organizer("Google").build(),
                            Hackathon.builder().id("dp-2").title("Web3 Summit")
                                    .platform("Devpost").deadline("2026-12-01")
                                    .status("Open").mode("In-Person")
                                    .tags(List.of("Web3", "Solidity")).organizer("ETH Foundation").build(),
                            Hackathon.builder().id("dp-3").title("IoT Innovation")
                                    .platform("Devpost").deadline("2026-12-20")
                                    .status("Upcoming").mode("Hybrid")
                                    .tags(List.of("IoT", "Arduino")).organizer("Intel").build()
                    );
                }
            };

            HackathonProvider devfolioProvider = new HackathonProvider() {
                @Override
                public String getProviderName() { return "Devfolio"; }

                @Override
                public List<Hackathon> fetchHackathons() {
                    return List.of(
                            Hackathon.builder().id("df-1").title("ETHIndia 2026")
                                    .platform("Devfolio").deadline("2026-12-15")
                                    .status("Open").mode("In-Person")
                                    .tags(List.of("Web3", "Ethereum")).organizer("Devfolio").build()
                    );
                }
            };

            HackathonAggregationService aggregationService =
                    new HackathonAggregationService(List.of(devpostProvider, devfolioProvider));
            HackathonService hackathonService = mock(HackathonService.class);
            NotificationService notificationService = mock(NotificationService.class);

            hackathonController = new HackathonController(
                    hackathonService, aggregationService, userRepository, notificationService
            );
        }

        @Test
        @DisplayName("Search hackathons by keyword filters results correctly")
        void testSearchByKeyword() {
            ResponseEntity<ApiResponse<HackathonPageResponse>> response =
                    hackathonController.getExternalHackathons(
                            null, null, "AI", null, null, "deadline", "asc", 0, 20);

            assertTrue(response.getBody().isSuccess());
            assertEquals(1, response.getBody().getData().getTotalElements());
            assertEquals("AI Global Hack", response.getBody().getData().getContent().get(0).getTitle());
        }

        @Test
        @DisplayName("Filter by platform returns only matching hackathons")
        void testFilterByPlatform() {
            ResponseEntity<ApiResponse<HackathonPageResponse>> response =
                    hackathonController.getExternalHackathons(
                            List.of("devfolio"), null, null, null, null, "deadline", "asc", 0, 20);

            assertEquals(1, response.getBody().getData().getTotalElements());
            assertEquals("ETHIndia 2026", response.getBody().getData().getContent().get(0).getTitle());
        }

        @Test
        @DisplayName("Filter by status returns only open hackathons")
        void testFilterByStatus() {
            ResponseEntity<ApiResponse<HackathonPageResponse>> response =
                    hackathonController.getExternalHackathons(
                            null, "open", null, null, null, "deadline", "asc", 0, 20);

            assertTrue(response.getBody().isSuccess());
            // Should exclude the "Upcoming" IoT hackathon
            assertEquals(3, response.getBody().getData().getTotalElements());
            assertTrue(response.getBody().getData().getContent().stream()
                    .allMatch(h -> "Open".equalsIgnoreCase(h.getStatus())));
        }

        @Test
        @DisplayName("Pagination works correctly across providers")
        void testPaginationAcrossProviders() {
            // Page 1: 2 items
            ResponseEntity<ApiResponse<HackathonPageResponse>> page1 =
                    hackathonController.getExternalHackathons(
                            null, null, null, null, null, "deadline", "asc", 0, 2);

            assertEquals(4, page1.getBody().getData().getTotalElements());
            assertEquals(2, page1.getBody().getData().getContent().size());
            assertEquals(2, page1.getBody().getData().getTotalPages());

            // Page 2: 2 items
            ResponseEntity<ApiResponse<HackathonPageResponse>> page2 =
                    hackathonController.getExternalHackathons(
                            null, null, null, null, null, "deadline", "asc", 1, 2);

            assertEquals(2, page2.getBody().getData().getContent().size());
        }

        @Test
        @DisplayName("Provider metadata tracks success status")
        void testProviderMetadata() {
            ResponseEntity<ApiResponse<HackathonPageResponse>> response =
                    hackathonController.getExternalHackathons(
                            null, null, null, null, null, "deadline", "asc", 0, 20);

            Map<String, ProviderMetaInfo> providers = response.getBody().getData().getProviders();
            assertTrue(providers.get("devpost").isSuccess());
            assertEquals(3, providers.get("devpost").getCount());
            assertTrue(providers.get("devfolio").isSuccess());
            assertEquals(1, providers.get("devfolio").getCount());
        }
    }

    // ═════════════════════════════════════════════════════════════
    // TEST 3: Teammate Search → Notification Integration
    // ═════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Teammate Search + Notification Flow Integration")
    class TeammateNotificationFlowTest {

        private AiController aiController;
        private NotificationController notificationController;
        private AiServiceClient aiServiceClient;
        private UserSkillRepository userSkillRepository;
        private NotificationService notificationService;
        private TeamRepository teamRepository;

        private User teammate;

        @BeforeEach
        void setUp() {
            aiServiceClient = mock(AiServiceClient.class);
            userSkillRepository = mock(UserSkillRepository.class);
            UserInterestRepository userInterestRepository = mock(UserInterestRepository.class);
            UserExperienceRepository userExperienceRepository = mock(UserExperienceRepository.class);
            UserPreferenceRepository userPreferenceRepository = mock(UserPreferenceRepository.class);
            ProjectIdeaRepository projectIdeaRepository = mock(ProjectIdeaRepository.class);
            ActivityService activityService = mock(ActivityService.class);
            notificationService = mock(NotificationService.class);
            teamRepository = mock(TeamRepository.class);

            aiController = new AiController(
                    aiServiceClient, userRepository, userSkillRepository,
                    userInterestRepository, userExperienceRepository,
                    userPreferenceRepository, projectIdeaRepository,
                    activityService, notificationService, teamRepository
            );

            notificationController = new NotificationController(notificationService, userRepository);

            // Set up teammate
            Role studentRole = Role.builder().id(1L).name("ROLE_STUDENT").build();
            teammate = User.builder()
                    .id(2L).firstName("Bob").lastName("Developer")
                    .email("bob@test.com").password("hashed")
                    .role(studentRole).isActive(true)
                    .build();

            // Skills setup
            Skill reactSkill = Skill.builder().id(101L).name("React").build();
            Skill pythonSkill = Skill.builder().id(102L).name("Python").build();

            when(userSkillRepository.findByUser(testUser)).thenReturn(List.of(
                    UserSkill.builder().id(1L).user(testUser).skill(reactSkill).build()
            ));
            when(userSkillRepository.findByUser(teammate)).thenReturn(List.of(
                    UserSkill.builder().id(2L).user(teammate).skill(pythonSkill).build()
            ));

            when(userRepository.findAll(any(org.springframework.data.domain.Sort.class)))
                    .thenReturn(List.of(testUser, teammate));
        }

        @Test
        @DisplayName("Search teammates by skill → Send invitation → Verify notification")
        void testSearchAndInviteWorkflow() {
            // ── Step 1: Search for Python teammates ──
            Map<String, Object> searchRequest = Map.of("skills", "Python");

            when(aiServiceClient.findCompatibleTeammates(anyMap(), anyList(), anyLong()))
                    .thenReturn(List.of(Map.of(
                            "userId", 2L,
                            "name", "Bob Developer",
                            "skills", List.of("Python"),
                            "compatibilityScore", 90.0,
                            "commonSkills", List.of("Python"),
                            "matchReason", "Strong Python match"
                    )));

            ResponseEntity<ApiResponse<List<TeammateMatchResponse>>> searchRes =
                    aiController.findTeammates(null, searchRequest, userDetails);

            assertTrue(searchRes.getBody().isSuccess());
            assertEquals(1, searchRes.getBody().getData().size());
            assertEquals(2L, searchRes.getBody().getData().get(0).getUserId());
            assertEquals("Bob Developer", searchRes.getBody().getData().get(0).getName());

            // ── Step 2: Send team invitation to the found teammate ──
            Map<String, Object> invitePayload = Map.of(
                    "receiverId", 2L,
                    "teamId", 10L,
                    "role", "Backend Developer"
            );

            ResponseEntity<ApiResponse<Void>> inviteRes =
                    notificationController.sendInvitation(invitePayload, userDetails);

            assertTrue(inviteRes.getBody().isSuccess());
            assertEquals("Team invitation sent successfully", inviteRes.getBody().getMessage());
            verify(notificationService).sendTeamInvitation(testUser, 2L, 10L, "Backend Developer");
        }

        @Test
        @DisplayName("No teammates found returns empty list gracefully")
        void testNoTeammatesFound() {
            Map<String, Object> searchRequest = Map.of("skills", "Rust");

            ResponseEntity<ApiResponse<List<TeammateMatchResponse>>> response =
                    aiController.findTeammates(null, searchRequest, userDetails);

            assertTrue(response.getBody().isSuccess());
            assertTrue(response.getBody().getData().isEmpty());
        }
    }

    // ═════════════════════════════════════════════════════════════
    // TEST 4: Chat Assistant + Activity Logging Integration
    // ═════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Chat Assistant Integration")
    class ChatAssistantIntegrationTest {

        private ChatController chatController;
        private ChatMessageRepository chatMessageRepository;
        private AiServiceClient aiServiceClient;

        @BeforeEach
        void setUp() {
            chatMessageRepository = mock(ChatMessageRepository.class);
            TeamRepository teamRepository = mock(TeamRepository.class);
            TeamMemberRepository teamMemberRepository = mock(TeamMemberRepository.class);
            UserSkillRepository userSkillRepository = mock(UserSkillRepository.class);
            aiServiceClient = mock(AiServiceClient.class);
            org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate =
                    mock(org.springframework.messaging.simp.SimpMessagingTemplate.class);

            chatController = new ChatController(
                    chatMessageRepository, teamRepository, teamMemberRepository,
                    userRepository, userSkillRepository, aiServiceClient, messagingTemplate
            );
        }

        @Test
        @DisplayName("Full chat flow: Ask → Get response → Retrieve history → Clear")
        void testChatWorkflow() {
            // ── Step 1: Ask the AI assistant ──
            when(aiServiceClient.askChatbot(anyString(), anyString(), anyList(), anyList()))
                    .thenReturn("For your AI hackathon project, consider using a transformer-based approach...");

            when(chatMessageRepository.save(any(ChatMessage.class))).thenAnswer(inv -> {
                ChatMessage msg = inv.getArgument(0);
                msg.setId(msg.getMessageType().equals("USER") ? 1L : 2L);
                return msg;
            });

            Map<String, String> body = Map.of("message", "What architecture should I use for my AI project?");
            ResponseEntity<ApiResponse<ChatMessage>> askRes = chatController.askAssistant(body, userDetails);

            assertTrue(askRes.getBody().isSuccess());
            assertNotNull(askRes.getBody().getData());
            assertEquals("AI", askRes.getBody().getData().getMessageType());
            assertTrue(askRes.getBody().getData().getContent().contains("transformer-based"));

            // ── Step 2: Retrieve conversation history ──
            ChatMessage userMsg = ChatMessage.builder()
                    .id(1L).conversationId(1L).senderId(1L)
                    .content("What architecture should I use?").messageType("USER")
                    .timestamp(LocalDateTime.now().minusMinutes(2)).build();
            ChatMessage aiMsg = ChatMessage.builder()
                    .id(2L).conversationId(1L).senderId(0L)
                    .content("Consider using transformer-based approach").messageType("AI")
                    .timestamp(LocalDateTime.now()).build();

            when(chatMessageRepository.findByConversationIdAndTeamIdIsNullOrderByTimestampAsc(1L))
                    .thenReturn(List.of(userMsg, aiMsg));

            ResponseEntity<ApiResponse<List<ChatMessage>>> historyRes =
                    chatController.getAssistantMessages(userDetails);

            assertTrue(historyRes.getBody().isSuccess());
            assertEquals(2, historyRes.getBody().getData().size());

            // ── Step 3: Clear conversation history ──
            ResponseEntity<ApiResponse<String>> clearRes =
                    chatController.clearAssistantHistory(userDetails);

            assertTrue(clearRes.getBody().isSuccess());
            verify(chatMessageRepository).deleteByConversationIdAndTeamIdIsNull(1L);
        }

        @Test
        @DisplayName("Empty message should return BAD_REQUEST")
        void testEmptyMessage() {
            Map<String, String> body = Map.of("message", "   ");
            ResponseEntity<ApiResponse<ChatMessage>> res = chatController.askAssistant(body, userDetails);

            assertEquals(400, res.getStatusCode().value());
            assertFalse(res.getBody().isSuccess());
        }

        @Test
        @DisplayName("Unauthenticated request should return UNAUTHORIZED")
        void testUnauthenticated() {
            Map<String, String> body = Map.of("message", "Hello");
            ResponseEntity<ApiResponse<ChatMessage>> res = chatController.askAssistant(body, null);

            assertEquals(401, res.getStatusCode().value());
        }
    }

    // ═════════════════════════════════════════════════════════════
    // TEST 5: DTO / Response Mapping Integration
    // ═════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("API Response Contract Validation")
    class ApiResponseContractTest {

        @Test
        @DisplayName("ApiResponse.success() creates correct structure")
        void testApiResponseSuccess() {
            ApiResponse<String> response = ApiResponse.success("Operation succeeded", "test-data");

            assertTrue(response.isSuccess());
            assertEquals("Operation succeeded", response.getMessage());
            assertEquals("test-data", response.getData());
        }

        @Test
        @DisplayName("ApiResponse.error() creates correct structure")
        void testApiResponseError() {
            ApiResponse<String> response = ApiResponse.error("Something went wrong");

            assertFalse(response.isSuccess());
            assertEquals("Something went wrong", response.getMessage());
            assertNull(response.getData());
        }

        @Test
        @DisplayName("ApiResponse builder creates correct response")
        void testApiResponseBuilder() {
            ApiResponse<Integer> response = ApiResponse.<Integer>builder()
                    .success(true)
                    .message("Count retrieved")
                    .data(42)
                    .build();

            assertTrue(response.isSuccess());
            assertEquals(42, response.getData());
        }
    }
}
