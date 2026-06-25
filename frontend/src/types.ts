export type Role = "CUSTOMER" | "ADMIN";

export type User = {
  id: number;
  fullName: string;
  email: string;
  role: Role;
  shippingAddressLine: string | null;
  shippingCity: string | null;
  shippingState: string | null;
  shippingPostalCode: string | null;
  shippingCountry: string | null;
  createdAt: string;
};

export type AuthResponse = {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: User;
};

export type Product = {
  id: number;
  name: string;
  description: string | null;
  price: number;
  availableStock: number;
  active: boolean;
  imageUrl: string | null;
  createdAt: string;
  updatedAt: string;
};

export type ProductPayload = {
  name: string;
  description: string;
  price: number;
  availableStock: number;
  active: boolean;
  imageUrl: string;
};

export type RegisterPayload = {
  fullName: string;
  email: string;
  password: string;
};

export type AddressPayload = {
  shippingAddressLine: string;
  shippingCity: string;
  shippingState: string;
  shippingPostalCode: string;
  shippingCountry: string;
};

export type CartItem = {
  id: number;
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
};

export type Cart = {
  id: number;
  items: CartItem[];
  totalItems: number;
  totalAmount: number;
};

export type OrderStatus =
  | "PENDING"
  | "CONFIRMED"
  | "SHIPPED"
  | "DELIVERED"
  | "CANCELLED";

export type OrderItem = {
  id: number;
  productId: number;
  productName: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
};

export type Order = {
  id: number;
  status: OrderStatus;
  customerName: string;
  customerEmail: string;
  shippingAddressLine: string;
  shippingCity: string;
  shippingState: string;
  shippingPostalCode: string;
  shippingCountry: string;
  items: OrderItem[];
  totalItems: number;
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
};

export type ApiError = {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  validationErrors: Record<string, string> | null;
};
