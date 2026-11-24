import React, { useMemo, useState, useEffect, useRef } from "react";
import {
  Send,
  Search,
  CheckCheck,
  Users as UsersIcon,
  MessageSquare,
  AlertCircle,
  Plus,
  X,
  Bot,
  Sparkles,
  Trash2,
  Loader2,
  Lightbulb,
  Zap,
  Target
} from "lucide-react";
import { useApp } from "../../context/AppContext";
import { chatAPI, teamAPI } from "../../api/api";
import "./Chat.css";

const SUGGESTION_PROMPTS = [
  "💡 Suggest winning project ideas for my tech stack",
  "👥 How should we structure roles in a 4-person team?",
  "🛠 Help me design our system architecture & database schema",
  "🏆 Give me a 3-minute hackathon pitch framework that wins",
  "⚡ What are common judging criteria and pitfalls to avoid?"
];

function Chat() {
  const { currentUser } = useApp();

  // Mode: "assistant" (AI Chatbot) or "squad" (Team Chat)
  const [chatMode, setChatMode] = useState("assistant");

  // ─── AI ASSISTANT STATE ─────────────────────────────
  const [assistantMessages, setAssistantMessages] = useState([]);
  const [loadingAssistant, setLoadingAssistant] = useState(false);
  const [isAiLoading, setIsAiLoading] = useState(false);
  const [aiInputText, setAiInputText] = useState("");
  const [aiError, setAiError] = useState(null);

  // ─── SQUAD CHAT STATE ───────────────────────────────
  const [teams, setTeams] = useState([]);
  const [selectedTeamId, setSelectedTeamId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [inputText, setInputText] = useState("");
  const [search, setSearch] = useState("");
  const [showRightSidebar, setShowRightSidebar] = useState(false);
  const [showMobileSidebar, setShowMobileSidebar] = useState(false);
  const [loadingTeams, setLoadingTeams] = useState(false);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [forbidden, setForbidden] = useState(false);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newTeamName, setNewTeamName] = useState("");
  const [newTeamDesc, setNewTeamDesc] = useState("");

  const messagesEndRef = useRef(null);
  const messagesAreaRef = useRef(null);

  // ──────────────────────────────────────────────────────────
  // 1. LOAD AI ASSISTANT CONVERSATION HISTORY
  // ──────────────────────────────────────────────────────────
  const loadAssistantHistory = async () => {
    setLoadingAssistant(true);
    setAiError(null);
    try {
      const res = await chatAPI.getAssistantMessages();
      if (res.data?.success && Array.isArray(res.data.data)) {
        setAssistantMessages(res.data.data);
      }
    } catch (err) {
      console.warn("Failed to load assistant history from backend:", err);
      // Fallback initial welcome if backend call fails
      setAssistantMessages([
        {
          id: "welcome-fallback",
          senderId: 0,
          senderName: "Hackathon Buddy AI",
          messageType: "AI",
          content: `👋 Hello ${currentUser?.firstName || "Developer"}! I am your **Hackathon Buddy AI Copilot**.\n\nI can help you with:\n• 💡 **Generating winning project ideas** tailored to your skills\n• 👥 **Finding compatible teammates** and auditing squad skill gaps\n• 🛠 **Designing full-stack architectures** and choosing tech stacks\n• 🏆 **Preparing 3-minute pitch decks** and demo strategies\n\nWhat would you like to build or discuss today?`,
          timestamp: new Date().toISOString()
        }
      ]);
    } finally {
      setLoadingAssistant(false);
    }
  };

  useEffect(() => {
    if (currentUser) {
      loadAssistantHistory();
    }
  }, [currentUser]);

  // ──────────────────────────────────────────────────────────
  // 2. SEND MESSAGE TO AI ASSISTANT
  // ──────────────────────────────────────────────────────────
  const handleSendAiMessage = async (customPrompt) => {
    const promptToSend = (typeof customPrompt === "string" ? customPrompt : aiInputText).trim();
    if (!promptToSend || isAiLoading) return;

    setAiInputText("");
    setAiError(null);

    // Optimistic UI update: append user message immediately
    const tempUserMsg = {
      id: "temp-" + Date.now(),
      senderId: currentUser?.id,
      senderName: currentUser?.fullName || currentUser?.firstName || "You",
      content: promptToSend,
      messageType: "USER",
      timestamp: new Date().toISOString()
    };

    setAssistantMessages((prev) => [...prev, tempUserMsg]);
    setIsAiLoading(true);

    try {
      const res = await chatAPI.sendAssistantMessage(promptToSend);
      if (res.data?.success && res.data.data) {
        const aiReply = res.data.data;
        setAssistantMessages((prev) => [...prev, aiReply]);
      } else {
        throw new Error(res.data?.message || "Failed to receive AI response");
      }
    } catch (err) {
      console.error("Error communicating with AI Assistant:", err);
      const errorMsg =
        err.response?.data?.message || err.message || "Could not reach AI Assistant. Please try again.";
      setAiError(errorMsg);

      // Append friendly error response bubble
      setAssistantMessages((prev) => [
        ...prev,
        {
          id: "err-" + Date.now(),
          senderId: 0,
          senderName: "Hackathon Buddy AI",
          messageType: "AI",
          content: `⚠️ **Connection Notice:** ${errorMsg}\n\n*Please ensure the backend is running, or verify your network connection and try again.*`,
          timestamp: new Date().toISOString()
        }
      ]);
    } finally {
      setIsAiLoading(false);
    }
  };

  // ──────────────────────────────────────────────────────────
  // 3. CLEAR ASSISTANT HISTORY
  // ──────────────────────────────────────────────────────────
  const handleClearAssistantHistory = async () => {
    if (!window.confirm("Are you sure you want to clear your AI Assistant chat history?")) return;
    try {
      await chatAPI.clearAssistantHistory();
      await loadAssistantHistory();
    } catch (e) {
      console.error("Failed to clear assistant history:", e);
      setAssistantMessages([]);
    }
  };

  // ──────────────────────────────────────────────────────────
  // 4. SQUAD CHAT MANAGEMENT
  // ──────────────────────────────────────────────────────────
  const loadMyTeams = async () => {
    setLoadingTeams(true);
    try {
      const res = await teamAPI.getAll();
      if (res.data?.success && Array.isArray(res.data.data)) {
        let teamList = res.data.data.map((t, idx) => ({
          id: t.id,
          name: t.name,
          short: t.name ? t.name.substring(0, 2).toUpperCase() : "TM",
          description: t.description,
          members: t.members || [],
          leader: t.leader,
          color: ["purple", "blue", "pink", "navy", "green"][idx % 5]
        }));

        if (teamList.length === 0 && currentUser) {
          try {
            const createRes = await teamAPI.create({
              name: "General Squad",
              description: "Default team channel for live squad collaboration"
            });
            if (createRes.data?.success && createRes.data.data) {
              const newTeam = createRes.data.data;
              teamList = [
                {
                  id: newTeam.id,
                  name: newTeam.name,
                  short: "GS",
                  description: newTeam.description,
                  members: newTeam.members || [],
                  leader: newTeam.leader,
                  color: "blue"
                }
              ];
            }
          } catch (e) {
            console.error("Error auto-creating default squad:", e);
          }
        }

        setTeams(teamList);
        if (teamList.length > 0 && (!selectedTeamId || !teamList.some((t) => t.id === selectedTeamId))) {
          setSelectedTeamId(teamList[0].id);
        }
      }
    } catch (err) {
      console.error("Failed to load user teams:", err);
    } finally {
      setLoadingTeams(false);
    }
  };

  useEffect(() => {
    loadMyTeams();
  }, [currentUser]);

  useEffect(() => {
    if (chatMode === "squad") {
      loadMyTeams();
    }
  }, [chatMode]);

  const fetchMessages = async (teamId) => {
    if (!teamId) return;
    setLoadingMessages(true);
    try {
      const res = await chatAPI.getMessages(teamId);
      if (res.data?.success && Array.isArray(res.data.data)) {
        setMessages(res.data.data);
        setForbidden(false);
      }
    } catch (err) {
      if (err.response?.status === 403) {
        setForbidden(true);
        setMessages([]);
      } else {
        console.error("Failed to fetch messages:", err);
      }
    } finally {
      setLoadingMessages(false);
    }
  };

  useEffect(() => {
    if (chatMode === "squad" && selectedTeamId) {
      fetchMessages(selectedTeamId);
    }
  }, [chatMode, selectedTeamId]);

  // Squad WebSocket & Polling
  useEffect(() => {
    if (chatMode !== "squad" || !selectedTeamId || forbidden) return;

    let ws;
    try {
      const protocol = window.location.protocol === "https:" ? "wss:" : "ws:";
      const wsUrl = `${protocol}//localhost:8081/ws/websocket`;
      ws = new WebSocket(wsUrl);

      ws.onopen = () => {
        ws.send("CONNECT\naccept-version:1.1,1.0\nheart-beat:10000,10000\n\n\0");
        setTimeout(() => {
          ws.send(`SUBSCRIBE\nid:sub-0\ndestination:/topic/chat/${selectedTeamId}\n\n\0`);
        }, 300);
      };

      ws.onmessage = (event) => {
        if (event.data && event.data.includes("MESSAGE")) {
          try {
            const bodyIdx = event.data.indexOf("\n\n");
            if (bodyIdx !== -1) {
              const jsonStr = event.data.substring(bodyIdx + 2, event.data.length - 1).trim();
              if (jsonStr.startsWith("{") && jsonStr.endsWith("}")) {
                const msg = JSON.parse(jsonStr);
                setMessages((prev) => {
                  if (prev.some((m) => m.id === msg.id)) return prev;
                  return [...prev, msg];
                });
              }
            }
          } catch (e) {}
        }
      };
    } catch (e) {
      console.warn("WebSocket warning:", e);
    }

    const pollInterval = setInterval(() => {
      chatAPI
        .getMessages(selectedTeamId)
        .then((res) => {
          if (res.data?.success && Array.isArray(res.data.data)) {
            setMessages((prev) => {
              const incoming = res.data.data;
              if (
                incoming.length === prev.length &&
                incoming[incoming.length - 1]?.id === prev[prev.length - 1]?.id &&
                incoming[incoming.length - 1]?.content === prev[prev.length - 1]?.content
              ) {
                return prev;
              }
              return incoming;
            });
          }
        })
        .catch(() => {});
    }, 2000);

    return () => {
      clearInterval(pollInterval);
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.close();
      }
    };
  }, [chatMode, selectedTeamId, forbidden]);

  // Scroll to bottom without scrolling window or ancestor elements
  const scrollToBottom = (smooth = true) => {
    if (messagesAreaRef.current) {
      const container = messagesAreaRef.current;
      if (smooth) {
        container.scrollTo({
          top: container.scrollHeight,
          behavior: "smooth"
        });
      } else {
        container.scrollTop = container.scrollHeight;
      }
    } else if (messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({
        behavior: smooth ? "smooth" : "auto",
        block: "nearest"
      });
    }
  };

  // Scroll to bottom on updates
  useEffect(() => {
    scrollToBottom(true);
  }, [assistantMessages, messages, isAiLoading]);

  // Scroll to bottom immediately on squad or mode change
  useEffect(() => {
    scrollToBottom(false);
  }, [chatMode, selectedTeamId]);

  const activeTeam = useMemo(() => {
    return teams.find((t) => t.id === selectedTeamId) || teams[0];
  }, [teams, selectedTeamId]);

  const filteredTeams = useMemo(() => {
    return teams.filter((t) => {
      const q = search.toLowerCase();
      return t.name.toLowerCase().includes(q) || (t.description && t.description.toLowerCase().includes(q));
    });
  }, [teams, search]);

  const handleSendSquadMessage = async () => {
    if (!inputText.trim() || !selectedTeamId) return;

    const content = inputText.trim();
    setInputText("");

    const tempMsg = {
      id: Date.now(),
      senderId: currentUser?.id,
      senderName: currentUser?.fullName || currentUser?.firstName || "You",
      content: content,
      timestamp: new Date().toISOString()
    };
    setMessages((prev) => [...prev, tempMsg]);

    try {
      const res = await chatAPI.sendMessage(selectedTeamId, content);
      if (res.data?.success && res.data.data) {
        const savedMsg = res.data.data;
        setMessages((prev) => prev.map((m) => (m.id === tempMsg.id ? savedMsg : m)));
      }
    } catch (err) {
      console.error("Error sending squad message:", err);
      setMessages((prev) => prev.filter((m) => m.id !== tempMsg.id));
      if (err.response?.status === 403) {
        alert("You are not authorized to send messages to this team.");
      }
    }
  };

  const handleCreateTeam = async (e) => {
    e.preventDefault();
    if (!newTeamName.trim()) return;
    try {
      const res = await teamAPI.create({
        name: newTeamName.trim(),
        description: newTeamDesc.trim() || "Team chat channel"
      });
      if (res.data?.success && res.data.data) {
        setShowCreateModal(false);
        setNewTeamName("");
        setNewTeamDesc("");
        await loadMyTeams();
      }
    } catch (err) {
      alert("Failed to create squad: " + (err.response?.data?.message || err.message));
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      if (chatMode === "assistant") {
        handleSendAiMessage();
      } else {
        handleSendSquadMessage();
      }
    }
  };

  const formatTime = (ts) => {
    if (!ts) return "Just now";
    try {
      const date = new Date(ts);
      return date.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" });
    } catch (e) {
      return "Just now";
    }
  };

  // Helper to render markdown-like formatted text cleanly in chat bubbles
  const renderFormattedContent = (text) => {
    if (!text) return "";
    const lines = text.split("\n");
    return lines.map((line, idx) => {
      if (line.startsWith("### ")) {
        return (
          <h4 key={idx} style={{ margin: "10px 0 6px 0", color: "#38bdf8", fontSize: "16px", fontWeight: "bold" }}>
            {line.replace("### ", "")}
          </h4>
        );
      }
      if (line.startsWith("## ")) {
        return (
          <h3 key={idx} style={{ margin: "12px 0 6px 0", color: "#38bdf8", fontSize: "17px", fontWeight: "bold" }}>
            {line.replace("## ", "")}
          </h3>
        );
      }
      if (line.startsWith("- ") || line.startsWith("• ")) {
        return (
          <div key={idx} style={{ display: "flex", gap: "6px", margin: "4px 0", paddingLeft: "8px" }}>
            <span style={{ color: "#38bdf8" }}>•</span>
            <span>{renderInlineBold(line.substring(2))}</span>
          </div>
        );
      }
      if (line.match(/^\d+\.\s/)) {
        return (
          <div key={idx} style={{ margin: "6px 0", paddingLeft: "4px" }}>
            {renderInlineBold(line)}
          </div>
        );
      }
      if (line.trim() === "") {
        return <div key={idx} style={{ height: "6px" }} />;
      }
      return (
        <p key={idx} style={{ margin: "4px 0", lineHeight: "1.5" }}>
          {renderInlineBold(line)}
        </p>
      );
    });
  };

  const renderInlineBold = (str) => {
    const parts = str.split(/(\*\*.*?\*\*)/g);
    return parts.map((part, i) => {
      if (part.startsWith("**") && part.endsWith("**")) {
        return <strong key={i} style={{ color: "#f8fafc" }}>{part.slice(2, -2)}</strong>;
      }
      return part;
    });
  };

  return (
    <div className="chat-page">
      {/* Mobile Drawer Backdrop */}
      {showMobileSidebar && (
        <div
          className="chat-mobile-backdrop"
          onClick={() => setShowMobileSidebar(false)}
        />
      )}

      {/* ─── LEFT SIDEBAR ─────────────────────────────── */}
      <aside className={`chat-sidebar ${showMobileSidebar ? "mobile-open" : ""}`}>
        <div className="sidebar-header" style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <div>
            <div className="section-kicker">COLLABORATION HUB</div>
            <h1>Messages</h1>
          </div>
          {chatMode === "squad" && (
            <button
              onClick={() => setShowCreateModal(true)}
              style={{
                background: "#38bdf8",
                color: "#0f172a",
                border: "none",
                borderRadius: "8px",
                padding: "6px 10px",
                cursor: "pointer",
                display: "flex",
                alignItems: "center",
                gap: "4px",
                fontSize: "12px",
                fontWeight: "bold"
              }}
              title="Create New Squad"
            >
              <Plus size={14} /> New Squad
            </button>
          )}
        </div>

        {/* MODE SELECTOR TABS */}
        <div style={{ display: "flex", padding: "0 18px 14px", gap: "8px" }}>
          <button
            onClick={() => setChatMode("assistant")}
            style={{
              flex: 1,
              padding: "10px 8px",
              background: chatMode === "assistant" ? "linear-gradient(135deg, #6366f1, #38bdf8)" : "#1e293b",
              color: chatMode === "assistant" ? "#ffffff" : "#94a3b8",
              border: "1px solid " + (chatMode === "assistant" ? "#818cf8" : "#334155"),
              borderRadius: "10px",
              cursor: "pointer",
              fontWeight: "bold",
              fontSize: "13px",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: "6px",
              transition: "all 0.2s"
            }}
          >
            <Bot size={16} /> AI Copilot
          </button>
          <button
            onClick={() => setChatMode("squad")}
            style={{
              flex: 1,
              padding: "10px 8px",
              background: chatMode === "squad" ? "linear-gradient(135deg, #3b82f6, #06b6d4)" : "#1e293b",
              color: chatMode === "squad" ? "#ffffff" : "#94a3b8",
              border: "1px solid " + (chatMode === "squad" ? "#60a5fa" : "#334155"),
              borderRadius: "10px",
              cursor: "pointer",
              fontWeight: "bold",
              fontSize: "13px",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              gap: "6px",
              transition: "all 0.2s"
            }}
          >
            <UsersIcon size={16} /> Squads ({teams.length})
          </button>
        </div>

        {/* SIDEBAR CONTENT ACCORDING TO ACTIVE MODE */}
        {chatMode === "assistant" ? (
          <div style={{ flex: 1, padding: "16px 18px", overflowY: "auto" }}>
            <div
              style={{
                background: "rgba(99, 102, 241, 0.12)",
                border: "1px solid rgba(99, 102, 241, 0.3)",
                borderRadius: "12px",
                padding: "14px",
                marginBottom: "16px"
              }}
            >
              <div style={{ display: "flex", alignItems: "center", gap: "8px", color: "#a5b4fc", fontWeight: "bold", fontSize: "13px" }}>
                <Sparkles size={16} /> HACKATHON ASSISTANT
              </div>
              <p style={{ fontSize: "12px", color: "#cbd5e1", marginTop: "6px", lineHeight: "1.5" }}>
                Ask anything about hackathon ideation, system architectures, teammate recruitment, and presentation pitches.
              </p>
            </div>

            <div style={{ fontSize: "12px", fontWeight: "bold", color: "#64748b", textTransform: "uppercase", letterSpacing: "1px", marginBottom: "8px" }}>
              Quick Starters
            </div>
            <div style={{ display: "flex", flexDirection: "column", gap: "8px" }}>
              {SUGGESTION_PROMPTS.map((prompt, i) => (
                <button
                  key={i}
                  onClick={() => handleSendAiMessage(prompt)}
                  disabled={isAiLoading}
                  style={{
                    background: "#0f172a",
                    border: "1px solid #334155",
                    color: "#cbd5e1",
                    padding: "9px 12px",
                    borderRadius: "8px",
                    fontSize: "12px",
                    textAlign: "left",
                    cursor: isAiLoading ? "not-allowed" : "pointer",
                    transition: "all 0.15s",
                    display: "flex",
                    alignItems: "center",
                    gap: "6px"
                  }}
                  onMouseEnter={(e) => {
                    if (!isAiLoading) e.currentTarget.style.borderColor = "#38bdf8";
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.borderColor = "#334155";
                  }}
                >
                  <span style={{ fontSize: "12px" }}>{prompt}</span>
                </button>
              ))}
            </div>

            <div style={{ marginTop: "24px", paddingTop: "16px", borderTop: "1px solid #334155" }}>
              <button
                onClick={handleClearAssistantHistory}
                style={{
                  width: "100%",
                  padding: "8px",
                  background: "transparent",
                  border: "1px solid #ef4444",
                  color: "#ef4444",
                  borderRadius: "8px",
                  cursor: "pointer",
                  fontSize: "12px",
                  fontWeight: "bold",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  gap: "6px"
                }}
              >
                <Trash2 size={14} /> Clear AI History
              </button>
            </div>
          </div>
        ) : (
          <>
            <div className="conversation-search">
              <Search size={16} />
              <input
                type="text"
                placeholder="Search teams..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
              />
            </div>

            <div className="conversation-list">
              {loadingTeams ? (
                <div style={{ padding: "20px", textAlign: "center", color: "#94a3b8" }}>Loading squads...</div>
              ) : filteredTeams.length === 0 ? (
                <div style={{ padding: "20px", textAlign: "center", color: "#94a3b8", fontSize: "14px" }}>
                  No squads found. Click "+ New Squad" to start!
                </div>
              ) : (
                filteredTeams.map((t) => (
                  <button
                    key={t.id}
                    className={`conversation-item ${selectedTeamId === t.id ? "selected" : ""}`}
                    onClick={() => setSelectedTeamId(t.id)}
                  >
                    <div className={`conversation-avatar ${t.color}`}>
                      {t.short}
                      <span className="online-dot" />
                    </div>

                    <div className="conversation-content">
                      <div className="conversation-top">
                        <strong>{t.name}</strong>
                        <span>{t.members.length} members</span>
                      </div>
                      <div className="conversation-bottom">
                        <p>{t.description || "Team Channel"}</p>
                      </div>
                    </div>
                  </button>
                ))
              )}
            </div>
          </>
        )}

        {/* SIDEBAR FOOTER */}
        <div className="sidebar-user">
          <div className="user-avatar">{currentUser?.firstName?.charAt(0) || "U"}</div>
          <div>
            <strong>{currentUser?.fullName || currentUser?.firstName || "User"}</strong>
            <span>{chatMode === "assistant" ? "Active with AI Copilot" : "Active in Team Chat"}</span>
          </div>
          <span className="user-online">●</span>
        </div>
      </aside>

      {/* ─── MAIN CHAT AREA ───────────────────────────── */}
      <main className="chat-main">
        {chatMode === "assistant" ? (
          <>
            {/* ASSISTANT HEADER */}
            <header className="chat-header">
              <div className="chat-title-area">
                <button
                  className="mobile-sidebar-toggle-btn"
                  onClick={() => setShowMobileSidebar(!showMobileSidebar)}
                  title="Toggle Chats / Squads"
                  type="button"
                >
                  <MessageSquare size={18} />
                </button>
                <div
                  className="large-chat-avatar"
                  style={{
                    background: "linear-gradient(135deg, #6366f1, #38bdf8)",
                    color: "#ffffff",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center"
                  }}
                >
                  <Bot size={22} />
                  <span className="online-dot" />
                </div>
                <div>
                  <h2 style={{ display: "flex", alignItems: "center", gap: "8px" }}>
                    Hackathon Buddy AI Copilot
                    <span
                      style={{
                        background: "rgba(56, 189, 248, 0.15)",
                        color: "#38bdf8",
                        border: "1px solid #38bdf8",
                        fontSize: "11px",
                        padding: "2px 6px",
                        borderRadius: "12px",
                        fontWeight: "normal"
                      }}
                    >
                      AI Verified
                    </span>
                  </h2>
                  <span>⚡ 24/7 Intelligent Hackathon Solution Architect</span>
                </div>
              </div>

              <div className="chat-header-actions">
                <button
                  onClick={handleClearAssistantHistory}
                  title="Clear Assistant Conversation"
                  style={{ display: "flex", alignItems: "center", gap: "4px", fontSize: "12px" }}
                >
                  <Trash2 size={16} />
                </button>
              </div>
            </header>

            {/* ASSISTANT MESSAGES STREAM */}
            <section className="messages-area" ref={messagesAreaRef}>
              <div className="project-banner">
                <div className="project-banner-icon">
                  <Sparkles size={16} style={{ color: "#38bdf8" }} />
                </div>
                <div className="project-banner-content">
                  <strong>AI Copilot Session</strong>
                  <span>Ask anything about projects, stacks, teammates, or pitches. Answers persist across sessions.</span>
                </div>
              </div>

              <div className="today-divider">
                <span />
                <b>AI CONVERSATION HISTORY</b>
                <span />
              </div>

              <div className="messages-list">
                {loadingAssistant ? (
                  <div style={{ textAlign: "center", color: "#94a3b8", padding: "30px" }}>
                    <Loader2 size={24} className="animate-spin" style={{ margin: "0 auto 8px" }} />
                    Loading conversation history...
                  </div>
                ) : assistantMessages.length === 0 ? (
                  <div style={{ textAlign: "center", color: "#94a3b8", padding: "40px 20px" }}>
                    No messages yet. Send a question below or click a starter chip to begin!
                  </div>
                ) : (
                  assistantMessages.map((m) => {
                    const isAi = m.senderId === 0 || m.messageType === "AI" || m.senderName?.includes("AI");

                    return (
                      <div
                        key={m.id || Math.random()}
                        className={`message-row ${!isAi ? "sent-row" : "received-row"}`}
                      >
                        {isAi && (
                          <div
                            className="message-avatar"
                            style={{
                              background: "linear-gradient(135deg, #6366f1, #38bdf8)",
                              color: "#fff",
                              display: "flex",
                              alignItems: "center",
                              justifyContent: "center"
                            }}
                          >
                            <Bot size={16} />
                          </div>
                        )}
                        <div className="message-block">
                          <span className="message-sender">{isAi ? "Hackathon Buddy AI" : "You"}</span>
                          <div
                            className={`message-bubble ${!isAi ? "sent-bubble" : "received-bubble"}`}
                            style={{
                              background: isAi ? "#1e293b" : "#6366f1",
                              color: "#f8fafc",
                              border: isAi ? "1px solid #334155" : "none",
                              maxWidth: "85%",
                              padding: "12px 16px"
                            }}
                          >
                            {isAi ? renderFormattedContent(m.content) : m.content}
                          </div>
                          <div className={`message-time ${!isAi ? "sent-time" : ""}`}>
                            {formatTime(m.timestamp || m.createdAt)}
                            {!isAi && <CheckCheck size={14} style={{ color: "#38bdf8", marginLeft: "4px" }} />}
                          </div>
                        </div>
                      </div>
                    );
                  })
                )}

                {/* AI LOADING TYPING INDICATOR */}
                {isAiLoading && (
                  <div className="message-row received-row">
                    <div
                      className="message-avatar"
                      style={{
                        background: "linear-gradient(135deg, #6366f1, #38bdf8)",
                        color: "#fff",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center"
                      }}
                    >
                      <Bot size={16} />
                    </div>
                    <div className="message-block">
                      <span className="message-sender">Hackathon Buddy AI</span>
                      <div
                        className="message-bubble received-bubble"
                        style={{
                          background: "#1e293b",
                          border: "1px solid #334155",
                          padding: "12px 18px",
                          display: "flex",
                          alignItems: "center",
                          gap: "8px",
                          color: "#38bdf8"
                        }}
                      >
                        <Loader2 size={16} className="animate-spin" />
                        <span style={{ fontSize: "13px" }}>Analyzing request & drafting solution...</span>
                      </div>
                    </div>
                  </div>
                )}

                <div ref={messagesEndRef} />
              </div>
            </section>

            {/* AI COMPOSER */}
            <div className="chat-composer-wrapper">
              {aiError && (
                <div style={{ color: "#f87171", fontSize: "12px", marginBottom: "8px", textAlign: "center" }}>
                  {aiError}
                </div>
              )}
              <div className="composer">
                <textarea
                  value={aiInputText}
                  onChange={(e) => setAiInputText(e.target.value)}
                  onKeyDown={handleKeyDown}
                  placeholder="Ask Hackathon Buddy AI (e.g. 'Suggest AI project ideas for React & Python')..."
                  rows="1"
                  disabled={isAiLoading}
                />

                <button
                  className="send-btn"
                  onClick={() => handleSendAiMessage()}
                  disabled={isAiLoading || !aiInputText.trim()}
                  style={{
                    opacity: isAiLoading || !aiInputText.trim() ? 0.6 : 1,
                    cursor: isAiLoading || !aiInputText.trim() ? "not-allowed" : "pointer"
                  }}
                  title="Send query"
                >
                  {isAiLoading ? <Loader2 size={16} className="animate-spin" /> : <Send size={16} />}
                </button>
              </div>
            </div>
          </>
        ) : (
          /* ─── SQUAD CHAT VIEW ───────────────────────── */
          <>
            {forbidden ? (
              <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "40px", textAlign: "center", color: "#ef4444" }}>
                <AlertCircle size={48} style={{ marginBottom: "16px" }} />
                <h2>Access Denied</h2>
                <p style={{ color: "#94a3b8", marginTop: "8px" }}>You are not a member of this team and cannot view or send messages.</p>
              </div>
            ) : !activeTeam ? (
              <div style={{ flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", padding: "40px", textAlign: "center" }}>
                <MessageSquare size={48} style={{ color: "#38bdf8", marginBottom: "16px" }} />
                <h2>No Squad Selected</h2>
                <p style={{ color: "#94a3b8", marginTop: "8px" }}>Create or select a squad from the sidebar to start chatting.</p>
              </div>
            ) : (
              <>
                <header className="chat-header">
                  <div className="chat-title-area">
                    <button
                      className="mobile-sidebar-toggle-btn"
                      onClick={() => setShowMobileSidebar(!showMobileSidebar)}
                      title="Toggle Chats / Squads"
                      type="button"
                    >
                      <MessageSquare size={18} />
                    </button>
                    <div className="large-chat-avatar">
                      {activeTeam.short}
                      <span className="online-dot" />
                    </div>
                    <div>
                      <h2>{activeTeam.name}</h2>
                      <span>👥 {activeTeam.members?.length || 1} Squad Members</span>
                    </div>
                  </div>

                  <div className="chat-header-actions">
                    <button onClick={() => setShowRightSidebar(!showRightSidebar)} title="Toggle Squad Members">
                      <UsersIcon size={18} />
                    </button>
                  </div>
                </header>

                <section className="messages-area" ref={messagesAreaRef}>
                  <div className="project-banner">
                    <div className="project-banner-icon">#</div>
                    <div className="project-banner-content">
                      <strong>{activeTeam.name}</strong>
                      <span>Official Squad Channel</span>
                    </div>
                  </div>

                  <div className="today-divider">
                    <span />
                    <b>LIVE SQUAD CHAT</b>
                    <span />
                  </div>

                  <div className="messages-list">
                    {loadingMessages && messages.length === 0 ? (
                      <div style={{ textAlign: "center", color: "#94a3b8", padding: "20px" }}>Loading messages...</div>
                    ) : messages.length === 0 ? (
                      <div style={{ textAlign: "center", color: "#94a3b8", padding: "40px 20px" }}>
                        No messages in <b>#{activeTeam.name}</b> yet. Type below to start the conversation!
                      </div>
                    ) : (
                      messages.map((m) => {
                        const isSystem = m.messageType === "SYSTEM" || m.senderId === 0 || m.senderName === "System";
                        if (isSystem) {
                          return (
                            <div key={m.id || Math.random()} style={{ textAlign: "center", margin: "14px 0" }}>
                              <span style={{
                                display: "inline-block",
                                background: "rgba(99, 102, 241, 0.12)",
                                color: "#a5b4fc",
                                border: "1px solid rgba(99, 102, 241, 0.25)",
                                padding: "6px 16px",
                                borderRadius: "20px",
                                fontSize: "12.5px",
                                fontWeight: "500",
                                boxShadow: "0 2px 6px rgba(0,0,0,0.2)"
                              }}>
                                {m.content}
                              </span>
                            </div>
                          );
                        }

                        const isSentByMe =
                          currentUser &&
                          (String(m.senderId) === String(currentUser.id) ||
                            (m.senderName && currentUser.fullName && m.senderName.toLowerCase() === currentUser.fullName.toLowerCase()) ||
                            (m.senderName && currentUser.firstName && m.senderName.toLowerCase().includes(currentUser.firstName.toLowerCase())) ||
                            (m.senderName && currentUser.email && m.senderName.toLowerCase() === currentUser.email.toLowerCase()));

                        const displayName = m.senderName && m.senderName !== "null" && m.senderName !== "null null"
                          ? m.senderName
                          : "Squad Member";

                        const senderInitial = displayName ? displayName.charAt(0).toUpperCase() : "U";

                        return (
                          <div key={m.id || Math.random()} className={`message-row ${isSentByMe ? "sent-row" : "received-row"}`}>
                            {!isSentByMe && <div className="message-avatar">{senderInitial}</div>}
                            <div className="message-block">
                              <span className="message-sender">{displayName}</span>
                              <div className={`message-bubble ${isSentByMe ? "sent-bubble" : "received-bubble"}`}>
                                {m.content}
                              </div>
                              <div className={`message-time ${isSentByMe ? "sent-time" : ""}`}>
                                {formatTime(m.timestamp || m.createdAt)}
                                {isSentByMe && <CheckCheck size={14} style={{ color: "#38bdf8", marginLeft: "4px" }} />}
                              </div>
                            </div>
                          </div>
                        );
                      })
                    )}
                    <div ref={messagesEndRef} />
                  </div>
                </section>

                <div className="chat-composer-wrapper">
                  <div className="composer">
                    <textarea
                      value={inputText}
                      onChange={(e) => setInputText(e.target.value)}
                      onKeyDown={handleKeyDown}
                      placeholder={`Message #${activeTeam.name}...`}
                      rows="1"
                    />

                    <button className="send-btn" onClick={handleSendSquadMessage} title="Send message">
                      <Send size={16} />
                    </button>
                  </div>
                </div>
              </>
            )}
          </>
        )}
      </main>

      {/* ─── RIGHT SIDEBAR (SQUAD DETAILS) ────────────── */}
      {chatMode === "squad" ? (
        <aside className={`project-sidebar ${showRightSidebar ? "open" : ""}`}>
          <div className="project-sidebar-header">
            <span>SQUAD DETAILS</span>
          </div>

          <div className="project-icon">&lt;/&gt;</div>
          <h2>{activeTeam?.name}</h2>
          <p>{activeTeam?.description || "Live squad collaboration space."}</p>

          <div className="project-divider" />

          <section className="project-section">
            <div className="section-title-row">
              <h3>Members</h3>
              <span>{activeTeam?.members?.length || 0}</span>
            </div>

            <div className="members-list">
              {activeTeam?.members?.map((member) => {
                const memName = member.name || member.fullName || member.email || "Squad Member";
                const memInitial = memName.charAt(0).toUpperCase();
                return (
                  <div className="member-item" key={member.id || member.userId || Math.random()}>
                    <div className="member-avatar">
                      {memInitial}
                      <span className="member-online" />
                    </div>
                    <div>
                      <strong>{memName}</strong>
                      <span>{member.role || "Member"}</span>
                    </div>
                  </div>
                );
              })}
            </div>
          </section>
        </aside>
      ) : (
        /* ASSISTANT CAPABILITIES SIDEBAR */
        <aside className="project-sidebar" style={{ display: "flex", flexDirection: "column", padding: "20px" }}>
          <div className="project-sidebar-header">
            <span>AI CAPABILITIES</span>
          </div>

          <div
            style={{
              width: "60px",
              height: "60px",
              borderRadius: "16px",
              background: "linear-gradient(135deg, #6366f1, #38bdf8)",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              margin: "10px auto 16px",
              color: "#fff"
            }}
          >
            <Bot size={32} />
          </div>

          <h2 style={{ textAlign: "center", fontSize: "18px", margin: "0 0 6px" }}>Hackathon Buddy AI</h2>
          <p style={{ textAlign: "center", fontSize: "13px", color: "#94a3b8", margin: "0 0 20px" }}>
            Your 24/7 dedicated Hackathon Architect & Strategist
          </p>

          <div className="project-divider" />

          <div style={{ display: "flex", flexDirection: "column", gap: "14px", marginTop: "12px" }}>
            <div style={{ display: "flex", gap: "10px", alignItems: "flex-start" }}>
              <div style={{ padding: "6px", background: "rgba(99, 102, 241, 0.15)", borderRadius: "8px", color: "#818cf8" }}>
                <Lightbulb size={18} />
              </div>
              <div>
                <strong style={{ fontSize: "13px", color: "#f8fafc", display: "block" }}>Idea Blueprints</strong>
                <span style={{ fontSize: "12px", color: "#94a3b8" }}>High-scoring, viable hackathon project concepts tailored to skills.</span>
              </div>
            </div>

            <div style={{ display: "flex", gap: "10px", alignItems: "flex-start" }}>
              <div style={{ padding: "6px", background: "rgba(56, 189, 248, 0.15)", borderRadius: "8px", color: "#38bdf8" }}>
                <Target size={18} />
              </div>
              <div>
                <strong style={{ fontSize: "13px", color: "#f8fafc", display: "block" }}>Squad Strategy</strong>
                <span style={{ fontSize: "12px", color: "#94a3b8" }}>Balanced role allocation and skill gap remediation advice.</span>
              </div>
            </div>

            <div style={{ display: "flex", gap: "10px", alignItems: "flex-start" }}>
              <div style={{ padding: "6px", background: "rgba(34, 197, 94, 0.15)", borderRadius: "8px", color: "#4ade80" }}>
                <Zap size={18} />
              </div>
              <div>
                <strong style={{ fontSize: "13px", color: "#f8fafc", display: "block" }}>Rapid Execution</strong>
                <span style={{ fontSize: "12px", color: "#94a3b8" }}>Tech stack choices, API contract blueprints, and database models.</span>
              </div>
            </div>
          </div>
        </aside>
      )}

      {/* CREATE SQUAD MODAL */}
      {showCreateModal && (
        <div style={{ position: "fixed", inset: 0, background: "rgba(15, 23, 42, 0.75)", backdropFilter: "blur(4px)", display: "flex", alignItems: "center", justifyContent: "center", zIndex: 9999 }}>
          <div style={{ background: "#1e293b", border: "1px solid #334155", borderRadius: "12px", width: "90%", maxWidth: "420px", padding: "24px", color: "#fff" }}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: "16px" }}>
              <h3 style={{ margin: 0, fontSize: "18px", fontWeight: "bold" }}>Create New Squad</h3>
              <button onClick={() => setShowCreateModal(false)} style={{ background: "none", border: "none", color: "#94a3b8", cursor: "pointer" }}>
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleCreateTeam}>
              <div style={{ marginBottom: "16px" }}>
                <label style={{ display: "block", fontSize: "13px", color: "#94a3b8", marginBottom: "6px" }}>Squad Name</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. AI Champions"
                  value={newTeamName}
                  onChange={(e) => setNewTeamName(e.target.value)}
                  style={{ width: "100%", padding: "10px", background: "#0f172a", border: "1px solid #334155", borderRadius: "6px", color: "#fff", outline: "none" }}
                />
              </div>
              <div style={{ marginBottom: "20px" }}>
                <label style={{ display: "block", fontSize: "13px", color: "#94a3b8", marginBottom: "6px" }}>Description</label>
                <input
                  type="text"
                  placeholder="e.g. Building AI hackathon project"
                  value={newTeamDesc}
                  onChange={(e) => setNewTeamDesc(e.target.value)}
                  style={{ width: "100%", padding: "10px", background: "#0f172a", border: "1px solid #334155", borderRadius: "6px", color: "#fff", outline: "none" }}
                />
              </div>
              <div style={{ display: "flex", justifyContent: "flex-end", gap: "10px" }}>
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  style={{ padding: "8px 16px", background: "transparent", border: "1px solid #475569", color: "#cbd5e1", borderRadius: "6px", cursor: "pointer" }}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  style={{ padding: "8px 16px", background: "#38bdf8", border: "none", color: "#0f172a", fontWeight: "bold", borderRadius: "6px", cursor: "pointer" }}
                >
                  Create & Open Chat
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}

export default Chat;