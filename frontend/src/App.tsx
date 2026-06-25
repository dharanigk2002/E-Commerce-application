import { createBrowserRouter, Navigate, RouterProvider } from "react-router-dom";
import { AdminLayout } from "./components/AdminLayout";
import { CustomerLayout } from "./components/CustomerLayout";
import { ProtectedRoute } from "./components/ProtectedRoute";
import { CartPage } from "./pages/CartPage";
import { CheckoutPage } from "./pages/CheckoutPage";
import { CustomerLoginPage } from "./pages/CustomerLoginPage";
import { CustomerOrdersPage } from "./pages/CustomerOrdersPage";
import {
  productCatalogLoader,
  productDetailLoader,
} from "./loaders/productLoaders";
import { RouteErrorPage } from "./pages/RouteErrorPage";
import { LoginPage } from "./pages/LoginPage";
import { ProductCatalogPage } from "./pages/ProductCatalogPage";
import { ProductDetailPage } from "./pages/ProductDetailPage";
import { ProfilePage } from "./pages/ProfilePage";
import { RegisterPage } from "./pages/RegisterPage";
import { OrdersPage } from "./pages/OrdersPage";
import { ProductsPage } from "./pages/ProductsPage";

const router = createBrowserRouter([
  {
    element: <CustomerLayout />,
    children: [
      {
        index: true,
        loader: productCatalogLoader,
        errorElement: <RouteErrorPage />,
        element: <ProductCatalogPage />,
      },
      {
        path: "/products/:id",
        loader: productDetailLoader,
        errorElement: <RouteErrorPage />,
        element: <ProductDetailPage />,
      },
      {
        path: "/login",
        element: <CustomerLoginPage />,
      },
      {
        path: "/register",
        element: <RegisterPage />,
      },
      {
        element: <ProtectedRoute allowedRoles={["CUSTOMER"]} />,
        children: [
          {
            path: "/cart",
            element: <CartPage />,
          },
          {
            path: "/checkout",
            element: <CheckoutPage />,
          },
          {
            path: "/orders",
            element: <CustomerOrdersPage />,
          },
          {
            path: "/orders/:id",
            element: <CustomerOrdersPage />,
          },
          {
            path: "/profile",
            element: <ProfilePage />,
          },
        ],
      },
    ],
  },
  {
    path: "/admin/login",
    element: <LoginPage />,
  },
  {
    element: (
      <ProtectedRoute allowedRoles={["ADMIN"]} redirectTo="/admin/login" />
    ),
    children: [
      {
        path: "/admin",
        element: <AdminLayout />,
        children: [
          {
            index: true,
            element: <Navigate to="/admin/products" replace />,
          },
          {
            path: "products",
            element: <ProductsPage />,
          },
          {
            path: "orders",
            element: <OrdersPage />,
          },
        ],
      },
    ],
  },
  {
    path: "*",
    element: <Navigate to="/" replace />,
  },
]);

export default function App() {
  return <RouterProvider router={router} />;
}
