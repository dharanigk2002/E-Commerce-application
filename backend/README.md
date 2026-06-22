# E-commerce Backend

Spring Boot backend for the e-commerce learning project.

## What is built first

- Product REST APIs
- PostgreSQL connection
- Flyway database migration
- JPA entity and repository
- DTO-based API contracts
- Validation
- Global exception handling
- Swagger UI
- Basic service tests

## Run locally from IntelliJ

1. Open the `backend` folder as a Maven project.
2. Start PostgreSQL with Docker Compose:

   ```bash
   docker compose up -d
   ```

3. Run `EcommerceApplication`.
4. Open Swagger UI:

   ```text
   http://localhost:8080/swagger-ui.html
   ```

## Product APIs

```http
GET    /api/products
GET    /api/products/{id}
POST   /api/admin/products
PUT    /api/admin/products/{id}
DELETE /api/admin/products/{id}
POST   /api/admin/products/{id}/image
```

Product image upload uses `multipart/form-data` with a `file` field. Uploaded images are stored in Cloudinary, and product responses return the Cloudinary HTTPS image URL.

Required Cloudinary environment variables:

```text
CLOUDINARY_CLOUD_NAME=your-cloud-name
CLOUDINARY_API_KEY=your-api-key
CLOUDINARY_API_SECRET=your-api-secret
```

## Authentication APIs

```http
POST /api/auth/register
POST /api/auth/login
GET  /api/users/me
```

Registration creates a `CUSTOMER` user. Admin product write APIs require an `ADMIN` token.

For local learning, register a user first and then promote that user in PostgreSQL:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

After updating the role, log in again and use the returned token:

```text
Authorization: Bearer <accessToken>
```

## Cart APIs

Cart APIs require a logged-in user.

```http
GET    /api/cart
POST   /api/cart/items
PUT    /api/cart/items/{itemId}
DELETE /api/cart/items/{itemId}
DELETE /api/cart
```

## Order APIs

Order APIs require a logged-in user.

```http
POST /api/orders
GET  /api/orders
GET  /api/orders/{id}
```

Admin order APIs require an `ADMIN` token.

```http
GET /api/admin/orders
PUT /api/admin/orders/{id}/status
```

## Deploy Backend To Render

This backend is prepared for Render Docker deployment with Render PostgreSQL and Cloudinary.

1. Create a Render PostgreSQL database first.
2. Create a Render Web Service from this GitHub repository.
3. Set the service root directory to:

   ```text
   backend
   ```

4. Use Docker as the runtime. Render will use `backend/Dockerfile`.
5. Set the health check path:

   ```text
   /actuator/health
   ```

Required Render environment variables:

```text
DB_URL=jdbc:postgresql://<render-internal-host>:5432/<database-name>
DB_USERNAME=<render-db-user>
DB_PASSWORD=<render-db-password>
JWT_SECRET=<strong-32-plus-character-secret>
JWT_EXPIRATION_MINUTES=60
CLOUDINARY_CLOUD_NAME=<your-cloud-name>
CLOUDINARY_API_KEY=<your-api-key>
CLOUDINARY_API_SECRET=<your-api-secret>
CLOUDINARY_PRODUCT_IMAGE_FOLDER=ecommerce/products
APP_CORS_ALLOWED_ORIGINS=http://localhost:5173,http://localhost:3000
```

After the frontend is deployed, update `APP_CORS_ALLOWED_ORIGINS` to include the frontend URL.

Smoke-test after deployment:

```http
GET https://<service-name>.onrender.com/actuator/health
GET https://<service-name>.onrender.com/swagger-ui.html
```

To create the first admin, register normally, then run this SQL in Render PostgreSQL:

```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```

Log in again after promotion so the new JWT contains the `ADMIN` role.
