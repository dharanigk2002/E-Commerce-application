import { useEffect, useState, type SyntheticEvent } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../auth/useAuth";
import { api } from "../lib/api";
import {
  addressFromUser,
  formatAddress,
  hasAddress,
} from "../lib/customerAddress";
import { formatCurrency } from "../lib/format";
import common from "../styles/common.module.css";
import type { AddressPayload, Cart, User } from "../types";
import styles from "./CustomerPages.module.css";
import { AddressFields } from "./ProfilePage";

export function CheckoutPage() {
  const { token, refreshUser } = useAuth();
  const navigate = useNavigate();
  const [cart, setCart] = useState<Cart | null>(null);
  const [user, setUser] = useState<User | null>(null);
  const [form, setForm] = useState<AddressPayload>({
    shippingAddressLine: "",
    shippingCity: "",
    shippingState: "",
    shippingPostalCode: "",
    shippingCountry: "",
  });
  const [editingAddress, setEditingAddress] = useState(false);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadCheckout() {
      if (!token) return;

      try {
        const [loadedCart, loadedUser] = await Promise.all([
          api.getCart(token),
          api.getCurrentUser(token),
        ]);
        setCart(loadedCart);
        setUser(loadedUser);
        setForm(addressFromUser(loadedUser));
        setEditingAddress(!hasAddress(loadedUser));
      } catch (exception) {
        setError(readError(exception, "Could not load checkout"));
      } finally {
        setLoading(false);
      }
    }

    void loadCheckout();
  }, [token]);

  async function handlePlaceOrder(event: SyntheticEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!token) return;

    setSubmitting(true);
    setError("");

    try {
      if (editingAddress || !user || !hasAddress(user)) {
        const updatedUser = await api.updateMyAddress(token, form);
        setUser(updatedUser);
        await refreshUser();
      }

      const order = await api.createOrder(token);
      navigate(`/orders/${order.id}`, {
        replace: true,
        state: { orderPlaced: true },
      });
    } catch (exception) {
      setError(readError(exception, "Could not place order"));
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) {
    return <p className={common.muted}>Loading checkout...</p>;
  }

  return (
    <form className={styles.twoColumn} onSubmit={handlePlaceOrder}>
      <section className={common.panel}>
        <span className={common.eyebrow}>Checkout</span>
        <h1>Shipping address</h1>
        {user && hasAddress(user) && !editingAddress ? (
          <div className={styles.addressBox}>
            <strong>Ship to</strong>
            <p className={styles.addressText}>{formatAddress(user)}</p>
            <button
              className={common.secondaryButton}
              type="button"
              onClick={() => setEditingAddress(true)}
            >
              Edit address
            </button>
          </div>
        ) : (
          <AddressFields form={form} setForm={setForm} />
        )}
      </section>

      <aside className={`${common.panel} ${common.formStack}`}>
        <h2>Order summary</h2>
        {cart?.items.map((item) => (
          <div className={styles.summaryRow} key={item.id}>
            <span>
              {item.productName} x {item.quantity}
            </span>
            <strong>{formatCurrency(item.lineTotal)}</strong>
          </div>
        ))}
        <div className={styles.summaryRow}>
          <strong>Total</strong>
          <strong>{formatCurrency(cart?.totalAmount || 0)}</strong>
        </div>
        {error && <p className={common.errorMessage}>{error}</p>}
        <button
          className={common.primaryButton}
          type="submit"
          disabled={submitting || !cart || cart.items.length === 0}
        >
          {submitting ? "Placing order..." : "Place order"}
        </button>
      </aside>
    </form>
  );
}

function readError(exception: unknown, fallback: string) {
  return exception instanceof Error ? exception.message : fallback;
}
