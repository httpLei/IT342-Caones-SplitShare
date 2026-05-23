const isLocalhost =
  window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';

const configuredApiBaseUrl = isLocalhost
  ? 'http://localhost:8080'
  : import.meta.env.VITE_API_URL || 'https://splitshare-api-r991.onrender.com';

export const API_BASE_URL = configuredApiBaseUrl.replace(/\/+$/, '');
