import { useState } from "react";
import LoginPage    from "./LoginPage";
import RegisterPage from "./RegisterPage";

export default function AuthPage() {
  const [tab, setTab] = useState("login");

  return tab === "login" ? (
    <LoginPage
      onSwitchToRegister={() => setTab("register")}
    />
  ) : (
    <RegisterPage
      onSwitchToLogin={() => setTab("login")}
      onRegistered={() => setTab("login")}
    />
  );
}
