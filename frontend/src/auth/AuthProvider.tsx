import {
  type ReactNode,
  useCallback,
  useEffect,
  useMemo,
  useState,
} from "react";
import { api, setUnauthorizedHandler } from "../lib/api";
import type { AuthResponse, RegisterPayload } from "../types";
import type { LoginOptions } from "./AuthContext";
import { AuthContext } from "./AuthContext";
import { clearStoredAuth, getStoredAuth, storeAuth } from "./authStorage";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [auth, setAuth] = useState(() => getStoredAuth());

  const saveAuth = useCallback((response: AuthResponse) => {
    const expiresAt = Date.now() + response.expiresInSeconds * 1000;

    storeAuth(response.accessToken, response.user, expiresAt);
    setAuth({
      token: response.accessToken,
      user: response.user,
      expiresAt,
    });

    return response.user;
  }, []);

  const logout = useCallback(() => {
    clearStoredAuth();
    setAuth({ token: null, user: null, expiresAt: null });
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(logout);
    return () => setUnauthorizedHandler(null);
  }, [logout]);

  useEffect(() => {
    if (!auth.expiresAt) {
      return;
    }

    const delay = auth.expiresAt - Date.now();

    const timeoutId = window.setTimeout(logout, Math.max(delay, 0));

    return () => window.clearTimeout(timeoutId);
  }, [auth.expiresAt, logout]);

  const login = useCallback(
    async (email: string, password: string, options?: LoginOptions) => {
      const response = await api.login(email, password);

      if (
        options?.requiredRole &&
        response.user.role !== options.requiredRole
      ) {
        throw new Error(
          options.roleErrorMessage || "This account cannot access this area",
        );
      }

      return saveAuth(response);
    },
    [saveAuth],
  );

  const register = useCallback(
    async (payload: RegisterPayload) => {
      const response = await api.register(payload);
      return saveAuth(response);
    },
    [saveAuth],
  );

  const refreshUser = useCallback(async () => {
    if (!auth.token) {
      return null;
    }

    const user = await api.getCurrentUser(auth.token);
    setAuth({ token: auth.token, expiresAt: auth.expiresAt, user });
    return user;
  }, [auth.expiresAt, auth.token]);

  const value = useMemo(
    () => ({
      token: auth.token,
      user: auth.user,
      expiresAt: auth.expiresAt,
      isAuthenticated: Boolean(auth.token && auth.user && auth.expiresAt),
      login,
      register,
      refreshUser,
      logout,
    }),
    [auth, login, logout, refreshUser, register],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
