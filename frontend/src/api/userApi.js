const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const ALLOWED_API_HOSTS = new Set(["transactions", "inventario"]);

const UUID_PATTERN =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

async function fetchUserData(
  userId,
  resource,
  token,
  { page = 1, size = 10 } = {},
) {
  const normalizedUserId = String(userId);

  if (!UUID_PATTERN.test(normalizedUserId)) {
    throw new Error("Id de usuario inválido.");
  }

  if (!ALLOWED_API_HOSTS.has(resource)) {
    throw new Error("Recurso no encontrado");
  }

  const safeUserId = encodeURIComponent(normalizedUserId);
  const params = new URLSearchParams({
    page: String(page),
    size: String(size),
  });

  const response = await fetch(
    `${API_BASE_URL}/users/${safeUserId}/${resource}?${params.toString()}`,
    {
      headers: {
        Accept: "application/json",
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
    },
  );

  const payload = await response.json().catch(() => ({}));

  if (!response.ok) {
    const message =
      payload?.error?.message ||
      payload?.message ||
      "No se pudieron cargar los datos del usuario.";
    throw new Error(message);
  }

  if (Array.isArray(payload)) {
    return payload;
  }

  if (payload && typeof payload === "object") {
    const content = Array.isArray(payload.content) ? payload.content : [];
    return {
      ...payload,
      content,
    };
  }

  throw new TypeError("La API devolvió un formato de datos inválido.");
}

export function fetchUserInventory(userId, token, options) {
  return fetchUserData(userId, "inventario", token, options);
}

export function fetchUserTransactions(userId, token, options) {
  return fetchUserData(userId, "transactions", token, options);
}
