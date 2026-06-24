import { type ReactNode, useEffect, useMemo, useState } from "react";
import { api, setUnauthorizedHandler } from "../lib/api";
import { AuthContext } from "./AuthContext";
import { clearStoredAuth, getStoredAuth, storeAuth } from "./authStorage";

export function AuthProvider({ children }: { children: ReactNode }) {
  const [auth, setAuth] = useState(() => getStoredAuth());

  useEffect(() => {
    setUnauthorizedHandler(logout);
    return () => setUnauthorizedHandler(null);
  }, []);

  useEffect(() => {
    if (!auth.expiresAt) {
      return;
    }

    const delay = auth.expiresAt - Date.now();

    if (delay <= 0) {
      logout();
      return;
    }

    const timeoutId = window.setTimeout(logout, delay);

    return () => window.clearTimeout(timeoutId);
  }, [auth.expiresAt, logout]);

  async function login(email: string, password: string) {
    const response = await api.login(email, password);

    if (response.user.role !== "ADMIN")
      throw new Error("Only ADMIN users can access this dashboard");

    const expiresAt = Date.now() + response.expiresInSeconds * 1000;

    storeAuth(response.accessToken, response.user, expiresAt);
    setAuth({
      token: response.accessToken,
      user: response.user,
      expiresAt,
    });
  }

  function logout() {
    clearStoredAuth();
    setAuth({ token: null, user: null, expiresAt: null });
  }

  const value = useMemo(
    () => ({
      token: auth.token,
      user: auth.user,
      expiresAt: auth.expiresAt,
      isAuthenticated: Boolean(auth.token && auth.user && auth.expiresAt),
      login,
      logout,
    }),
    [auth],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
