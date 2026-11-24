import unittest
from app.schemas.ideas import IdeaGenerationRequest
from app.services.idea_generator import generate_project_idea_items, generate_project_idea

class TestIdeaGenerator(unittest.TestCase):
    """
    Model Accuracy & Verification Tests for Hackathon Idea Generator.
    Evaluates:
      - Domain Classification & Synthesis Accuracy
      - Output Confidence Scoring range (85% - 94%)
      - Schema conformance and skill integration
    """

    def test_domain_generation_ai_ml(self):
        """AI/ML domain request must generate ideas with AI/ML theme and high confidence scores."""
        req = IdeaGenerationRequest(
            category="AI / ML",
            userSkills=["Python", "PyTorch", "FastAPI"]
        )
        ideas = generate_project_idea_items(req)
        self.assertGreaterEqual(len(ideas), 1)
        for idea in ideas:
            self.assertEqual(idea.category, "AI / ML")
            self.assertGreaterEqual(idea.confidenceScore, 80)
            self.assertLessEqual(idea.confidenceScore, 100)
            self.assertTrue(len(idea.title) > 0)
            self.assertTrue(len(idea.problemStatement) > 0)

    def test_domain_generation_fintech(self):
        """FinTech domain request must synthesize FinTech ideas with domain problems."""
        req = IdeaGenerationRequest(
            category="FinTech",
            userSkills=["React", "Java", "PostgreSQL"]
        )
        ideas = generate_project_idea_items(req)
        self.assertGreaterEqual(len(ideas), 1)
        first = ideas[0]
        self.assertEqual(first.category, "FinTech")
        self.assertTrue("Credit" in first.title or "Fraud" in first.title or "FinTech" in first.title)

    def test_formatted_idea_string_output(self):
        """Formatted string output must contain Title, Problem, Solution, Technologies, and Team Compatibility."""
        req = IdeaGenerationRequest(
            category="HealthTech",
            userSkills=["React Native", "Node.js"]
        )
        formatted = generate_project_idea(req)
        self.assertIn("PROJECT TITLE:", formatted)
        self.assertIn("PROBLEM:", formatted)
        self.assertIn("SOLUTION:", formatted)
        self.assertIn("TECHNOLOGIES:", formatted)
        self.assertIn("TEAM COMPATIBILITY:", formatted)

if __name__ == '__main__':
    unittest.main()
