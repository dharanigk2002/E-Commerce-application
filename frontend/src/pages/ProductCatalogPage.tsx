import { useMemo } from "react";
import { ImagePlus } from "lucide-react";
import { Link, useLoaderData } from "react-router-dom";
import { EmptyState } from "../components/EmptyState";
import { formatCurrency } from "../lib/format";
import common from "../styles/common.module.css";
import type { Product } from "../types";
import styles from "./CustomerPages.module.css";

export function ProductCatalogPage() {
  const products = useLoaderData<Product[]>();

  const activeProducts = useMemo(
    () => products.filter((product) => product.active),
    [products],
  );

  return (
    <section>
      <header className={styles.hero}>
        <div>
          <span className={common.eyebrow}>Customer store</span>
          <h1>Shop products</h1>
          <p>Browse active products and add your favorites to cart.</p>
        </div>
      </header>

      {activeProducts.length === 0 ? (
        <EmptyState
          title="No products available"
          message="Active products will appear here."
        />
      ) : (
        <div className={styles.productGrid}>
          {activeProducts.map((product) => (
            <Link
              className={styles.cardLink}
              to={`/products/${product.id}`}
              key={product.id}
            >
              <article className={styles.card}>
                <div className={styles.productImage}>
                  {product.imageUrl ? (
                    <img src={product.imageUrl} alt={product.name} />
                  ) : (
                    <ImagePlus size={30} />
                  )}
                </div>
                <div>
                  <h2>{product.name}</h2>
                  <p>{product.description || "No description"}</p>
                </div>
                <div className={styles.meta}>
                  <span className={styles.stock}>
                    {product.availableStock} in stock
                  </span>
                  <p>
                    <strong className={styles.price}>
                      {formatCurrency(product.price)}
                    </strong>
                  </p>
                </div>
              </article>
            </Link>
          ))}
        </div>
      )}
    </section>
  );
}
