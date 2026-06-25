import { createContext } from "react";
import type { RegisterPayload, Role, User } from "../types";

export type LoginOptions = {
  requiredRole?: Role;
  roleErrorMessage?: string;
};

export type AuthState = {
  token: string | null;
  user: User | null;
  expiresAt: number | null;
  isAuthenticated: boolean;
  login: (email: string, password: string, options?: LoginOptions) => Promise<User>;
  register: (payload: RegisterPayload) => Promise<User>;
  refreshUser: () => Promise<User | null>;
  logout: () => void;
};

export const AuthContext = createContext<AuthState | null>(null);
