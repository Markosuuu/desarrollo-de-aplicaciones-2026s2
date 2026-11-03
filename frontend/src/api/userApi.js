const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const ALLOWED_API_HOSTS = new Set(["transactions", "inventario"]);

async function fetchUserData(userId, resource, token) {
  if (!ALLOWED_API_HOSTS.includes(resource)) {
    throw new Error("API inválida");
  }

  const response = await fetch(
    `${API_BASE_URL}/users/${encodeURIComponent(userId)}/${resource}`,
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
