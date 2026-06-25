# Frontend Architecture Standard

This document follows the project-wide clean code rules in `../CLEAN_CODE_STANDARD.md`.

## Current V1 Position

The current frontend is acceptable for v1 deployment. It is intentionally simple and focuses on a working customer/admin experience. Do not block v1 deployment only to refactor state management.

After v1 is live, new frontend expansion should move toward a feature-based structure with React Query for server state and Redux Toolkit for shared client state.

## State Management Rules

Use React Query for API/server state:

- Products and product details
- Cart and cart item mutations
- Customer orders and admin orders
- Profile and saved shipping address
- Admin product create/update/delete
- Product image upload
- Order status updates
- Checkout/order creation

Use Redux Toolkit only for client/global state:

- Auth session and logged-in user
- Shared UI state that must survive route changes
- Checkout draft state if it becomes multi-step
- Non-server preferences such as selected filters or layout options

Do not use Redux as a replacement for React Query cache. Backend data should remain owned by the backend and cached through React Query.

## Feature Structure

Future frontend code should move toward this shape:

```text
src/
  app/
    store.ts
    queryClient.ts
    router.tsx
  features/
    auth/
    products/
    cart/
    orders/
    profile/
    admin/
  components/
  lib/
  styles/
  types/
```

Each feature should own its API hooks, components, and page-specific helpers. Shared utilities stay in `lib`, shared UI stays in `components`, and global styles stay in `styles`.

## Data Fetching Pattern

Prefer feature hooks over direct API calls in pages.

Examples:

```ts
useProducts()
useProduct(productId)
useCart()
useAddCartItem()
useCustomerOrders()
useUpdateAddress()
useAdminOrders()
```

Pages should mostly compose hooks and components. Avoid repeating `useEffect + useState` for API-loaded data unless the data is truly local to that component and not worth caching.

Use React Query invalidation after mutations:

- Add/update/remove cart item invalidates cart query.
- Product create/update/delete invalidates products query.
- Image upload invalidates products and product detail queries.
- Checkout invalidates cart and customer orders.
- Address update invalidates current user/profile query.
- Admin order status update invalidates admin orders.

## Router Loaders

React Router loaders are acceptable for page-level data when they make navigation clearer, such as public product catalog/detail pages.

For authenticated data, prefer React Query hooks unless the route loader has a clean way to access auth and redirect safely. If loaders are kept, they should either prefetch React Query data or remain limited to simple public routes.

## Clean Code Rules

- Keep pages thin.
- Keep forms reusable when the same shape appears in multiple places, such as address and product forms.
- Keep API request/response types explicit in TypeScript.
- Keep business data out of UI-only state when it belongs in React Query.
- Keep Redux slices small and focused on client state.
- Prefer clear query keys and colocated hooks per feature.
- Run `npm.cmd run lint` and `npm.cmd run build` before completing frontend changes.

## Migration Approach

Do not rewrite the whole v1 frontend in one pass. Migrate feature by feature:

1. Add React Query provider and query client.
2. Add Redux Toolkit store for auth/client state.
3. Move auth to `features/auth`.
4. Migrate products to React Query.
5. Migrate cart and checkout.
6. Migrate customer/admin orders.
7. Migrate profile/address.
8. Split reusable forms/components after behavior is stable.

This keeps the app deployable while improving the codebase safely.
