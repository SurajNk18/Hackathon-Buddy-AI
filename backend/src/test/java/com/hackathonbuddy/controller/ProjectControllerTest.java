package com.hackathonbuddy.controller;

import com.hackathonbuddy.dto.request.CreateProjectRequest;
import com.hackathonbuddy.dto.request.CreateTaskRequest;
import com.hackathonbuddy.dto.response.ApiResponse;
import com.hackathonbuddy.dto.response.ProjectIdeaResponse;
import com.hackathonbuddy.dto.response.ProjectResponse;
import com.hackathonbuddy.dto.response.ProjectTaskResponse;
import com.hackathonbuddy.entity.Project;
import com.hackathonbuddy.entity.ProjectIdea;
import com.hackathonbuddy.entity.ProjectTask;
import com.hackathonbuddy.entity.Team;
import com.hackathonbuddy.entity.User;
import com.hackathonbuddy.repository.*;
import com.hackathonbuddy.service.ActivityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ProjectControllerTest {

    private ProjectIdeaRepository projectIdeaRepository;
    private ProjectRepository projectRepository;
    private ProjectTaskRepository projectTaskRepository;
    private UserRepository userRepository;
    private TeamRepository teamRepository;
    private ActivityService activityService;
    private ProjectController projectController;

    private User testUser;
    private UserDetails userDetails;
    private Team testTeam;
    private Project testProject;

    @BeforeEach
    void setUp() {
        projectIdeaRepository = mock(ProjectIdeaRepository.class);
        projectRepository = mock(ProjectRepository.class);
        projectTaskRepository = mock(ProjectTaskRepository.class);
        userRepository = mock(UserRepository.class);
        teamRepository = mock(TeamRepository.class);
        activityService = mock(ActivityService.class);

        projectController = new ProjectController(
                projectIdeaRepository, projectRepository, projectTaskRepository,
                userRepository, teamRepository, activityService
        );

        testUser = User.builder()
                .id(1L)
                .email("sanika@test.com")
                .firstName("Sanika")
                .lastName("Patil")
                .build();

        testTeam = Team.builder()
                .id(10L)
                .name("Team Alpha")
                .leader(testUser)
                .members(new ArrayList<>())
                .build();

        testProject = Project.builder()
                .id(100L)
                .name("AI Study Assistant")
                .domain("AI / ML")
                .status("Active")
                .description("An intelligent study companion")
                .technologies("React, Spring Boot, Python")
                .members(3)
                .progress(0)
                .user(testUser)
                .team(testTeam)
                .createdAt(LocalDateTime.now())
                .build();

        userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("sanika@test.com");
        when(userRepository.findByEmail("sanika@test.com")).thenReturn(Optional.of(testUser));
    }

    @Test
    @DisplayName("GET /api/projects - Should retrieve all user projects")
    void testGetAllProjects_Success() {
        when(teamRepository.findTeamsByMember(testUser)).thenReturn(List.of(testTeam));
        when(projectRepository.findProjectsForTeamsAndUser(anyList(), eq(testUser)))
                .thenReturn(List.of(testProject));

        ResponseEntity<ApiResponse<List<ProjectResponse>>> response =
                projectController.getAllProjects(userDetails);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("AI Study Assistant", response.getBody().getData().get(0).getName());
        assertEquals("AI / ML", response.getBody().getData().get(0).getDomain());
    }

    @Test
    @DisplayName("GET /api/projects - Returns empty list when user has no projects")
    void testGetAllProjects_Empty() {
        when(teamRepository.findTeamsByMember(testUser)).thenReturn(List.of());
        when(projectRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(List.of());

        ResponseEntity<ApiResponse<List<ProjectResponse>>> response =
                projectController.getAllProjects(userDetails);

        assertTrue(response.getBody().isSuccess());
        assertTrue(response.getBody().getData().isEmpty());
    }

    @Test
    @DisplayName("POST /api/projects - Should create a new project successfully")
    void testCreateProject_Success() {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Smart Campus Navigator");
        request.setDomain("IoT");
        request.setDescription("Navigate campus with real-time data");
        request.setTechnologies(List.of("React", "Node.js", "MongoDB"));

        when(teamRepository.findFirstTeamForUser(testUser)).thenReturn(Optional.of(testTeam));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            p.setId(200L);
            return p;
        });

        ResponseEntity<ApiResponse<ProjectResponse>> response =
                projectController.createProject(request, userDetails);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Project created successfully", response.getBody().getMessage());
        assertEquals("Smart Campus Navigator", response.getBody().getData().getName());
        assertEquals("IoT", response.getBody().getData().getDomain());

        verify(activityService).logActivity(eq(testUser), eq("PROJECT"), anyString(), anyString(), eq("PROJECT"));
    }

    @Test
    @DisplayName("POST /api/projects - Null name defaults to 'Untitled Project'")
    void testCreateProject_DefaultName() {
        CreateProjectRequest request = new CreateProjectRequest();
        // name is null

        when(teamRepository.findFirstTeamForUser(testUser)).thenReturn(Optional.empty());
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            p.setId(201L);
            return p;
        });

        ResponseEntity<ApiResponse<ProjectResponse>> response =
                projectController.createProject(request, userDetails);

        assertEquals("Untitled Project", response.getBody().getData().getName());
    }

    @Test
    @DisplayName("DELETE /api/projects/{id} - Should delete project and its tasks")
    void testDeleteProject_Success() {
        when(projectRepository.findById(100L)).thenReturn(Optional.of(testProject));

        ResponseEntity<ApiResponse<Void>> response = projectController.deleteProject(100L, userDetails);

        assertTrue(response.getBody().isSuccess());
        assertEquals("Project deleted successfully", response.getBody().getMessage());
        verify(projectTaskRepository).deleteByProject(testProject);
        verify(projectRepository).delete(testProject);
    }

    @Test
    @DisplayName("GET /api/projects/tasks - Should retrieve tasks for user's projects")
    void testGetTasks_Success() {
        ProjectTask task1 = ProjectTask.builder()
                .id(1L).project(testProject).title("Set up repository")
                .description("Initialize Git repo").assignedTo("Sanika")
                .assignedToUserId(1L).priority("High").status("To Do")
                .build();
        ProjectTask task2 = ProjectTask.builder()
                .id(2L).project(testProject).title("Design database schema")
                .description("Create ER diagram").assignedTo("Sanika")
                .assignedToUserId(1L).priority("Medium").status("Completed")
                .build();

        when(teamRepository.findTeamsByMember(testUser)).thenReturn(List.of(testTeam));
        when(projectRepository.findProjectsForTeamsAndUser(anyList(), eq(testUser)))
                .thenReturn(List.of(testProject));
        when(projectTaskRepository.findByProjectInOrderByIdAsc(anyList()))
                .thenReturn(List.of(task1, task2));

        ResponseEntity<ApiResponse<List<ProjectTaskResponse>>> response =
                projectController.getTasks(userDetails);

        assertTrue(response.getBody().isSuccess());
        assertEquals(2, response.getBody().getData().size());
        assertEquals("Set up repository", response.getBody().getData().get(0).getTitle());
        assertEquals("High", response.getBody().getData().get(0).getPriority());
    }

    @Test
    @DisplayName("POST /api/projects/tasks - Should create task with correct assignment")
    void testCreateTask_Success() {
        CreateTaskRequest taskReq = new CreateTaskRequest();
        taskReq.setProjectId(100L);
        taskReq.setTitle("Implement login API");
        taskReq.setDescription("Build JWT authentication");
        taskReq.setAssignedTo("Sanika");
        taskReq.setAssignedToUserId(1L);
        taskReq.setAssignedToEmail("sanika@test.com");
        taskReq.setPriority("High");

        when(projectRepository.findById(100L)).thenReturn(Optional.of(testProject));
        when(teamRepository.findTeamsByMember(testUser)).thenReturn(List.of(testTeam));
        when(projectRepository.findProjectsForTeamsAndUser(anyList(), eq(testUser)))
                .thenReturn(List.of(testProject));
        when(projectTaskRepository.save(any(ProjectTask.class))).thenAnswer(inv -> {
            ProjectTask t = inv.getArgument(0);
            t.setId(10L);
            return t;
        });
        when(projectTaskRepository.findByProjectIdOrderByIdAsc(100L)).thenReturn(List.of());

        ResponseEntity<ApiResponse<ProjectTaskResponse>> response =
                projectController.createTask(taskReq, userDetails);

        assertTrue(response.getBody().isSuccess());
        assertEquals("Task added successfully", response.getBody().getMessage());
        assertEquals("Implement login API", response.getBody().getData().getTitle());
        assertEquals("High", response.getBody().getData().getPriority());
        assertEquals("To Do", response.getBody().getData().getStatus());
    }

    @Test
    @DisplayName("POST /api/projects/tasks - Unauthorized user gets 403")
    void testCreateTask_Unauthorized() {
        CreateTaskRequest taskReq = new CreateTaskRequest();
        taskReq.setProjectId(100L);
        taskReq.setTitle("Some task");

        when(projectRepository.findById(100L)).thenReturn(Optional.of(testProject));
        when(teamRepository.findTeamsByMember(testUser)).thenReturn(List.of());
        when(projectRepository.findByUserOrderByCreatedAtDesc(testUser)).thenReturn(List.of());

        ResponseEntity<ApiResponse<ProjectTaskResponse>> response =
                projectController.createTask(taskReq, userDetails);

        assertEquals(403, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("PUT /api/projects/tasks/{id}/status - Assigned user can update status")
    void testUpdateTaskStatus_AssignedUser() {
        ProjectTask task = ProjectTask.builder()
                .id(5L).project(testProject).title("Build UI")
                .assignedTo("Sanika Patil").assignedToUserId(1L)
                .assignedToEmail("sanika@test.com")
                .priority("Medium").status("To Do")
                .build();

        when(projectTaskRepository.findById(5L)).thenReturn(Optional.of(task));
        when(projectTaskRepository.save(any(ProjectTask.class))).thenAnswer(inv -> inv.getArgument(0));
        when(projectTaskRepository.findByProjectIdOrderByIdAsc(100L)).thenReturn(List.of(task));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<ApiResponse<ProjectTaskResponse>> response =
                projectController.updateTaskStatus(5L, "In Progress", userDetails);

        assertTrue(response.getBody().isSuccess());
        assertEquals("In Progress", response.getBody().getData().getStatus());
    }

    @Test
    @DisplayName("PUT /api/projects/tasks/{id}/status - Unassigned non-leader gets 403")
    void testUpdateTaskStatus_Forbidden() {
        User otherUser = User.builder().id(99L).email("other@test.com")
                .firstName("Other").lastName("User").build();
        Team otherTeam = Team.builder().id(20L).leader(otherUser).build();
        Project otherProject = Project.builder().id(200L).user(otherUser).team(otherTeam).build();

        ProjectTask task = ProjectTask.builder()
                .id(6L).project(otherProject).title("Some Task")
                .assignedTo("Other User").assignedToUserId(99L)
                .priority("Low").status("To Do")
                .build();

        when(projectTaskRepository.findById(6L)).thenReturn(Optional.of(task));

        ResponseEntity<ApiResponse<ProjectTaskResponse>> response =
                projectController.updateTaskStatus(6L, "Completed", userDetails);

        assertEquals(403, response.getStatusCode().value());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    @DisplayName("DELETE /api/projects/tasks/{id} - Should delete task and update progress")
    void testDeleteTask_Success() {
        ProjectTask task = ProjectTask.builder()
                .id(7L).project(testProject).title("Review code")
                .priority("Low").status("Completed")
                .build();

        when(projectTaskRepository.findById(7L)).thenReturn(Optional.of(task));
        when(projectTaskRepository.findByProjectIdOrderByIdAsc(100L)).thenReturn(List.of());
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<ApiResponse<Void>> response =
                projectController.deleteTask(7L, userDetails);

        assertTrue(response.getBody().isSuccess());
        verify(projectTaskRepository).delete(task);
    }

    @Test
    @DisplayName("POST /api/projects/generate - Should generate and persist project idea")
    void testGenerateIdea_Success() {
        when(projectIdeaRepository.save(any(ProjectIdea.class))).thenAnswer(inv -> {
            ProjectIdea idea = inv.getArgument(0);
            idea.setId(50L);
            return idea;
        });

        ResponseEntity<ApiResponse<ProjectIdeaResponse>> response =
                projectController.generateIdea(null, userDetails);

        assertTrue(response.getBody().isSuccess());
        assertEquals("Project idea generated", response.getBody().getMessage());
        assertEquals("AI Study Assistant", response.getBody().getData().getTitle());
        assertEquals("AI/ML", response.getBody().getData().getCategory());
        assertEquals(92, response.getBody().getData().getAiConfidenceScore());

        verify(activityService).logActivity(eq(testUser), eq("IDEA"), anyString(), anyString(), eq("PROJECT_IDEA"));
    }

    @Test
    @DisplayName("GET /api/projects/recommendations - Should return saved project ideas")
    void testGetRecommendations_Success() {
        ProjectIdea idea = ProjectIdea.builder()
                .id(51L).title("Smart Traffic System").description("AI-powered traffic")
                .techStack("Python, OpenCV").problemStatement("Traffic congestion")
                .category("AI/ML").aiConfidenceScore(88).generatedFor(testUser)
                .generatedAt(LocalDateTime.now())
                .build();

        when(projectIdeaRepository.findByGeneratedForOrderByGeneratedAtDesc(testUser))
                .thenReturn(List.of(idea));

        ResponseEntity<ApiResponse<List<ProjectIdeaResponse>>> response =
                projectController.getRecommendations(userDetails);

        assertTrue(response.getBody().isSuccess());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("Smart Traffic System", response.getBody().getData().get(0).getTitle());
    }

    @Test
    @DisplayName("Project response maps technologies string to list correctly")
    void testProjectResponse_TechnologiesParsing() {
        when(teamRepository.findTeamsByMember(testUser)).thenReturn(List.of());
        when(projectRepository.findByUserOrderByCreatedAtDesc(testUser))
                .thenReturn(List.of(testProject));

        ResponseEntity<ApiResponse<List<ProjectResponse>>> response =
                projectController.getAllProjects(userDetails);

        List<String> techs = response.getBody().getData().get(0).getTechnologies();
        assertEquals(3, techs.size());
        assertTrue(techs.contains("React"));
        assertTrue(techs.contains("Spring Boot"));
        assertTrue(techs.contains("Python"));
    }
}
