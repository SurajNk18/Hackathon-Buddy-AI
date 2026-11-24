import React from "react";

/**
 * HackathonBuddy brand logo component.
 * Renders the icon-mark inline as an SVG so it works on any background
 * without needing an external asset import.
 *
 * Props
 * ─────
 * size       — icon box size in px (default 38)
 * showName   — render the "HACKATHON BUDDY" wordmark (default true)
 * nameSize   — wordmark font-size in px (default 17)
 * variant    — "dark" (dark text, for light bg) | "light" (white text, for dark bg)
 */
export default function HBLogo({
  size = 38,
  showName = true,
  nameSize = 17,
  variant = "dark",
  style,
  className = "",
  ...rest
}) {
  const textColor = variant === "light" ? "#F8FAFC" : "#0F172A";
  const accentColor = "#3730A3";
  const radius = Math.round(size * 0.26);

  return (
    <div
      className={`hb-logo ${className}`}
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: size > 30 ? 10 : 8,
        ...style,
      }}
      {...rest}
    >
      {/* ── Icon mark ── */}
      <svg
        xmlns="http://www.w3.org/2000/svg"
        width={size}
        height={size}
        viewBox="0 0 48 48"
        fill="none"
        aria-hidden="true"
        style={{ flexShrink: 0, borderRadius: radius, boxShadow: "0 4px 14px rgba(55,48,163,0.22)" }}
      >
        <rect width="48" height="48" rx="12" fill={accentColor} />
        <path
          d="M24 8C24 8 18 16 18 24C18 28.5 20.5 32 24 34C27.5 32 30 28.5 30 24C30 16 24 8 24 8Z"
          fill="white"
          opacity="0.95"
        />
        <path
          d="M24 34C22.5 36 21.5 38 21.5 39.5C21.5 41 22.6 42 24 42C25.4 42 26.5 41 26.5 39.5C26.5 38 25.5 36 24 34Z"
          fill="#818CF8"
          opacity="0.9"
        />
        <circle cx="15" cy="28" r="3" fill="white" opacity="0.7" />
        <line x1="18" y1="27" x2="21" y2="25" stroke="white" strokeWidth="1.5" opacity="0.5" strokeLinecap="round" />
        <circle cx="33" cy="28" r="3" fill="white" opacity="0.7" />
        <line x1="30" y1="27" x2="27" y2="25" stroke="white" strokeWidth="1.5" opacity="0.5" strokeLinecap="round" />
        <circle cx="24" cy="12" r="1.5" fill="#C7D2FE" />
        <line x1="15" y1="28" x2="33" y2="28" stroke="white" strokeWidth="1" opacity="0.25" strokeLinecap="round" strokeDasharray="2 3" />
      </svg>

      {/* ── Wordmark ── */}
      {showName && (
        <div
          style={{
            fontSize: nameSize,
            fontWeight: 800,
            letterSpacing: "-0.5px",
            lineHeight: 1.1,
            whiteSpace: "nowrap",
            color: textColor,
          }}
        >
          HACKATHON
          <span style={{ color: accentColor }}>BUDDY</span>
        </div>
      )}
    </div>
  );
}

