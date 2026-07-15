# LinkShrink 🔗

A full-stack SaaS URL shortener with JWT authentication, freemium tier gating, click analytics, and Stripe payment integration — built with Spring Boot and deployed on Railway.

**Live demo:** (https://linkshrink-yyx1.onrender.com)

---

## Features

- **JWT Authentication** — stateless signup/login with access tokens and refresh tokens
- **URL Shortening** — generate unique 6-character short codes with collision-safe generation
- **Click Analytics** — track how many times each short link has been clicked
- **Freemium Tier Gate** — free users capped at 5 links; upgrade prompt on limit hit
- **Stripe Payments** — full Stripe Checkout integration; successful payment upgrades user to PAID tier
- **Admin Dashboard** — view and manage your short links with copy and refresh support
- **Responsive Frontend** — plain HTML/CSS/JS served from Spring's static folder (same origin, no CORS complexity)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3 |
| Security | Spring Security, JWT (jjwt), BCrypt |
| Database | MySQL, Spring Data JPA / Hibernate |
| Payments | Stripe Java SDK (Checkout Sessions) |
| Frontend | HTML, CSS, JavaScript (no framework) |
| Build | Maven |
| Deploy | Render |

---

## Architecture

```
Browser (HTML/CSS/JS)
       │
       │  HTTP requests (same origin)
       ▼
Spring Boot Application
       │
       ├── SecurityConfig (JWT filter chain)
       │       └── JwtAuthFilter (validates token on every protected request)
       │
       ├── Controllers
       │       ├── AuthController        /auth/v1/login, /signup
       │       ├── TokenController       /auth/v1/refreshToken
       │       ├── ShortUrlController    /api/shorten, /api/my-links, /r/{code}
       │       └── PaymentController     /api/create-checkout-session, /api/confirm-upgrade
       │
       ├── Services
       │       ├── UserDetailsServiceImpl  (Spring Security user loading)
       │       ├── JwtService              (token generation + validation)
       │       ├── RefreshTokenService     (refresh token lifecycle)
       │       ├── ShortUrlService         (create links, resolve redirects, count clicks)
       │       └── StripeService           (checkout sessions, payment verification)
       │
       └── MySQL Database
               ├── users          (userId, username, password, tier)
               ├── roles          (role definitions)
               ├── users_roles    (join table)
               ├── tokens         (refresh tokens)
               └── short_urls     (shortCode, longUrl, owner, clickCount, createdAt)
                                         │
                                         ▼
                                   Stripe API
                              (hosted checkout page)
```

---

## Auth Flow

**Signup / Login (Cycle 1 — once):**
```
Client → POST /auth/v1/login → AuthController
       → AuthenticationManager → DaoAuthenticationProvider
       → loadUserByUsername + BCrypt verify
       → JwtService.generateToken() → returns accessToken + refreshToken
```

**Protected Requests (Cycle 2 — every request):**
```
Client attaches: Authorization: Bearer <accessToken>
       → JwtAuthFilter extracts + validates token
       → sets SecurityContext → request reaches protected endpoint
```

---

## Freemium Model

| Feature | Free | Pro ($9/mo) |
|---|---|---|
| Short links | 5 | Unlimited |
| Click tracking | ✓ | ✓ |
| Copy + share | ✓ | ✓ |

Tier enforcement is **server-side** in `ShortUrlService` — the free limit cannot be bypassed from the frontend.

---

## Payment Flow

```
1. User hits free limit → upgrade prompt on dashboard
2. Click "Upgrade to Pro" → POST /api/create-checkout-session (JWT required)
3. Backend creates Stripe Checkout Session → returns hosted URL
4. Browser redirects to Stripe → user pays with card
5. Stripe redirects back to dashboard?upgraded=true&session_id=...
6. Dashboard calls POST /api/confirm-upgrade with session_id
7. Backend verifies session with Stripe (status == "complete")
8. User's tier updated to PAID in DB → unlimited links
```

---

## API Endpoints

### Auth
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /auth/v1/signup | Public | Register a new user |
| POST | /auth/v1/login | Public | Login, returns JWT + refresh token |
| POST | /auth/v1/refreshToken | Public | Exchange refresh token for new JWT |

### Short Links
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /api/shorten | Required | Create a short link |
| GET | /api/my-links | Required | List all your links with click counts |
| GET | /r/{shortCode} | Public | Redirect to original URL (counts click) |

### Payments
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | /api/create-checkout-session | Required | Start Stripe checkout |
| POST | /api/confirm-upgrade | Required | Verify payment + upgrade tier |

---

## Project Structure

```
src/main/java/com/arnav/authsystem/
├── auth/
│   ├── SecurityConfig.java       (filter chain, CORS, route rules)
│   ├── JwtAuthFilter.java        (OncePerRequestFilter — validates JWT)
│   └── UserConfig.java           (BCryptPasswordEncoder bean)
├── controller/
│   ├── AuthController.java
│   ├── TokenController.java
│   ├── ShortUrlController.java
│   ├── PaymentController.java
│   └── TestController.java
├── service/
│   ├── UserDetailsServiceImpl.java
│   ├── JwtService.java
│   ├── RefreshTokenService.java
│   ├── ShortUrlService.java
│   └── StripeService.java
├── entities/
│   ├── UserInfo.java
│   ├── UserRole.java
│   ├── RefreshToken.java
│   └── ShortUrl.java
├── repository/
│   ├── UserRepository.java
│   ├── ShortUrlRepository.java
│   └── (others)
├── dto/ request/ response/
└── AuthsystemApplication.java

src/main/resources/
├── static/
│   ├── index.html
│   ├── login.html
│   ├── signup.html
│   ├── dashboard.html
│   ├── upgrade.html
│   ├── css/styles.css
│   └── js/
│       ├── auth.js      (JWT helpers, fetchProtected)
│       └── nav.js       (dynamic login-aware navbar)
└── application.properties  (env var references, no secrets)
```

---

## Running Locally

**Prerequisites:** Java 17, Maven, MySQL

**1. Clone the repo:**
```bash
git clone https://github.com/arnavsharma569-creator/spring-boot-saas-api-.git
cd spring-boot-saas-api-
```

**2. Create a MySQL database:**
```sql
CREATE DATABASE authsystem;
```

**3. Set environment variables:**
```bash
export JWT_SECRET=your_jwt_secret_min_32_chars
export STRIPE_SECRET_KEY=sk_test_your_stripe_key
export STRIPE_PRICE_ID=price_your_price_id
```

**4. Run:**
```bash
./mvnw spring-boot:run
```

**5. Open:** `http://localhost:8080`

---

## Environment Variables

| Variable | Description |
|---|---|
| SPRING_DATASOURCE_URL | MySQL JDBC URL |
| SPRING_DATASOURCE_USERNAME | DB username |
| SPRING_DATASOURCE_PASSWORD | DB password |
| JWT_SECRET | HS256 signing secret (min 32 chars) |
| STRIPE_SECRET_KEY | Stripe secret key (sk_test_... or sk_live_...) |
| STRIPE_PRICE_ID | Stripe Price ID for Pro plan |
| STRIPE_SUCCESS_URL | Redirect URL after successful payment |
| STRIPE_CANCEL_URL | Redirect URL after cancelled payment |

---

## Key Design Decisions

**Why stateless JWT (no sessions)?**
Scalable — any server instance can validate a token without shared state. Each request is self-contained.

**Why protect data endpoints, not HTML pages?**
The HTML shell carries no sensitive data. Real protection lives on `/api/**` endpoints (server-side). Frontend JS guards are UX only, not security.

**Why enforce the free tier in the service, not the frontend?**
Frontend code can be bypassed (DevTools, direct API calls). Tier enforcement in `ShortUrlService` means the limit holds regardless of how the request arrives.

**Why success-redirect verification over webhooks?**
For this portfolio implementation, success-redirect + session verification is simpler to test locally with no Stripe CLI required. Production upgrade: add `/webhook/stripe` with signature verification and idempotency (store processed event IDs to prevent duplicate upgrades).

---

## Author

**Arnav Sharma** — final-year Computer Science student at RMIT Melbourne.

[GitHub](https://github.com/arnavsharma569-creator) 