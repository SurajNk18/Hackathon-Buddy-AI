import React from "react";
import { useLocation } from "react-router-dom";
import Header from "./Header";
import Footer from "./Footer";

function Layout({ children }) {
  const location = useLocation();
  const isChatPage = location.pathname === "/chat";

  return (
    <div
      className={`app-layout ${isChatPage ? "chat-layout-active" : ""}`}
      style={{
        display: "flex",
        flexDirection: "column",
        height: isChatPage ? "100vh" : undefined,
        minHeight: "100vh",
        maxHeight: isChatPage ? "100vh" : undefined,
        overflow: isChatPage ? "hidden" : undefined
      }}
    >
      <Header />
      <div
        className="app-main-content"
        style={{
          flex: 1,
          minHeight: 0,
          display: isChatPage ? "flex" : undefined,
          flexDirection: isChatPage ? "column" : undefined,
          height: isChatPage ? "calc(100vh - 68px)" : undefined,
          maxHeight: isChatPage ? "calc(100vh - 68px)" : undefined,
          overflow: isChatPage ? "hidden" : undefined
        }}
      >
        {children}
      </div>
      {!isChatPage && <Footer />}
    </div>
  );
}

export default Layout;
