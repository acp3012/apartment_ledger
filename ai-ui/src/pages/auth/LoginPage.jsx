import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { authApi } from "../../api/api";
import { useAuth } from "../../context/AuthContext";
import { Alert, Field, TextInput, PasswordInput, BuildingIcon } from "../../components/UI";

export default function LoginPage({ onSwitchToRegister }) {
  const { login } = useAuth();
  const navigate   = useNavigate();

  const [form, setForm]       = useState({ email: "", password: "" });
  const [errors, setErrors]   = useState({});
  const [alert, setAlert]     = useState(null);
  const [loading, setLoading] = useState(false);

  const set = (field) => (e) => {
    setForm(p => ({ ...p, [field]: e.target.value }));
    setErrors(p => ({ ...p, [field]: "" }));
    setAlert(null);
  };

  const validate = () => {
    const e = {};
    if (!form.email.trim() || !/\S+@\S+\.\S+/.test(form.email))
      e.email    = "Enter a valid email address";
    if (!form.password)
      e.password = "Password is required";
    return e;
  };

  const handleSubmit = async (ev) => {
    ev.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }

    setLoading(true);
    try {
      const data = await authApi.login({ email: form.email, password: form.password });
      /*
        Expected response from Spring Boot:
        {
          token, userId, userName, email, role,
          flatId, apartmentName
        }
      */
      login(data);
      navigate("/dashboard");
    } catch (err) {
      setAlert({ type: "error", message: err.message || "Invalid email or password" });
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
          <button className="tab-btn active">Sign in</button>
          <button className="tab-btn" onClick={onSwitchToRegister}>Register</button>
        </div>

        <form onSubmit={handleSubmit} noValidate>
          <Alert type={alert?.type} message={alert?.message} />

          <Field label="Email address" htmlFor="l-email" error={errors.email}>
            <TextInput
              id="l-email" type="email" placeholder="you@example.com"
              autoComplete="email" value={form.email} onChange={set("email")}
              error={errors.email}
            />
          </Field>

          <Field label="Password" htmlFor="l-password" error={errors.password}>
            <PasswordInput
              id="l-password" placeholder="••••••••"
              autoComplete="current-password" value={form.password}
              onChange={set("password")} error={errors.password}
            />
          </Field>

          <div className="forgot-row">
            <button type="button" className="btn-link">Forgot password?</button>
          </div>

          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? "Signing in…" : "Sign in"}
          </button>
        </form>

        <p className="switch-text">
          New here?{" "}
          <button className="btn-link" onClick={onSwitchToRegister}>
            Create an account
          </button>
        </p>
      </div>
    </div>
  );
}
