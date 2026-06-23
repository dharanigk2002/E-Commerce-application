import type { User } from "../types";

export const TOKEN_KEY = "ecommerce_admin_token";
export const USER_KEY = "ecommerce_admin_user";
export const EXPIRES_AT_KEY = "ecommerce_admin_expires_at";

export function getStoredToken() {
  return localStorage.getItem(TOKEN_KEY);
}

export function getStoredUser() {
  const savedUser = localStorage.getItem(USER_KEY);
  return savedUser ? (JSON.parse(savedUser) as User) : null;
}

export function getStoredExpiresAt() {
  const savedExpiresAt = localStorage.getItem(EXPIRES_AT_KEY);
  return savedExpiresAt ? Number(savedExpiresAt) : null;
}

export function getStoredAuth() {
  const token = getStoredToken();
  const user = getStoredUser();
  const expiresAt = getStoredExpiresAt();

  if (!token || !user || !expiresAt || expiresAt <= Date.now()) {
    clearStoredAuth();
    return { token: null, user: null, expiresAt: null };
  }

  return { token, user, expiresAt };
}

export function storeAuth(token: string, user: User, expiresAt: number) {
  localStorage.setItem(TOKEN_KEY, token);
  localStorage.setItem(USER_KEY, JSON.stringify(user));
  localStorage.setItem(EXPIRES_AT_KEY, String(expiresAt));
}

export function clearStoredAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(USER_KEY);
  localStorage.removeItem(EXPIRES_AT_KEY);
}
