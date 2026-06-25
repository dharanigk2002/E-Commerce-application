import { useState, type SyntheticEvent } from "react";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { LogIn } from "lucide-react";
import { useAuth } from "../auth/useAuth";
import { getRememberedCustomerPath } from "../lib/navigationMemory";
import common from "../styles/common.module.css";
import styles from "./CustomerAuth.module.css";

export function CustomerLoginPage() {
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const from =
    location.state && typeof location.state === "object" && "from" in location.state
      ? String(location.state.from)
      : getRememberedCustomerPath();

  if (isAuthenticated) {
    return <Navigate to={from} replace />;
  }

  async function handleSubmit(event: SyntheticEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setSubmitting(true);

    try {
      await login(email, password, {
        requiredRole: "CUSTOMER",
        roleErrorMessage: "This is an admin account. Please use admin login.",
      });
      navigate(from, { replace: true });
    } catch (exception) {
      setError(
        exception instanceof Error ? exception.message : "Could not login",
      );
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className={styles.authPanel}>
      <div className={styles.authHeader}>
        <LogIn size={28} />
        <div>
          <span className={common.eyebrow}>Customer login</span>
          <h1>Welcome back</h1>
          <p>Sign in to manage your cart, checkout, and view your orders.</p>
        </div>
      </div>

      <form className={common.formStack} onSubmit={handleSubmit}>
        <label>
          Email
          <input
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            required
          />
        </label>
        <label>
          Password
          <input
            type="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            required
          />
        </label>
        {error && <p className={common.errorMessage}>{error}</p>}
        <button
          className={common.primaryButton}
          type="submit"
          disabled={submitting}
        >
          {submitting ? "Signing in..." : "Login"}
        </button>
      </form>

      <p className={styles.authLink}>
        New customer? <Link to="/register">Create an account</Link>
      </p>
    </section>
  );
}
