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
import com.hackathonbuddy.repository.ProjectIdeaRepository;
import com.hackathonbuddy.repository.ProjectRepository;
import com.hackathonbuddy.repository.ProjectTaskRepository;
import com.hackathonbuddy.repository.TeamRepository;
import com.hackathonbuddy.repository.UserRepository;
import com.hackathonbuddy.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectIdeaRepository projectIdeaRepository;
    private final ProjectRepository projectRepository;
    private final ProjectTaskRepository projectTaskRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final ActivityService activityService;

    private User getUser(UserDetails ud) {
        return userRepository.findByEmail(ud.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private List<Project> getAccessibleProjects(User user) {
        List<Team> teams = teamRepository.findTeamsByMember(user);
        if (teams != null && !teams.isEmpty()) {
            return projectRepository.findProjectsForTeamsAndUser(teams, user);
        } else {
            return projectRepository.findByUserOrderByCreatedAtDesc(user);
        }
    }

    // ─── PROJECT WORKSPACE PERSISTENCE ──────────────────────────

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProjectResponse>>> getAllProjects(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        List<Project> userProjects = getAccessibleProjects(user);

        List<ProjectResponse> responses = userProjects.stream()
                .map(this::toProjectResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<ProjectResponse>>builder()
                .success(true).message("Projects retrieved").data(responses).build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProjectResponse>> createProject(
            @RequestBody CreateProjectRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);

        String techStr = "";
        if (req.getTechnologies() instanceof List<?>) {
            techStr = ((List<?>) req.getTechnologies()).stream()
                    .map(Object::toString).collect(Collectors.joining(", "));
        } else if (req.getTechnologies() != null) {
            techStr = req.getTechnologies().toString();
        }

        Team userTeam = teamRepository.findFirstTeamForUser(user).orElse(null);
        int memberCount = 1;
        if (userTeam != null && userTeam.getMembers() != null && !userTeam.getMembers().isEmpty()) {
            memberCount = (int) userTeam.getMembers().stream()
                    .filter(m -> !"REJECTED".equalsIgnoreCase(m.getStatus()))
                    .count();
            if (memberCount == 0) memberCount = 1;
        }

        Project project = Project.builder()
                .name(req.getName() != null ? req.getName().trim() : "Untitled Project")
                .domain(req.getDomain() != null ? req.getDomain().trim() : "AI / ML")
                .description(req.getDescription() != null ? req.getDescription().trim() : "")
                .technologies(techStr)
                .status("Active")
                .members(memberCount)
                .progress(0)
                .user(user)
                .team(userTeam)
                .createdAt(LocalDateTime.now())
                .build();
        project = projectRepository.save(project);

        activityService.logActivity(user, "PROJECT",
                "Created project workspace: " + project.getName(),
                project.getId().toString(), "PROJECT");

        return ResponseEntity.ok(ApiResponse.<ProjectResponse>builder()
                .success(true).message("Project created successfully").data(toProjectResponse(project)).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found"));
        projectTaskRepository.deleteByProject(project);
        projectRepository.delete(project);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Project deleted successfully").build());
    }

    // ─── TASKS PERSISTENCE ──────────────────────────────────────

    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<List<ProjectTaskResponse>>> getTasks(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        List<Project> projects = getAccessibleProjects(user);
        
        List<ProjectTask> tasks = projects.isEmpty()
                ? new ArrayList<>()
                : projectTaskRepository.findByProjectInOrderByIdAsc(projects);

        List<ProjectTaskResponse> responses = tasks.stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<ProjectTaskResponse>>builder()
                .success(true).message("Tasks retrieved").data(responses).build());
    }

    @PostMapping("/tasks")
    public ResponseEntity<ApiResponse<ProjectTaskResponse>> createTask(
            @RequestBody CreateTaskRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        Project project = projectRepository.findById(req.getProjectId())
                .orElseThrow(() -> new RuntimeException("Project not found"));

        List<Project> accessible = getAccessibleProjects(user);
        boolean hasAccess = accessible.stream().anyMatch(p -> p.getId().equals(project.getId()));
        if (!hasAccess) {
            return ResponseEntity.status(403).body(ApiResponse.<ProjectTaskResponse>builder()
                    .success(false).message("Unauthorized to add tasks to this project").build());
        }

        String assignedTo = req.getAssignedTo() != null ? req.getAssignedTo().trim() : user.getFirstName();
        Long assignedUserId = req.getAssignedToUserId();
        String assignedEmail = req.getAssignedToEmail();

        if (assignedUserId == null || assignedEmail == null) {
            Optional<User> matchedUser = userRepository.findAll().stream()
                    .filter(u -> {
                        String fullName = (u.getFirstName() + " " + u.getLastName()).trim();
                        return fullName.equalsIgnoreCase(assignedTo) ||
                                u.getFirstName().equalsIgnoreCase(assignedTo) ||
                                u.getEmail().equalsIgnoreCase(assignedTo);
                    })
                    .findFirst();
            if (matchedUser.isPresent()) {
                if (assignedUserId == null) assignedUserId = matchedUser.get().getId();
                if (assignedEmail == null) assignedEmail = matchedUser.get().getEmail();
            }
        }

        ProjectTask task = ProjectTask.builder()
                .project(project)
                .title(req.getTitle() != null ? req.getTitle().trim() : "Untitled Task")
                .description(req.getDescription() != null ? req.getDescription().trim() : "")
                .assignedTo(assignedTo)
                .assignedToUserId(assignedUserId)
                .assignedToEmail(assignedEmail)
                .priority(req.getPriority() != null ? req.getPriority().trim() : "Medium")
                .status("To Do")
                .createdAt(LocalDateTime.now())
                .build();
        task = projectTaskRepository.save(task);

        updateProjectProgress(project);

        return ResponseEntity.ok(ApiResponse.<ProjectTaskResponse>builder()
                .success(true).message("Task added successfully").data(toTaskResponse(task)).build());
    }

    @PutMapping("/tasks/{taskId}/status")
    public ResponseEntity<ApiResponse<ProjectTaskResponse>> updateTaskStatus(
            @PathVariable Long taskId,
            @RequestParam String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        Project project = task.getProject();

        boolean isAssigned = (task.getAssignedToUserId() != null && task.getAssignedToUserId().equals(user.getId()))
                || (task.getAssignedToEmail() != null && task.getAssignedToEmail().equalsIgnoreCase(user.getEmail()))
                || (task.getAssignedTo() != null && (
                        task.getAssignedTo().equalsIgnoreCase((user.getFirstName() + " " + user.getLastName()).trim()) ||
                        task.getAssignedTo().equalsIgnoreCase(user.getFirstName()) ||
                        task.getAssignedTo().equalsIgnoreCase(user.getEmail())
                   ));

        boolean isLeader = (project.getUser() != null && project.getUser().getId().equals(user.getId()))
                || (project.getTeam() != null && project.getTeam().getLeader() != null && project.getTeam().getLeader().getId().equals(user.getId()));

        if (!isAssigned && !isLeader) {
            return ResponseEntity.status(403).body(ApiResponse.<ProjectTaskResponse>builder()
                    .success(false).message("Only the assigned team member can update the status of this work").build());
        }

        task.setStatus(status);
        task = projectTaskRepository.save(task);

        updateProjectProgress(task.getProject());

        return ResponseEntity.ok(ApiResponse.<ProjectTaskResponse>builder()
                .success(true).message("Task status updated").data(toTaskResponse(task)).build());
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long taskId,
            @AuthenticationPrincipal UserDetails userDetails) {
        ProjectTask task = projectTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        Project project = task.getProject();
        projectTaskRepository.delete(task);

        updateProjectProgress(project);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true).message("Task deleted successfully").build());
    }

    private void updateProjectProgress(Project project) {
        List<ProjectTask> tasks = projectTaskRepository.findByProjectIdOrderByIdAsc(project.getId());
        if (tasks.isEmpty()) {
            project.setProgress(0);
        } else {
            long completed = tasks.stream().filter(t -> "Completed".equalsIgnoreCase(t.getStatus())).count();
            int pct = (int) Math.round(((double) completed / tasks.size()) * 100);
            project.setProgress(pct);
            if (pct >= 100) {
                project.setStatus("Completed");
            } else {
                project.setStatus("Active");
            }
        }
        projectRepository.save(project);
    }

    // ─── LEGACY RECOMMENDATIONS & GENERATE ──────────────────────

    @GetMapping("/recommendations")
    public ResponseEntity<ApiResponse<List<ProjectIdeaResponse>>> getRecommendations(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        List<ProjectIdeaResponse> ideas = projectIdeaRepository
                .findByGeneratedForOrderByGeneratedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<ProjectIdeaResponse>>builder()
                .success(true).message("Project ideas retrieved").data(ideas).build());
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<ProjectIdeaResponse>> generateIdea(
            @RequestParam(required = false) Long hackathonId,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails);
        ProjectIdea idea = ProjectIdea.builder()
                .title("AI Study Assistant")
                .description("An intelligent study companion that adapts to learning styles")
                .techStack("Python, TensorFlow, React, FastAPI")
                .problemStatement("Students struggle with personalized learning")
                .category("AI/ML")
                .aiConfidenceScore(92)
                .generatedFor(user)
                .build();
        idea = projectIdeaRepository.save(idea);
        activityService.logActivity(user, "IDEA",
                "Project idea generated: " + idea.getTitle(),
                idea.getId().toString(), "PROJECT_IDEA");
        return ResponseEntity.ok(ApiResponse.<ProjectIdeaResponse>builder()
                .success(true).message("Project idea generated").data(toResponse(idea)).build());
    }

    private ProjectResponse toProjectResponse(Project p) {
        List<String> techList = new ArrayList<>();
        if (p.getTechnologies() != null && !p.getTechnologies().isBlank()) {
            techList = Arrays.stream(p.getTechnologies().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        }

        String createdDateStr = p.getCreatedAt() != null
                ? p.getCreatedAt().toLocalDate().toString()
                : LocalDateTime.now().toLocalDate().toString();

        return ProjectResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .domain(p.getDomain())
                .status(p.getStatus() != null ? p.getStatus() : "Active")
                .description(p.getDescription())
                .technologies(techList)
                .members(p.getMembers() != null ? p.getMembers() : 1)
                .progress(p.getProgress() != null ? p.getProgress() : 0)
                .createdAt(createdDateStr)
                .teamId(p.getTeam() != null ? p.getTeam().getId() : null)
                .teamName(p.getTeam() != null ? p.getTeam().getName() : null)
                .userId(p.getUser() != null ? p.getUser().getId() : null)
                .build();
    }

    private ProjectTaskResponse toTaskResponse(ProjectTask t) {
        return ProjectTaskResponse.builder()
                .id(t.getId())
                .projectId(t.getProject() != null ? t.getProject().getId() : null)
                .title(t.getTitle())
                .description(t.getDescription())
                .assignedTo(t.getAssignedTo())
                .assignedToUserId(t.getAssignedToUserId())
                .assignedToEmail(t.getAssignedToEmail())
                .priority(t.getPriority())
                .status(t.getStatus())
                .build();
    }

    private ProjectIdeaResponse toResponse(ProjectIdea idea) {
        return ProjectIdeaResponse.builder()
                .id(idea.getId())
                .title(idea.getTitle())
                .description(idea.getDescription())
                .techStack(idea.getTechStack())
                .problemStatement(idea.getProblemStatement())
                .category(idea.getCategory())
                .aiConfidenceScore(idea.getAiConfidenceScore())
                .generatedAt(idea.getGeneratedAt() != null ? idea.getGeneratedAt().toString() : null)
                .build();
    }
}
