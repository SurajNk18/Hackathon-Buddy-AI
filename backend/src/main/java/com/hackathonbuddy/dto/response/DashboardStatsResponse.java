package com.hackathonbuddy.dto.response;

import lombok.*;

@Data
@Builder
public class DashboardStatsResponse {
    private HackathonStats hackathons;
    private TeamStats teams;
    private ProjectIdeaStats projectIdeas;
    private SkillMatchStats skillMatch;

    public DashboardStatsResponse() {
    }

    public DashboardStatsResponse(HackathonStats hackathons, TeamStats teams,
                                  ProjectIdeaStats projectIdeas, SkillMatchStats skillMatch) {
        this.hackathons = hackathons;
        this.teams = teams;
        this.projectIdeas = projectIdeas;
        this.skillMatch = skillMatch;
    }

    @Data @Builder
    public static class HackathonStats {
        private int value;
        private int registered;

        public HackathonStats() {
        }

        public HackathonStats(int value, int registered) {
            this.value = value;
            this.registered = registered;
        }
    }

    @Data @Builder
    public static class TeamStats {
        private int value;
        private int active;

        public TeamStats() {
        }

        public TeamStats(int value, int active) {
            this.value = value;
            this.active = active;
        }
    }

    @Data @Builder
    public static class ProjectIdeaStats {
        private int value;
        private int generated;

        public ProjectIdeaStats() {
        }

        public ProjectIdeaStats(int value, int generated) {
            this.value = value;
            this.generated = generated;
        }
    }

    @Data @Builder
    public static class SkillMatchStats {
        private int value;
        private String label;

        public SkillMatchStats() {
        }

        public SkillMatchStats(int value, String label) {
            this.value = value;
            this.label = label;
        }
    }
}
