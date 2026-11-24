import math
from typing import List, Dict, Any
from app.schemas.matching import UserProfileSchema, TeammateMatchResult
from app.services.ml_matcher import compute_tfidf_similarity

def calculate_jaccard_similarity(list1: List[str], list2: List[str]) -> float:
    set1 = set(list1)
    set2 = set(list2)
    if not set1 and not set2:
        return 0.0
    intersection = set1.intersection(set2)
    union = set1.union(set2)
    return len(intersection) / len(union)

def get_clean_skills(profile: UserProfileSchema) -> List[str]:
    skills_list = []
    if profile.skillNames:
        skills_list.extend(profile.skillNames)
    if profile.skills:
        for s in profile.skills:
            if isinstance(s, str) and s not in skills_list:
                skills_list.append(s)
            elif isinstance(s, dict) and "name" in s and s["name"] not in skills_list:
                skills_list.append(s["name"])
            elif hasattr(s, "name") and s.name not in skills_list:
                skills_list.append(s.name)
    return skills_list

def score_skills(user: UserProfileSchema, candidate: UserProfileSchema) -> dict:
    user_skill_list = get_clean_skills(user)
    cand_skill_list = get_clean_skills(candidate)
    user_skills = set([s.lower() for s in user_skill_list])
    candidate_skills = set([s.lower() for s in cand_skill_list])
    
    if not user_skills and not candidate_skills:
        return {"score": 0.0, "common": [], "complementary": []}
        
    common = user_skills.intersection(candidate_skills)
    complementary = candidate_skills - user_skills
    
    # We want some overlap for communication, but mostly complementary skills for a team
    overlap_ratio = len(common) / max(len(user_skills), 1)
    
    # Score favors candidates who have skills the user lacks, but penalizes if there's ZERO overlap or TOO MUCH overlap
    if overlap_ratio == 0:
        base_score = 0.4 # Need some common ground
    elif overlap_ratio > 0.8:
        base_score = 0.3 # Too similar, need diversity
    else:
        base_score = 1.0 # Good mix
        
    complementary_score = min(len(complementary) / 3, 1.0) # Cap at 3 complementary skills
    
    final_score = (base_score * 0.4) + (complementary_score * 0.6)
    
    # Return original case names
    common_orig = [s for s in cand_skill_list if s.lower() in common]
    complementary_orig = [s for s in cand_skill_list if s.lower() in complementary]
    
    return {
        "score": final_score,
        "common": common_orig,
        "complementary": complementary_orig
    }

def score_interests(user: UserProfileSchema, candidate: UserProfileSchema) -> float:
    user_int = [i.lower() for i in user.interests]
    cand_int = [i.lower() for i in candidate.interests]
    
    if not user_int or not cand_int:
        return 0.5 # Neutral if not specified
        
    similarity = calculate_jaccard_similarity(user_int, cand_int)
    # Even a small overlap in interests is great for team bonding
    return min(similarity * 2, 1.0) 

def score_role(user: UserProfileSchema, candidate: UserProfileSchema) -> float:
    user_role = user.preferredRole
    cand_role = candidate.preferredRole
    
    if not user_role or not cand_role:
        return 0.5
        
    user_role = user_role.lower()
    cand_role = cand_role.lower()
    
    # If they want the exact same role, it might cause conflicts (e.g., two frontend devs)
    if user_role == cand_role:
        return 0.2
        
    # Standard complementary roles
    complementary_pairs = [
        ({"frontend", "ui", "ux"}, {"backend", "api", "database", "server"}),
        ({"machine learning", "data", "ai"}, {"backend", "frontend", "fullstack"}),
        ({"design", "product"}, {"frontend", "backend", "fullstack", "developer"})
    ]
    
    for pair1, pair2 in complementary_pairs:
        u_in_1 = any(r in user_role for r in pair1)
        u_in_2 = any(r in user_role for r in pair2)
        c_in_1 = any(r in cand_role for r in pair1)
        c_in_2 = any(r in cand_role for r in pair2)
        
        if (u_in_1 and c_in_2) or (u_in_2 and c_in_1):
            return 1.0 # Perfect match
            
    return 0.7 # Different roles, generally good

def score_experience(user: UserProfileSchema, candidate: UserProfileSchema) -> float:
    user_exp = user.yearsOfExperience or 0
    cand_exp = candidate.yearsOfExperience or 0
    
    # We want people roughly in the same experience bracket to avoid massive imbalances,
    # but a slight difference (mentorship) is fine
    diff = abs(user_exp - cand_exp)
    if diff <= 1:
        return 1.0
    elif diff <= 3:
        return 0.7
    else:
        return 0.4

def generate_reason(skill_data: dict, role_score: float, cand_role: str) -> str:
    reasons = []
    
    if role_score > 0.8 and cand_role:
        reasons.append(f"Their role ({cand_role}) perfectly complements yours.")
        
    comp_skills = skill_data["complementary"]
    if comp_skills:
        skill_str = ", ".join(comp_skills[:2])
        if len(comp_skills) > 2:
            skill_str += " and others"
        reasons.append(f"They bring new skills like {skill_str}.")
        
    if not reasons:
        reasons.append("Good overall profile match.")
        
    return " ".join(reasons)

def normalize_skill(skill: str) -> str:
    return "".join(c for c in skill.lower() if c.isalnum() or c in "+#")

def skills_match(cand_skill: str, target_skill: str) -> bool:
    if not cand_skill or not target_skill:
        return False
    c = cand_skill.strip().lower()
    t = target_skill.strip().lower()
    if not c or not t:
        return False
    if c == t:
        return True
    c_norm = normalize_skill(c)
    t_norm = normalize_skill(t)
    if c_norm == t_norm:
        return True
    if c_norm.endswith("js") and c_norm[:-2] == t_norm:
        return True
    if t_norm.endswith("js") and t_norm[:-2] == c_norm:
        return True
    if (c_norm == "go" and t_norm == "golang") or (t_norm == "go" and c_norm == "golang"):
        return True
    return False

def match_teammates(user: UserProfileSchema, candidates: List[UserProfileSchema]) -> List[TeammateMatchResult]:
    # 1. Determine searched skills
    searched = getattr(user, 'searchedSkills', None)
    if searched:
        target_skills = [s.strip() for s in searched if s and s.strip()]
    else:
        target_skills = []
    
    # 2. Strict Skill Filtering:
    # Return ONLY students whose saved profiles contain the searched skill.
    eligible_candidates = []
    for cand in candidates:
        cand_skills = get_clean_skills(cand)
        if target_skills:
            has_matching_skill = any(
                skills_match(cs, target)
                for target in target_skills
                for cs in cand_skills
            )
            if has_matching_skill:
                eligible_candidates.append(cand)
        else:
            eligible_candidates.append(cand)
            
    # If no students match the searched skill, return empty results immediately
    if not eligible_candidates:
        return []
        
    # 3. ML Cosine Similarity Ranking among eligible students:
    user_skills_str = ", ".join(target_skills) if target_skills else ", ".join(get_clean_skills(user))
    cand_skills_strs = [", ".join(get_clean_skills(c)) for c in eligible_candidates]
    
    # Compute TF-IDF cosine similarity scores
    cosine_sims = compute_tfidf_similarity(user_skills_str, cand_skills_strs)
    
    results = []
    for i, cand in enumerate(eligible_candidates):
        ml_sim = cosine_sims[i] if i < len(cosine_sims) else 0.5
        cand_skills = get_clean_skills(cand)

        if target_skills:
            common_skills = [cs for cs in cand_skills if any(skills_match(cs, t) for t in target_skills)]
            comp_skills = [cs for cs in cand_skills if not any(skills_match(cs, t) for t in target_skills)]
            skill_coverage = len(common_skills) / max(len(target_skills), 1)
            skill_score = (0.5 * ml_sim) + (0.5 * skill_coverage)
            skill_data = {
                "score": skill_score,
                "common": common_skills,
                "complementary": comp_skills
            }
        else:
            skill_data = score_skills(user, cand)
            skill_score = max(ml_sim, skill_data["score"])

        interest_score = score_interests(user, cand)
        role_score = score_role(user, cand)
        exp_score = score_experience(user, cand)
        
        # Weighted score: 50% ML skill score + 20% role + 20% interests + 10% experience
        final_score = (
            (skill_score * 0.50) +
            (role_score * 0.20) +
            (interest_score * 0.20) +
            (exp_score * 0.10)
        )
        
        # Scale to 65.0 - 99.0 for candidates meeting the skill criteria
        score_100 = round(max(65.0, min(99.0, 50.0 + (final_score * 49.0))), 1)
        reason = generate_reason(skill_data, role_score, cand.preferredRole)
        
        result = TeammateMatchResult(
            userId=cand.userId,
            name=cand.name,
            email=cand.email,
            profilePhotoUrl=cand.profilePhotoUrl,
            preferredRole=cand.preferredRole,
            bio=cand.bio,
            yearsOfExperience=cand.yearsOfExperience or 0,
            projectCount=cand.projectCount or 0,
            skills=cand_skills,
            interests=cand.interests,
            compatibilityScore=score_100,
            commonSkills=skill_data["common"],
            complementarySkills=skill_data["complementary"],
            matchReason=reason
        )
        results.append(result)
        
    # Sort deterministically: compatibility score descending, then userId ascending for tie-breaks
    results.sort(key=lambda x: (-x.compatibilityScore, x.userId))
    return results
