import React, { useMemo, useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
  Search,
  Plus,
  UserPlus,
  Users,
  X,
  Check,
  Sparkles,
  SlidersHorizontal,
  Mail,
  Trash2,
  CheckCircle2,
  MapPin,
  Code2,
  MessageSquare
} from "lucide-react";
import { useApp } from "../../context/AppContext";
import { teamAPI, userAPI, aiAPI, notificationAPI } from "../../api/api";
import "./Teams.css";

function Teams() {
  const navigate = useNavigate();
  const {
    currentUser,
    teamMembers: contextTeamMembers,
    setTeamMembers,
    addTeamMember,
    removeTeamMember,
    addNotification,
    notifications,
    respondToNotification
  } = useApp();

  const [search, setSearch] = useState("");
  const [selectedRole, setSelectedRole] = useState("All Roles");
  const [selectedSkill, setSelectedSkill] = useState("All Skills");
  const [showCreateTeam, setShowCreateTeam] = useState(false);
  const [selectedCandidate, setSelectedCandidate] = useState(null);
  const [teamName, setTeamName] = useState("CodeCrafters");
  const [teamData, setTeamData] = useState(null);
  const [squadMembers, setSquadMembers] = useState([]);

  const pendingInvitation = useMemo(() => {
    return (notifications || []).find(
      (n) => n.action === "ACCEPT_REJECT" && n.responseStatus === "PENDING" && n.type === "team_invitation"
    );
  }, [notifications]);

  const [candidates, setCandidates] = useState([]);

  useEffect(() => {
    if (!currentUser) {
      setSquadMembers([]);
      setTeamData(null);
      return;
    }

    // Fetch live team details from Spring Boot TeamController (/api/teams/my-team)
    teamAPI.getMyTeam()
      .then((res) => {
        if (res.data.success && res.data.data) {
          const t = res.data.data;
          setTeamData(t);
          setTeamName(t.name || "CodeCrafters");
          if (t.members && t.members.length > 0) {
            const formatted = t.members.map(m => ({
              id: m.id || m.userId,
              userId: m.userId,
              name: m.name,
              email: m.email,
              role: m.role || "Developer",
              status: m.status || "ACCEPTED",
              skills: m.skills || [],
              letter: m.name ? m.name.charAt(0).toUpperCase() : "T",
              isYou: m.isCurrentUser || (currentUser && (String(m.userId) === String(currentUser.id) || m.name === currentUser.fullName || m.name === currentUser.firstName)) || false
            }));
            setSquadMembers(formatted);
            if (setTeamMembers) setTeamMembers(formatted);
          } else {
            const onlyYou = [{
              id: currentUser.id || "you",
              userId: currentUser.id,
              name: currentUser.fullName || (currentUser.firstName ? `${currentUser.firstName} ${currentUser.lastName || ''}`.trim() : currentUser.email) || "Leader",
              email: currentUser.email,
              role: currentUser.primaryRole || currentUser.role || "Team Leader",
              skills: currentUser.techSkills ? currentUser.techSkills.split(",").map(s => s.trim()).filter(Boolean) : (currentUser.skills || []),
              letter: (currentUser.fullName || currentUser.firstName || "L").charAt(0).toUpperCase(),
              isYou: true
            }];
            setSquadMembers(onlyYou);
            if (setTeamMembers) setTeamMembers(onlyYou);
          }
        }
      })
      .catch(() => {});


    // Fetch live registered student candidates with AI matching (and candidate API fallback)
    const processAndSetCandidates = (rawList) => {
      const forbiddenKeywords = [
        "admin",
        "super admin",
        "hackadmin",
        "devadmin",
        "hackathon admin",
        "developer admin",
        "hackathon organizer",
        "system administrator",
        "developer manager",
        "system owner",
        "organizer",
        "company"
      ];
      const isNonStudent = (c) => {
        const r = (c.role || "").toLowerCase();
        if (r && r !== "student" && r !== "role_student") {
          return true;
        }
        const name = (c.name || "").toLowerCase();
        const preferredRole = (c.preferredRole || "").toLowerCase();
        return forbiddenKeywords.some((kw) => name.includes(kw) || preferredRole.includes(kw));
      };

      const liveCand = rawList
        .filter((u) => !isNonStudent(u))
        .map((u) => ({
          id: u.userId,
          userId: u.userId,
          name: u.name,
          email: u.email,
          role: u.preferredRole || "Developer",
          location: u.location || "India",
          skills: u.skills && u.skills.length > 0 ? u.skills : ["React", "Python", "JavaScript"],
          interests: u.interests || ["AI / ML", "Web"],
          match: Math.round(u.compatibilityScore || 92),
          letter: u.name ? u.name.charAt(0).toUpperCase() : "S",
          bio: u.bio || "Registered platform student."
        }));

      setCandidates(liveCand);
    };

    // 1. Instantly load candidates so all users display immediately without delay
    userAPI.getCandidates()
      .then((userRes) => {
        if (userRes.data?.success && Array.isArray(userRes.data.data) && userRes.data.data.length > 0) {
          processAndSetCandidates(userRes.data.data);
        }
      })
      .catch((err) => {
        console.warn("Direct candidate fetch error:", err);
      });

    // 2. In parallel, enhance with AI compatibility scoring if available
    aiAPI.findTeammates()
      .then((res) => {
        if (res.data?.success && Array.isArray(res.data.data) && res.data.data.length > 0) {
          processAndSetCandidates(res.data.data);
        }
      })
      .catch(() => {
        // AI service non-blocking background fallback
      });
  }, [currentUser?.id]);

  useEffect(() => {
    if (contextTeamMembers) {
      setSquadMembers(contextTeamMembers);
    }
  }, [contextTeamMembers]);

  const userName = currentUser?.fullName || "Developer";
  const userRole = currentUser?.primaryRole || currentUser?.role || "Full Stack Developer";

  const roles = [
    "All Roles",
    "Frontend Developer",
    "Backend Developer",
    "Full Stack Developer",
    "ML Engineer",
    "Data Scientist",
    "UI/UX Designer",
    "DevOps Engineer",
    "Cyber Security Engineer",
  ];

  const skills = [
    "All Skills",
    "React",
    "Python",
    "Figma",
    "AWS",
    "Docker",
    "Node.js",
    "PostgreSQL",
    "Kubernetes",
  ];

  // Team skills safely extracted as an array regardless of whether backend returned strings or arrays
  const teamSkills = useMemo(() => {
    const set = new Set();
    (squadMembers || []).forEach((m) => {
      if (!m || !m.skills) return;
      if (Array.isArray(m.skills)) {
        m.skills.forEach((s) => s && set.add(String(s).trim()));
      } else if (typeof m.skills === "string") {
        m.skills.split(",").forEach((s) => s && set.add(s.trim()));
      }
    });
    return Array.from(set);
  }, [squadMembers]);

  const requiredSkills = ["UI/UX", "Machine Learning", "AWS", "Docker", "React", "Node.js"];
  const missingSkills = useMemo(() => {
    return requiredSkills.filter(
      (req) => !teamSkills.some((ts) => ts && ts.toLowerCase() === req.toLowerCase())
    );
  }, [teamSkills]);

  // Safe Filter candidates
  const filteredCandidates = useMemo(() => {
    const forbiddenKeywords = [
      "admin",
      "super admin",
      "hackadmin",
      "devadmin",
      "hackathon admin",
      "developer admin",
      "hackathon organizer",
      "system administrator",
      "developer manager",
      "system owner",
      "organizer",
      "company"
    ];

    return (candidates || []).filter((candidate) => {
      if (!candidate) return false;

      // Filter out admin / organizer / manager / company accounts
      const candName = (candidate.name || "").toLowerCase();
      const candRole = (candidate.role || "").toLowerCase();
      const isForbidden = forbiddenKeywords.some(
        (kw) => candName.includes(kw) || candRole.includes(kw)
      );
      if (isForbidden) return false;

      const q = (search || "").toLowerCase().trim();
      const candSkills = Array.isArray(candidate.skills)
        ? candidate.skills
        : (typeof candidate.skills === "string" ? candidate.skills.split(",") : []);

      const matchesSearch =
        !q ||
        (candidate.name && candidate.name.toLowerCase().includes(q)) ||
        (candidate.role && candidate.role.toLowerCase().includes(q)) ||
        candSkills.some((s) => s && String(s).toLowerCase().includes(q));

      const matchesRole =
        selectedRole === "All Roles" || candidate.role === selectedRole;

      const normalize = (s) => (s || "").toLowerCase().replace(/[^a-z0-9+#]/g, "");
      const skillsMatch = (candS, queryS) => {
        const c = normalize(candS);
        const q = normalize(queryS);
        if (c === q) return true;
        if (c.endsWith("js") && c.slice(0, -2) === q) return true;
        if (q.endsWith("js") && q.slice(0, -2) === c) return true;
        return false;
      };

      const matchesSkill =
        selectedSkill === "All Skills" ||
        candSkills.some((s) => s && skillsMatch(String(s), selectedSkill));

      const isYou = currentUser && (
        (candidate.userId && currentUser.id && String(currentUser.id) === String(candidate.userId)) ||
        (candidate.email && currentUser.email && candidate.email.toLowerCase() === currentUser.email.toLowerCase()) ||
        (candidate.name && currentUser.fullName && candidate.name.toLowerCase() === currentUser.fullName.toLowerCase())
      );
      if (isYou) return false;

      const isAlreadyMember = (squadMembers || []).some(
        (m) =>
          (candidate.userId && m.userId && String(m.userId) === String(candidate.userId)) ||
          (candidate.email && m.email && m.email.toLowerCase() === candidate.email.toLowerCase()) ||
          (m && m.name && candidate.name && m.name.toLowerCase() === candidate.name.toLowerCase())
      );

      return matchesSearch && matchesRole && matchesSkill && !isAlreadyMember;
    });
  }, [candidates, search, selectedRole, selectedSkill, squadMembers, currentUser]);

  const handleInviteCandidate = async (candidate) => {
    if (!candidate) return;
    const candSkills = Array.isArray(candidate.skills)
      ? candidate.skills
      : (typeof candidate.skills === "string" ? candidate.skills.split(",").map(s => s.trim()) : []);

    const targetUserId = candidate.userId || candidate.id;
    const newMemberObj = {
      id: targetUserId,
      userId: targetUserId,
      name: candidate.name || "Member",
      email: candidate.email,
      role: candidate.role || "Developer",
      status: "PENDING",
      skills: candSkills,
      letter: candidate.letter || (candidate.name ? candidate.name.charAt(0) : "M")
    };

    setSquadMembers((prev) => {
      const exists = (prev || []).some(
        (m) =>
          (targetUserId && (String(m.id) === String(targetUserId) || String(m.userId) === String(targetUserId))) ||
          (m.name && candidate.name && m.name.toLowerCase() === candidate.name.toLowerCase())
      );
      if (exists) return prev;
      return [...(prev || []), newMemberObj];
    });

    if (addTeamMember) {
      await addTeamMember(newMemberObj);
    }

    try {
      const teamRes = await teamAPI.getMyTeam();
      if (teamRes.data?.success && teamRes.data.data) {
        setTeamData(teamRes.data.data);
      }
    } catch (e) {
      // ok
    }

    setSelectedCandidate(null);
  };

  const handleSaveTeamName = async (e) => {
    e.preventDefault();
    if (!teamName.trim()) return;

    try {
      if (teamData?.id) {
        const res = await teamAPI.update(teamData.id, { name: teamName.trim(), description: "Active Hackathon Squad" });
        if (res.data?.success && res.data.data) {
          setTeamData(res.data.data);
        }
      } else {
        const res = await teamAPI.create({ name: teamName.trim(), description: "Active Hackathon Squad" });
        if (res.data?.success && res.data.data) {
          setTeamData(res.data.data);
        }
      }
    } catch (e) {
      // Quiet fallback
    }

    addNotification({
      type: "team",
      icon: "👥",
      title: "Team Updated",
      message: `Team renamed to "${teamName.trim()}".`,
      action: "View Team",
      route: "/teams"
    });
    setShowCreateTeam(false);
  };

  return (
    <div className="teams-page">
      {/* MAIN CONTENT */}
      <main className="teams-main">
        {/* HEADER */}
        <header className="teams-header">
          <div>
            <div className="teams-breadcrumb" style={{ color: "#7a8295", fontSize: "13px", marginBottom: "6px" }}>HackathonBuddy / Teams & Candidates</div>
            <h1>Find Your Dream Team</h1>
            <p>
              Build your winning hackathon squad with AI skill-based matching and gap analysis.
            </p>
          </div>

          <div className="teams-header-right">
            <button
              className="create-team-btn"
              onClick={() => setShowCreateTeam(true)}
              style={{
                display: "flex",
                alignItems: "center",
                gap: "8px",
                padding: "9px 16px",
                background: "#7c3aed",
                border: "none",
                borderRadius: "8px",
                color: "white",
                fontWeight: "600",
                fontSize: "13px",
                cursor: "pointer"
              }}
            >
              <Plus size={16} />
              Rename Team
            </button>
          </div>
        </header>

        {/* HERO */}
        <section className="teams-hero">
          <div className="teams-hero-content">
            <div className="teams-hero-badge">
              <Sparkles size={14} />
              AI-POWERED SQUAD BUILDER
            </div>
            <h2>
              Find teammates who
              <span> complete your skills.</span>
            </h2>
            <p>
              We calculate compatibility based on role balance, missing technical skills,
              and project domain alignment.
            </p>
          </div>

          <div className="teams-hero-graphic">
            <div className="hero-avatar one">P</div>
            <div className="hero-avatar two">R</div>
            <div className="hero-avatar three">A</div>
            <div className="hero-center-icon">🤝</div>
          </div>
        </section>

        {/* PENDING INVITATION BANNER */}
        {pendingInvitation && (
          <div style={{
            background: "linear-gradient(135deg, rgba(99, 102, 241, 0.22), rgba(168, 85, 247, 0.22))",
            border: "1px solid rgba(139, 92, 246, 0.4)",
            borderRadius: "14px",
            padding: "18px 22px",
            marginBottom: "24px",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            flexWrap: "wrap",
            gap: "14px",
            boxShadow: "0 4px 20px rgba(99, 102, 241, 0.15)"
          }}>
            <div style={{ display: "flex", alignItems: "center", gap: "14px" }}>
              <span style={{ fontSize: "28px" }}>📩</span>
              <div>
                <strong style={{ color: "#f8fafc", fontSize: "15.5px", display: "block" }}>{pendingInvitation.title}</strong>
                <span style={{ color: "#cbd5e1", fontSize: "13.5px" }}>{pendingInvitation.message}</span>
              </div>
            </div>
            <div style={{ display: "flex", gap: "10px" }}>
              <button
                onClick={async () => {
                  await respondToNotification(pendingInvitation.id, "ACCEPT");
                  teamAPI.getMyTeam().then(res => {
                    if (res.data?.success && res.data.data) {
                      setTeamData(res.data.data);
                      setTeamName(res.data.data.name || "CodeCrafters");
                      const formatted = (res.data.data.members || []).map(m => ({
                        id: m.id || m.userId,
                        userId: m.userId,
                        name: m.name,
                        email: m.email,
                        role: m.role || "Developer",
                        status: m.status || "ACCEPTED",
                        skills: m.skills || [],
                        letter: m.name ? m.name.charAt(0).toUpperCase() : "T",
                        isYou: m.isCurrentUser || false
                      }));
                      setSquadMembers(formatted);
                    }
                  });
                }}
                style={{
                  background: "#22c55e",
                  color: "#fff",
                  border: "none",
                  padding: "9px 20px",
                  borderRadius: "8px",
                  fontWeight: "600",
                  cursor: "pointer",
                  fontSize: "13.5px"
                }}
              >
                Accept & Join Squad
              </button>
              <button
                onClick={async () => {
                  await respondToNotification(pendingInvitation.id, "REJECT");
                }}
                style={{
                  background: "rgba(239, 68, 68, 0.15)",
                  color: "#ef4444",
                  border: "1px solid #ef4444",
                  padding: "9px 18px",
                  borderRadius: "8px",
                  fontWeight: "600",
                  cursor: "pointer",
                  fontSize: "13.5px"
                }}
              >
                Decline
              </button>
            </div>
          </div>
        )}

        {/* ACTIVE TEAM SECTION */}
        <section className="active-team-card" style={{
          background: "linear-gradient(145deg, #111726, #0e1320)",
          border: "1px solid #1e293b",
          borderRadius: "16px",
          padding: "24px",
          marginBottom: "32px"
        }}>
          <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "18px" }}>
            <div>
              <span style={{ color: "#a78bfa", fontSize: "11px", fontWeight: "800", letterSpacing: "1px" }}>CURRENT SQUAD</span>
              <h2 style={{ color: "#f8fafc", fontSize: "20px", margin: "4px 0 0" }}>
                {teamName} ({(squadMembers || []).length} Members)
              </h2>
            </div>
            <button
              onClick={() => navigate("/chat")}
              style={{
                display: "flex",
                alignItems: "center",
                gap: "6px",
                padding: "8px 14px",
                background: "rgba(139, 92, 246, 0.15)",
                border: "1px solid #8b5cf6",
                color: "#c4b5fd",
                borderRadius: "8px",
                fontSize: "13px",
                fontWeight: "600",
                cursor: "pointer"
              }}
            >
              <MessageSquare size={16} />
              Team Chat
            </button>
          </div>

          {/* Members list */}
          <div className="squad-members-responsive-grid">
            {(squadMembers || []).map((member) => (
              <div
                key={member.id}
                style={{
                  background: "#0a0e1a",
                  border: "1px solid #1f293d",
                  borderRadius: "12px",
                  padding: "16px",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "space-between"
                }}
              >
                <div style={{ display: "flex", alignItems: "center", gap: "12px" }}>
                  <div style={{
                    width: "40px",
                    height: "40px",
                    borderRadius: "50%",
                    background: member.isYou ? "linear-gradient(135deg, #7c3aed, #a855f7)" : "#1e293b",
                    color: "white",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontWeight: "700"
                  }}>
                    {member.letter || member.name.charAt(0)}
                  </div>
                  <div>
                    <strong style={{ color: "#f8fafc", fontSize: "14px", display: "flex", alignItems: "center", gap: "6px", flexWrap: "wrap" }}>
                      <span>{member.name}</span>
                      {member.isYou && <small style={{ color: "#a78bfa" }}>(You)</small>}
                      {member.status === "PENDING" && (
                        <span style={{ fontSize: "10px", padding: "2px 6px", borderRadius: "4px", background: "rgba(234, 179, 8, 0.15)", color: "#facc15", border: "1px solid rgba(234, 179, 8, 0.3)", fontWeight: "600" }}>
                          Pending ⏳
                        </span>
                      )}
                    </strong>
                    <span style={{ color: "#64748b", fontSize: "12px" }}>{member.role}</span>
                  </div>
                </div>

                {!member.isYou && (
                  <button
                    onClick={() => {
                      const targetId = member.userId || member.id;
                      removeTeamMember(targetId);
                      setSquadMembers((prev) => (prev || []).filter((m) => m.id !== member.id && m.userId !== targetId && m.id !== targetId));
                      if (teamData?.id && targetId) {
                        teamAPI.removeMember(teamData.id, targetId).catch(() => {});
                      }
                    }}
                    title="Remove from team"
                    style={{
                      background: "transparent",
                      border: "none",
                      color: "#64748b",
                      cursor: "pointer",
                      padding: "6px"
                    }}
                  >
                    <Trash2 size={16} />
                  </button>
                )}
              </div>
            ))}
          </div>

          {/* Skill Gap Banner */}
          <div style={{
            marginTop: "20px",
            padding: "16px",
            background: "rgba(124, 58, 237, 0.08)",
            border: "1px solid rgba(139, 92, 246, 0.25)",
            borderRadius: "10px",
            display: "flex",
            alignItems: "center",
            justifyContent: "space-between",
            flexWrap: "wrap",
            gap: "12px"
          }}>
            <div>
              <strong style={{ color: "#e2e8f0", fontSize: "13.5px" }}>Skills Covered:</strong>
              <div style={{ display: "flex", flexWrap: "wrap", gap: "6px", marginTop: "6px" }}>
                {teamSkills.slice(0, 8).map((sk) => (
                  <span key={sk} style={{ background: "rgba(16, 185, 129, 0.15)", color: "#34d399", padding: "3px 8px", borderRadius: "6px", fontSize: "11px", fontWeight: "600" }}>
                    ✓ {sk}
                  </span>
                ))}
              </div>
            </div>

            {missingSkills.length > 0 && (
              <div>
                <strong style={{ color: "#fbbf24", fontSize: "13.5px" }}>Missing Skills Needed:</strong>
                <div style={{ display: "flex", flexWrap: "wrap", gap: "6px", marginTop: "6px" }}>
                  {missingSkills.map((sk) => (
                    <span key={sk} style={{ background: "rgba(245, 158, 11, 0.15)", color: "#fbbf24", padding: "3px 8px", borderRadius: "6px", fontSize: "11px", fontWeight: "600" }}>
                      + {sk}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>
        </section>

        {/* SEARCH & FILTERS FOR TEAMMATES */}
        <section className="teams-filter-bar" style={{
          display: "flex",
          gap: "12px",
          alignItems: "center",
          flexWrap: "wrap",
          marginBottom: "24px"
        }}>
          <div style={{
            flex: 1,
            minWidth: "260px",
            display: "flex",
            alignItems: "center",
            gap: "10px",
            background: "#111726",
            border: "1px solid #1e293b",
            borderRadius: "10px",
            padding: "0 14px",
            height: "44px"
          }}>
            <Search size={18} color="#64748b" />
            <input
              type="text"
              placeholder="Search hackers by name, role, or skills..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              style={{
                width: "100%",
                background: "transparent",
                border: "none",
                outline: "none",
                color: "#f8fafc",
                fontSize: "13.5px"
              }}
            />
          </div>

          <select
            value={selectedRole}
            onChange={(e) => setSelectedRole(e.target.value)}
            style={{
              height: "44px",
              background: "#111726",
              border: "1px solid #1e293b",
              borderRadius: "10px",
              color: "#f8fafc",
              padding: "0 14px",
              fontSize: "13px"
            }}
          >
            {roles.map((r) => (
              <option key={r} value={r}>{r}</option>
            ))}
          </select>

          <select
            value={selectedSkill}
            onChange={(e) => setSelectedSkill(e.target.value)}
            style={{
              height: "44px",
              background: "#111726",
              border: "1px solid #1e293b",
              borderRadius: "10px",
              color: "#f8fafc",
              padding: "0 14px",
              fontSize: "13px"
            }}
          >
            {skills.map((s) => (
              <option key={s} value={s}>{s}</option>
            ))}
          </select>
        </section>

        {/* CANDIDATES GRID */}
        <section className="candidates-responsive-grid">
          {filteredCandidates.length === 0 ? (
            <div style={{
              gridColumn: "1 / -1",
              textAlign: "center",
              padding: "48px 20px",
              background: "#0c101c",
              borderRadius: "16px",
              border: "1px dashed #1e293b"
            }}>
              <Users size={40} color="#64748b" style={{ marginBottom: "12px" }} />
              <h3 style={{ color: "#f8fafc" }}>No candidates found</h3>
              <p style={{ color: "#64748b", fontSize: "13px" }}>Try broadening your search or role filters.</p>
            </div>
          ) : (
            filteredCandidates.map((cand) => (
              <div
                key={cand.id}
                style={{
                  background: "linear-gradient(145deg, #111726, #0d121e)",
                  border: "1px solid #1e293b",
                  borderRadius: "14px",
                  padding: "20px",
                  display: "flex",
                  flexDirection: "column",
                  justifyContent: "space-between",
                  transition: "transform 0.2s, border-color 0.2s"
                }}
              >
                <div>
                  <div style={{ display: "flex", justifyContent: "space-between", alignItems: "flex-start", marginBottom: "12px" }}>
                    <div style={{ display: "flex", gap: "12px" }}>
                      <div style={{
                        width: "44px",
                        height: "44px",
                        borderRadius: "10px",
                        background: "linear-gradient(135deg, #6366f1, #a855f7)",
                        color: "white",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        fontWeight: "700",
                        fontSize: "16px"
                      }}>
                        {cand.letter}
                      </div>
                      <div>
                        <strong style={{ color: "#f8fafc", fontSize: "15px", display: "block" }}>{cand.name}</strong>
                        <span style={{ color: "#38bdf8", fontSize: "12.5px" }}>{cand.role}</span>
                      </div>
                    </div>

                    <div style={{
                      padding: "4px 8px",
                      background: "rgba(16, 185, 129, 0.15)",
                      border: "1px solid rgba(16, 185, 129, 0.3)",
                      borderRadius: "6px",
                      color: "#34d399",
                      fontSize: "12px",
                      fontWeight: "700"
                    }}>
                      {cand.match}% MATCH
                    </div>
                  </div>

                  <p style={{ color: "#94a3b8", fontSize: "13px", lineHeight: "1.5", margin: "0 0 14px" }}>
                    {cand.bio}
                  </p>

                  <div style={{ display: "flex", alignItems: "center", gap: "6px", color: "#64748b", fontSize: "12px", marginBottom: "12px" }}>
                    <MapPin size={14} />
                    <span>{cand.location}</span>
                  </div>

                  <div style={{ display: "flex", flexWrap: "wrap", gap: "6px", marginBottom: "16px" }}>
                    {cand.skills.map((sk) => (
                      <span key={sk} style={{
                        background: "#182236",
                        border: "1px solid #27354d",
                        color: "#cbd5e1",
                        padding: "3px 8px",
                        borderRadius: "6px",
                        fontSize: "11px"
                      }}>
                        {sk}
                      </span>
                    ))}
                  </div>
                </div>

                <div style={{ display: "flex", gap: "10px" }}>
                  <button
                    onClick={() => handleInviteCandidate(cand)}
                    style={{
                      flex: 1,
                      padding: "9px 14px",
                      background: "#7c3aed",
                      border: "none",
                      borderRadius: "8px",
                      color: "white",
                      fontSize: "13px",
                      fontWeight: "600",
                      cursor: "pointer",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      gap: "6px"
                    }}
                  >
                    <UserPlus size={16} />
                    Add to Squad
                  </button>
                  <button
                    onClick={() => navigate("/chat")}
                    style={{
                      padding: "9px 12px",
                      background: "rgba(255, 255, 255, 0.05)",
                      border: "1px solid #1e293b",
                      borderRadius: "8px",
                      color: "#94a3b8",
                      cursor: "pointer"
                    }}
                    title="Message candidate"
                  >
                    <MessageSquare size={16} />
                  </button>
                </div>
              </div>
            ))
          )}
        </section>
      </main>

      {/* RENAME / CREATE TEAM MODAL */}
      {showCreateTeam && (
        <div
          className="modal-overlay"
          onClick={() => setShowCreateTeam(false)}
          style={{
            position: "fixed",
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            background: "rgba(0, 0, 0, 0.75)",
            backdropFilter: "blur(6px)",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            zIndex: 1100,
            padding: "20px"
          }}
        >
          <div
            onClick={(e) => e.stopPropagation()}
            style={{
              background: "#111726",
              border: "1px solid #26334a",
              borderRadius: "16px",
              padding: "28px",
              maxWidth: "460px",
              width: "100%",
              boxShadow: "0 20px 50px rgba(0, 0, 0, 0.6)"
            }}
          >
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "18px" }}>
              <h3 style={{ color: "#f8fafc", margin: 0, fontSize: "18px" }}>Edit Team Name</h3>
              <button
                onClick={() => setShowCreateTeam(false)}
                style={{ background: "transparent", border: "none", color: "#64748b", cursor: "pointer" }}
              >
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleSaveTeamName}>
              <div style={{ marginBottom: "18px" }}>
                <label style={{ display: "block", color: "#94a3b8", fontSize: "12px", fontWeight: "700", marginBottom: "8px" }}>
                  TEAM / SQUAD NAME
                </label>
                <input
                  type="text"
                  value={teamName}
                  onChange={(e) => setTeamName(e.target.value)}
                  placeholder="e.g. NeuralCoders, CodeCrafters"
                  required
                  style={{
                    width: "100%",
                    height: "44px",
                    background: "#090d16",
                    border: "1px solid #1e293b",
                    borderRadius: "8px",
                    color: "#f8fafc",
                    padding: "0 12px",
                    fontSize: "14px"
                  }}
                />
              </div>

              <button
                type="submit"
                style={{
                  width: "100%",
                  height: "44px",
                  background: "#7c3aed",
                  border: "none",
                  borderRadius: "8px",
                  color: "white",
                  fontWeight: "600",
                  fontSize: "14px",
                  cursor: "pointer"
                }}
              >
                Save Team Changes
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Teams;