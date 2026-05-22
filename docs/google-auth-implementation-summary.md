# SplitShare Google Sign-In Implementation Summary

## Project Title
SplitShare

## Brief Description
Google Sign-In was added as an alternative authentication method beside the existing email and password login. The backend uses Spring Security OAuth2 Client to authenticate users with Google. After a successful Google login, the backend creates or finds the user by email, issues the same JWT access and refresh tokens used by the existing login flow, and redirects the user back to the web or mobile client.

## Backend Technologies and Libraries
- Spring Boot Security
- Spring Security OAuth2 Client
- Existing JWT service
- Existing JPA user repository

## Frontend Integration Summary
The React login page now includes a "Sign in with Google" button. It redirects to the backend OAuth2 authorization endpoint. After Google authentication succeeds, the backend redirects to `/oauth2/success` with JWT session data. React saves the session through the existing `AuthContext` and redirects the user to the dashboard or admin page.

## Mobile Integration Summary
The Android login screen now includes a "Sign in with Google" button. It opens the backend Google OAuth2 endpoint in the browser and requests a mobile deep-link redirect. After successful authentication, Android receives `splitshare://oauth2/success`, saves the JWT session through `SessionManager`, and opens the main app.

## Google OAuth Setup
Create a Google OAuth Client ID in Google Cloud Console and add this authorized redirect URI:

```text
http://localhost:8080/login/oauth2/code/google
```

Run the backend with:

```text
GOOGLE_CLIENT_ID=your-client-id
GOOGLE_CLIENT_SECRET=your-client-secret
```

## Challenges Encountered
The main integration concern was supporting both React and Android with one backend OAuth2 flow. This was handled by preserving a safe redirect target in the OAuth2 state and redirecting to either the React success page or the Android deep link after the backend issues JWTs.
