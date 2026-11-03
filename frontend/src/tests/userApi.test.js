import { afterEach, describe, expect, test, vi } from "vitest";
import { fetchUserInventory, fetchUserTransactions } from "../api/userApi";

afterEach(() => {
  vi.restoreAllMocks();
});

describe("userApi", () => {
  test.each([
    ["inventario", fetchUserInventory],
    ["transactions", fetchUserTransactions],
  ])("solicita el recurso %s con el token de autenticación", async (resource, fetchResource) => {
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValue(
        new Response(JSON.stringify([]), {
          status: 200,
          headers: { "Content-Type": "application/json" },
        }),
      );

    await fetchResource("user-1", "token-prueba");

    expect(fetchMock).toHaveBeenCalledWith(
      `http://localhost:8080/api/users/user-1/${resource}`,
      {
        headers: {
          Accept: "application/json",
          Authorization: "Bearer token-prueba",
        },
      },
    );
  });

  test("propaga el mensaje informado por la API ante una respuesta fallida", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(JSON.stringify({ message: "Usuario no encontrado" }), {
        status: 404,
        headers: { "Content-Type": "application/json" },
      }),
    );

    await expect(fetchUserInventory("user-1", "token-prueba")).rejects.toThrow(
      "Usuario no encontrado",
    );
  });

  test("rechaza respuestas que no contienen una lista", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(JSON.stringify({ data: [] }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }),
    );

    await expect(fetchUserInventory("user-1", "token-prueba")).rejects.toThrow(
      "La API devolvió un formato de datos inválido.",
    );
  });
});
