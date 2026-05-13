import { createContext, useContext, useEffect, useState, type ReactNode } from "react";
import type { UserDto } from "./types/auth";
import { useCurrency, type CurrencyCode } from "../settings/contexts/CurrencyContext";

interface AuthContextType {
  user: UserDto | null;
  token: string | null;
  login: (user: UserDto, accessToken: string, refreshToken: string) => void;
  updateUser: (user: UserDto) => void;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserDto | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const { setCurrency } = useCurrency();

  useEffect(() => {
    const storedToken = localStorage.getItem("accessToken");
    const storedUser = localStorage.getItem("user");
    if (storedToken && storedUser) {
      const parsedUser = JSON.parse(storedUser) as UserDto;
      setToken(storedToken);
      setUser(parsedUser);
      if (parsedUser.currency) {
        setCurrency(parsedUser.currency as CurrencyCode);
      }
    }
  }, [setCurrency]);

  const login = (nextUser: UserDto, accessToken: string, refreshToken: string) => {
    setUser(nextUser);
    setToken(accessToken);
    localStorage.setItem("accessToken", accessToken);
    localStorage.setItem("refreshToken", refreshToken);
    localStorage.setItem("user", JSON.stringify(nextUser));
    if (nextUser.currency) {
      setCurrency(nextUser.currency as CurrencyCode);
    }
  };

  const updateUser = (nextUser: UserDto) => {
    setUser(nextUser);
    localStorage.setItem("user", JSON.stringify(nextUser));
    if (nextUser.currency) {
      setCurrency(nextUser.currency as CurrencyCode);
    }
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem("accessToken");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user");
  };

  return (
    <AuthContext.Provider value={{ user, token, login, updateUser, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used inside <AuthProvider>");
  return ctx;
}
