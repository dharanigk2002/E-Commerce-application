import { createContext } from "react";
import type { User } from "../types";

export type AuthState = {
  token: string | null;
  user: User | null;
  expiresAt: number | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
};

export const AuthContext = createContext<AuthState | null>(null);
