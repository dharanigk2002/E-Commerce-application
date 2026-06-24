import type { User } from "../types";

export const TOKEN_KEY = "ecommerce_admin_token";

export function getStoredAuth() {
  const storedAuth = localStorage.getItem(TOKEN_KEY);
  return storedAuth ? JSON.parse(storedAuth) : {};
}

export function storeAuth(token: string, user: User, expiresAt: number) {
  const jwt = {
    token,
    user,
    expiresAt,
  };
  localStorage.setItem(TOKEN_KEY, JSON.stringify(jwt));
}

export function clearStoredAuth() {
  localStorage.removeItem(TOKEN_KEY);
}
