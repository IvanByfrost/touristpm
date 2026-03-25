/**
 * TouristChain API Client
 * Centralized fetch wrapper for communicating with the backend.
 */

const API_BASE_URL = '/api';

/**
 * Helper to get the user's JWT token from localStorage.
 */
function getToken() {
  const user = JSON.parse(localStorage.getItem('tc_user') || 'null');
  return user ? user.token : null;
}

/**
 * Base fetch wrapper with common headers and error handling.
 */
async function apiFetch(endpoint, options = {}) {
  const token = getToken();
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const finalUrl = `${API_BASE_URL}${endpoint}`;
  const response = await fetch(finalUrl, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const errorText = await response.text();
    let errorMessage = `Error: ${response.status}`;
    try {
        const errorJson = JSON.parse(errorText);
        errorMessage = errorJson.message || errorMessage;
    } catch (e) {
        errorMessage = errorText || errorMessage;
    }
    throw new Error(errorMessage);
  }

  // Return null for 204 No Content
  if (response.status === 204) return null;

  // Some endpoints return plain strings (like /signup)
  const contentType = response.headers.get('content-type');
  if (contentType && contentType.includes('application/json')) {
    return await response.json();
  } else {
    return await response.text();
  }
}

/**
 * Authentication API
 */
export const authApi = {
  login: (email, password) => apiFetch('/auth/signin', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  }),
  
  signup: (userData) => apiFetch('/auth/signup', {
    method: 'POST',
    body: JSON.stringify(userData),
  }),
};

/**
 * Partners API
 */
export const partnerApi = {
  getAll: () => apiFetch('/partners'),
  
  search: (query) => apiFetch(`/partners/search?query=${encodeURIComponent(query)}`),
  
  create: (partner) => apiFetch('/partners', {
    method: 'POST',
    body: JSON.stringify(partner),
  }),
  
  update: (id, partner) => apiFetch(`/partners/${id}`, {
    method: 'PUT',
    body: JSON.stringify(partner),
  }),
};

/**
 * Packages (Travels/Destinations) API
 */
export const packageApi = {
  getAll: () => apiFetch('/packages'),
  getById: (id) => apiFetch(`/packages/${id}`),
};

/**
 * Bookings API
 */
export const bookingApi = {
  getUserBookings: () => apiFetch('/bookings/my'),
  cancel: (id) => apiFetch(`/bookings/${id}/cancel`, { method: 'POST' }),
};
