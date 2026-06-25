// ============================================================
//  auth.js — shared auth helpers used by every page.
//  Include with <script src="js/auth.js"></script> BEFORE nav.js
// ============================================================

// Base URL of your Spring Boot backend. Change to your deployed URL later.
const API_BASE = "http://localhost:8080";

// ---- token helpers (the JWT lives in the browser's localStorage) ----

// is the user logged in?  -> true if we have a stored access token
function isLoggedIn() {
  return localStorage.getItem('accessToken') !== null;
}

// get the saved JWT (used to call protected endpoints later)
function getToken() {
  return localStorage.getItem('accessToken');
}

// log out: wipe the tokens and send user home
function logout() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  window.location.href = 'index.html';
}

// ---- SIGNUP: POST /auth/v1/signup ----
async function signupUser(username, password) {
  const res = await fetch(`${API_BASE}/auth/v1/signup`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },   // tells Spring it's JSON -> maps to your DTO
    body: JSON.stringify({ username, password })       // field names must match your DTO fields
  });
  return res;   // caller checks res.ok
}

// ---- LOGIN: POST /auth/v1/login -> returns + stores the JWT ----
async function loginUser(username, password) {
  const res = await fetch(`${API_BASE}/auth/v1/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password })
  });

  if (res.ok) {
    const data = await res.json();
    // save the tokens so protected pages can use them
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.token);
  }
  return res;
}

// ---- helper to call a PROTECTED endpoint with the token attached ----
// use this later on the dashboard: fetchProtected('/api/shorten', {...})
async function fetchProtected(path, options = {}) {
  const token = getToken();
  return fetch(`${API_BASE}${path}`, {
    ...options,
    headers: {
      ...(options.headers || {}),
      'Authorization': `Bearer ${token}`   // <-- this is how Cycle 2 works from the frontend
    }
  });
}