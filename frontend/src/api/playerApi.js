const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
// Letras (con acentos/ñ), números, espacios y algunos signos comunes en nombres
const SAFE_TERM = /^[\p{L}\p{N}\s.'’-]{1,100}$/u;

function toSafeTerm(value) {
  if (typeof value !== 'string') return undefined;
  const trimmed = value.trim();
  return SAFE_TERM.test(trimmed) ? trimmed : undefined;
}

export async function fetchPlayers({ page = 1, perPage = 20, nombre, equipo, liga } = {}) {
  const params = new URLSearchParams();

  params.set('page', String(page));
  params.set('perPage', String(perPage));

  const safeNombre = toSafeTerm(nombre);
  const safeEquipo = toSafeTerm(equipo);
  
  if (safeNombre) params.set('nombre', encodeURIComponent(safeNombre));
  if (safeEquipo) params.set('equipo', encodeURIComponent(safeEquipo));
  if (liga) params.set('liga', liga);

  const queryString = params.toString();
  const response = await fetch(`${API_BASE_URL}/players?${queryString}`, {
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
