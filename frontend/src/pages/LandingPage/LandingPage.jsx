import React, { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import {
  Sparkles,
  Users,
  Brain,
  Trophy,
  ArrowRight,
  Search,
} from "lucide-react";
import heroImage from "../../assets/hero-3d.svg";
import HBLogo from "../../components/HBLogo";
import "./LandingPage.css";

function LandingPage() {
  const navigate = useNavigate();
  const heroRef = useRef(null);
  const featuresRef = useRef(null);

  /* ── Fade-in on scroll (IntersectionObserver) ── */
  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            entry.target.classList.add("lp-visible");
          }
        });
      },
      { threshold: 0.15 }
    );

    const targets = document.querySelectorAll(".lp-animate");
    targets.forEach((el) => observer.observe(el));
    return () => targets.forEach((el) => observer.unobserve(el));
  }, []);

  return (
    <div className="lp">
      {/* ── NAVBAR ─────────────────────────────────────── */}
      <nav className="lp-nav">
        <div className="lp-nav-inner">
          {/* Brand */}
          <div
            className="lp-brand"
            onClick={() => navigate("/")}
            role="button"
            tabIndex={0}
          >
            <HBLogo size={38} nameSize={17} variant="dark" />
          </div>

          {/* Center nav links */}
          <div className="lp-nav-links">
            <a href="#features">Features</a>
            <a href="#about">About</a>
          </div>

          {/* Right actions */}
          <div className="lp-nav-actions">
            <button
              className="lp-signin-btn"
              onClick={() => navigate("/login")}
            >
              Sign In
            </button>
            <button
              className="lp-cta-nav-btn"
              onClick={() => navigate("/create-profile")}
            >
              Get Started
            </button>
          </div>
        </div>
      </nav>

      {/* ── HERO SECTION ───────────────────────────────── */}
      <section className="lp-hero" ref={heroRef}>
        {/* Decorative blurs */}
        <div className="lp-hero-glow lp-hero-glow--left" />
        <div className="lp-hero-glow lp-hero-glow--right" />

        <div className="lp-hero-inner">
          {/* Left: copy */}
          <div className="lp-hero-content lp-animate lp-fade-up">
            <div className="lp-hero-badge">
              <Sparkles size={14} />
              <span>AI-Powered Hackathon Platform</span>
            </div>

            <h1 className="lp-hero-title">
              Build Teams.
              <br />
              Create Ideas.
              <br />
              <span className="lp-gradient-text">Win Hackathons.</span>
            </h1>

            <p className="lp-hero-desc">
              The all-in-one platform to discover hackathons, form the perfect
              team with AI-powered matching, and generate winning project
              ideas&nbsp;— all in one place.
            </p>

            <div className="lp-hero-btns">
              <button
                className="lp-btn-primary"
                onClick={() => navigate("/create-profile")}
              >
                <span>Get Started</span>
                <ArrowRight size={18} />
              </button>

              <button
                className="lp-btn-secondary"
                onClick={() => navigate("/hackathons")}
              >
                <Search size={18} />
                <span>Explore Hackathons</span>
              </button>
            </div>
          </div>

          {/* Right: 3D hero illustration */}
          <div className="lp-hero-visual lp-animate lp-fade-up lp-delay-1">
            <img
              src={heroImage}
              alt="AI hackathon platform — team matching, idea generation, and hackathon discovery"
              className="lp-hero-img"
              loading="eager"
            />
          </div>
        </div>
      </section>

      {/* ── FEATURES SECTION ───────────────────────────── */}
      <section id="features" className="lp-features" ref={featuresRef}>
        <div className="lp-features-inner">
          <div className="lp-section-header lp-animate lp-fade-up">
            <p className="lp-section-kicker">Why Hackathon Buddy?</p>
            <h2 className="lp-section-title">Everything you need to win</h2>
          </div>

          <div className="lp-features-grid">
            {/* Card 1 */}
            <div className="lp-feature-card lp-animate lp-fade-up">
              <div className="lp-feature-icon lp-icon-violet">
                <Users size={24} />
              </div>
              <h3>AI-Powered Team Matching</h3>
              <p>
                Our smart algorithm analyzes skills, roles, and domain
                preferences to recommend teammates that perfectly complement
                your strengths.
              </p>
            </div>

            {/* Card 2 */}
            <div className="lp-feature-card lp-animate lp-fade-up lp-delay-1">
              <div className="lp-feature-icon lp-icon-teal">
                <Brain size={24} />
              </div>
              <h3>Smart Project Idea Generation</h3>
              <p>
                Generate unique, prize-worthy project ideas tailored to your
                team&rsquo;s tech stack and the hackathon&rsquo;s theme
                using&nbsp;AI.
              </p>
            </div>

            {/* Card 3 */}
            <div className="lp-feature-card lp-animate lp-fade-up lp-delay-2">
              <div className="lp-feature-icon lp-icon-amber">
                <Trophy size={24} />
              </div>
              <h3>Hackathon Discovery</h3>
              <p>
                Browse a curated directory of active hackathons with details on
                prizes, deadlines, and registration&nbsp;— all in one place.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* ── ABOUT / CTA STRIP ──────────────────────────── */}
      <section id="about" className="lp-cta-strip">
        <div className="lp-cta-strip-inner lp-animate lp-fade-up">
          <h2>Ready to win your next hackathon?</h2>
          <p>
            Join developers, designers, and creators who use Hackathon Buddy
            to build winning teams and ship great projects.
          </p>
          <div className="lp-hero-btns" style={{ justifyContent: "center" }}>
            <button
              className="lp-btn-primary"
              onClick={() => navigate("/create-profile")}
            >
              <span>Get Started — It's Free</span>
              <ArrowRight size={18} />
            </button>
            <button
              className="lp-btn-secondary"
              onClick={() => navigate("/login")}
            >
              <span>Sign In</span>
            </button>
          </div>
        </div>
      </section>

      {/* ── FOOTER ─────────────────────────────────────── */}
      <footer className="lp-footer">
        <div className="lp-footer-inner">
          <HBLogo size={30} nameSize={15} variant="dark" />
          <p>© 2026 HackathonBuddy. Built for hackathon builders.</p>
        </div>
      </footer>
    </div>
  );
}

export default LandingPage;
