const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

export async function fetchSubastas({ page = 1, size = 10, token } = {}) {
  const params = new URLSearchParams();

  params.set("page", String(page));
  params.set("size", String(size));

  const response = await fetch(
    `${API_BASE_URL}/subastas?${params.toString()}`,
    {
      headers: {
        Accept: "application/json",
        Authorization: `Bearer ${token}`,
      },
    },
  );

  const payload = await response.json().catch(() => ({}));

  if (!response.ok) {
    const message =
      payload?.error?.message ||
      payload?.message ||
      "No se pudieron cargar las subastas.";

    throw new Error(message);
  }

  return payload;
}

export async function buscarSubastasPorNombre({
  page = 1,
  size = 10,
  user = null,
  jugador,
  token,
} = {}) {
  const params = new URLSearchParams();

  params.set("page", String(page));
  params.set("size", String(size));

  const response = await fetch(
    `${API_BASE_URL}/subastas/buscarPorNombre?${params.toString()}`,
    {
      method: "POST",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({ user, jugador }),
    },
  );

  return handleResponse(response, "No se pudieron buscar las subastas.");
}

async function handleResponse(response, defaultMessage) {
  const payload = await response.json().catch(() => ({}));

  if (!response.ok) {
    const message =
      payload?.error?.message || payload?.message || defaultMessage;

    throw new Error(message);
  }

  return payload;
}

export async function fetchSubasta(id, token) {
  const response = await fetch(`${API_BASE_URL}/subastas/${id}`, {
    headers: {
      Accept: "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  return handleResponse(response, "No se pudo cargar la subasta.");
}

export async function cerrarSubasta(id, token) {
  const response = await fetch(`${API_BASE_URL}/subastas/${id}/cerrar`, {
    method: "PATCH",
    headers: {
      Accept: "application/json",
      Authorization: `Bearer ${token}`,
    },
  });

  return handleResponse(response, "No se pudo cerrar la subasta.");
}

export async function ofertarSubasta({ subastaId, puja, token }) {
  const response = await fetch(`${API_BASE_URL}/subastas/ofertar`, {
    method: "POST",
    headers: {
      Accept: "application/json",
      "Content-Type": "application/json",
      Authorization: `Bearer ${token}`,
    },
    body: JSON.stringify({
      subastaId,
      puja,
      idempotencyKey: crypto.randomUUID(),
    }),
  });

  return handleResponse(response, "No se pudo realizar la oferta.");
}
