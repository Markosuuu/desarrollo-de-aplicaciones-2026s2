
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export async function fetchSubastas({ page = 1, size = 10, token } = {}) {
  const params = new URLSearchParams();

  params.set('page', String(page));
  params.set('size', String(size));

  const response = await fetch(`${API_BASE_URL}/subastas?${params.toString()}`, {
    headers: {
      Accept: 'application/json',
      Authorization: `Bearer ${token}`,
    },
  });

  const payload = await response.json().catch(() => ({}));

  if (!response.ok) {
    const message =
      payload?.error?.message ||
      payload?.message ||
      'No se pudieron cargar las subastas.';

    throw new Error(message);
  }

  return payload;
}
