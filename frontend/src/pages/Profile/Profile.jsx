import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  User,
  Mail,
  MapPin,
  Edit3,
  Save,
  X,
  Code2,
  Trophy,
  Users,
  FolderKanban,
  Calendar,
  CheckCircle2,
  Phone,
  Globe,
  Sparkles,
  Crown,
  ShieldCheck,
  Shield,
  ArrowLeft,
  Activity,
  Database,
  Lock,
  Layers,
  Terminal
} from "lucide-react";
import { useApp } from "../../context/AppContext";
import "./Profile.css";

function Profile({ targetRole }) {
  const navigate = useNavigate();
  const {
    currentUser,
    updateProfile,
    hackathons,
    registeredHackathons,
    teamMembers,
    projects,
    adminUsers,
    addNotification
  } = useApp();

  // Normalize user role
  const userRole = (currentUser?.role || targetRole || "STUDENT").toUpperCase().replace("ROLE_", "");
  const isSuperAdmin = userRole === "SUPER_ADMIN";
  const isHackathonAdmin = userRole === "HACKATHON_ADMIN";
  const isDevAdmin = userRole === "DEVELOPER_ADMIN";
  const isAdmin = userRole === "ADMIN" || isHackathonAdmin || isDevAdmin;
  const isStudent = !isSuperAdmin && !isAdmin;

  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    fullName: currentUser?.fullName || (isSuperAdmin ? "Super Admin" : isAdmin ? "System Administrator" : "Sanika Haridas Pandhare"),
    firstName: currentUser?.firstName || (isSuperAdmin ? "Super" : isAdmin ? "Admin" : "Sanika"),
    lastName: currentUser?.lastName || (isSuperAdmin ? "Admin" : isAdmin ? "User" : "Pandhare"),
    email: currentUser?.email || (isSuperAdmin ? "superadmin@hackathonbuddy.com" : isAdmin ? "admin@hackathonbuddy.com" : "sanika@example.com"),
    phone: currentUser?.phone || "+91 98765 43210",
    primaryRole: currentUser?.primaryRole || (isSuperAdmin ? "Main Developer / System Owner" : isHackathonAdmin ? "Hackathon Organizer" : isDevAdmin ? "Developer Manager" : isAdmin ? "Platform Administrator" : "Full Stack Developer"),
    location: currentUser?.location || (isSuperAdmin ? "Developer HQ" : isAdmin ? "HQ Command Center" : "Pune, India"),
    bio: currentUser?.bio || (isSuperAdmin ? "Main developer with complete system control. Manages credentials, roles, and access for all administrators." : isAdmin ? "Platform Administrator with permissions over hackathon challenges, user moderation, and AI telemetry." : "Full Stack Developer passionate about building high-impact web apps, AI integrations, and winning hackathons."),
    githubUrl: currentUser?.githubUrl || "https://github.com/sanikapandhare",
    linkedinUrl: currentUser?.linkedinUrl || "https://linkedin.com/in/sanikapandhare",
    techSkills: currentUser?.techSkills || "React, Node.js, Spring Boot, JavaScript, PostgreSQL, Python",
    projectDomains: currentUser?.projectDomains || "AI / ML, Web Development, HealthTech, FinTech"
  });

  const getDashboardPath = () => {
    switch (userRole) {
      case "SUPER_ADMIN": return "/super-admin/dashboard";
      case "HACKATHON_ADMIN": return "/admin/hackathons/dashboard";
      case "DEVELOPER_ADMIN": return "/admin/developers/dashboard";
      case "ADMIN": return "/admin";
      default: return "/student/dashboard";
    }
  };

  const getDashboardLabel = () => {
    switch (userRole) {
      case "SUPER_ADMIN": return "Super Admin Console";
      case "HACKATHON_ADMIN": return "Hackathon Console";
      case "DEVELOPER_ADMIN": return "Developer Admin Console";
      case "ADMIN": return "Admin Console";
      default: return "Student Dashboard";
    }
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleEdit = () => {
    setFormData({
      fullName: currentUser?.fullName || "",
      firstName: currentUser?.firstName || "",
      lastName: currentUser?.lastName || "",
      email: currentUser?.email || "",
      phone: currentUser?.phone || "",
      primaryRole: currentUser?.primaryRole || "",
      location: currentUser?.location || "",
      bio: currentUser?.bio || "",
      githubUrl: currentUser?.githubUrl || "",
      linkedinUrl: currentUser?.linkedinUrl || "",
      techSkills: currentUser?.techSkills || "",
      projectDomains: currentUser?.projectDomains || ""
    });
    setIsEditing(true);
  };

  const handleCancel = () => {
    setIsEditing(false);
  };

  const handleSave = () => {
    const skillsArr = (formData.techSkills || "").split(",").map((s) => s.trim()).filter(Boolean);
    const domainsArr = (formData.projectDomains || "").split(",").map((d) => d.trim()).filter(Boolean);

    // Update profile without mutating the system authentication role
    updateProfile({
      fullName: formData.fullName,
      firstName: formData.firstName,
      lastName: formData.lastName,
      email: formData.email,
      phone: formData.phone,
      primaryRole: formData.primaryRole,
      location: formData.location,
      bio: formData.bio,
      githubUrl: formData.githubUrl,
      linkedinUrl: formData.linkedinUrl,
      techSkills: formData.techSkills,
      projectDomains: formData.projectDomains,
      skills: skillsArr,
      domains: domainsArr
    });

    addNotification({
      type: "system",
      icon: "⚙️",
      title: "Profile Updated",
      message: "Your profile information and preferences were saved.",
      action: "View Profile",
      route: isSuperAdmin ? "/super-admin/profile" : isAdmin ? "/admin/profile" : "/student/profile"
    });

    setIsEditing(false);
  };

  const skillsList = currentUser?.skills || (formData.techSkills || "").split(",").map((s) => s.trim()).filter(Boolean);
  const domainsList = currentUser?.domains || (formData.projectDomains || "").split(",").map((d) => d.trim()).filter(Boolean);

  // Role-specific stats
  const getStats = () => {
    if (isSuperAdmin) {
      return [
        { icon: Crown, value: "Root (Tier 0)", label: "Security Clearance", color: "purple" },
        { icon: ShieldCheck, value: "Full Control", label: "Admin Authority", color: "green" },
        { icon: Database, value: "Global Platform", label: "Governance Scope", color: "orange" },
        { icon: Activity, value: "Active 256-bit", label: "Session Security", color: "blue" },
      ];
    }
    if (isAdmin) {
      return [
        { icon: Trophy, value: `${hackathons.length}`, label: "Managed Hackathons", color: "purple" },
        { icon: Users, value: `${adminUsers.length || 8}`, label: "Moderated Users", color: "green" },
        { icon: ShieldCheck, value: isHackathonAdmin ? "Hackathon Admin" : isDevAdmin ? "Dev Admin" : "Tier 1 Admin", label: "Access Tier", color: "orange" },
        { icon: Activity, value: "Verified Active", label: "Operational Status", color: "blue" },
      ];
    }
    return [
      { icon: Trophy, value: `${registeredHackathons.length} / ${hackathons.length}`, label: "Hackathons", color: "purple" },
      { icon: Users, value: teamMembers.length.toString(), label: "Squad Members", color: "green" },
      { icon: FolderKanban, value: projects.length.toString(), label: "Projects", color: "orange" },
      { icon: Code2, value: "94%", label: "Profile Synergy", color: "blue" },
    ];
  };

  const stats = getStats();

  return (
    <div className="profile-page">
      {/* HEADER */}
      <div className="profile-page-header">
        <div>
          <span className="profile-eyebrow">
            {isSuperAdmin
              ? "PLATFORM OWNER & SYSTEM GOVERNANCE"
              : isAdmin
              ? "ADMINISTRATOR PRIVILEGES & OPERATIONS"
              : "DEVELOPER IDENTITY & SETTINGS"}
          </span>
          <h1>
            {isSuperAdmin
              ? "Super Admin Profile"
              : isAdmin
              ? "Administrator Profile"
              : "My Profile"}
          </h1>
          <p>
            {isSuperAdmin
              ? "Root credentials, security governance, and multi-tenant administrative authorities."
              : isAdmin
              ? "Manage administrative profile, moderation settings, and operational scope."
              : "Manage your developer persona, stack credentials, and teammate matching preferences."}
          </p>
        </div>

        <div style={{ display: "flex", alignItems: "center", gap: "12px", flexWrap: "wrap" }}>
          <button className="profile-dashboard-btn" onClick={() => navigate(getDashboardPath())}>
            <ArrowLeft size={16} />
            <span>{getDashboardLabel()}</span>
          </button>

          {!isEditing ? (
            <button className="profile-edit-btn" onClick={handleEdit}>
              <Edit3 size={17} /> Edit Profile
            </button>
          ) : (
            <div className="profile-action-buttons">
              <button className="profile-cancel-btn" onClick={handleCancel}>
                <X size={17} /> Cancel
              </button>
              <button className="profile-save-btn" onClick={handleSave}>
                <Save size={17} /> Save Changes
              </button>
            </div>
          )}
        </div>
      </div>

      {/* HERO CARD */}
      <section className="profile-hero-card">
        <div className="profile-avatar">
          {currentUser?.firstName?.charAt(0) || currentUser?.fullName?.charAt(0) || (isSuperAdmin ? "S" : isAdmin ? "A" : "U")}
        </div>

        <div className="profile-hero-content">
          {!isEditing ? (
            <>
              <h2>{currentUser?.fullName}</h2>
              <div className="profile-role">
                {currentUser?.primaryRole || (isSuperAdmin ? "System Owner" : isAdmin ? "Platform Administrator" : "Developer")}
              </div>

              {/* Verified Role Badge */}
              <div className={`profile-role-badge ${
                isSuperAdmin ? "profile-badge-superadmin" :
                isHackathonAdmin ? "profile-badge-hackadmin" :
                isDevAdmin ? "profile-badge-devadmin" :
                isAdmin ? "profile-badge-admin" :
                "profile-badge-student"
              }`}>
                {isSuperAdmin && <><Crown size={14} /> <span>SUPER_ADMIN — Platform Owner</span></>}
                {isHackathonAdmin && <><Trophy size={14} /> <span>HACKATHON_ADMIN — Event Organizer</span></>}
                {isDevAdmin && <><Users size={14} /> <span>DEVELOPER_ADMIN — Talent Manager</span></>}
                {!isHackathonAdmin && !isDevAdmin && isAdmin && <><ShieldCheck size={14} /> <span>ADMIN — Platform Administrator</span></>}
                {isStudent && <><CheckCircle2 size={14} /> <span>STUDENT — Hackathon Developer</span></>}
              </div>

              <div className="profile-meta" style={{ marginTop: "12px" }}>
                <span>
                  <Mail size={15} /> {currentUser?.email}
                </span>
                <span>
                  <MapPin size={15} /> {currentUser?.location || "India"}
                </span>
                <span>
                  <Phone size={15} /> {currentUser?.phone || "+91 98765 43210"}
                </span>
              </div>
            </>
          ) : (
            <div className="hero-edit-fields">
              <input
                name="fullName"
                value={formData.fullName}
                onChange={handleChange}
                placeholder="Full Name"
              />
              <input
                name="primaryRole"
                value={formData.primaryRole}
                onChange={handleChange}
                placeholder="Primary / Job Role"
              />
              <input
                name="location"
                value={formData.location}
                onChange={handleChange}
                placeholder="Location"
              />
            </div>
          )}
        </div>

        <div className="profile-completion">
          <div className="completion-header">
            <span>{isSuperAdmin || isAdmin ? "Account Verification" : "Profile Completeness"}</span>
            <strong>{isSuperAdmin || isAdmin ? "100%" : "95%"}</strong>
          </div>
          <div className="completion-bar">
            <div style={{ width: isSuperAdmin || isAdmin ? "100%" : "95%" }} />
          </div>
          <small>{isSuperAdmin || isAdmin ? "✓ Full System Authority Verified" : "✓ Ready for AI squad matching"}</small>
        </div>
      </section>

      {/* STATS */}
      <section className="profile-stats-grid">
        {stats.map((stat) => {
          const Icon = stat.icon;
          return (
            <div className={`profile-stat-card ${stat.color}`} key={stat.label}>
              <div className="stat-icon">
                <Icon size={22} />
              </div>
              <div>
                <strong>{stat.value}</strong>
                <span>{stat.label}</span>
              </div>
            </div>
          );
        })}
      </section>

      {/* CONTENT GRID */}
      <div className="profile-content-grid">
        {/* LEFT COLUMN */}
        <div className="profile-main-column">
          {/* PERSONAL INFORMATION */}
          <section className="profile-section-card">
            <div className="section-title">
              <div>
                <h3>Personal Information</h3>
                <p>{isSuperAdmin || isAdmin ? "Administrator contact and identification details." : "Basic developer contact credentials."}</p>
              </div>
              <User size={20} />
            </div>

            {!isEditing ? (
              <div className="information-grid">
                <div className="information-item">
                  <span>Full Name</span>
                  <strong>{currentUser?.fullName}</strong>
                </div>
                <div className="information-item">
                  <span>Email Address</span>
                  <strong>{currentUser?.email}</strong>
                </div>
                <div className="information-item">
                  <span>Phone Number</span>
                  <strong>{currentUser?.phone || "+91 98765 43210"}</strong>
                </div>
                <div className="information-item">
                  <span>Location</span>
                  <strong>{currentUser?.location || "India"}</strong>
                </div>
              </div>
            ) : (
              <div className="edit-form-grid">
                <div className="form-field">
                  <label>Full Name</label>
                  <input name="fullName" value={formData.fullName} onChange={handleChange} />
                </div>
                <div className="form-field">
                  <label>Email</label>
                  <input name="email" value={formData.email} onChange={handleChange} />
                </div>
                <div className="form-field">
                  <label>Phone</label>
                  <input name="phone" value={formData.phone} onChange={handleChange} />
                </div>
                <div className="form-field">
                  <label>Location</label>
                  <input name="location" value={formData.location} onChange={handleChange} />
                </div>
              </div>
            )}
          </section>

          {/* BIO / RESPONSIBILITIES */}
          <section className="profile-section-card">
            <div className="section-title">
              <div>
                <h3>{isSuperAdmin || isAdmin ? "Administrative Scope & Bio" : "Developer Bio"}</h3>
                <p>{isSuperAdmin || isAdmin ? "Summary of organizational responsibilities and platform focus." : "Summary displayed on your public teammate profile."}</p>
              </div>
            </div>

            {isEditing ? (
              <textarea
                className="profile-bio-input"
                name="bio"
                value={formData.bio}
                onChange={handleChange}
                rows="4"
              />
            ) : (
              <p className="profile-bio">{currentUser?.bio || formData.bio}</p>
            )}
          </section>

          {/* TECHNICAL SKILLS / ADMIN SPECIALIZATIONS */}
          <section className="profile-section-card">
            <div className="section-title">
              <div>
                <h3>{isSuperAdmin || isAdmin ? "Technical & Operational Specializations" : "Technical Skills"}</h3>
                <p>{isSuperAdmin || isAdmin ? "Platform administration competencies and technical oversight." : "Core programming languages, frameworks, and tools."}</p>
              </div>
              <Code2 size={20} />
            </div>

            {isEditing ? (
              <div className="form-field">
                <label>Skills / Specializations (comma-separated)</label>
                <input
                  name="techSkills"
                  value={formData.techSkills}
                  onChange={handleChange}
                />
              </div>
            ) : (
              <div className="skill-list">
                {skillsList.map((skill) => (
                  <span key={skill}>{skill}</span>
                ))}
              </div>
            )}
          </section>

          {/* PROJECT / OPERATIONAL DOMAINS */}
          <section className="profile-section-card">
            <div className="section-title">
              <div>
                <h3>{isSuperAdmin || isAdmin ? "Oversight Domains" : "Hackathon Domains"}</h3>
                <p>{isSuperAdmin || isAdmin ? "Categories under your administrative authority." : "Target tracks and industry problem domains."}</p>
              </div>
            </div>

            {isEditing ? (
              <div className="form-field">
                <label>Domains (comma-separated)</label>
                <input
                  name="projectDomains"
                  value={formData.projectDomains}
                  onChange={handleChange}
                />
              </div>
            ) : (
              <div className="domain-list">
                {domainsList.map((domain) => (
                  <span key={domain}>{domain}</span>
                ))}
              </div>
            )}
          </section>
        </div>

        {/* RIGHT COLUMN */}
        <aside className="profile-side-column">
          {/* ROLE-SPECIFIC RIGHT CARDS */}
          {isSuperAdmin ? (
            <>
              <section className="profile-section-card">
                <div className="section-title">
                  <div>
                    <h3>Super Admin Privileges</h3>
                    <p>Root system authority</p>
                  </div>
                  <Crown size={20} color="#e11d48" />
                </div>
                <div className="admin-privileges-list">
                  <div className="admin-privilege-item">
                    <ShieldCheck size={18} color="#e11d48" />
                    <div>
                      <strong>Full Platform Control</strong>
                      <span>Manage all users, administrators & roles</span>
                    </div>
                  </div>
                  <div className="admin-privilege-item">
                    <Database size={18} color="#e11d48" />
                    <div>
                      <strong>Database & Audit Logs</strong>
                      <span>Direct access to telemetry and credentials</span>
                    </div>
                  </div>
                  <div className="admin-privilege-item">
                    <Lock size={18} color="#e11d48" />
                    <div>
                      <strong>Credential Authority</strong>
                      <span>Create and reset administrator passwords</span>
                    </div>
                  </div>
                </div>
                <button className="admin-console-btn" onClick={() => navigate("/super-admin/dashboard")}>
                  <Crown size={16} /> Open Super Admin Console
                </button>
              </section>
            </>
          ) : isAdmin ? (
            <>
              <section className="profile-section-card">
                <div className="section-title">
                  <div>
                    <h3>Administrator Scope</h3>
                    <p>Active permissions</p>
                  </div>
                  <ShieldCheck size={20} color="#0284c7" />
                </div>
                <div className="admin-privileges-list">
                  <div className="admin-privilege-item">
                    <Trophy size={18} color="#0284c7" />
                    <div>
                      <strong>Hackathon Management</strong>
                      <span>Publish, edit, and moderate competitions</span>
                    </div>
                  </div>
                  <div className="admin-privilege-item">
                    <Users size={18} color="#0284c7" />
                    <div>
                      <strong>Participant Moderation</strong>
                      <span>Monitor teams and review registered developers</span>
                    </div>
                  </div>
                  <div className="admin-privilege-item">
                    <Activity size={18} color="#0284c7" />
                    <div>
                      <strong>AI Hub Telemetry</strong>
                      <span>Inspect matching scores and skill gaps</span>
                    </div>
                  </div>
                </div>
                <button className="admin-console-btn" onClick={() => navigate(getDashboardPath())}>
                  <ShieldCheck size={16} /> Open {getDashboardLabel()}
                </button>
              </section>
            </>
          ) : (
            <>
              {/* SOCIAL LINKS */}
              <section className="profile-section-card">
                <div className="section-title">
                  <div>
                    <h3>Developer Links</h3>
                    <p>Reputation profiles and portfolios.</p>
                  </div>
                </div>

                {isEditing ? (
                  <div style={{ display: "flex", flexDirection: "column", gap: "10px" }}>
                    <div className="form-field">
                      <label>GitHub URL</label>
                      <input name="githubUrl" value={formData.githubUrl} onChange={handleChange} />
                    </div>
                    <div className="form-field">
                      <label>LinkedIn URL</label>
                      <input name="linkedinUrl" value={formData.linkedinUrl} onChange={handleChange} />
                    </div>
                  </div>
                ) : (
                  <div className="social-links">
                    <a
                      href={currentUser?.githubUrl || "https://github.com"}
                      target="_blank"
                      rel="noreferrer"
                      className="social-link"
                    >
                      <div className="social-logo github-logo">GH</div>
                      <div>
                        <strong>GitHub</strong>
                        <span>View repositories</span>
                      </div>
                    </a>

                    <a
                      href={currentUser?.linkedinUrl || "https://linkedin.com"}
                      target="_blank"
                      rel="noreferrer"
                      className="social-link"
                    >
                      <div className="social-logo linkedin-logo">in</div>
                      <div>
                        <strong>LinkedIn</strong>
                        <span>View professional profile</span>
                      </div>
                    </a>
                  </div>
                )}
              </section>

              {/* ACTIVE TEAM CARD */}
              <section className="profile-section-card">
                <div className="section-title">
                  <div>
                    <h3>Active Squad</h3>
                    <p>Current hackathon squad.</p>
                  </div>
                </div>

                <div className="current-team">
                  <div className="team-avatar">CC</div>
                  <div>
                    <strong>CodeCrafters</strong>
                    <span>{teamMembers.length} team members</span>
                  </div>
                </div>
              </section>

              {/* STATUS */}
              <section className="profile-section-card">
                <div className="section-title">
                  <div>
                    <h3>Matching Status</h3>
                    <p>Teammate discovery settings.</p>
                  </div>
                </div>

                <div className="profile-status-item">
                  <CheckCircle2 size={18} color="#10b981" />
                  <div>
                    <strong>Visible to Teammate Search</strong>
                    <span>Your profile is actively suggested in AI Hub.</span>
                  </div>
                </div>

                <div className="profile-status-item">
                  <Calendar size={18} color="#8b5cf6" />
                  <div>
                    <strong>Hackathon Season</strong>
                    <span>Active 2026 Circuit</span>
                  </div>
                </div>
              </section>
            </>
          )}
        </aside>
      </div>

      {/* MY PROJECTS SECTION (FOR STUDENTS) */}
      {isStudent && (
        <section className="profile-project-section">
          <div className="projects-header">
            <div>
              <span className="profile-eyebrow">WORKSPACE</span>
              <h2>My Projects ({projects.length})</h2>
              <p>Active codebases and repositories you are collaborating on.</p>
            </div>
          </div>

          <div className="profile-project-grid">
            {projects.map((proj) => (
              <div className="profile-project-card" key={proj.id}>
                <div className="project-top">
                  <div className="project-icon">
                    <Code2 size={20} />
                  </div>
                  <span className="project-status">{proj.status}</span>
                </div>

                <h3>{proj.name}</h3>
                <p>{proj.domain}</p>

                <div className="project-progress-header">
                  <span>Sprint Progress</span>
                  <strong>{proj.progress || 60}%</strong>
                </div>

                <div className="project-progress">
                  <div style={{ width: `${proj.progress || 60}%` }} />
                </div>
              </div>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}

export default Profile;