import { useState, useEffect, type FormEvent, type ChangeEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { authApi } from "../services/api";
import { useAuth } from "../context/AuthContext";
import axios from "axios";

export default function Register() {
  const navigate = useNavigate();
  const { login, isAuthenticated } = useAuth();

  // Already logged in → skip register page entirely
  useEffect(() => {
    if (isAuthenticated) navigate("/dashboard", { replace: true });
  }, [isAuthenticated, navigate]);

  const [form, setForm] = useState({
    firstname: "", lastname: "", email: "", password: "", confirm: "",
  });
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [apiError, setApiError] = useState("");
  const [success,  setSuccess]  = useState("");
  const [loading,  setLoading]  = useState(false);

  const set = (key: keyof typeof form) => (e: ChangeEvent<HTMLInputElement>) => {
    setForm(prev => ({ ...prev, [key]: e.target.value }));
    setFieldErrors(prev => ({ ...prev, [key]: "" }));
  };

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setApiError("");
    setSuccess("");

    if (form.password !== form.confirm) {
      setFieldErrors(prev => ({ ...prev, confirm: "Passwords do not match." }));
      return;
    }

    setLoading(true);
    try {
      const { confirm, ...payload } = form;
      void confirm;
      const res  = await authApi.register(payload);
      const body = res.data;
      if (body.success && body.data) {
        login(body.data.user, body.data.accessToken, body.data.refreshToken);
        setSuccess("Account created successfully! Redirecting...");
        setTimeout(() => navigate("/dashboard", { replace: true }), 1500);
      } else {
        setApiError(body.error?.message ?? "Registration failed.");
      }
    } catch (err: unknown) {
      if (axios.isAxiosError(err) && err.response?.data) {
        const data = err.response.data;
        if (data.error?.code === "VALID-001" && typeof data.error.details === "object") {
          setFieldErrors(data.error.details as Record<string, string>);
        } else {
          setApiError(data.error?.message ?? "Registration failed.");
        }
      } else {
        setApiError("Unable to reach server. Is the backend running?");
      }
    } finally {
      setLoading(false);
    }
  };

  const inputClass = (key: string) =>
    `w-full px-4 py-2.5 text-sm border rounded-lg outline-none transition ${
      fieldErrors[key] ? "border-red-400 bg-red-50" : "border-gray-200"
    }`;

  return (
    <div className="min-h-screen flex flex-col" style={{ background: "linear-gradient(135deg, #3b0764 0%, #662498 60%, #a855f7 100%)" }}>
      {/* Top nav */}
      <nav className="flex items-center gap-8 px-10 py-5">
        <Link
          to="/login"
          className="text-xs font-bold tracking-widest uppercase opacity-50 hover:opacity-100 transition"
          style={{ color: "#C9BEFF" }}
        >
          Sign In
        </Link>
        <Link
          to="/register"
          className="text-xs font-bold tracking-widest uppercase"
          style={{ color: "#FFDBFD" }}
        >
          Sign Up
        </Link>
      </nav>

      {/* Body */}
      <div className="flex flex-1 max-w-5xl mx-auto w-full">
        {/* Left  decorative */}
        <div className="hidden md:flex flex-col justify-center items-start px-20 w-[50%]">
          <div className="w-20 h-20 rounded-2xl flex items-center justify-center mb-8"
               style={{ background: "rgba(255,219,253,0.15)" }}>
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"
                    stroke="#FFDBFD" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <h2 className="text-6xl font-extrabold leading-tight mb-5" style={{ color: "#FFDBFD" }}>
            Join SplitShare
          </h2>
          <p className="text-xl leading-relaxed" style={{ color: "#C9BEFF" }}>
            Split bills fairly.<br />Settle debts instantly.<br />No more awkward conversations.
          </p>


        </div>

        {/* Right  form card */}
        <div className="flex flex-1 items-center justify-center p-6 md:p-8">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-10">
            <h1 className="text-2xl font-bold text-gray-900 mb-1">Create account</h1>
            <p className="text-sm text-gray-400 mb-8">Start splitting expenses with friends</p>

            {success && (
              <div className="text-sm rounded-lg px-4 py-3 mb-5"
                   style={{ background: "#f0fdf4", color: "#16a34a", border: "1px solid #bbf7d0" }}>
                {success}
              </div>
            )}

            {apiError && (
              <div className="text-sm rounded-lg px-4 py-3 mb-5"
                   style={{ background: "#FFDBFD", color: "#662498", border: "1px solid #e9b8f5" }}>
                {apiError}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                    First Name
                  </label>
                  <input type="text" required value={form.firstname} onChange={set("firstname")}
                    placeholder="John"
                    className={inputClass("firstname")}
                    onFocus={e => (e.target.style.borderColor = "#662498")}
                    onBlur={e  => (e.target.style.borderColor = fieldErrors.firstname ? "#f87171" : "#e5e7eb")}
                  />
                  {fieldErrors.firstname && <p className="mt-1 text-xs text-red-500">{fieldErrors.firstname}</p>}
                </div>
                <div>
                  <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                    Last Name
                  </label>
                  <input type="text" required value={form.lastname} onChange={set("lastname")}
                    placeholder="Doe"
                    className={inputClass("lastname")}
                    onFocus={e => (e.target.style.borderColor = "#662498")}
                    onBlur={e  => (e.target.style.borderColor = fieldErrors.lastname ? "#f87171" : "#e5e7eb")}
                  />
                  {fieldErrors.lastname && <p className="mt-1 text-xs text-red-500">{fieldErrors.lastname}</p>}
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                  Email
                </label>
                <input type="email" required value={form.email} onChange={set("email")}
                  placeholder="you@example.com"
                  className={inputClass("email")}
                  onFocus={e => (e.target.style.borderColor = "#662498")}
                  onBlur={e  => (e.target.style.borderColor = fieldErrors.email ? "#f87171" : "#e5e7eb")}
                />
                {fieldErrors.email && <p className="mt-1 text-xs text-red-500">{fieldErrors.email}</p>}
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                  Password
                </label>
                <input type="password" required value={form.password} onChange={set("password")}
                  placeholder="8+ characters"
                  className={inputClass("password")}
                  onFocus={e => (e.target.style.borderColor = "#662498")}
                  onBlur={e  => (e.target.style.borderColor = fieldErrors.password ? "#f87171" : "#e5e7eb")}
                />
                {fieldErrors.password && <p className="mt-1 text-xs text-red-500">{fieldErrors.password}</p>}
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                  Confirm Password
                </label>
                <input type="password" required value={form.confirm} onChange={set("confirm")}
                  placeholder="Re-enter password"
                  className={inputClass("confirm")}
                  onFocus={e => (e.target.style.borderColor = "#662498")}
                  onBlur={e  => (e.target.style.borderColor = fieldErrors.confirm ? "#f87171" : "#e5e7eb")}
                />
                {fieldErrors.confirm && <p className="mt-1 text-xs text-red-500">{fieldErrors.confirm}</p>}
              </div>

              <button
                type="submit" disabled={loading}
                className="w-full py-3 text-sm font-bold text-white rounded-lg transition mt-1 cursor-pointer disabled:opacity-60"
                style={{ background: "#662498" }}
                onMouseEnter={e => !loading && ((e.target as HTMLElement).style.background = "#4a1870")}
                onMouseLeave={e => ((e.target as HTMLElement).style.background = "#662498")}
              >
                {loading ? "Creating account" : "Sign Up"}
              </button>
            </form>

            <p className="mt-6 text-center text-sm text-gray-400">
              Already have an account?{" "}
              <Link to="/login" className="font-semibold hover:underline" style={{ color: "#662498" }}>
                Sign In
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
