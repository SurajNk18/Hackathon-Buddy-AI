import React, { useState, useEffect } from "react";
import {
  ArrowRight,
  UserPlus,
  ShieldCheck,
  Crown,
  Shield,
  GraduationCap,
  Code2,
  Trophy,
  CheckCircle2,
  KeyRound,
  X,
} from "lucide-react";
import { useNavigate, useLocation } from "react-router-dom";
import { useApp } from "../../context/AppContext";
import HBLogo from "../../components/HBLogo";
import "./LoginPage.css";

const ROLES = [
  {
    id: "STUDENT",
    label: "Student",
    icon: GraduationCap,
    color: "#3730A3",
    desc: "Browse hackathons, register, build projects",
    redirect: "/student/dashboard",
  },
  {
    id: "ADMIN",
    label: "Admin",
    icon: Shield,
    color: "#2563EB",
    desc: "Choose admin type after selection",
    redirect: "/admin",
    subRoles: [
      {
        id: "HACKATHON_ADMIN",
        label: "Hackathon Admin",
        icon: Trophy,
        color: "#0D9488",
        desc: "Manage hackathons & participants",
        redirect: "/admin/hackathons/dashboard",
      },
      {
        id: "DEVELOPER_ADMIN",
        label: "Developer Admin",
        icon: Code2,
        color: "#4F46E5",
        desc: "Manage developer profiles & activity",
        redirect: "/admin/developers/dashboard",
      },
    ],
  },
  {
    id: "SUPER_ADMIN",
    label: "Super Admin",
    icon: Crown,
    color: "#DC2626",
    desc: "Full platform control & credential management",
    redirect: "/super-admin/dashboard",
  },
];

const LoginPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login, resetPassword } = useApp();

  const [selectedRole, setSelectedRole] = useState("STUDENT");
  const [selectedSubRole, setSelectedSubRole] = useState(null);
  const [email, setEmail] = useState(location.state?.email || "");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [successBanner, setSuccessBanner] = useState(
    location.state?.message || ""
  );
  const [loading, setLoading] = useState(false);

  // Forgot password modal state
  const [showForgotModal, setShowForgotModal] = useState(false);
  const [resetEmail, setResetEmail] = useState("");
  const [resetNewPassword, setResetNewPassword] = useState("");
  const [resetConfirmPassword, setResetConfirmPassword] = useState("");
  const [resetError, setResetError] = useState("");
  const [resetLoading, setResetLoading] = useState(false);

  useEffect(() => {
    if (location.state?.email) {
      setEmail(location.state.email);
    }
    if (location.state?.message) {
      setSuccessBanner(location.state.message);
    }
  }, [location.state]);

  const currentRole = ROLES.find((r) => r.id === selectedRole);
  const showSubRoles = selectedRole === "ADMIN";

  const effectiveRole =
    showSubRoles && selectedSubRole ? selectedSubRole : selectedRole;

  const getRedirectPath = (userRole) => {
    const role = (userRole || effectiveRole).toUpperCase();
    switch (role) {
      case "SUPER_ADMIN":
        return "/super-admin/dashboard";
      case "HACKATHON_ADMIN":
        return "/admin/hackathons/dashboard";
      case "DEVELOPER_ADMIN":
        return "/admin/developers/dashboard";
      case "ADMIN":
        return "/admin";
      default:
        return "/student/dashboard";
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");

    if (selectedRole === "ADMIN" && !selectedSubRole) {
      setError(
        "Please select an admin type (Hackathon Admin or Developer Admin)."
      );
      return;
    }

    setLoading(true);

    try {
      const result = await login(email.trim(), password.trim());

      if (result.success) {
        const userRole = result.user?.role || "";
        const redirectPath = getRedirectPath(userRole);
        navigate(redirectPath);
      } else {
        setError(result.message || "Invalid email or password.");
      }
    } catch (err) {
      setError("Login failed. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  const handleOpenForgotModal = () => {
    setResetEmail(email || "");
    setResetNewPassword("");
    setResetConfirmPassword("");
    setResetError("");
    setShowForgotModal(true);
  };

  const handleCloseForgotModal = () => {
    setShowForgotModal(false);
    setResetError("");
  };

  const handleResetSubmit = async (e) => {
    e.preventDefault();
    setResetError("");

    if (!resetEmail.trim()) {
      setResetError("Please enter your registered email address.");
      return;
    }

    if (!resetNewPassword || resetNewPassword.length < 6) {
      setResetError("New password must be at least 6 characters.");
      return;
    }

    if (resetNewPassword !== resetConfirmPassword) {
      setResetError("Passwords do not match. Please re-enter.");
      return;
    }

    setResetLoading(true);

    try {
      const result = await resetPassword(resetEmail.trim(), resetNewPassword);
      if (result.success) {
        setShowForgotModal(false);
        setSuccessBanner(
          result.message ||
            "Password reset successfully! Please sign in with your new password."
        );
        setEmail(resetEmail.trim());
        setPassword("");
        setError("");
      } else {
        setResetError(
          result.message ||
            "Failed to reset password. Please verify the email."
        );
      }
    } catch (err) {
      setResetError(
        "An error occurred while resetting password. Please try again."
      );
    } finally {
      setResetLoading(false);
    }
  };

  /* ── Derive the button label ── */
  const buttonLabel = loading
    ? "Signing in…"
    : `Sign in as ${
        showSubRoles && selectedSubRole
          ? ROLES.find((r) => r.id === "ADMIN").subRoles.find(
              (s) => s.id === selectedSubRole
            )?.label
          : currentRole?.label || "User"
      }`;

  return (
    <main className="lg-page">
      <section className="lg-container">
        {/* ── BRAND ── */}
        <div style={{ marginBottom: 32 }}>
          <HBLogo size={44} nameSize={19} variant="dark" />
        </div>

        {/* ── HEADING ── */}
        <div className="lg-heading">
          <h1>Welcome back</h1>
          <p>
            Sign in to continue building teams, discovering hackathons, and
            creating amazing projects.
          </p>
        </div>

        {/* ── LOGIN CARD ── */}
        <div className="lg-card">
          <div className="lg-card-header">
            <h2>Sign in to your account</h2>
            <p>Select your role and enter credentials</p>
          </div>

          {/* Success banner */}
          {successBanner && (
            <div className="lg-success-banner">
              <CheckCircle2 size={17} />
              <span>{successBanner}</span>
            </div>
          )}

          {/* ── ROLE SELECTOR ── */}
          <div className="lg-roles">
            <label className="lg-label">Login as</label>
            <div className="lg-role-list">
              {ROLES.map((role) => {
                const Icon = role.icon;
                const isActive = selectedRole === role.id;
                return (
                  <button
                    key={role.id}
                    type="button"
                    className={`lg-role-btn ${isActive ? "active" : ""}`}
                    onClick={() => {
                      setSelectedRole(role.id);
                      setSelectedSubRole(null);
                      setError("");
                    }}
                  >
                    <div
                      className="lg-role-icon"
                      style={{
                        "--r-color": role.color,
                        "--r-bg": `${role.color}14`,
                      }}
                    >
                      <Icon size={19} />
                    </div>
                    <div className="lg-role-text">
                      <strong>{role.label}</strong>
                      <small>{role.desc}</small>
                    </div>
                    {isActive && (
                      <CheckCircle2
                        size={18}
                        className="lg-role-check"
                      />
                    )}
                  </button>
                );
              })}
            </div>
          </div>

          {/* ── ADMIN SUB-ROLE ── */}
          {showSubRoles && (
            <div className="lg-sub-roles">
              <label className="lg-label">Admin type</label>
              <div className="lg-sub-role-grid">
                {ROLES.find((r) => r.id === "ADMIN").subRoles.map((sub) => {
                  const SubIcon = sub.icon;
                  const isActive = selectedSubRole === sub.id;
                  return (
                    <button
                      key={sub.id}
                      type="button"
                      className={`lg-sub-role-btn ${isActive ? "active" : ""}`}
                      onClick={() => {
                        setSelectedSubRole(sub.id);
                        setError("");
                      }}
                    >
                      <SubIcon size={18} />
                      <div>
                        <strong>{sub.label}</strong>
                        <small>{sub.desc}</small>
                      </div>
                    </button>
                  );
                })}
              </div>
            </div>
          )}

          {/* ── FORM ── */}
          <form onSubmit={handleSubmit}>
            <div className="lg-field">
              <label htmlFor="email">Email address</label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="name@example.com"
                autoComplete="username"
                required
              />
            </div>

            <div className="lg-field">
              <div className="lg-field-row">
                <label htmlFor="password">Password</label>
                <button
                  type="button"
                  className="lg-forgot"
                  onClick={handleOpenForgotModal}
                >
                  Forgot password?
                </button>
              </div>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="Enter your password"
                autoComplete="current-password"
                required
              />
            </div>

            {error && <div className="lg-error">{error}</div>}

            <button type="submit" className="lg-submit" disabled={loading}>
              <span>{buttonLabel}</span>
              <ArrowRight size={18} />
            </button>
          </form>

          {/* ── DIVIDER ── */}
          <div className="lg-divider">
            <span>or</span>
          </div>

          {/* ── CREATE ACCOUNT ── */}
          <div className="lg-create">
            <div className="lg-create-text">
              <h3>New to HackathonBuddy?</h3>
              <p>
                Create your developer profile and start finding the right
                hackathon team.
              </p>
            </div>
            <button
              type="button"
              className="lg-create-btn"
              onClick={() => navigate("/create-profile")}
            >
              <UserPlus size={17} />
              <span>Create Student Account</span>
            </button>
          </div>
        </div>

        {/* ── FOOTER ── */}
        <p className="lg-footer">
          HackathonBuddy &bull; Build. Match. Ship.
        </p>
      </section>

      {/* ── FORGOT PASSWORD MODAL ── */}
      {showForgotModal && (
        <div className="lg-modal-backdrop" onClick={handleCloseForgotModal}>
          <div className="lg-modal" onClick={(e) => e.stopPropagation()}>
            <div className="lg-modal-header">
              <div className="lg-modal-title">
                <div className="lg-modal-icon">
                  <KeyRound size={19} />
                </div>
                <div>
                  <h3>Reset Password</h3>
                  <p>Enter your account email and choose a new password.</p>
                </div>
              </div>
              <button
                type="button"
                className="lg-modal-close"
                onClick={handleCloseForgotModal}
                aria-label="Close"
              >
                <X size={18} />
              </button>
            </div>

            <form onSubmit={handleResetSubmit} className="lg-modal-form">
              <div className="lg-field">
                <label htmlFor="reset-email">Registered email</label>
                <input
                  id="reset-email"
                  type="email"
                  value={resetEmail}
                  onChange={(e) => setResetEmail(e.target.value)}
                  placeholder="name@example.com"
                  autoComplete="email"
                  required
                />
              </div>

              <div className="lg-field">
                <label htmlFor="reset-new-password">New password</label>
                <input
                  id="reset-new-password"
                  type="password"
                  value={resetNewPassword}
                  onChange={(e) => setResetNewPassword(e.target.value)}
                  placeholder="Min. 6 characters"
                  autoComplete="new-password"
                  required
                />
              </div>

              <div className="lg-field">
                <label htmlFor="reset-confirm-password">
                  Confirm new password
                </label>
                <input
                  id="reset-confirm-password"
                  type="password"
                  value={resetConfirmPassword}
                  onChange={(e) => setResetConfirmPassword(e.target.value)}
                  placeholder="Re-enter new password"
                  autoComplete="new-password"
                  required
                />
              </div>

              {resetError && <div className="lg-error">{resetError}</div>}

              <div className="lg-modal-actions">
                <button
                  type="button"
                  className="lg-modal-cancel"
                  onClick={handleCloseForgotModal}
                  disabled={resetLoading}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="lg-modal-submit"
                  disabled={resetLoading}
                >
                  {resetLoading ? "Updating…" : "Update Password"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </main>
  );
};

export default LoginPage;
