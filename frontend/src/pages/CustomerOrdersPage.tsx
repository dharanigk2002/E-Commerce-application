import { useEffect, useState } from "react";
import { Link, useLocation, useParams } from "react-router-dom";
import { useAuth } from "../auth/useAuth";
import { EmptyState } from "../components/EmptyState";
import { api } from "../lib/api";
import { formatCurrency, formatDateTime } from "../lib/format";
import common from "../styles/common.module.css";
import type { Order } from "../types";
import styles from "./CustomerPages.module.css";

export function CustomerOrdersPage() {
  const { id } = useParams();
  const { token } = useAuth();
  const location = useLocation();
  const [orders, setOrders] = useState<Order[]>([]);
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const orderPlaced = Boolean(
    location.state &&
    typeof location.state === "object" &&
    "orderPlaced" in location.state,
  );

  useEffect(() => {
    async function loadOrders() {
      if (!token) return;

      try {
        if (id) {
          setOrder(await api.getCurrentUserOrder(token, Number(id)));
        } else {
          setOrders(await api.getCurrentUserOrders(token));
        }
      } catch (exception) {
        setError(readError(exception, "Could not load orders"));
      } finally {
        setLoading(false);
      }
    }

    void loadOrders();
  }, [id, token]);

  if (loading) {
    return <p className={common.muted}>Loading orders...</p>;
  }

  if (error) {
    return <p className={common.errorBanner}>{error}</p>;
  }

  if (id && order) {
    return (
      <section className={common.pageStack}>
        {orderPlaced && (
          <p className={common.successMessage}>Order placed successfully.</p>
        )}
        <OrderDetails order={order} />
        <Link className={common.secondaryButton} to="/orders">
          View all orders
        </Link>
      </section>
    );
  }

  return (
    <section className={common.pageStack}>
      <header className={styles.hero}>
        <div>
          <span className={common.eyebrow}>Orders</span>
          <h1>Your orders</h1>
        </div>
      </header>

      {orders.length === 0 ? (
        <EmptyState
          title="No orders yet"
          message="Placed orders will appear here."
        />
      ) : (
        <div className={common.panel}>
          {orders.map((item) => (
            <Link
              className={styles.cardLink}
              to={`/orders/${item.id}`}
              key={item.id}
            >
              <article className={styles.orderRow}>
                <div>
                  <strong>Ordered on </strong>
                  <span className={styles.meta}>
                    {formatDateTime(item.createdAt)}
                  </span>
                </div>
                <span>{item.status}</span>
                <strong>{formatCurrency(item.totalAmount)}</strong>
              </article>
            </Link>
          ))}
        </div>
      )}
    </section>
  );
}

function OrderDetails({ order }: { order: Order }) {
  const address = [
    order.shippingAddressLine,
    order.shippingCity,
    order.shippingState,
    order.shippingPostalCode,
    order.shippingCountry,
  ].join(", ");

  return (
    <section className={common.panel}>
      <span className={common.eyebrow}>Order #{order.id}</span>
      <h1>{order.status}</h1>
      <p className={styles.meta}>{formatDateTime(order.createdAt)}</p>
      <div className={styles.addressBox}>
        <strong>Ship to</strong>
        <p className={styles.addressText}>{address}</p>
      </div>
      {order.items.map((item) => (
        <div className={styles.itemRow} key={item.id}>
          <strong>{item.productName}</strong>
          <span>
            {item.quantity} x {formatCurrency(item.unitPrice)}
          </span>
          <span>{formatCurrency(item.lineTotal)}</span>
        </div>
      ))}
      <div className={styles.summaryRow}>
        <strong>Total</strong>
        <strong>{formatCurrency(order.totalAmount)}</strong>
      </div>
    </section>
  );
}

function readError(exception: unknown, fallback: string) {
  return exception instanceof Error ? exception.message : fallback;
}
