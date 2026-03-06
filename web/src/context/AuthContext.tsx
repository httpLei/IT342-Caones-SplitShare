import { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import type { UserDto } from '../types/auth';

interface AuthContextType {
  user: UserDto | null;
  token: string | null;
  login: (user: UserDto, accessToken: string, refreshToken: string) => void;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserDto | null>(null);
  const [token, setToken] = useState<string | null>(null);

  // Restore session from localStorage on page load
  useEffect(() => {
    const storedToken = localStorage.getItem('accessToken');
    const storedUser  = localStorage.getItem('user');
    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
    }
  }, []);

  const login = (user: UserDto, accessToken: string, refreshToken: string) => {
    setUser(user);
    setToken(accessToken);
    localStorage.setItem('accessToken',  accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    localStorage.setItem('user', JSON.stringify(user));
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  };

  return (
    <AuthContext.Provider value={{ user, token, login, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used inside <AuthProvider>');
  return ctx;
}
