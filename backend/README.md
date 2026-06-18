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
```
