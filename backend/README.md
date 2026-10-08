# ShopEase backend (Spring Boot 3 / Java 17 / PostgreSQL)

## Run it
```bash
# 1. database (Docker)
docker run --name shopease-db -e POSTGRES_DB=shopease -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:16

# 2. (optional) local mail catcher so order emails have somewhere to go
docker run --name mailpit -p 1025:1025 -p 8025:8025 -d axllent/mailpit

# 3. start the API
mvn spring-boot:run
```
- Swagger UI: http://localhost:8080/swagger-ui.html
- Flyway creates the tables on first start (`db/migration`).
- Default admin (dev only): `admin@shopease.com` / `Admin@12345` (env `ADMIN_EMAIL`, `ADMIN_PASSWORD`).

## Endpoints
| Method | Path | Access |
|---|---|---|
| POST | /api/v1/auth/register, /login | public |
| GET | /api/v1/auth/me | logged in |
| GET | /api/v1/products, /products/{id}, /products/{id}/reviews | public |
| GET | /api/v1/categories | public |
| POST | /api/v1/products/{id}/reviews | logged in |
| GET/POST/PATCH/DELETE | /api/v1/cart, /cart/items, /cart/items/{productId} | logged in |
| POST/GET | /api/v1/orders, /orders/{id}, POST /orders/{id}/cancel | logged in |
| POST | /api/v1/payments/create-intent | logged in |
| POST | /api/v1/payments/webhook | Stripe only (signature checked) |
| POST/PUT/DELETE | /api/v1/categories | admin |
| POST/PUT/DELETE | /api/v1/admin/products | admin |
| GET/PATCH | /api/v1/admin/orders, /admin/orders/{id}/status | admin |
| GET/PATCH | /api/v1/admin/users, /admin/users/{id}/enabled?value=false | admin |

## Config (environment variables)
`DB_URL, DB_USERNAME, DB_PASSWORD, JWT_SECRET (base64, 32+ bytes), CORS_ORIGINS, STRIPE_SECRET_KEY, STRIPE_WEBHOOK_SECRET, MAIL_HOST, MAIL_PORT`
Run tests with `mvn test`.
