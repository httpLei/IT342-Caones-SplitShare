import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../AuthContext";
import type { UserDto } from "../types/auth";

export default function OAuth2SuccessPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [searchParams] = useSearchParams();
  const [error, setError] = useState("");

  useEffect(() => {
    const googleError = searchParams.get("error");
    if (googleError) {
      setError(googleError);
      return;
    }

    const accessToken = searchParams.get("accessToken");
    const refreshToken = searchParams.get("refreshToken");
    const email = searchParams.get("email");
    const firstname = searchParams.get("firstname");
    const lastname = searchParams.get("lastname");
    const role = searchParams.get("role") ?? "ROLE_USER";
    const currency = searchParams.get("currency") ?? undefined;

    if (!accessToken || !refreshToken || !email || !firstname || !lastname) {
      setError("Google sign-in did not return a complete session.");
      return;
    }

    const user: UserDto = { email, firstname, lastname, role, currency };
    login(user, accessToken, refreshToken);
    navigate(role === "ROLE_ADMIN" ? "/admin" : "/dashboard", { replace: true });
  }, [login, navigate, searchParams]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-950 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 text-center shadow-2xl">
        <h1 className="text-xl font-bold text-gray-900">Signing you in...</h1>
        {error ? (
          <>
            <p className="mt-3 text-sm text-red-600">{error}</p>
            <Link to="/login" className="mt-6 inline-flex rounded-lg bg-purple-700 px-4 py-2 text-sm font-bold text-white">
              Back to login
            </Link>
          </>
        ) : (
          <p className="mt-3 text-sm text-gray-500">Finishing your Google login.</p>
        )}
      </div>
    </div>
  );
}
