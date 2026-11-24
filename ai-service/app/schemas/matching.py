from pydantic import BaseModel
from typing import List, Optional, Dict, Any

class SkillSchema(BaseModel):
    name: str
    category: Optional[str] = None
    proficiencyLevel: Optional[int] = None

class UserProfileSchema(BaseModel):
    userId: int
    name: str
    email: Optional[str] = None
    skills: List[Any] = []
    skillNames: List[str] = []
    searchedSkills: Optional[List[str]] = []
    interests: List[str] = []
    yearsOfExperience: Optional[int] = 0
    projectCount: Optional[int] = 0
    hasInternship: Optional[bool] = False
    certifications: Optional[str] = None
    preferredRole: Optional[str] = None
    preferredDomains: Optional[str] = None
    bio: Optional[str] = None
    location: Optional[str] = None
    profilePhotoUrl: Optional[str] = None

class MatchTeammatesRequest(BaseModel):
    user: UserProfileSchema
    candidates: List[UserProfileSchema]
    hackathonId: Optional[int] = 0

class TeammateMatchResult(BaseModel):
    userId: int
    name: str
    email: Optional[str] = None
    profilePhotoUrl: Optional[str] = None
    preferredRole: Optional[str] = None
    bio: Optional[str] = None
    yearsOfExperience: int
    projectCount: int
    skills: List[str]
    interests: List[str]
    compatibilityScore: float
    commonSkills: List[str]
    complementarySkills: List[str]
    matchReason: str

class MatchTeammatesResponse(BaseModel):
    matches: List[TeammateMatchResult]

class MatchScoreRequest(BaseModel):
    userSkills: List[str] = []
    hackathonId: Optional[int] = 0

class MatchScoreResponse(BaseModel):
    score: int
    userSkillCount: int
    hackathonId: int

