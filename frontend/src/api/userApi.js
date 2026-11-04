const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const ALLOWED_API_HOSTS = new Set(["transactions", "inventario"]);

const UUID_PATTERN =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

async function fetchUserData(userId, resource, token) {
  const normalizedUserId = String(userId);

    if (!UUID_PATTERN.test(normalizedUserId)) {
        throw new Error("Id de usuario inválido.");
    }

    if (!ALLOWED_API_HOSTS.has(resource)) {
        throw new Error("Recurso no encontrado");
    }

    const safeUserId = encodeURIComponent(normalizedUserId);

    const response = await fetch(
        `${API_BASE_URL}/users/${safeUserId}/${resource}`,
        {
            headers: {
                Accept: "application/json",
                Authorization: `Bearer ${token}`,
            },
        }
    );

  const payload = await response.json().catch(() => ({}));

  if (!response.ok) {
    const message =
      payload?.error?.message ||
      payload?.message ||
      "No se pudieron cargar los datos del usuario.";
    throw new Error(message);
  }

  if (!Array.isArray(payload)) {
    throw new TypeError("La API devolvió un formato de datos inválido.");
  }

  return payload;
}

export function fetchUserInventory(userId, token) {
  return fetchUserData(userId, "inventario", token);
}

export function fetchUserTransactions(userId, token) {
  return fetchUserData(userId, "transactions", token);
}
