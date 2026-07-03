import { useState } from "react";
import { userApi } from "../../api/api";
import {
  Alert, Field, TextInput, PasswordInput, StrengthBar, BuildingIcon,
} from "../../components/UI";

// Role IDs must match your seeded roles table in PostgreSQL
// roles: { role_id: 1, role_name: 'ADMIN' }, { role_id: 2, role_name: 'OWNER' }
const ROLE_ID = { ADMIN: 1, OWNER: 2 };

function RoleCard({ icon, title, subtitle, selected, onClick }) {
  return (
    <button type="button" onClick={onClick}
      className={`role-card ${selected ? "selected" : ""}`}>
      <span className="role-icon">{icon}</span>
      <div>
        <p className="role-title">{title}</p>
        <p className="role-sub">{subtitle}</p>
      </div>
    </button>
  );
}

export default function RegisterPage({ onSwitchToLogin, onRegistered }) {
  const [form, setForm] = useState({
    role:            "ADMIN",
    email:           "",
    mobileNumber:    "",
    flatNumber:      "",
    password:        "",
    confirmPassword: "",
  });
  const [errors, setErrors]   = useState({});
  const [alert, setAlert]     = useState(null);
  const [loading, setLoading] = useState(false);

  const set = (field) => (e) => {
    setForm(p => ({ ...p, [field]: e.target.value }));
    setErrors(p => ({ ...p, [field]: "" }));
    setAlert(null);
  };

  const setRole = (role) => {
    setForm(p => ({ ...p, role, flatNumber: "" }));
    setErrors(p => ({ ...p, flatNumber: "" }));
  };

  const validate = () => {
    const e = {};
    if (!form.email.trim() || !/\S+@\S+\.\S+/.test(form.email))
      e.email          = "Enter a valid email address";
    if (!form.mobileNumber.trim())
      e.mobileNumber   = "Mobile number is required";
    if (form.role === "OWNER" && !form.flatNumber.trim())
      e.flatNumber     = "Flat number is required for owners";
    if (!form.password || form.password.length < 8)
      e.password       = "Minimum 8 characters required";
    if (form.password !== form.confirmPassword)
      e.confirmPassword = "Passwords do not match";
    return e;
  };

  const handleSubmit = async (ev) => {
    ev.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }

    setLoading(true);
    try {
      /*
        POST /api/v1/users
        Payload matches AppUserRequest DTO.
        userName is derived from the email prefix (e.g. john from john@mail.com).
        Adjust if your backend expects a separate userName field.
      */
      const payload = {
        userName:     form.email.split("@")[0],
        email:        form.email,
        mobileNumber: form.mobileNumber,
        password:     form.password,
        roleId:       ROLE_ID[form.role],
        ...(form.role === "OWNER" && form.flatNumber
              ? { flatNumber: form.flatNumber }
              : {}),
      };

      await userApi.register(payload);
      setAlert({ type: "success", message: "Account created! You can now sign in." });
      setTimeout(() => onRegistered?.(), 1800);
    } catch (err) {
      setAlert({ type: "error", message: err.message || "Registration failed. Please try again." });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      {/* Brand */}
      <div className="auth-brand">
        <div className="auth-brand-icon"><BuildingIcon size={20} /></div>
        <span className="auth-brand-name">ApartmentLedger</span>
      </div>

      <div className="auth-card">
        {/* Tabs */}
        <div className="tabs">
          <button className="tab-btn" onClick={onSwitchToLogin}>Sign in</button>
          <button className="tab-btn active">Register</button>
        </div>

        <form onSubmit={handleSubmit} noValidate>
          <Alert type={alert?.type} message={alert?.message} />

          {/* Role */}
          <div className="field">
            <label>Account type</label>
            <div className="role-grid">
              <RoleCard icon="🛡️" title="Admin"  subtitle="Full access"
                selected={form.role === "ADMIN"} onClick={() => setRole("ADMIN")} />
              <RoleCard icon="🏠" title="Owner"  subtitle="Flat owner"
                selected={form.role === "OWNER"} onClick={() => setRole("OWNER")} />
            </div>
          </div>

          {/* Email */}
          <Field label="Email address" htmlFor="r-email" error={errors.email}>
            <TextInput
              id="r-email" type="email" placeholder="you@example.com"
              autoComplete="email" value={form.email} onChange={set("email")}
              error={errors.email}
            />
          </Field>

          {/* Mobile */}
          <Field label="Mobile number" htmlFor="r-mobile" error={errors.mobileNumber}>
            <TextInput
              id="r-mobile" type="tel" placeholder="+91 98765 43210"
              value={form.mobileNumber} onChange={set("mobileNumber")}
              error={errors.mobileNumber}
            />
          </Field>

          {/* Flat — only for OWNER */}
          {form.role === "OWNER" && (
            <Field label="Flat number" htmlFor="r-flat" error={errors.flatNumber}>
              <TextInput
                id="r-flat" type="text" placeholder="e.g. A-101"
                value={form.flatNumber} onChange={set("flatNumber")}
                error={errors.flatNumber}
              />
            </Field>
          )}

          {/* Password */}
          <Field label="Password" htmlFor="r-password" error={errors.password}>
            <PasswordInput
              id="r-password" placeholder="Min. 8 characters"
              autoComplete="new-password" value={form.password}
              onChange={set("password")} error={errors.password}
            />
            <StrengthBar password={form.password} />
          </Field>

          {/* Confirm */}
          <Field label="Confirm password" htmlFor="r-confirm" error={errors.confirmPassword}>
            <PasswordInput
              id="r-confirm" placeholder="Re-enter password"
              autoComplete="new-password" value={form.confirmPassword}
              onChange={set("confirmPassword")} error={errors.confirmPassword}
            />
          </Field>

          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? "Creating account…" : "Create account"}
          </button>
        </form>

        <p className="switch-text">
          Already have an account?{" "}
          <button className="btn-link" onClick={onSwitchToLogin}>Sign in</button>
        </p>
      </div>
    </div>
  );
}
