import { useEffect, useState, type FormEvent } from "react";
import { Link, useNavigate } from "react-router-dom";
import axios from "axios";
import { Mail, Lock, LogIn } from "lucide-react";
import { authApi } from "../../../shared/services/api";
import { useAuth } from "../AuthContext";

export default function LoginPage() {
  const navigate = useNavigate();
  const { login, isAuthenticated } = useAuth();

  useEffect(() => {
    if (isAuthenticated) {
      navigate("/dashboard", { replace: true });
    }
  }, [isAuthenticated, navigate]);

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [loading, setLoading] = useState(false);

  const handleGoogleSignIn = () => {
    setError("");
    setSuccess("Redirecting to Google...");
    const redirectUri = `${window.location.origin}/oauth2/success`;
    window.location.href = `/oauth2/authorization/google?redirect_uri=${encodeURIComponent(redirectUri)}`;
  };

  const handleSubmit = async (event: FormEvent) => {
    event.preventDefault();
    setError("");
    setSuccess("");
    setLoading(true);
    try {
      const response = await authApi.login({ email, password });
      const body = response.data;
      if (body.success && body.data) {
        login(body.data.user, body.data.accessToken, body.data.refreshToken);
        const redirectPath = body.data.user.role === "ROLE_ADMIN" ? "/admin" : "/dashboard";
        navigate(redirectPath, { replace: true });
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
      <nav className="flex items-center gap-8 px-10 py-5">
        <Link to="/login" className="text-xs font-bold tracking-widest uppercase" style={{ color: "#FFDBFD" }}>
          Sign In
        </Link>
        <Link to="/register" className="text-xs font-bold tracking-widest uppercase opacity-50 hover:opacity-100 transition" style={{ color: "#C9BEFF" }}>
          Sign Up
        </Link>
      </nav>

      <div className="flex flex-1 max-w-5xl mx-auto w-full">
        <div className="hidden md:flex flex-col justify-center items-start px-20 w-[50%]">
          <div className="w-20 h-20 rounded-2xl flex items-center justify-center mb-8" style={{ background: "rgba(255,219,253,0.15)" }}>
            <svg width="40" height="40" viewBox="0 0 24 24" fill="none">
              <path d="M12 2L2 7l10 5 10-5-10-5zM2 17l10 5 10-5M2 12l10 5 10-5" stroke="#FFDBFD" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </div>
          <h2 className="text-6xl font-extrabold leading-tight mb-5" style={{ color: "#FFDBFD" }}>
            SplitShare
          </h2>
          <p className="text-xl leading-relaxed" style={{ color: "#C9BEFF" }}>
            Track shared expenses<br />effortlessly. Know exactly<br />who owes who.
          </p>
        </div>

        <div className="flex flex-1 items-center justify-center p-6 md:p-8">
          <div className="bg-white rounded-2xl shadow-2xl w-full max-w-md p-10">
            <h1 className="text-2xl font-bold text-gray-900 mb-1">Welcome back</h1>
            <p className="text-sm text-gray-400 mb-8">Sign in to manage your shared expenses</p>

            {success && <div className="text-sm rounded-lg px-4 py-3 mb-5" style={{ background: "#f0fdf4", color: "#16a34a", border: "1px solid #bbf7d0" }}>{success}</div>}
            {error && <div className="text-sm rounded-lg px-4 py-3 mb-5" style={{ background: "#FFDBFD", color: "#662498", border: "1px solid #e9b8f5" }}>{error}</div>}

            <form onSubmit={handleSubmit} className="space-y-5">
              <div>
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">Email</label>
                <div className="relative">
                  <Mail size={18} className="absolute left-3 top-3.5 text-gray-400" />
                  <input
                    type="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    placeholder="you@example.com"
                    className="w-full pl-10 pr-4 py-2.5 text-sm text-gray-900 placeholder-gray-400 border border-gray-200 rounded-lg outline-none transition focus:ring-2 focus:ring-purple-200"
                    onFocus={(e) => (e.target.style.borderColor = "#662498")}
                    onBlur={(e) => (e.target.style.borderColor = "#e5e7eb")}
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-500 uppercase tracking-wide mb-2">Password</label>
                <div className="relative">
                  <Lock size={18} className="absolute left-3 top-3.5 text-gray-400" />
                  <input
                    type="password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="••••••••"
                    className="w-full pl-10 pr-4 py-2.5 text-sm text-gray-900 placeholder-gray-400 border border-gray-200 rounded-lg outline-none transition focus:ring-2 focus:ring-purple-200"
                    onFocus={(e) => (e.target.style.borderColor = "#662498")}
                    onBlur={(e) => (e.target.style.borderColor = "#e5e7eb")}
                  />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 text-sm font-bold text-white rounded-lg transition mt-2 cursor-pointer disabled:opacity-60 flex items-center justify-center gap-2 hover:shadow-lg transform hover:scale-105"
                style={{ background: "linear-gradient(135deg, #662498 0%, #a855f7 100%)" }}
              >
                <LogIn size={18} />
                {loading ? "Signing in..." : "Sign In"}
              </button>
            </form>

            <div className="my-6 flex items-center gap-3">
              <div className="h-px flex-1 bg-gray-200" />
              <span className="text-xs font-semibold uppercase tracking-wide text-gray-400">or</span>
              <div className="h-px flex-1 bg-gray-200" />
            </div>

            <button
              type="button"
              onClick={handleGoogleSignIn}
              className="flex w-full items-center justify-center gap-3 rounded-lg border border-gray-200 bg-white px-4 py-3 text-sm font-bold text-gray-700 transition hover:bg-gray-50 hover:shadow-sm"
            >
              <span className="flex h-5 w-5 items-center justify-center" aria-hidden="true">
                <svg viewBox="0 0 48 48" className="h-5 w-5" role="img" aria-label="Google">
                  <path fill="#FFC107" d="M43.611 20.083H42V20H24v8h11.303C33.655 32.657 29.193 36 24 36c-6.627 0-12-5.373-12-12s5.373-12 12-12c3.059 0 5.844 1.154 7.962 3.038l5.657-5.657C34.046 6.053 29.27 4 24 4 12.955 4 4 12.955 4 24s8.955 20 20 20 20-8.955 20-20c0-1.341-.138-2.65-.389-3.917z"/>
                  <path fill="#FF3D00" d="M6.306 14.691l6.571 4.819C14.655 15.108 18.961 12 24 12c3.059 0 5.844 1.154 7.962 3.038l5.657-5.657C34.046 6.053 29.27 4 24 4 16.318 4 9.656 8.337 6.306 14.691z"/>
                  <path fill="#4CAF50" d="M24 44c5.169 0 9.86-1.977 13.409-5.193l-6.19-5.238C29.142 35.091 26.715 36 24 36c-5.172 0-9.625-3.328-11.283-7.946l-6.52 5.025C9.511 39.556 16.227 44 24 44z"/>
                  <path fill="#1976D2" d="M43.611 20.083H42V20H24v8h11.303c-.792 2.504-2.39 4.664-4.503 6.199l.004-.003 6.19 5.238C36.556 39.762 44 34 44 24c0-1.341-.138-2.65-.389-3.917z"/>
                </svg>
              </span>
              Sign in with Google
            </button>

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
