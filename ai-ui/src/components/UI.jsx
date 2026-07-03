import { useState } from "react";

// ── Eye icons ─────────────────────────────────────────────────────
export function EyeIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
      stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M1 12S5 4 12 4s11 8 11 8-4 8-11 8S1 12 1 12z" />
      <circle cx="12" cy="12" r="3" />
    </svg>
  );
}
export function EyeOffIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none"
      stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M17.94 17.94A10.07 10.07 0 0112 20C5 20 1 12 1 12a18.09 18.09 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19M1 1l22 22" />
    </svg>
  );
}

// ── Building icon (brand) ─────────────────────────────────────────
export function BuildingIcon({ size = 20 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none"
      stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 21h18M9 8h1m-1 4h1m4-4h1m-1 4h1M5 21V5a2 2 0 012-2h10a2 2 0 012 2v16" />
    </svg>
  );
}

// ── Alert box ─────────────────────────────────────────────────────
export function Alert({ type, message }) {
  if (!message) return null;
  return <div className={`alert ${type}`}>{message}</div>;
}

// ── Form field wrapper ────────────────────────────────────────────
export function Field({ label, htmlFor, error, children }) {
  return (
    <div className="field">
      {label && <label htmlFor={htmlFor}>{label}</label>}
      {children}
      {error && <p className="field-error">{error}</p>}
    </div>
  );
}

// ── Plain text input ──────────────────────────────────────────────
export function TextInput({ error, className = "", ...props }) {
  return (
    <input
      className={`input ${error ? "error" : ""} ${className}`}
      {...props}
    />
  );
}

// ── Password input with show/hide toggle ──────────────────────────
export function PasswordInput({ error, ...props }) {
  const [show, setShow] = useState(false);
  return (
    <div className="input-wrap">
      <input
        type={show ? "text" : "password"}
        className={`input ${error ? "error" : ""}`}
        {...props}
      />
      <button
        type="button"
        className="eye-btn"
        onClick={() => setShow(v => !v)}
        aria-label={show ? "Hide password" : "Show password"}
      >
        {show ? <EyeOffIcon /> : <EyeIcon />}
      </button>
    </div>
  );
}

// ── Password strength bar ─────────────────────────────────────────
export function StrengthBar({ password }) {
  if (!password) return null;
  let score = 0;
  if (password.length >= 8) score++;
  if (/[A-Z]/.test(password)) score++;
  if (/[0-9]/.test(password)) score++;
  if (/[^A-Za-z0-9]/.test(password)) score++;
  const levels = [
    null,
    { label: "Weak",   color: "var(--s-weak)",   width: "25%" },
    { label: "Fair",   color: "var(--s-fair)",   width: "50%" },
    { label: "Good",   color: "var(--s-good)",   width: "75%" },
    { label: "Strong", color: "var(--s-strong)", width: "100%" },
  ];
  const lvl = levels[score] || levels[1];
  return (
    <>
      <div className="strength-bar-wrap">
        <div className="strength-bar"
          style={{ width: lvl.width, background: lvl.color }} />
      </div>
      <p className="strength-label" style={{ color: lvl.color }}>{lvl.label}</p>
    </>
  );
}
