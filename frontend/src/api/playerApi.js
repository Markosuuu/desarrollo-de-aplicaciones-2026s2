const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

export async function fetchPlayers({ page = 1, perPage = 20, nombre, equipo, liga } = {}) {
  const params = new URLSearchParams();

  params.set('page', String(page));
  params.set('perPage', String(perPage));

  if (nombre) params.set('nombre', nombre);
  if (equipo) params.set('equipo', equipo);
  if (liga) params.set('liga', liga);

  const queryString = params.toString();
  const response = await fetch(`${API_BASE_URL}/players${queryString ? `?${queryString}` : ''}`, {
    headers: {
      Accept: 'application/json',
    },
  });

  const payload = await response.json().catch(() => ({}));

  if (!response.ok) {
    const message = payload?.error?.message || payload?.message || 'No se pudo cargar el catálogo de jugadores.';
    throw new Error(message);
  }

  return payload;
}
