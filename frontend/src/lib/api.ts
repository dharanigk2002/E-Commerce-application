import type {
  ApiError,
  AuthResponse,
  CreateOrderPayload,
  Order,
  OrderStatus,
  Product,
  ProductPayload,
} from "../types";

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, "") ||
  "http://localhost:8080";

let unauthorizedHandler: (() => void) | null = null;

export function setUnauthorizedHandler(handler: (() => void) | null) {
  unauthorizedHandler = handler;
}

type RequestOptions = {
  token?: string;
  body?: unknown;
  method?: "GET" | "POST" | "PUT" | "DELETE";
  headers?: HeadersInit;
};

export class ApiClientError extends Error {
  status: number;
  validationErrors: Record<string, string> | null;

  constructor(error: ApiError) {
    super(error.message);
    this.status = error.status;
    this.validationErrors = error.validationErrors;
  }
}

async function request<T>(
  path: string,
  options: RequestOptions = {},
): Promise<T> {
  const headers = new Headers(options.headers);

  if (options.body && !(options.body instanceof FormData)) {
    headers.set("Content-Type", "application/json");
  }

  if (options.token) {
    headers.set("Authorization", `Bearer ${options.token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    method: options.method || "GET",
    headers,
    body:
      options.body instanceof FormData
        ? options.body
        : options.body
          ? JSON.stringify(options.body)
          : undefined,
  });

  if (!response.ok) {
    const error = (await response.json()) as ApiError;
    if (response.status === 401) {
      unauthorizedHandler?.();
    }
    throw new ApiClientError(error);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return (await response.json()) as T;
}

export const api = {
  login(email: string, password: string) {
    return request<AuthResponse>("/api/auth/login", {
      method: "POST",
      body: { email, password },
    });
  },

  getProducts() {
    return request<Product[]>("/api/products");
  },

  createProduct(token: string, payload: ProductPayload) {
    return request<Product>("/api/admin/products", {
      method: "POST",
      token,
      body: payload,
    });
  },

  updateProduct(token: string, productId: number, payload: ProductPayload) {
    return request<Product>(`/api/admin/products/${productId}`, {
      method: "PUT",
      token,
      body: payload,
    });
  },

  deleteProduct(token: string, productId: number) {
    return request<void>(`/api/admin/products/${productId}`, {
      method: "DELETE",
      token,
    });
  },

  uploadProductImage(token: string, productId: number, file: File) {
    const formData = new FormData();
    formData.append("file", file);

    return request<Product>(`/api/admin/products/${productId}/image`, {
      method: "POST",
      token,
      body: formData,
    });
  },

  getAdminOrders(token: string) {
    return request<Order[]>("/api/admin/orders", { token });
  },

  createOrder(token: string, payload: CreateOrderPayload) {
    return request<Order>("/api/orders", {
      method: "POST",
      token,
      body: payload,
    });
  },

  updateOrderStatus(token: string, orderId: number, status: OrderStatus) {
    return request<Order>(`/api/admin/orders/${orderId}/status`, {
      method: "PUT",
      token,
      body: { status },
    });
  },
};
