# AuthSystem — JWT Authentication API

A stateless authentication system built with Spring Boot and JWT.  
Handles user signup, login, and token refresh with role-based access control.

## Tech Stack
- Java 17
- Spring Boot 4.x
- Spring Security
- MySQL
- JWT (jjwt 0.11.5)
- Lombok
- Maven

## How It Works
1. User signs up → password BCrypt encoded → saved to DB with ROLE_USER
2. User logs in → credentials verified → JWT access token + refresh token returned
3. Client sends JWT in `Authorization: Bearer <token>` header for protected routes
4. Access token expires (15 min) → use refresh token to get a new one
5. Refresh token expires (10 min) → user must log in again

## Run Locally

### Prerequisites
- Java 17+
- MySQL running on port 3306

### Setup
1. Clone the repo
```bash
   git clone https://github.com/yourusername/authsystem.git
   cd authsystem
```

2. Create the database
```sql
   CREATE DATABASE authsystem;
   USE authsystem;
   INSERT INTO roles (name) VALUES ('ROLE_USER');
```

3. Update `src/main/resources/application.properties` with your MySQL password

4. Run the app
```bash
   mvn spring-boot:run
```

## API Endpoints

| Method | Endpoint | Auth Required | Description |
|--------|----------|---------------|-------------|
| POST | `/auth/v1/signup` | No | Register a new user |
| POST | `/auth/v1/login` | No | Login and get tokens |
| POST | `/auth/v1/refreshToken` | No | Get new access token |
| GET | `/api/hello` | Yes (JWT) | Test protected route |

## Request Examples

### Signup
```json
POST /auth/v1/signup
{
  "username": "arnav123",
  "password": "yourpassword"
}
```

### Login
```json
POST /auth/v1/login
{
  "username": "arnav123",
  "password": "yourpassword"
}
```

### Refresh Token
```json
POST /auth/v1/refreshToken
{
  "token": "your-refresh-token-here"
}
```

### Protected Route
```
GET /api/hello
Authorization: Bearer <your-jwt-token>
```

## Response Format
```json
{
  "accessToken": "eyJhbGci...",
  "token": "uuid-refresh-token"
}
```

## Security Notes
- Passwords are encoded with BCrypt
- JWT tokens are signed with HMAC-SHA256
- Refresh tokens are stored in the database and replaced on each login
- CSRF is disabled (stateless JWT API)
- Secret key should be moved to environment variables before production deployment