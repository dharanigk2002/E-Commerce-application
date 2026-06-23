import { useEffect, useState } from "react";
import { RefreshCw } from "lucide-react";
import { useAuth } from "../auth/useAuth";
import { EmptyState } from "../components/EmptyState";
import { api } from "../lib/api";
import { formatCurrency, formatDateTime } from "../lib/format";
import common from "../styles/common.module.css";
import type { Order, OrderStatus } from "../types";
import styles from "./OrdersPage.module.css";

const orderStatuses: OrderStatus[] = [
  "PENDING",
  "CONFIRMED",
  "SHIPPED",
  "DELIVERED",
  "CANCELLED",
];

export function OrdersPage() {
  const { token } = useAuth();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [updatingId, setUpdatingId] = useState<number | null>(null);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!token) {
      return;
    }

    let ignore = false;

    api
      .getAdminOrders(token)
      .then((loadedOrders) => {
        if (!ignore) {
          setOrders(loadedOrders);
        }
      })
      .catch((exception) => {
        if (!ignore) {
          setError(readError(exception, "Could not load orders"));
        }
      })
      .finally(() => {
        if (!ignore) {
          setLoading(false);
        }
      });

    return () => {
      ignore = true;
    };
  }, [token]);

  async function loadOrders() {
    if (!token) {
      return;
    }

    setLoading(true);
    setError("");

    try {
      setOrders(await api.getAdminOrders(token));
    } catch (exception) {
      setError(readError(exception, "Could not load orders"));
    } finally {
      setLoading(false);
    }
  }

  async function handleStatusChange(orderId: number, status: OrderStatus) {
    if (!token) {
      return;
    }

    setUpdatingId(orderId);
    setError("");

    try {
      const updatedOrder = await api.updateOrderStatus(token, orderId, status);
      setOrders((current) =>
        current.map((order) => (order.id === orderId ? updatedOrder : order)),
      );
    } catch (exception) {
      setError(readError(exception, "Could not update order status"));
    } finally {
      setUpdatingId(null);
    }
  }

  return (
    <section className={common.pageStack}>
      <header className={common.pageHeader}>
        <div>
          <span className={common.eyebrow}>Fulfillment</span>
          <h1>Order management</h1>
          <p>
            Review customer orders and move them through the order lifecycle.
          </p>
        </div>
        <button
          className={common.secondaryButton}
          type="button"
          onClick={loadOrders}
        >
          <RefreshCw size={18} />
          Refresh
        </button>
      </header>

      {error && <p className={common.errorBanner}>{error}</p>}

      <section className={common.panel}>
        {loading ? (
          <p className={common.muted}>Loading orders...</p>
        ) : orders.length === 0 ? (
          <EmptyState
            title="No orders yet"
            message="Orders will appear here after customers check out."
          />
        ) : (
          <div className={styles.orderList}>
            {orders.map((order) => (
              <article className={styles.orderRow} key={order.id}>
                <div className={styles.orderSummary}>
                  <div>
                    <h2>Order #{order.id}</h2>
                    <p>{formatDateTime(order.createdAt)}</p>
                  </div>
                  <div className={styles.customerInfo}>
                    <span>Customer</span>
                    <strong>{order.customerName}</strong>
                    <p>{order.customerEmail}</p>
                  </div>
                  <div className={styles.orderTotal}>
                    <strong>{formatCurrency(order.totalAmount)}</strong>
                    <span>{order.totalItems} items</span>
                  </div>
                  <label>
                    Status
                    <select
                      defaultValue={order.status}
                      disabled={updatingId === order.id}
                      onChange={(event) =>
                        void handleStatusChange(
                          order.id,
                          event.target.value as OrderStatus,
                        )
                      }
                    >
                      {orderStatuses.map((status) => (
                        <option value={status} key={status}>
                          {status}
                        </option>
                      ))}
                    </select>
                  </label>
                </div>

                <div className={styles.shippingAddress}>
                  <span>Ship to</span>
                  <p>{formatShippingAddress(order)}</p>
                </div>

                <div className={styles.orderItems}>
                  {order.items.map((item) => (
                    <div className={styles.orderItem} key={item.id}>
                      <span>{item.productName}</span>
                      <span>
                        {item.quantity} x {formatCurrency(item.unitPrice)}
                      </span>
                      <strong>{formatCurrency(item.lineTotal)}</strong>
                    </div>
                  ))}
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </section>
  );
}

function readError(exception: unknown, fallback: string) {
  if (exception instanceof Error) {
    return exception.message;
  }

  return fallback;
}

function formatShippingAddress(order: Order) {
  const address = [
    order.shippingAddressLine,
    order.shippingCity,
    order.shippingState,
    order.shippingPostalCode,
    order.shippingCountry,
  ]
    .filter(Boolean)
    .join(", ");

  return address || "Address not available";
}
