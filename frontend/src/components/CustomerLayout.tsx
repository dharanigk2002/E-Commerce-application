import {
  LogOut,
  PackageSearch,
  ReceiptText,
  ShoppingCart,
  UserRound,
} from "lucide-react";
import { NavLink, Outlet, useLocation } from "react-router-dom";
import { useEffect } from "react";
import { useAuth } from "../auth/useAuth";
import { rememberCustomerPath } from "../lib/navigationMemory";
import common from "../styles/common.module.css";
import styles from "./CustomerLayout.module.css";
import type { ReactElement } from "react";

export function CustomerLayout() {
  const { isAuthenticated, user, logout } = useAuth();
  const location = useLocation();
  const isCustomer = isAuthenticated && user?.role === "CUSTOMER";
  type Link = {
    to: string;
    link: string;
    icon?: ReactElement;
  };

  const links: Link[] = [
    {
      to: "/cart",
      link: "Cart",
      icon: <ShoppingCart size={17} />,
    },
    {
      to: "/orders",
      link: "Orders",
      icon: <ReceiptText size={17} />,
    },
    {
      to: "/profile",
      link: "Profile",
      icon: <UserRound size={17} />,
    },
  ];

  const navLinks = links.map((navLink, id) => (
    <NavLink to={navLink.to} key={id}>
      {navLink.icon} {navLink.link}
    </NavLink>
  ));

  useEffect(() => {
    if (!isCustomer || location.pathname === "/login" || location.pathname === "/register") {
      return;
    }

    rememberCustomerPath(`${location.pathname}${location.search}${location.hash}`);
  }, [isCustomer, location.hash, location.pathname, location.search]);

  return (
    <div className={styles.customerShell}>
      <header className={styles.topbar}>
        <NavLink className={styles.brand} to="/">
          <PackageSearch size={24} />
          <strong>ShopEase</strong>
        </NavLink>

        <nav className={styles.nav} aria-label="Customer navigation">
          <NavLink to="/">Products</NavLink>
          {isCustomer ? navLinks : ""}
        </nav>

        <div className={styles.actions}>
          {isAuthenticated ? (
            <button
              className={common.secondaryButton}
              type="button"
              onClick={logout}
            >
              <LogOut size={17} />
              Logout
            </button>
          ) : (
            <>
              <NavLink className={common.secondaryButton} to="/login">
                Login
              </NavLink>
              <NavLink className={common.primaryButton} to="/register">
                Register
              </NavLink>
            </>
          )}
        </div>
      </header>

      <main className={styles.customerMain}>
        <Outlet />
      </main>
    </div>
  );
}
