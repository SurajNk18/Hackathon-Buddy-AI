import unittest
from app.schemas.skills import (
    SkillGapRequest,
    TeamSkillSchema,
    TeamMemberSkillSchema,
    ProjectRequirementSchema
)
from app.services.skill_gap_analyzer import analyze_skill_gap

class TestSkillGapAnalyzer(unittest.TestCase):
    """
    Model Accuracy & Verification Tests for Skill Gap Analysis.
    Evaluates:
      - Skill Coverage Percentage calculation accuracy: (available / required) * 100
      - Semantic / Substring Skill Matching precision
      - Correct classification into available_skills vs missing_skills
      - Squad member ownership attribution
    """

    def test_full_skill_coverage_accuracy_100_percent(self):
        """When team has all required skills, coverage must be exactly 100.0%."""
        req = SkillGapRequest(
            teamSkills=["React", "Python", "Docker", "PostgreSQL"],
            requiredSkills=["React", "Python", "Docker", "PostgreSQL"]
        )
        res = analyze_skill_gap(req)
        self.assertEqual(res.skill_coverage_percentage, 100.0)
        self.assertEqual(res.total_required_skills, 4)
        self.assertEqual(res.available_skills_count, 4)
        self.assertEqual(res.missing_skills_count, 0)
        self.assertEqual(len(res.missing_skills), 0)

    def test_zero_skill_coverage_accuracy_0_percent(self):
        """When team has no matching skills, coverage must be 0.0% and all skills classified as missing."""
        req = SkillGapRequest(
            teamSkills=["Rust", "Solidity"],
            requiredSkills=["Flutter", "Swift", "Kotlin"]
        )
        res = analyze_skill_gap(req)
        self.assertEqual(res.skill_coverage_percentage, 0.0)
        self.assertEqual(res.total_required_skills, 3)
        self.assertEqual(res.available_skills_count, 0)
        self.assertEqual(res.missing_skills_count, 3)
        self.assertEqual(len(res.available_skills), 0)

    def test_partial_coverage_accuracy_calculation(self):
        """When 2 out of 4 skills are possessed, coverage must be exactly 50.0%."""
        req = SkillGapRequest(
            teamSkills=["React", "Node.js"],
            requiredSkills=["React", "Node.js", "Docker", "Kubernetes"]
        )
        res = analyze_skill_gap(req)
        self.assertEqual(res.skill_coverage_percentage, 50.0)
        self.assertEqual(res.available_skills_count, 2)
        self.assertEqual(res.missing_skills_count, 2)
        available_names = [s.required_skill for s in res.available_skills]
        missing_names = [s.required_skill for s in res.missing_skills]
        self.assertIn("React", available_names)
        self.assertIn("Node.js", available_names)
        self.assertIn("Docker", missing_names)
        self.assertIn("Kubernetes", missing_names)

    def test_team_member_ownership_attribution(self):
        """Matches must correctly attribute skills to the specific squad member who possesses them."""
        member1 = TeamMemberSkillSchema(name="Alice", skills=["Python", "FastAPI"])
        member2 = TeamMemberSkillSchema(name="Bob", skills=["React", "TypeScript"])
        team = TeamSkillSchema(team_name="AlphaSquad", members=[member1, member2])
        proj = ProjectRequirementSchema(project_title="Hackathon Platform", required_skills=["Python", "React"])

        req = SkillGapRequest(team=team, project=proj)
        res = analyze_skill_gap(req)
        self.assertEqual(res.skill_coverage_percentage, 100.0)
        
        # Check member attribution
        for matched in res.available_skills:
            if matched.required_skill == "Python":
                self.assertIn("Alice", matched.members)
            elif matched.required_skill == "React":
                self.assertIn("Bob", matched.members)

if __name__ == '__main__':
    unittest.main()
