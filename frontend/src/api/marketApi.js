const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

async function request(path, { method = "GET", token, body } = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers: {
      Accept: "application/json",
      ...(body ? { "Content-Type": "application/json" } : {}),
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
    ...(body ? { body: JSON.stringify(body) } : {}),
  });

  const payload = await response.json().catch(() => ({}));

  if (!response.ok) {
    const message =
      payload?.error?.message ||
      payload?.message ||
      "No se pudo completar la operación del mercado.";
    throw new Error(message);
  }

  return payload;
}

export async function fetchListings(token, { page = 1, size = 20 } = {}) {
  const params = new URLSearchParams();
  params.set("page", String(page));
  params.set("size", String(size));

  const payload = await request(`/marketplace/listings?${params.toString()}`, {
    token,
  });

  if (Array.isArray(payload)) {
    return payload;
  }

  if (payload && typeof payload === "object") {
    const content = extraerContenido(payload);

    return {
      ...payload,
      content,
      listings: content,
    };
  }

  throw new TypeError("La API devolvió un listado de anuncios inválido.");
}

function extraerContenido(payload) {
  if (Array.isArray(payload.content)) return payload.content;
  if (Array.isArray(payload.listings)) return payload.listings;
  return [];
}

export async function createSellOrder({ jugadorId, cantidad }, token) {
  const payload = await request("/orders/sell", {
    method: "POST",
    token,
    body: { jugadorId, cantidad },
  });

  if (!payload?.id) {
    throw new Error("La API no devolvió el anuncio creado.");
  }

  return payload;
}

export function buyListing(anuncioId, cantidad, token) {
  return request("/orders/buy", {
    method: "POST",
    token,
    body: { anuncioId, cantidad },
  });
}

export function cancelListing(anuncioId, token) {
  return request(`/orders/${encodeURIComponent(anuncioId)}`, {
    method: "DELETE",
    token,
  });
}
