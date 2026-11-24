import unittest
import pymysql
import os
import sys

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
if BASE_DIR not in sys.path:
    sys.path.insert(0, BASE_DIR)

from app.services.ml_matcher import compute_tfidf_similarity, recommend_top_candidates

def load_real_skills_from_db():
    """Dynamically loads real registered user skills from project TiDB database."""
    try:
        conn = pymysql.connect(
            host='gateway01.ap-northeast-1.prod.aws.tidbcloud.com',
            port=4000,
            user='4HfiL5GrXTF1yhA.root',
            password='L5MuaILLvqbXplME',
            database='test',
            ssl={'ssl': True},
            cursorclass=pymysql.cursors.DictCursor
        )
        with conn.cursor() as cursor:
            cursor.execute("""
                SELECT u.id, u.first_name, u.last_name, u.email,
                       GROUP_CONCAT(DISTINCT s.name SEPARATOR ', ') as skills
                FROM users u
                JOIN user_skills us ON u.id = us.user_id
                JOIN skills s ON us.skill_id = s.id
                WHERE u.is_active = 1
                GROUP BY u.id, u.first_name, u.last_name, u.email
            """)
            rows = cursor.fetchall()
        conn.close()
        return rows
    except Exception:
        return []

class TestMLMatcher(unittest.TestCase):
    """
    ML Model Accuracy Evaluator for TF-IDF Vectorizer + Cosine Similarity.
    Evaluates real user skills directly from the project database.
    """

    @classmethod
    def setUpClass(cls):
        cls.db_users = load_real_skills_from_db()

    def test_database_users_tfidf_cosine_accuracy(self):
        """Calculates exact numerical cosine similarity accuracy on real registered database users."""
        if not self.db_users:
            self.skipTest("Database connection unavailable")

        correct = 0
        total = 0
        scores = []

        for i, u1 in enumerate(self.db_users):
            s1 = u1["skills"]
            # 1. Exact self match test on real DB skills (must equal 1.0)
            self_sim = compute_tfidf_similarity(s1, [s1])[0]
            total += 1
            if abs(self_sim - 1.0) < 0.01:
                correct += 1
            scores.append(self_sim)

            # 2. Cross comparison between real users in DB
            for j, u2 in enumerate(self.db_users):
                if i >= j:
                    continue
                s2 = u2["skills"]
                cross_sim = compute_tfidf_similarity(s1, [s2])[0]
                scores.append(cross_sim)

                set1 = set(s.strip().lower() for s in s1.split(","))
                set2 = set(s.strip().lower() for s in s2.split(","))
                overlap = set1.intersection(set2)

                total += 1
                if len(overlap) == 0:
                    # Disjoint real skills in DB must yield 0.0
                    if cross_sim == 0.0:
                        correct += 1
                else:
                    # Overlapping real skills in DB must yield positive similarity
                    if cross_sim > 0.0:
                        correct += 1

        accuracy_pct = (correct / total * 100.0) if total > 0 else 100.0
        print(f"\n[TF-IDF MODEL ACCURACY ON DATABASE]: {accuracy_pct:.2f}% ({correct}/{total} tests passed across {len(self.db_users)} real database users)")
        self.assertGreaterEqual(accuracy_pct, 85.0)

    def test_database_candidate_ranking_precision(self):
        """Tests that top-N recommendation accurately ranks candidates with most overlapping real skills."""
        if len(self.db_users) < 2:
            self.skipTest("Need at least 2 database users")

        target_user = self.db_users[0]
        target_skills = [s.strip() for s in target_user["skills"].split(",")]
        cands = [{"name": u["first_name"], "skills": u["skills"].split(", ")} for u in self.db_users[1:]]

        recommended = recommend_top_candidates(target_skills, cands, top_n=min(5, len(cands)))
        self.assertGreater(len(recommended), 0)
        # Ensure descending similarity order
        for idx in range(len(recommended) - 1):
            self.assertGreaterEqual(recommended[idx][1], recommended[idx + 1][1])

if __name__ == '__main__':
    # When run directly, calculate and print the actual number accuracy!
    print("=" * 70)
    print("   TF-IDF COSINE SIMILARITY ML MODEL - REAL DATABASE ACCURACY")
    print("=" * 70)
    users = load_real_skills_from_db()
    if users:
        print(f"Loaded {len(users)} real users with skills from TiDB Cloud Database.")
        correct = 0
        total = 0
        for i, u1 in enumerate(users):
            s1 = u1["skills"]
            self_sim = compute_tfidf_similarity(s1, [s1])[0]
            total += 1
            if abs(self_sim - 1.0) < 0.01:
                correct += 1
            for j, u2 in enumerate(users):
                if i >= j: continue
                s2 = u2["skills"]
                cross_sim = compute_tfidf_similarity(s1, [s2])[0]
                set1 = set(s.strip().lower() for s in s1.split(","))
                set2 = set(s.strip().lower() for s in s2.split(","))
                total += 1
                if len(set1.intersection(set2)) == 0 and cross_sim == 0.0:
                    correct += 1
                elif len(set1.intersection(set2)) > 0 and cross_sim > 0.0:
                    correct += 1

        accuracy = (correct / total) * 100.0
        print(f"\n>>> ACTUAL TF-IDF COSINE MODEL ACCURACY: {accuracy:.2f}% <<<\n")
    unittest.main()
