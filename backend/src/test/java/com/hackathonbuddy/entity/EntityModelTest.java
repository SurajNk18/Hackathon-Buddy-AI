package com.hackathonbuddy.entity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Entity model classes — verifying builder patterns,
 * default values, and @PrePersist lifecycle callbacks.
 */
class EntityModelTest {

    // ─── User Entity ────────────────────────────────────────────

    @Test
    @DisplayName("User builder creates entity with all fields")
    void testUserBuilder() {
        Role role = Role.builder().id(1L).name("ROLE_STUDENT").build();
        User user = User.builder()
                .id(1L)
                .firstName("Sanika")
                .lastName("Patil")
                .email("sanika@hackathonbuddy.com")
                .password("hashed_password")
                .phone("9876543210")
                .role(role)
                .primaryRole("Full Stack Developer")
                .location("Mumbai")
                .bio("Hackathon enthusiast")
                .githubUrl("https://github.com/sanika")
                .linkedinUrl("https://linkedin.com/in/sanika")
                .build();

        assertEquals("Sanika", user.getFirstName());
        assertEquals("Patil", user.getLastName());
        assertEquals("sanika@hackathonbuddy.com", user.getEmail());
        assertEquals("ROLE_STUDENT", user.getRole().getName());
        assertTrue(user.getIsActive());
        assertFalse(user.getProfileComplete());
    }

    @Test
    @DisplayName("User default values are set correctly")
    void testUserDefaults() {
        User user = User.builder()
                .firstName("Test").lastName("User")
                .email("test@test.com").password("pass")
                .build();

        assertTrue(user.getIsActive());
        assertFalse(user.getProfileComplete());
    }

    // ─── Project Entity ─────────────────────────────────────────

    @Test
    @DisplayName("Project builder with default values")
    void testProjectBuilderDefaults() {
        Project project = Project.builder()
                .name("AI Assistant")
                .domain("AI / ML")
                .build();

        assertEquals("AI Assistant", project.getName());
        assertEquals("Active", project.getStatus());
        assertEquals(1, project.getMembers());
        assertEquals(0, project.getProgress());
    }

    @Test
    @DisplayName("Project @PrePersist sets defaults for null fields")
    void testProjectPrePersist() {
        Project project = new Project();
        project.setName("Test Project");

        project.onCreate();

        assertNotNull(project.getCreatedAt());
        assertEquals("Active", project.getStatus());
        assertEquals(1, project.getMembers());
        assertEquals(0, project.getProgress());
    }

    @Test
    @DisplayName("Project builder with custom values overrides defaults")
    void testProjectCustomValues() {
        Project project = Project.builder()
                .name("Finished Project")
                .status("Completed")
                .members(5)
                .progress(100)
                .build();

        assertEquals("Completed", project.getStatus());
        assertEquals(5, project.getMembers());
        assertEquals(100, project.getProgress());
    }

    // ─── Team Entity ────────────────────────────────────────────

    @Test
    @DisplayName("Team builder creates entity with members list")
    void testTeamBuilder() {
        User leader = User.builder().id(1L).firstName("Sanika").lastName("Patil")
                .email("sanika@test.com").password("pw").build();

        Team team = Team.builder()
                .id(10L)
                .name("Hackathon Heroes")
                .description("A competitive hackathon team")
                .leader(leader)
                .build();

        assertEquals("Hackathon Heroes", team.getName());
        assertEquals(leader, team.getLeader());
        assertTrue(team.getIsActive());
        assertNotNull(team.getMembers());
        assertTrue(team.getMembers().isEmpty());
    }

    // ─── Hackathon Entity ───────────────────────────────────────

    @Test
    @DisplayName("Hackathon builder with default status and participantCount")
    void testHackathonBuilder() {
        Hackathon hackathon = Hackathon.builder()
                .id(1L)
                .title("Global AI Challenge")
                .description("Build innovative AI solutions")
                .category("AI/ML")
                .organizer("Google")
                .startDate(LocalDate.of(2026, 11, 1))
                .endDate(LocalDate.of(2026, 11, 3))
                .prizePool("$50,000")
                .mode("ONLINE")
                .maxTeamSize(5)
                .minTeamSize(2)
                .level("Intermediate")
                .build();

        assertEquals("Global AI Challenge", hackathon.getTitle());
        assertEquals("Open", hackathon.getStatus());
        assertEquals(0, hackathon.getParticipantCount());
        assertTrue(hackathon.getIsActive());
        assertEquals("ONLINE", hackathon.getMode());
    }

    // ─── Notification Entity ────────────────────────────────────

    @Test
    @DisplayName("Notification builder with default isRead = false")
    void testNotificationBuilder() {
        User user = User.builder().id(1L).firstName("Sanika").lastName("Patil")
                .email("sanika@test.com").password("pw").build();

        Notification notification = Notification.builder()
                .id(1L)
                .user(user)
                .title("Team Invite")
                .message("You have been invited to join Team Alpha")
                .type("team")
                .icon("group")
                .action("INVITE")
                .route("/teams")
                .targetTeamId(10L)
                .targetUserId(2L)
                .responseStatus("PENDING")
                .build();

        assertEquals("Team Invite", notification.getTitle());
        assertFalse(notification.getIsRead());
        assertEquals("PENDING", notification.getResponseStatus());
        assertEquals(10L, notification.getTargetTeamId());
    }

    // ─── ProjectTask Entity ─────────────────────────────────────

    @Test
    @DisplayName("ProjectTask builder with default priority and status")
    void testProjectTaskDefaults() {
        ProjectTask task = ProjectTask.builder()
                .title("Design UI wireframes")
                .description("Create mockups for main pages")
                .assignedTo("Sanika")
                .build();

        assertEquals("Design UI wireframes", task.getTitle());
        assertEquals("Medium", task.getPriority());
        assertEquals("To Do", task.getStatus());
    }

    @Test
    @DisplayName("ProjectTask @PrePersist sets defaults for null fields")
    void testProjectTaskPrePersist() {
        ProjectTask task = new ProjectTask();
        task.setTitle("Test Task");

        task.onCreate();

        assertNotNull(task.getCreatedAt());
        assertEquals("Medium", task.getPriority());
        assertEquals("To Do", task.getStatus());
    }

    // ─── ProjectIdea Entity ─────────────────────────────────────

    @Test
    @DisplayName("ProjectIdea builder constructs all fields")
    void testProjectIdeaBuilder() {
        User user = User.builder().id(1L).firstName("Sanika").lastName("Patil")
                .email("sanika@test.com").password("pw").build();

        ProjectIdea idea = ProjectIdea.builder()
                .id(1L)
                .title("Smart Campus Navigator")
                .description("Navigate campus buildings with AI")
                .techStack("React Native, TensorFlow, Firebase")
                .problemStatement("Students struggle to find rooms in large campuses")
                .category("AI/ML")
                .aiConfidenceScore(95)
                .generatedFor(user)
                .build();

        assertEquals("Smart Campus Navigator", idea.getTitle());
        assertEquals(95, idea.getAiConfidenceScore());
        assertEquals(user, idea.getGeneratedFor());
    }

    // ─── ChatMessage Entity ─────────────────────────────────────

    @Test
    @DisplayName("ChatMessage builder constructs correctly")
    void testChatMessageBuilder() {
        ChatMessage message = ChatMessage.builder()
                .id(1L)
                .conversationId(10L)
                .senderId(1L)
                .content("Hello team! Let's discuss our hackathon strategy.")
                .messageType("USER")
                .timestamp(LocalDateTime.now())
                .build();

        assertEquals("USER", message.getMessageType());
        assertEquals(10L, message.getConversationId());
        assertTrue(message.getContent().contains("hackathon strategy"));
    }

    // ─── Role Entity ────────────────────────────────────────────

    @Test
    @DisplayName("Role builder creates role with id and name")
    void testRoleBuilder() {
        Role role = Role.builder().id(1L).name("ROLE_STUDENT").build();
        assertEquals("ROLE_STUDENT", role.getName());

        Role admin = Role.builder().id(2L).name("ROLE_ADMIN").build();
        assertEquals("ROLE_ADMIN", admin.getName());

        Role superAdmin = Role.builder().id(3L).name("ROLE_SUPER_ADMIN").build();
        assertEquals("ROLE_SUPER_ADMIN", superAdmin.getName());
    }

    // ─── Skill Entity ───────────────────────────────────────────

    @Test
    @DisplayName("Skill builder creates skill")
    void testSkillBuilder() {
        Skill skill = Skill.builder().id(1L).name("React").build();
        assertEquals("React", skill.getName());
        assertEquals(1L, skill.getId());
    }
}
