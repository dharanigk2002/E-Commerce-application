import { useEffect, useState, type SyntheticEvent } from "react";
import { useAuth } from "../auth/useAuth";
import { api } from "../lib/api";
import { addressFromUser, formatAddress, hasAddress } from "../lib/customerAddress";
import common from "../styles/common.module.css";
import type { AddressPayload, User } from "../types";
import styles from "./CustomerPages.module.css";

const emptyAddress: AddressPayload = {
  shippingAddressLine: "",
  shippingCity: "",
  shippingState: "",
  shippingPostalCode: "",
  shippingCountry: "",
};

export function ProfilePage() {
  const { token, refreshUser } = useAuth();
  const [user, setUser] = useState<User | null>(null);
  const [form, setForm] = useState<AddressPayload>(emptyAddress);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    async function loadProfile() {
      if (!token) return;
      try {
        const loadedUser = await api.getCurrentUser(token);
        setUser(loadedUser);
        setForm(addressFromUser(loadedUser));
      } catch (exception) {
        setError(readError(exception, "Could not load profile"));
      } finally {
        setLoading(false);
      }
    }

    void loadProfile();
  }, [token]);

  async function handleSubmit(event: SyntheticEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!token) return;

    setSaving(true);
    setError("");
    setMessage("");

    try {
      const updatedUser = await api.updateMyAddress(token, form);
      setUser(updatedUser);
      await refreshUser();
      setMessage("Address saved");
    } catch (exception) {
      setError(readError(exception, "Could not save address"));
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className={styles.twoColumn}>
      <div className={common.panel}>
        <span className={common.eyebrow}>Profile</span>
        <h1>{user?.fullName || "Customer profile"}</h1>
        <p className={styles.meta}>{user?.email}</p>
        {user && hasAddress(user) && (
          <div className={styles.addressBox}>
            <strong>Saved shipping address</strong>
            <p className={styles.addressText}>{formatAddress(user)}</p>
          </div>
        )}
      </div>

      <form className={`${common.panel} ${common.formStack}`} onSubmit={handleSubmit}>
        <h2>Shipping address</h2>
        {loading ? <p className={common.muted}>Loading...</p> : null}
        <AddressFields form={form} setForm={setForm} />
        {error && <p className={common.errorMessage}>{error}</p>}
        {message && <p className={common.muted}>{message}</p>}
        <button className={common.primaryButton} type="submit" disabled={saving}>
          {saving ? "Saving..." : "Save address"}
        </button>
      </form>
    </section>
  );
}

export function AddressFields({
  form,
  setForm,
}: {
  form: AddressPayload;
  setForm: (form: AddressPayload) => void;
}) {
  return (
    <div className={styles.formGrid}>
      <label className={styles.fullWidth}>
        Address line
        <input value={form.shippingAddressLine} onChange={(event) => setForm({ ...form, shippingAddressLine: event.target.value })} required />
      </label>
      <label>
        City
        <input value={form.shippingCity} onChange={(event) => setForm({ ...form, shippingCity: event.target.value })} required />
      </label>
      <label>
        State
        <input value={form.shippingState} onChange={(event) => setForm({ ...form, shippingState: event.target.value })} required />
      </label>
      <label>
        Postal code
        <input value={form.shippingPostalCode} onChange={(event) => setForm({ ...form, shippingPostalCode: event.target.value })} required />
      </label>
      <label>
        Country
        <input value={form.shippingCountry} onChange={(event) => setForm({ ...form, shippingCountry: event.target.value })} required />
      </label>
    </div>
  );
}

function readError(exception: unknown, fallback: string) {
  return exception instanceof Error ? exception.message : fallback;
}
