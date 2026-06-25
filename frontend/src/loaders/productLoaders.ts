import type { LoaderFunctionArgs } from "react-router-dom";
import { api } from "../lib/api";

export async function productCatalogLoader() {
  return api.getProducts();
}

export async function productDetailLoader({ params }: LoaderFunctionArgs) {
  const productId = Number(params.id);

  if (!productId) {
    throw new Response("Product not found", { status: 404 });
  }

  return api.getProductById(productId);
}
