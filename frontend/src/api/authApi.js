const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

async function request(endpoint, options = {}) {
  const { method = 'GET', body, token } = options;

  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
  };

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });

  const payload = await response.json().catch(() => ({}));

  if (!response.ok) {
    const message = payload?.error?.message || 'Ocurrió un error inesperado.';
    throw new Error(message);
  }

  return payload;
}

export function loginRequest(payload) {
  return request('/auth/login', { method: 'POST', body: payload });
}

export function registerRequest(payload) {
  return request('/auth/register', { method: 'POST', body: payload });
}
