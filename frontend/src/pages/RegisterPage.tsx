import { useState, type SyntheticEvent } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { UserPlus } from "lucide-react";
import { useAuth } from "../auth/useAuth";
import common from "../styles/common.module.css";
import styles from "./CustomerAuth.module.css";

export function RegisterPage() {
  const { isAuthenticated, register } = useAuth();
  const navigate = useNavigate();
  const [fullName, setFullName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [submitting, setSubmitting] = useState(false);

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  async function handleSubmit(event: SyntheticEvent<HTMLFormElement>) {
    event.preventDefault();
    setError("");
    setSubmitting(true);

    try {
      await register({ fullName, email, password });
      navigate("/", { replace: true });
    } catch (exception) {
      setError(exception instanceof Error ? exception.message : "Could not register");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className={styles.authPanel}>
      <div className={styles.authHeader}>
        <UserPlus size={28} />
        <div>
          <span className={common.eyebrow}>Create account</span>
          <h1>Start shopping</h1>
          <p>Your shipping address can be saved during first checkout.</p>
        </div>
      </div>

      <form className={common.formStack} onSubmit={handleSubmit}>
        <label>
          Full name
          <input value={fullName} onChange={(event) => setFullName(event.target.value)} required />
        </label>
        <label>
          Email
          <input type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
        </label>
        <label>
          Password
          <input type="password" minLength={8} value={password} onChange={(event) => setPassword(event.target.value)} required />
        </label>
        {error && <p className={common.errorMessage}>{error}</p>}
        <button className={common.primaryButton} type="submit" disabled={submitting}>
          {submitting ? "Creating..." : "Create account"}
        </button>
      </form>

      <p className={styles.authLink}>
        Already registered? <Link to="/login">Login</Link>
      </p>
    </section>
  );
}
