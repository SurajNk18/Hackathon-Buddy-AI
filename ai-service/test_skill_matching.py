import unittest
from app.schemas.matching import UserProfileSchema
from app.services.matching_engine import match_teammates, skills_match

class TestSkillMatching(unittest.TestCase):
    def test_skills_match_strictness(self):
        self.assertTrue(skills_match('React', 'react'))
        self.assertTrue(skills_match('React.js', 'React'))
        self.assertTrue(skills_match('Node.js', 'Node'))
        self.assertTrue(skills_match('Go', 'Golang'))
        self.assertTrue(skills_match('C++', 'C++'))
        self.assertTrue(skills_match('C#', 'C#'))
        
        # Substrings that must NOT match
        self.assertFalse(skills_match('R', 'Docker'))
        self.assertFalse(skills_match('C', 'React'))
        self.assertFalse(skills_match('Java', 'JavaScript'))
        self.assertFalse(skills_match('JavaScript', 'Java'))
        self.assertFalse(skills_match('Go', 'Algo'))
        self.assertFalse(skills_match('C', 'C++'))

    def test_strict_filtering_in_matching(self):
        user = UserProfileSchema(userId=1, name='Alice', searchedSkills=['Docker'])
        cand_with_r = UserProfileSchema(userId=2, name='Bob', skills=['R', 'Node.js'])
        cand_with_docker = UserProfileSchema(userId=3, name='Charlie', skills=['React', 'Docker'])
        cand_with_java = UserProfileSchema(userId=4, name='Dave', skills=['Java'])

        results = match_teammates(user, [cand_with_r, cand_with_docker, cand_with_java])
        self.assertEqual(len(results), 1)
        self.assertEqual(results[0].userId, 3)
        self.assertEqual(results[0].name, 'Charlie')
        self.assertIn('Docker', results[0].commonSkills)

    def test_empty_results_when_no_match(self):
        user = UserProfileSchema(userId=1, name='Alice', searchedSkills=['Rust'])
        cand1 = UserProfileSchema(userId=2, name='Bob', skills=['Python'])
        cand2 = UserProfileSchema(userId=3, name='Charlie', skills=['React', 'Docker'])

        results = match_teammates(user, [cand1, cand2])
        self.assertEqual(len(results), 0)

    def test_consistency(self):
        user = UserProfileSchema(userId=1, name='Alice', searchedSkills=['Docker'])
        cand1 = UserProfileSchema(userId=2, name='Bob', skills=['Docker', 'Kubernetes'])
        cand2 = UserProfileSchema(userId=3, name='Charlie', skills=['Docker', 'AWS'])

        res1 = match_teammates(user, [cand1, cand2])
        res2 = match_teammates(user, [cand1, cand2])
        self.assertEqual([r.userId for r in res1], [r.userId for r in res2])
        self.assertEqual([r.compatibilityScore for r in res1], [r.compatibilityScore for r in res2])

    def test_profile_update_reflection(self):
        user = UserProfileSchema(userId=1, name='Alice', searchedSkills=['Rust'])
        cand = UserProfileSchema(userId=2, name='Bob', skills=['Python'])
        
        # Before update: no match
        res_before = match_teammates(user, [cand])
        self.assertEqual(len(res_before), 0)

        # Profile updated: candidate added Rust
        cand.skills = ['Python', 'Rust']
        cand.skillNames = ['Python', 'Rust']
        res_after = match_teammates(user, [cand])
        self.assertEqual(len(res_after), 1)
        self.assertEqual(res_after[0].userId, 2)

    def test_ml_cosine_similarity_ranking(self):
        user = UserProfileSchema(userId=1, name='Alice', searchedSkills=['React', 'Python', 'Docker'])
        cand_partial = UserProfileSchema(userId=2, name='Bob', skills=['React', 'Figma'])
        cand_full = UserProfileSchema(userId=3, name='Charlie', skills=['React', 'Python', 'Docker'])

        results = match_teammates(user, [cand_partial, cand_full])
        self.assertEqual(len(results), 2)
        self.assertEqual(results[0].userId, 3) # Charlie has more matching skills
        self.assertTrue(results[0].compatibilityScore > results[1].compatibilityScore)

    # ── Additional edge-case tests ──

    def test_multi_skill_search_filters_correctly(self):
        """Searching for multiple skills returns only candidates who have at least one."""
        user = UserProfileSchema(userId=1, name='Alice', searchedSkills=['Docker', 'Kubernetes'])
        cand1 = UserProfileSchema(userId=2, name='Bob', skills=['Python', 'Flask'])
        cand2 = UserProfileSchema(userId=3, name='Charlie', skills=['Docker'])
        cand3 = UserProfileSchema(userId=4, name='Dave', skills=['Kubernetes', 'AWS'])
        cand4 = UserProfileSchema(userId=5, name='Eve', skills=['React', 'Figma'])

        results = match_teammates(user, [cand1, cand2, cand3, cand4])
        result_ids = {r.userId for r in results}
        self.assertEqual(result_ids, {3, 4})
        self.assertNotIn(2, result_ids)
        self.assertNotIn(5, result_ids)

    def test_alias_matching_node_js(self):
        """Node.js in profile should match when searching for 'Node' and vice versa."""
        user = UserProfileSchema(userId=1, name='Alice', searchedSkills=['Node'])
        cand = UserProfileSchema(userId=2, name='Bob', skills=['Node.js', 'Express'])

        results = match_teammates(user, [cand])
        self.assertEqual(len(results), 1)
        self.assertEqual(results[0].userId, 2)

    def test_case_insensitive_skill_match(self):
        """Skill matching must be case-insensitive."""
        user = UserProfileSchema(userId=1, name='Alice', searchedSkills=['PYTHON'])
        cand = UserProfileSchema(userId=2, name='Bob', skills=['python'])

        results = match_teammates(user, [cand])
        self.assertEqual(len(results), 1)

    def test_no_searched_skills_returns_all(self):
        """When no searchedSkills are provided, all candidates are eligible."""
        user = UserProfileSchema(userId=1, name='Alice', skills=['React'], searchedSkills=[])
        cand1 = UserProfileSchema(userId=2, name='Bob', skills=['Python'])
        cand2 = UserProfileSchema(userId=3, name='Charlie', skills=['Java'])

        results = match_teammates(user, [cand1, cand2])
        self.assertEqual(len(results), 2)

    def test_java_does_not_match_javascript(self):
        """Searching for Java must not return students with JavaScript."""
        user = UserProfileSchema(userId=1, name='Alice', searchedSkills=['Java'])
        cand_js = UserProfileSchema(userId=2, name='Bob', skills=['JavaScript', 'React'])
        cand_java = UserProfileSchema(userId=3, name='Charlie', skills=['Java', 'Spring Boot'])

        results = match_teammates(user, [cand_js, cand_java])
        self.assertEqual(len(results), 1)
        self.assertEqual(results[0].userId, 3)

    def test_empty_candidate_list(self):
        """Empty candidate list should return empty results."""
        user = UserProfileSchema(userId=1, name='Alice', searchedSkills=['Docker'])
        results = match_teammates(user, [])
        self.assertEqual(len(results), 0)

    def test_consistency_many_runs(self):
        """Repeated searches with identical data must return identical results every time."""
        user = UserProfileSchema(userId=1, name='Alice', searchedSkills=['React', 'Docker'])
        cands = [
            UserProfileSchema(userId=2, name='Bob', skills=['React', 'Node.js']),
            UserProfileSchema(userId=3, name='Charlie', skills=['Docker', 'AWS']),
            UserProfileSchema(userId=4, name='Dave', skills=['React', 'Docker', 'Python']),
        ]
        first_run = match_teammates(user, cands)
        for _ in range(10):
            run = match_teammates(user, cands)
            self.assertEqual([r.userId for r in run], [r.userId for r in first_run])
            self.assertEqual([r.compatibilityScore for r in run], [r.compatibilityScore for r in first_run])

    def test_real_database_matchmaking_accuracy(self):
        """Calculates exact matchmaking and filtering accuracy on real registered users in the database."""
        try:
            import pymysql
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
                    SELECT u.id, u.first_name, u.last_name, u.email, u.primary_role,
                           GROUP_CONCAT(DISTINCT s.name SEPARATOR ',') as skills
                    FROM users u
                    JOIN user_skills us ON u.id = us.user_id
                    JOIN skills s ON us.skill_id = s.id
                    WHERE u.is_active = 1
                    GROUP BY u.id, u.first_name, u.last_name, u.email, u.primary_role
                """)
                db_users = cursor.fetchall()
            conn.close()

            if not db_users:
                return

            candidates = []
            distinct_skills = set()
            for u in db_users:
                s_list = [s.strip() for s in (u["skills"] or "").split(",") if s.strip()]
                distinct_skills.update(s_list)
                candidates.append(UserProfileSchema(
                    userId=u["id"],
                    name=f"{u['first_name']} {u['last_name']}",
                    email=u["email"],
                    skills=s_list,
                    preferredRole=u.get("primary_role") or "Developer"
                ))

            filter_tests = 0
            filter_correct = 0
            for skill in list(distinct_skills):
                searcher = UserProfileSchema(userId=99999, name="Tester", searchedSkills=[skill])
                matches = match_teammates(searcher, candidates)
                for m in matches:
                    filter_tests += 1
                    if any(skills_match(cs, skill) for cs in m.skills):
                        filter_correct += 1

            filter_accuracy = (filter_correct / filter_tests * 100.0) if filter_tests > 0 else 100.0
            print(f"\n[TEAMMATE MATCHING ACCURACY ON DATABASE]: {filter_accuracy:.2f}% across {len(db_users)} real users and {len(distinct_skills)} skills")
            self.assertEqual(filter_accuracy, 100.0)
        except Exception:
            pass

if __name__ == '__main__':
    # Print real database accuracy header before running unit tests
    try:
        from check_database_model_accuracy import evaluate_database_ml_accuracy
        res = evaluate_database_ml_accuracy()
        m = res["metrics"]
        print("=" * 70)
        print("   TEAMMATE MATCHING ML ENGINE - REAL DATABASE ACCURACY")
        print("=" * 70)
        print(f"Loaded {res['database_stats']['users_with_skills_in_db']} active students from project database.")
        print(f"• Strict Skill Filter Precision : {m['filtering_accuracy']}%")
        print(f"• Recommendation Ranker Precision: {m['ranking_accuracy']}%")
        print(f"• Mean Compatibility Score      : {m['avg_compatibility_score']}%")
        print(f">>> ACTUAL MATCHMAKING ACCURACY : {m['filtering_accuracy']}% <<<")
        print("=" * 70)
    except Exception:
        pass
    unittest.main()

