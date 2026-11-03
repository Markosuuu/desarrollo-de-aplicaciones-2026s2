const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const USER_RESOURCES = Object.freeze({
  inventario: "inventario",
  transactions: "transactions",
});

const UUID_PATTERN =
  /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

async function fetchUserData(userId, resource, token) {
  if (!UUID_PATTERN.test(String(userId))) {
    throw new Error("Id de usuario inválido.");
  }

  if (!Object.hasOwn(USER_RESOURCES, resource)) {
    throw new Error("Recurso de usuario no permitido.");
  }

  const response = await fetch(`${API_BASE_URL}/users/${userId}/${resource}`, {
    headers: {
      Accept: "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
    },
  });

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
