import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  ArrowLeft,
  UserPlus,
  CheckCircle2,
  UserRound,
  Code2,
  MapPin,
  Globe,
  FolderKanban,
  Lock,
  Mail,
  ArrowRight,
} from "lucide-react";
import { useApp } from "../../context/AppContext";
import HBLogo from "../../components/HBLogo";
import "./CreateProfile.css";

function CreateProfile() {
  const navigate = useNavigate();
  const { registerUser } = useApp();

  const [formData, setFormData] = useState({
    fullName: "",
    email: "",
    password: "",
    primaryRole: "Full Stack Developer",
    techSkills: "",
    projectDomains: "",
    githubUrl: "",
    location: "India",
  });

  const [error, setError] = useState("");
  const [success, setSuccess] = useState(false);
  const [loading, setLoading] = useState(false);

  // ==========================================
  // HANDLE INPUT CHANGE
  // ==========================================

  const handleChange = (e) => {
    const { name, value } = e.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  // ==========================================
  // CREATE ACCOUNT
  // ==========================================

  const handleSubmit = async (e) => {
    e.preventDefault();

    setError("");

    // ------------------------------------------
    // VALIDATION
    // ------------------------------------------

    if (
      !formData.fullName.trim() ||
      !formData.email.trim() ||
      !formData.password.trim() ||
      !formData.techSkills.trim()
    ) {
      setError(
        "Please fill all required fields (Full Name, Email, Password, Skills)."
      );
      return;
    }

    if (formData.password.length < 6) {
      setError("Password must contain at least 6 characters.");
      return;
    }

    const email = formData.email.trim().toLowerCase();

    setLoading(true);

    try {
      const newUser = await registerUser({ ...formData, email });
      if (newUser) {
        setSuccess(true);
        setTimeout(() => {
          navigate("/login", {
            state: {
              email: formData.email,
              message: "Account created successfully! Please sign in with your email and password.",
            },
          });
        }, 1200);
      } else {
        setError("Registration failed.");
      }
    } catch (err) {
      setError("An error occurred during registration. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <main className="cp-page">
      {/* ── BACK BUTTON ── */}
      <button
        className="cp-back"
        onClick={() => navigate(-1)}
        aria-label="Go back"
      >
        <ArrowLeft size={20} />
      </button>

      <div className="cp-wrapper">
        {/* ── BRAND ── */}
        <div className="cp-brand">
          <HBLogo size={40} nameSize={18} variant="dark" />
        </div>

        {/* ── HEADING ── */}
        <div className="cp-heading">
          <h1>Build Your Developer Profile</h1>
          <p>
            Create your account to match with the right hackathon teams,
            discover events, and showcase your skills.
          </p>
        </div>

        {/* ── FORM CARD ── */}
        <div className="cp-card">
          <form onSubmit={handleSubmit}>

            {/* ═══════════════════════════════════════
                SECTION 1 — Account Information
            ═══════════════════════════════════════ */}
            <div className="cp-section">
              <div className="cp-section-header">
                <div className="cp-section-icon">
                  <UserRound size={18} />
                </div>
                <div>
                  <h2>Account Information</h2>
                  <p>Your login credentials and identity</p>
                </div>
              </div>

              <div className="cp-fields-grid">
                {/* Full Name */}
                <div className="cp-field">
                  <label htmlFor="fullName">
                    Full Name <span className="cp-req">*</span>
                  </label>
                  <div className="cp-input-wrap">
                    <UserRound size={17} className="cp-input-icon" />
                    <input
                      id="fullName"
                      name="fullName"
                      type="text"
                      placeholder="e.g. Sanika Pandhare"
                      value={formData.fullName}
                      onChange={handleChange}
                      required
                    />
                  </div>
                </div>

                {/* Email */}
                <div className="cp-field">
                  <label htmlFor="email">
                    Email Address <span className="cp-req">*</span>
                  </label>
                  <div className="cp-input-wrap">
                    <Mail size={17} className="cp-input-icon" />
                    <input
                      id="email"
                      name="email"
                      type="email"
                      placeholder="sanika@example.com"
                      value={formData.email}
                      onChange={handleChange}
                      required
                    />
                  </div>
                </div>

                {/* Password */}
                <div className="cp-field">
                  <label htmlFor="password">
                    Password <span className="cp-req">*</span>
                  </label>
                  <div className="cp-input-wrap">
                    <Lock size={17} className="cp-input-icon" />
                    <input
                      id="password"
                      name="password"
                      type="password"
                      placeholder="Min 6 characters"
                      value={formData.password}
                      onChange={handleChange}
                      required
                    />
                  </div>
                </div>

                {/* Primary Role */}
                <div className="cp-field">
                  <label htmlFor="primaryRole">
                    Primary Role <span className="cp-req">*</span>
                  </label>
                  <div className="cp-input-wrap">
                    <Code2 size={17} className="cp-input-icon" />
                    <select
                      id="primaryRole"
                      name="primaryRole"
                      value={formData.primaryRole}
                      onChange={handleChange}
                    >
                      <option>Full Stack Developer</option>
                      <option>Frontend Developer</option>
                      <option>Backend Developer</option>
                      <option>ML Engineer</option>
                      <option>Data Scientist</option>
                      <option>UI / UX Designer</option>
                      <option>DevOps Engineer</option>
                      <option>Cyber Security Engineer</option>
                      <option>Product Manager</option>
                    </select>
                  </div>
                </div>
              </div>
            </div>

            {/* ═══════════════════════════════════════
                SECTION 2 — Skills & Stack
            ═══════════════════════════════════════ */}
            <div className="cp-section">
              <div className="cp-section-header">
                <div className="cp-section-icon cp-section-icon--blue">
                  <Code2 size={18} />
                </div>
                <div>
                  <h2>Skills &amp; Stack</h2>
                  <p>Help our AI match you with the best teams</p>
                </div>
              </div>

              <div className="cp-fields-grid">
                {/* Tech Skills */}
                <div className="cp-field cp-field--full">
                  <label htmlFor="techSkills">
                    Technical Skills <span className="cp-req">*</span>
                  </label>
                  <div className="cp-input-wrap">
                    <Code2 size={17} className="cp-input-icon" />
                    <input
                      id="techSkills"
                      name="techSkills"
                      type="text"
                      placeholder="React, Node.js, Python, Java, AWS..."
                      value={formData.techSkills}
                      onChange={handleChange}
                      required
                    />
                  </div>
                  <small className="cp-hint">
                    Comma-separated — used for smart AI team matching
                  </small>
                </div>

                {/* Project Domains */}
                <div className="cp-field cp-field--full">
                  <label htmlFor="projectDomains">Project Domains</label>
                  <div className="cp-input-wrap">
                    <FolderKanban size={17} className="cp-input-icon" />
                    <input
                      id="projectDomains"
                      name="projectDomains"
                      type="text"
                      placeholder="AI / ML, HealthTech, FinTech, Web3..."
                      value={formData.projectDomains}
                      onChange={handleChange}
                    />
                  </div>
                </div>
              </div>
            </div>

            {/* ═══════════════════════════════════════
                SECTION 3 — Links & Location
            ═══════════════════════════════════════ */}
            <div className="cp-section cp-section--last">
              <div className="cp-section-header">
                <div className="cp-section-icon cp-section-icon--teal">
                  <MapPin size={18} />
                </div>
                <div>
                  <h2>Links &amp; Location</h2>
                  <p>Optional details to complete your profile</p>
                </div>
              </div>

              <div className="cp-fields-grid">
                {/* GitHub */}
                <div className="cp-field">
                  <label htmlFor="githubUrl">GitHub URL</label>
                  <div className="cp-input-wrap">
                    <Globe size={17} className="cp-input-icon" />
                    <input
                      id="githubUrl"
                      name="githubUrl"
                      type="url"
                      placeholder="https://github.com/username"
                      value={formData.githubUrl}
                      onChange={handleChange}
                    />
                  </div>
                </div>

                {/* Location */}
                <div className="cp-field">
                  <label htmlFor="location">Location</label>
                  <div className="cp-input-wrap">
                    <MapPin size={17} className="cp-input-icon" />
                    <input
                      id="location"
                      name="location"
                      type="text"
                      placeholder="Pune, India"
                      value={formData.location}
                      onChange={handleChange}
                    />
                  </div>
                </div>
              </div>
            </div>

            {/* ── ERROR ── */}
            {error && (
              <div className="cp-error">{error}</div>
            )}

            {/* ── SUCCESS ── */}
            {success && (
              <div className="cp-success">
                <CheckCircle2 size={18} />
                Account created successfully! Redirecting to Sign In page…
              </div>
            )}

            {/* ── SUBMIT ── */}
            <button
              type="submit"
              className="cp-submit"
              disabled={success || loading}
            >
              <UserPlus size={18} />
              <span>
                {loading ? "Creating account…" : "Create Account"}
              </span>
              {!loading && <ArrowRight size={17} />}
            </button>

            {/* ── DIVIDER ── */}
            <div className="cp-divider"><span>or</span></div>

            {/* ── SIGN IN ── */}
            <div className="cp-signin-row">
              <span>Already have an account?</span>
              <button type="button" onClick={() => navigate("/login")}>
                Sign In
              </button>
            </div>

          </form>
        </div>

        {/* ── FOOTER ── */}
        <p className="cp-footer">
          HackathonBuddy &bull; Build. Match. Ship.
        </p>
      </div>
    </main>
  );
}

export default CreateProfile;
