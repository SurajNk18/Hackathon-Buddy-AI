import React, { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import {
  LayoutDashboard,
  Trophy,
  Users,
  Brain,
  FolderKanban,
  MessageSquare,
  Bell,
  User,
  ArrowLeft,
  Search,
  Menu,
  X,
  LogOut,
  Sparkles,
  ChevronDown,
  Crown,
  ShieldCheck,
  Code2
} from "lucide-react";
import { useApp } from "../context/AppContext";
import HBLogo from "./HBLogo";
import "./Header.css";

function Header() {
  const { currentUser, notifications, logout } = useApp();
  const location = useLocation();
  const navigate = useNavigate();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [userDropdownOpen, setUserDropdownOpen] = useState(false);

  const normPath = location.pathname.replace(/\/+$/, "") || "/";
  const isAuthPage = normPath === "/" || normPath === "/login" || normPath === "/create-profile";

  if (isAuthPage) {
    return null;
  }

  const unreadCount = Array.isArray(notifications) ? notifications.filter((n) => n.unread).length : 0;
  const isDashboard =
    normPath === "/student/dashboard" ||
    normPath === "/dashboard" ||
    normPath === "/super-admin/dashboard" ||
    normPath === "/admin/hackathons/dashboard" ||
    normPath === "/admin/developers/dashboard" ||
    normPath === "/admin";

  // Derive the user's role name (normalized to uppercase)
  const userRole = (currentUser?.role || "STUDENT").toUpperCase().replace("ROLE_", "");

  const isStudentRole = ["STUDENT", "DEVELOPER", "ORGANIZER", "COMPANY"].includes(userRole);

  const getBrandRedirect = () => {
    switch (userRole) {
      case "SUPER_ADMIN": return "/super-admin/dashboard";
      case "HACKATHON_ADMIN": return "/admin/hackathons/dashboard";
      case "DEVELOPER_ADMIN": return "/admin/developers/dashboard";
      case "ADMIN": return "/admin";
      default: return "/student/dashboard";
    }
  };

  const getProfilePath = () => {
    switch (userRole) {
      case "SUPER_ADMIN": return "/super-admin/profile";
      case "HACKATHON_ADMIN":
      case "DEVELOPER_ADMIN":
      case "ADMIN": return "/admin/profile";
      default: return "/student/profile";
    }
  };

  const getNavItems = () => {
    if (userRole === "SUPER_ADMIN") {
      return [
        { label: "Super Admin Console", icon: Crown, path: "/super-admin/dashboard" },
        { label: "Admin Console", icon: ShieldCheck, path: "/admin" },
        { label: "Notifications", icon: Bell, path: "/notifications", badge: unreadCount },
        { label: "Profile", icon: User, path: "/super-admin/profile" },
      ];
    }
    if (userRole === "HACKATHON_ADMIN") {
      return [
        { label: "Hackathons", icon: Trophy, path: "/admin/hackathons/dashboard" },
        { label: "Admin Console", icon: ShieldCheck, path: "/admin" },
        { label: "Notifications", icon: Bell, path: "/notifications", badge: unreadCount },
        { label: "Profile", icon: User, path: "/admin/profile" },
      ];
    }
    if (userRole === "DEVELOPER_ADMIN") {
      return [
        { label: "Developer Admin", icon: Code2, path: "/admin/developers/dashboard" },
        { label: "Admin Console", icon: ShieldCheck, path: "/admin" },
        { label: "Notifications", icon: Bell, path: "/notifications", badge: unreadCount },
        { label: "Profile", icon: User, path: "/admin/profile" },
      ];
    }
    if (userRole === "ADMIN") {
      return [
        { label: "Admin Console", icon: ShieldCheck, path: "/admin" },
        { label: "Hackathons", icon: Trophy, path: "/admin/hackathons/dashboard" },
        { label: "Developers", icon: Code2, path: "/admin/developers/dashboard" },
        { label: "Notifications", icon: Bell, path: "/notifications", badge: unreadCount },
        { label: "Profile", icon: User, path: "/admin/profile" },
      ];
    }
    // Default: student/developer navigation
    return [
      { label: "Dashboard", icon: LayoutDashboard, path: "/student/dashboard" },
      { label: "Hackathons", icon: Trophy, path: "/hackathons" },
      { label: "Teams", icon: Users, path: "/teams" },
      { label: "AI Hub", icon: Brain, path: "/ai-hub" },
      { label: "Projects", icon: FolderKanban, path: "/projects" },
      { label: "Chat", icon: MessageSquare, path: "/chat" },
      { label: "Notifications", icon: Bell, path: "/notifications", badge: unreadCount },
      { label: "Profile", icon: User, path: "/student/profile" },
    ];
  };

  const navItems = getNavItems();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <header className="sticky-header">
      <div className="header-container">
        {/* Brand & Left Actions */}
        <div className="header-left-section">
          <Link to={getBrandRedirect()} className="header-brand">
            <HBLogo size={34} nameSize={16} variant="light" />
          </Link>
        </div>

        {/* Desktop Navigation */}
        <nav className="desktop-nav">
          {navItems.map((item) => {
            const Icon = item.icon;
            const isActive = location.pathname === item.path;
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`nav-item ${isActive ? "active" : ""}`}
              >
                <Icon size={17} />
                <span>{item.label}</span>
                {item.badge > 0 && <span className="nav-badge">{item.badge}</span>}
              </Link>
            );
          })}
        </nav>

        {/* Right Actions */}
        <div className="header-right-section">

          {/* User Profile Pill */}
          <div className="user-profile-menu">
            <button
              className="user-profile-btn"
              onClick={() => setUserDropdownOpen(!userDropdownOpen)}
              aria-label="User menu"
            >
              <div className="user-avatar-small">
                {currentUser?.firstName?.charAt(0) || currentUser?.fullName?.charAt(0) || "U"}
              </div>
              <div className="user-text-info">
                <span className="user-name-text">
                  {currentUser?.firstName || currentUser?.fullName?.split(" ")[0] || "User"}
                </span>
                <span className="user-role-text">
                  {currentUser?.primaryRole || currentUser?.role || "Developer"}
                </span>
              </div>
              <ChevronDown size={14} className="dropdown-caret" />
            </button>

            {userDropdownOpen && (
              <div
                className="user-dropdown-card"
                onMouseLeave={() => setUserDropdownOpen(false)}
              >
                <div className="dropdown-header">
                  <strong>{currentUser?.fullName}</strong>
                  <small>{currentUser?.email}</small>
                </div>
                <div className="dropdown-divider" />
                <Link
                  to={getProfilePath()}
                  className="dropdown-item"
                  onClick={() => setUserDropdownOpen(false)}
                >
                  <User size={16} />
                  My Profile
                </Link>
                {!isStudentRole && (
                  <Link
                    to="/notifications"
                    className="dropdown-item"
                    onClick={() => setUserDropdownOpen(false)}
                    style={{ justifyContent: "space-between" }}
                  >
                    <div style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                      <Bell size={16} />
                      Notifications
                    </div>
                    {unreadCount > 0 && (
                      <span style={{
                        background: "#e11d48",
                        color: "white",
                        fontSize: "10px",
                        fontWeight: "bold",
                        padding: "2px 6px",
                        borderRadius: "10px"
                      }}>
                        {unreadCount}
                      </span>
                    )}
                  </Link>
                )}
                <Link
                  to={getBrandRedirect()}
                  className="dropdown-item"
                  onClick={() => setUserDropdownOpen(false)}
                >
                  <LayoutDashboard size={16} />
                  Dashboard
                </Link>
                {/* Show role-specific admin links */}
                {(userRole === "SUPER_ADMIN" || userRole === "ADMIN" || userRole === "HACKATHON_ADMIN" || userRole === "DEVELOPER_ADMIN") && (
                  <>
                    {userRole === "SUPER_ADMIN" && (
                      <Link
                        to="/super-admin/dashboard"
                        className="dropdown-item"
                        onClick={() => setUserDropdownOpen(false)}
                        style={{ color: "#e11d48" }}
                      >
                        <Sparkles size={16} />
                        Super Admin Console
                      </Link>
                    )}
                    {userRole === "HACKATHON_ADMIN" && (
                      <Link
                        to="/admin/hackathons/dashboard"
                        className="dropdown-item"
                        onClick={() => setUserDropdownOpen(false)}
                        style={{ color: "#14b8a6" }}
                      >
                        <Sparkles size={16} />
                        Hackathon Admin
                      </Link>
                    )}
                    {userRole === "DEVELOPER_ADMIN" && (
                      <Link
                        to="/admin/developers/dashboard"
                        className="dropdown-item"
                        onClick={() => setUserDropdownOpen(false)}
                        style={{ color: "#6366f1" }}
                      >
                        <Sparkles size={16} />
                        Developer Admin
                      </Link>
                    )}
                    {userRole === "ADMIN" && (
                      <Link
                        to="/admin"
                        className="dropdown-item"
                        onClick={() => setUserDropdownOpen(false)}
                        style={{ color: "#38bdf8" }}
                      >
                        <Sparkles size={16} />
                        Admin Console
                      </Link>
                    )}
                  </>
                )}
                <div className="dropdown-divider" />
                <button className="dropdown-item logout" onClick={handleLogout}>
                  <LogOut size={16} />
                  Sign Out
                </button>
              </div>
            )}
          </div>

          {/* Mobile Hamburger Toggle */}
          <button
            className="mobile-toggle-btn"
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            aria-label="Toggle navigation"
          >
            {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
          </button>
        </div>
      </div>

      {/* Mobile Drawer Menu */}
      {mobileMenuOpen && (
        <div className="mobile-drawer">
          <div className="mobile-nav-list">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = location.pathname === item.path;
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`mobile-nav-link ${isActive ? "active" : ""}`}
                  onClick={() => setMobileMenuOpen(false)}
                >
                  <Icon size={20} />
                  <span>{item.label}</span>
                  {item.badge > 0 && <span className="nav-badge">{item.badge}</span>}
                </Link>
              );
            })}
          </div>

          <div className="mobile-drawer-footer">
            <div className="mobile-user-row">
              <div className="user-avatar-small">
                {currentUser?.firstName?.charAt(0) || "U"}
              </div>
              <div>
                <strong>{currentUser?.fullName}</strong>
                <small>{currentUser?.email}</small>
              </div>
            </div>
            <button className="mobile-logout-btn" onClick={handleLogout}>
              <LogOut size={16} />
              Sign Out
            </button>
          </div>
        </div>
      )}
    </header>
  );
}

export default Header;
