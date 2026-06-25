import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth/useAuth";
import type { Role } from "../types";

type ProtectedRouteProps = {
  allowedRoles?: Role[];
  redirectTo?: string;
};

export function ProtectedRoute({
  allowedRoles,
  redirectTo = "/login",
}: ProtectedRouteProps) {
  const { isAuthenticated, user } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return (
      <Navigate
        to={redirectTo}
        state={{ from: `${location.pathname}${location.search}${location.hash}` }}
        replace
      />
    );
  }

  if (!user || !allowedRoles?.includes(user.role)) {
    return (
      <Navigate to={user?.role === "ADMIN" ? "/admin/products" : "/"} replace />
    );
  }

  return <Outlet />;
}
