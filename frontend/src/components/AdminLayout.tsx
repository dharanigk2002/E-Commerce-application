import {
  Boxes,
  LayoutDashboard,
  LogOut,
  PackagePlus,
  ReceiptText,
} from "lucide-react";
import { NavLink, Outlet } from "react-router-dom";
import { useAuth } from "../auth/useAuth";
import common from "../styles/common.module.css";
import styles from "./AdminLayout.module.css";

export function AdminLayout() {
  const { user, logout } = useAuth();

  return (
    <div className={styles.adminShell}>
      <aside className={styles.sidebar}>
        <div className={styles.brand}>
          <div className={styles.brandMark}>
            <Boxes size={24} />
          </div>
          <div>
            <span className={common.eyebrow}>E-Commerce</span>
            <strong>Admin</strong>
          </div>
        </div>

        <nav className={styles.sidebarNav} aria-label="Admin navigation">
          <NavLink to="/admin/products">
            <PackagePlus size={18} />
            Products
          </NavLink>
          <NavLink to="/admin/orders">
            <ReceiptText size={18} />
            Orders
          </NavLink>
        </nav>

        <div className={styles.sidebarFooter}>
          <div className={styles.userChip}>
            <LayoutDashboard size={18} />
            <div>
              <strong>{user?.fullName}</strong>
              <span>{user?.email}</span>
            </div>
          </div>
          <button className={common.ghostButton} type="button" onClick={logout}>
            <LogOut size={18} />
            Logout
          </button>
        </div>
      </aside>

      <main className={styles.adminMain}>
        <Outlet />
      </main>
    </div>
  );
}
