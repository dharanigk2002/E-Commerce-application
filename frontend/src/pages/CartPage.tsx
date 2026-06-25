import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { Trash2 } from "lucide-react";
import { useAuth } from "../auth/useAuth";
import { EmptyState } from "../components/EmptyState";
import { api } from "../lib/api";
import { formatCurrency } from "../lib/format";
import common from "../styles/common.module.css";
import type { Cart } from "../types";
import styles from "./CustomerPages.module.css";

export function CartPage() {
  const { token } = useAuth();
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!token) return;

    api
      .getCart(token)
      .then(setCart)
      .catch((exception) => setError(readError(exception, "Could not load cart")))
      .finally(() => setLoading(false));
  }, [token]);

  const loadCart = useCallback(async () => {
    if (!token) return;

    setLoading(true);
    setError("");

    try {
      setCart(await api.getCart(token));
    } catch (exception) {
      setError(readError(exception, "Could not load cart"));
    } finally {
      setLoading(false);
    }
  }, [token]);

  async function updateQuantity(itemId: number, quantity: number) {
    if (!token) return;
    try {
      setCart(await api.updateCartItem(token, itemId, quantity));
    } catch (exception) {
      setError(readError(exception, "Could not update cart"));
    }
  }

  async function removeItem(itemId: number) {
    if (!token) return;
    try {
      await api.removeCartItem(token, itemId);
      await loadCart();
    } catch (exception) {
      setError(readError(exception, "Could not remove item"));
    }
  }

  return (
    <section className={common.pageStack}>
      <header className={styles.hero}>
        <div>
          <span className={common.eyebrow}>Your cart</span>
          <h1>Shopping cart</h1>
        </div>
      </header>

      {error && <p className={common.errorBanner}>{error}</p>}

      <section className={common.panel}>
        {loading ? (
          <p className={common.muted}>Loading cart...</p>
        ) : !cart || cart.items.length === 0 ? (
          <EmptyState title="Your cart is empty" message="Add products to start checkout." />
        ) : (
          <>
            {cart.items.map((item) => (
              <div className={styles.itemRow} key={item.id}>
                <strong>{item.productName}</strong>
                <input
                  type="number"
                  min="1"
                  value={item.quantity}
                  onChange={(event) => void updateQuantity(item.id, Number(event.target.value))}
                />
                <span>{formatCurrency(item.lineTotal)}</span>
                <button className={`${common.iconButton} ${common.danger}`} type="button" onClick={() => void removeItem(item.id)}>
                  <Trash2 size={18} />
                </button>
              </div>
            ))}
            <div className={styles.summaryRow}>
              <strong>Total</strong>
              <strong>{formatCurrency(cart.totalAmount)}</strong>
            </div>
            <Link className={common.primaryButton} to="/checkout">
              Checkout
            </Link>
          </>
        )}
      </section>
    </section>
  );
}

function readError(exception: unknown, fallback: string) {
  return exception instanceof Error ? exception.message : fallback;
}
