# Authentication Flow

## Sequence Diagram

```mermaid
sequenceDiagram
    participant U as User
    participant W as Web Application
    participant G as Google OAuth
    participant B as Backend API
    participant DB as PostgreSQL

    U->>W: Click "Sign in with Google"
    W->>G: Open Google consent popup (auth-code flow)
    G->>U: Show consent screen
    U->>G: Grant permission
    G->>W: Return authorization code

    W->>B: POST /api/v1/auth/google {code, redirectUri}
    B->>G: Exchange code for ID token (server-side)
    G->>B: Return ID token with user info

    B->>B: Verify ID token signature
    B->>DB: Find user by email
    alt User exists
        DB->>B: Return existing user
    else New user
        B->>DB: Create user with default USER role
        DB->>B: Return new user
    end

    B->>B: Generate JWT access token (15 min)
    B->>B: Generate refresh token (7 days)
    B->>DB: Store refresh token with JTI
    B->>W: Set HTTP-only cookies (access_token, refresh_token)
    W->>B: GET /api/v1/users/me
    B->>DB: Fetch current user profile
    DB->>B: User data
    B->>W: CurrentUserResponse
    W->>W: Store user in Zustand, redirect to dashboard
    W->>U: Display dashboard

    Note over U,DB: Token Refresh Flow (on 401)

    W->>B: API request (expired access token)
    B->>W: 401 Unauthorized
    W->>B: POST /api/v1/auth/refresh (refresh token cookie)
    B->>DB: Validate refresh token JTI
    alt Valid refresh token
        B->>B: Generate new access token
        B->>B: Generate new refresh token (rotation)
        B->>DB: Store new refresh token, delete old
        B->>W: Set new HTTP-only cookies
        W->>B: Retry original request with new token
        B->>W: Success response
    else Invalid/expired refresh token
        B->>W: 401 Unauthorized
        W->>W: Clear stores, redirect to login
    end

    Note over U,DB: Logout Flow

    U->>W: Click logout
    W->>B: POST /api/v1/auth/logout
    B->>DB: Invalidate access token (add JTI to blacklist)
    B->>DB: Delete refresh token
    B->>W: Clear cookies
    W->>W: Clear Zustand stores
    W->>U: Redirect to login page
```

## Flow Description

1. **Google Sign-In Initiation** - User clicks the Google sign-in button, which opens a Google consent popup using the `@react-oauth/google` library with auth-code flow.

2. **Authorization Code Grant** - After the user grants permission, Google returns an authorization code to the frontend (not an ID token directly, for security).

3. **Server-Side Token Exchange** - The frontend sends the authorization code and redirect URI to `POST /api/v1/auth/google`. The backend securely exchanges this code with Google's servers for an ID token containing the user's email and name.

4. **User Resolution** - The backend looks up the user by email. If the user doesn't exist, a new account is created with the default `USER` role and linked to a default team.

5. **JWT Token Generation** - The backend generates a JWT access token (15-minute expiry, contains userId, role, JTI) and a refresh token (7-day expiry). The refresh token is persisted in the `refresh_tokens` table.

6. **Secure Cookie Delivery** - Both tokens are set as HTTP-only cookies (secure + SameSite in production). This prevents XSS attacks from accessing tokens via JavaScript.

7. **User Profile Fetch** - The frontend immediately calls `GET /api/v1/users/me` to get the full user profile, stores it in the Zustand user store, and redirects to the dashboard.

8. **Transparent Token Refresh** - When the access token expires, the Axios interceptor catches the 401 response, calls the refresh endpoint, and retries the original request. A single-flight pattern prevents concurrent refresh races.

9. **Logout** - On logout, the backend invalidates the access token by adding its JTI to a blacklist table, deletes the refresh token, and clears the cookies. The frontend clears all Zustand stores and redirects to login.
