import { useState, useEffect, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import { authApi } from "../services/api";
import { useAuth } from "../context/AuthContext";
import axios from "axios";

export default function Login() {
  const navigate = useNavigate();
  const { login, isAuthenticated } = useAuth();

  // Already logged in → skip login page entirely
  useEffect(() => {
    if (isAuthenticated) navigate("/dashboard", { replace: true });
  }, [isAuthenticated, navigate]);

  const [email,    setEmail]    = useState("");
  const [password, setPassword] = useState("");
  const [error,    setError]    = useState("");
  const [success,  setSuccess]  = useState("");
  const [loading,  setLoading]  = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);
    try {
      const res  = await authApi.login({ email, password });
      const body = res.data;
      if (body.success && body.data) {
        login(body.data.user, body.data.accessToken, body.data.refreshToken);
        setSuccess("Successfully logged in! Redirecting...");
        setTimeout(() => navigate("/dashboard", { replace: true }), 1500);
      } else {
        setError(body.error?.message ?? "Login failed.");
      }
    } catch (err: unknown) {
      if (axios.isAxiosError(err) && err.response?.data) {
        setError(err.response.data.error?.message ?? "Invalid credentials.");
      } else {
        setError("Unable to reach server. Is the backend running?");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col" style={{ background: "linear-gradient(135deg, #3b0764 0%, #662498 60%, #a855f7 100%)" }}>
      {/* Top nav */}
      <nav className="flex items-center gap-8 px-10 py-5">
        <Link
          to="/login"
          className="text-xs font-bold tracking-widest uppercase"
          style={{ color: "#FFDBFD" }}
        >
          Sign In
        </Link>
        <Link
          to="/register"
          className="text-xs font-bold tracking-widest uppercase opacity-50 hover:opacity-100 transition"
          style={{ color: "#C9BEFF" }}
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
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5"
                    stroke="#FFDBFD" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </div>
          <h2 className="text-6xl font-extrabold leading-tight mb-5" style={{ color: "#FFDBFD" }}>
            SplitShare
          </h2>
          <p className="text-xl leading-relaxed" style={{ color: "#C9BEFF" }}>
            Track shared expenses<br />effortlessly. Know exactly<br />who owes who.
          </p>


        </div>

        {/* Right  form card */}
        <div className="flex flex-1 items-center justify-center p-6 md:p-8">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-10">
            <h1 className="text-2xl font-bold text-gray-900 mb-1">Welcome back</h1>
            <p className="text-sm text-gray-400 mb-8">Sign in to manage your shared expenses</p>

            {success && (
              <div className="text-sm rounded-lg px-4 py-3 mb-5"
                   style={{ background: "#f0fdf4", color: "#16a34a", border: "1px solid #bbf7d0" }}>
                {success}
              </div>
            )}
            {error && (
              <div className="text-sm rounded-lg px-4 py-3 mb-5"
                   style={{ background: "#FFDBFD", color: "#662498", border: "1px solid #e9b8f5" }}>
                {error}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                  Email
                </label>
                <input
                  type="email" required value={email}
                  onChange={e => setEmail(e.target.value)}
                  placeholder="you@example.com"
                  className="w-full px-4 py-2.5 text-sm border border-gray-200 rounded-lg outline-none transition"
                  onFocus={e => (e.target.style.borderColor = "#662498")}
                  onBlur={e  => (e.target.style.borderColor = "#e5e7eb")}
                />
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-1.5">
                  Password
                </label>
                <input
                  type="password" required value={password}
                  onChange={e => setPassword(e.target.value)}
                  placeholder=""
                  className="w-full px-4 py-2.5 text-sm border border-gray-200 rounded-lg outline-none transition"
                  onFocus={e => (e.target.style.borderColor = "#662498")}
                  onBlur={e  => (e.target.style.borderColor = "#e5e7eb")}
                />
              </div>

              <button
                type="submit" disabled={loading}
                className="w-full py-3 text-sm font-bold text-white rounded-lg transition mt-2 cursor-pointer disabled:opacity-60"
                style={{ background: "#662498" }}
                onMouseEnter={e => !loading && ((e.target as HTMLElement).style.background = "#4a1870")}
                onMouseLeave={e => ((e.target as HTMLElement).style.background = "#662498")}
              >
                {loading ? "Signing in" : "Sign In"}
              </button>
            </form>

            <p className="mt-6 text-center text-sm text-gray-400">
              Don't have an account?{" "}
              <Link to="/register" className="font-semibold hover:underline" style={{ color: "#662498" }}>
                Sign Up
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
