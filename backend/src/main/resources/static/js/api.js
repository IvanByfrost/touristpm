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

  const finalUrl = `${API_BASE_URL}${endpoint}`;

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  } else {
    console.warn("⚠️ [apiFetch] Sin token para: " + finalUrl);
  }

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
 * User API
 */
export const userApi = {
  update: (id, userData) => apiFetch(`/users/${id}`, {
    method: 'PUT',
    body: JSON.stringify(userData),
  }),
  getAll: () => apiFetch('/users'),
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
  update: (id, data) => apiFetch(`/packages/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  }),
};

/**
 * Bookings API
 */
export const bookingApi = {
  getAll: () => apiFetch('/bookings'),
  search: (params) => {
    const q = new URLSearchParams(params).toString();
    return apiFetch(`/bookings/search?${q}`);
  },
  create: (bookingData) => apiFetch('/bookings', {
    method: 'POST',
    body: JSON.stringify(bookingData),
  }),
  confirm: (id) => apiFetch(`/bookings/${id}/confirm`, { method: 'PUT' }),
  cancel: (id, reason) => apiFetch(`/bookings/${id}/cancel?reason=${encodeURIComponent(reason)}`, { method: 'PUT' }),
};

/**
 * Itineraries API
 */
export const itineraryApi = {
  getByBooking: (bookingId) => apiFetch(`/itineraries/booking/${bookingId}`),
  update: (id, data) => apiFetch(`/itineraries/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  }),
};

/**
 * Destinations API (Fees & Management)
 */
export const destinationApi = {
  getAll: () => apiFetch('/destinations'),
  getById: (id) => apiFetch(`/destinations/${id}`),
  update: (id, data) => apiFetch(`/destinations/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  }),
};

/**
 * Audit Logs API
 */
export const auditApi = {
  getAll: () => apiFetch('/audit-logs'),
};

/**
 * Payments & Cards API
 */
export const paymentApi = {
  getAll: () => apiFetch('/payment-methods'),
  create: (data) => apiFetch('/payment-methods', {
    method: 'POST',
    body: JSON.stringify(data),
  }),
  delete: (id) => apiFetch(`/payment-methods/${id}`, { method: 'DELETE' }),
};
/**
 * Admin API (Tests & Management)
 */
export const adminApi = {
  getTests: () => apiFetch('/admin/tests'),
  runTest: (testClass, testMethod = '') => apiFetch('/admin/tests/run', {
    method: 'POST',
    body: JSON.stringify({ testClass, testMethod }),
  }),
};
