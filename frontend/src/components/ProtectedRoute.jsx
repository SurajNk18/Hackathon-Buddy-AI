import React from "react";
import { Navigate } from "react-router-dom";
import { useApp } from "../context/AppContext";

/**
 * ProtectedRoute — wraps a page component to enforce role-based access.
 *
 * Props:
 *   - children:       The component to render if authorized
 *   - allowedRoles:   Array of role strings that can access this route
 *                     e.g. ["SUPER_ADMIN", "HACKATHON_ADMIN"]
 *   - requireAuth:    (default true) Whether the user must be logged in
 *   - redirectTo:     Where to redirect unauthorized users (default "/login")
 */
function ProtectedRoute({ children, allowedRoles = [], requireAuth = true, redirectTo = "/login" }) {
  const { currentUser, isLoggedIn, isAuthLoading } = useApp();

  // If initial auth check from token is still in flight, render loading indicator
  // to avoid prematurely redirecting to login or an incorrect role dashboard.
  if (isAuthLoading) {
    return (
      <div style={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "65vh" }}>
        <div style={{
          width: "36px",
          height: "36px",
          border: "3px solid rgba(99, 102, 241, 0.2)",
          borderTopColor: "#6366f1",
          borderRadius: "50%",
          animation: "spin 0.8s linear infinite"
        }} />
      </div>
    );
  }

  // Not logged in → redirect to login
  if (requireAuth && !isLoggedIn) {
    return <Navigate to={redirectTo} replace />;
  }

  // If there are allowedRoles, check user's role
  if (allowedRoles.length > 0 && currentUser) {
    const userRole = (currentUser.role || "STUDENT").toUpperCase();

    // Normalize: remove "ROLE_" prefix if present
    const normalizedRole = userRole.replace("ROLE_", "");

    // Map unrecognized or display roles to STUDENT as a safe default
    const validRoles = ["SUPER_ADMIN", "HACKATHON_ADMIN", "DEVELOPER_ADMIN", "ADMIN", "STUDENT", "DEVELOPER", "ORGANIZER", "COMPANY"];
    let finalRole = normalizedRole;
    if (!validRoles.includes(finalRole)) {
      finalRole = "STUDENT";
    }

    // Check if user's role is in the allowed list
    const isAllowed = allowedRoles.some(
      (r) => r.toUpperCase().replace("ROLE_", "") === finalRole
    );

    if (!isAllowed) {
      // Redirect to their proper dashboard based on verified role
      const roleRedirects = {
        SUPER_ADMIN: "/super-admin/dashboard",
        HACKATHON_ADMIN: "/admin/hackathons/dashboard",
        DEVELOPER_ADMIN: "/admin/developers/dashboard",
        ADMIN: "/admin",
        STUDENT: "/student/dashboard",
        DEVELOPER: "/student/dashboard",
        ORGANIZER: "/student/dashboard",
        COMPANY: "/student/dashboard",
      };
      const fallback = roleRedirects[finalRole] || "/student/dashboard";
      
      // Prevent infinite redirect loops!
      if (window.location.pathname === fallback) {
        localStorage.removeItem("hackathon_token");
        return <Navigate to="/login" replace />;
      }
      
      return <Navigate to={fallback} replace />;
    }
  }

  return children;
}

export default ProtectedRoute;
