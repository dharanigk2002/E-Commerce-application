import { useEffect, useState, type SyntheticEvent } from "react";
import { ImagePlus, ShoppingCart } from "lucide-react";
import { Link, useLoaderData, useNavigate, useParams } from "react-router-dom";
import { useAuth } from "../auth/useAuth";
import { api } from "../lib/api";
import { formatCurrency } from "../lib/format";
import common from "../styles/common.module.css";
import type { Product } from "../types";
import styles from "./CustomerPages.module.css";

export function ProductDetailPage() {
  const { id } = useParams();
  const product = useLoaderData<Product>();
  const navigate = useNavigate();
  const { token, isAuthenticated, user } = useAuth();
  const [quantity, setQuantity] = useState(1);
  const [error, setError] = useState("");
  const [message, setMessage] = useState<string | null>("");
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    const timeout = window.setTimeout(() => setMessage(null), 2500);

    return () => window.clearTimeout(timeout);
  }, [message]);

  async function handleAddToCart(event: SyntheticEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!isAuthenticated || !token || user?.role !== "CUSTOMER") {
      navigate("/login", { state: { from: `/products/${id}` } });
      return;
    }

    setSubmitting(true);
    setError("");
    setMessage("");

    try {
      await api.addCartItem(token, product.id, quantity);
      setMessage("Added to cart");
    } catch (exception) {
      setError(readError(exception, "Could not add item to cart"));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <section className={styles.detailLayout}>
      <div className={styles.detailImage}>
        {product.imageUrl ? (
          <img src={product.imageUrl} alt={product.name} />
        ) : (
          <ImagePlus size={42} />
        )}
      </div>

      <aside className={common.panel}>
        <span className={common.eyebrow}>Product details</span>
        <h1>{product.name}</h1>
        <p>{product.description || "No description"}</p>
        <p className={styles.price}>{formatCurrency(product.price)}</p>
        <p className={styles.meta}>{product.availableStock} in stock</p>

        {error && <p className={common.errorMessage}>{error}</p>}
        {message && <p className={common.successMessage}>{message}</p>}

        <form className={common.formStack} onSubmit={handleAddToCart}>
          <label>
            Quantity
            <input
              type="number"
              min="1"
              max={product.availableStock}
              value={quantity}
              onChange={(event) => setQuantity(Number(event.target.value))}
            />
          </label>
          <button
            className={common.primaryButton}
            type="submit"
            disabled={submitting || product.availableStock === 0}
          >
            <ShoppingCart size={18} />
            {submitting ? "Adding..." : "Add to cart"}
          </button>
          <Link className={common.secondaryButton} to="/">
            Continue shopping
          </Link>
        </form>
      </aside>
    </section>
  );
}

function readError(exception: unknown, fallback: string) {
  return exception instanceof Error ? exception.message : fallback;
}
