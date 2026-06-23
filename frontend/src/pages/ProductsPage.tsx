import {
  useEffect,
  useMemo,
  useRef,
  useState,
  type SyntheticEvent,
} from "react";
import { ImagePlus, Pencil, Plus, Trash2, X } from "lucide-react";
import { useAuth } from "../auth/useAuth";
import { EmptyState } from "../components/EmptyState";
import { ApiClientError, api } from "../lib/api";
import { formatCurrency } from "../lib/format";
import common from "../styles/common.module.css";
import type { Product, ProductPayload } from "../types";
import styles from "./ProductsPage.module.css";

const emptyForm: ProductPayload = {
  name: "",
  description: "",
  price: 0,
  availableStock: 0,
  active: false,
  imageUrl: "",
};

export function ProductsPage() {
  const { token } = useAuth();
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string>("");
  const [form, setForm] = useState<ProductPayload>(emptyForm);
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const imageInputRef = useRef<HTMLInputElement | null>(null);

  const activeCount = useMemo(
    () => products.filter((product) => product.active).length,
    [products],
  );

  useEffect(() => {
    void loadProducts();
  }, []);

  async function loadProducts() {
    setLoading(true);
    setError("");

    try {
      setProducts(await api.getProducts());
    } catch (exception) {
      setError(readError(exception, "Could not load products"));
    } finally {
      setLoading(false);
    }
  }

  function startEdit(product: Product) {
    setEditingProduct(product);
    setImageFile(null);
    clearImageInput();
    setForm({
      name: product.name,
      description: product.description || "",
      price: product.price,
      availableStock: product.availableStock,
      active: product.active,
      imageUrl: product.imageUrl || "",
    });
  }

  function resetForm() {
    setEditingProduct(null);
    setImageFile(null);
    clearImageInput();
    setForm(emptyForm);
  }

  function clearImageInput() {
    if (imageInputRef.current) {
      imageInputRef.current.value = "";
    }
  }

  async function handleSubmit(event: SyntheticEvent<HTMLFormElement>) {
    event.preventDefault();

    if (!token) {
      return;
    }

    setSubmitting(true);
    setError("");

    try {
      const savedProduct = editingProduct
        ? await api.updateProduct(token, editingProduct.id, form)
        : await api.createProduct(token, form);

      if (imageFile) {
        await api.uploadProductImage(token, savedProduct.id, imageFile);
      }

      resetForm();
      await loadProducts();
    } catch (exception) {
      setError(readError(exception, "Could not save product"));
    } finally {
      setSubmitting(false);
    }
  }

  async function handleDelete(productId: number) {
    if (!token || !confirm("Delete this product?")) {
      return;
    }

    try {
      await api.deleteProduct(token, productId);
      await loadProducts();
    } catch (exception) {
      setError(readError(exception, "Could not delete product"));
    }
  }

  return (
    <section className={common.pageStack}>
      <header className={common.pageHeader}>
        <div>
          <span className={common.eyebrow}>Catalog</span>
          <h1>Product management</h1>
          <p>
            Create products, upload Cloudinary images, and keep stock visible.
          </p>
        </div>
        <div className={styles.metricRow}>
          <div className={styles.metric}>
            <strong>{products.length}</strong>
            <span>Total products</span>
          </div>
          <div className={styles.metric}>
            <strong>{activeCount}</strong>
            <span>Active</span>
          </div>
        </div>
      </header>

      {error && <p className={common.errorBanner}>{error}</p>}

      <section className={styles.splitLayout}>
        <form
          className={`${common.panel} ${common.formStack}`}
          onSubmit={handleSubmit}
        >
          <div className={common.sectionHeading}>
            <div>
              <h2>{editingProduct ? "Edit product" : "Create product"}</h2>
              <p>
                Add the product details and optionally attach an image in the
                same form.
              </p>
            </div>
            {editingProduct && (
              <button
                className={common.iconButton}
                type="button"
                onClick={resetForm}
              >
                <X size={18} />
                <span className={common.srOnly}>Cancel edit</span>
              </button>
            )}
          </div>

          <label>
            Name
            <input
              value={form.name}
              onChange={(event) =>
                setForm({ ...form, name: event.target.value })
              }
              placeholder="Mechanical Keyboard"
              required
            />
          </label>

          <label>
            Description
            <textarea
              value={form.description}
              onChange={(event) =>
                setForm({ ...form, description: event.target.value })
              }
              placeholder="Short product description"
              rows={4}
            />
          </label>

          <div className={styles.fieldGrid}>
            <label>
              Price
              <input
                type="number"
                min="0.01"
                step="0.01"
                value={form.price}
                onChange={(event) =>
                  setForm({ ...form, price: Number(event.target.value) })
                }
                required
              />
            </label>
            <label>
              Stock
              <input
                type="number"
                min="0"
                value={form.availableStock}
                onChange={(event) =>
                  setForm({
                    ...form,
                    availableStock: Number(event.target.value),
                  })
                }
                required
              />
            </label>
          </div>

          <label className={styles.checkboxRow}>
            <input
              type="checkbox"
              checked={form.active}
              onChange={(event) =>
                setForm({ ...form, active: event.target.checked })
              }
            />
            Product is active
          </label>

          <label>
            Product image
            <input
              ref={imageInputRef}
              type="file"
              accept="image/jpeg,image/png,image/webp"
              onChange={(event) =>
                setImageFile(event.target.files?.[0] || null)
              }
            />
          </label>

          <button
            className={common.primaryButton}
            type="submit"
            disabled={submitting}
          >
            <Plus size={18} />
            {submitting
              ? "Saving..."
              : editingProduct
                ? "Update product"
                : "Create product"}
          </button>
        </form>

        <section className={common.panel}>
          <div className={common.sectionHeading}>
            <div>
              <h2>Products</h2>
              <p>Images are loaded from the Cloudinary URL saved by backend.</p>
            </div>
          </div>

          {loading ? (
            <p className={common.muted}>Loading products...</p>
          ) : products.length === 0 ? (
            <EmptyState
              title="No products yet"
              message="Create your first product to start filling the catalog."
            />
          ) : (
            <div className={styles.productList}>
              {products.map((product) => (
                <article className={styles.productRow} key={product.id}>
                  <div className={styles.productImage}>
                    {product.imageUrl ? (
                      <img src={product.imageUrl} alt={product.name} />
                    ) : (
                      <ImagePlus size={28} />
                    )}
                  </div>
                  <div className={styles.productInfo}>
                    <div>
                      <h3>{product.name}</h3>
                      <p>{product.description || "No description"}</p>
                    </div>
                    <div className={styles.productMeta}>
                      <span>{formatCurrency(product.price)}</span>
                      <span>{product.availableStock} in stock</span>
                      <span
                        className={
                          product.active
                            ? styles.pill
                            : `${styles.pill} ${styles.mutedPill}`
                        }
                      >
                        {product.active ? "Active" : "Inactive"}
                      </span>
                    </div>
                  </div>
                  <div className={styles.rowActions}>
                    <button
                      className={common.iconButton}
                      type="button"
                      onClick={() => startEdit(product)}
                    >
                      <Pencil size={18} />
                      <span className={common.srOnly}>Edit product</span>
                    </button>
                    <button
                      className={`${common.iconButton} ${common.danger}`}
                      type="button"
                      onClick={() => void handleDelete(product.id)}
                    >
                      <Trash2 size={18} />
                      <span className={common.srOnly}>Delete product</span>
                    </button>
                  </div>
                </article>
              ))}
            </div>
          )}
        </section>
      </section>
    </section>
  );
}

function readError(exception: unknown, fallback: string) {
  if (exception instanceof ApiClientError) {
    return exception.message;
  }

  if (exception instanceof Error) {
    return exception.message;
  }

  return fallback;
}
