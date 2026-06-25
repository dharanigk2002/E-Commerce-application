import { useState, type SyntheticEvent } from "react";
import { Navigate } from "react-router-dom";
import { LockKeyhole } from "lucide-react";
import { useAuth } from "../auth/useAuth";
import common from "../styles/common.module.css";
import styles from "./LoginPage.module.css";

export function LoginPage() {
  const { login, isAuthenticated, user } = useAuth();
  const [email, setEmail] = useState<string>("");
  const [password, setPassword] = useState<string>("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  if (isAuthenticated && user?.role === "ADMIN") {
    return <Navigate to="/admin/products" replace />;
  }

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  async function handleSubmit(event: SyntheticEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setSubmitting(true);

    try {
      await login(email, password, {
        requiredRole: "ADMIN",
        roleErrorMessage: "Only ADMIN users can access this dashboard",
      });
    } catch (exception) {
      setError(
        exception instanceof Error ? exception.message : "Could not login",
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <main className={styles.loginPage}>
      <section className={styles.loginPanel}>
        <div className={styles.loginHeader}>
          <div className={styles.loginIcon}>
            <LockKeyhole size={28} />
          </div>
          <span className={common.eyebrow}>Admin access</span>
        </div>
        <h1>Sign in to manage the store</h1>
        <p>
          Use an admin account to manage products, upload images, and update
          order status.
        </p>

        <form className={common.formStack} onSubmit={handleSubmit}>
          <label>
            Email
            <input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              placeholder="admin@example.com"
              required
            />
          </label>

          <label>
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              placeholder="Enter password"
              required
            />
          </label>

          {error && <p className={common.errorMessage}>{error}</p>}

          <button
            className={common.primaryButton}
            type="submit"
            disabled={submitting}
          >
            {submitting ? "Signing in..." : "Sign in"}
          </button>
        </form>
      </section>
    </main>
  );
}
