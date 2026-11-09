import { afterEach, describe, expect, test, vi } from "vitest";
import { fetchUserInventory, fetchUserTransactions } from "../api/userApi";

const userId = "3f2b8c1e-5a47-4d9b-8e6a-1c7f90d2b4a8";

afterEach(() => {
  vi.restoreAllMocks();
});

describe("userApi", () => {
  test.each([
    ["inventario", fetchUserInventory],
    ["transactions", fetchUserTransactions],
  ])(
    "solicita el recurso %s con el token de autenticación",
    async (resource, fetchResource) => {
      const fetchMock = vi.spyOn(globalThis, "fetch").mockResolvedValue(
        new Response(JSON.stringify([]), {
          status: 200,
          headers: { "Content-Type": "application/json" },
        }),
      );

      await fetchResource(`${userId}`, "token-prueba");

      expect(fetchMock).toHaveBeenCalledWith(
        `http://localhost:8080/api/users/${userId}/${resource}?page=1&size=10`,
        {
          headers: {
            Accept: "application/json",
            Authorization: "Bearer token-prueba",
          },
        },
      );
    },
  );

  test("propaga el mensaje informado por la API ante una respuesta fallida", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(JSON.stringify({ message: "Usuario no encontrado" }), {
        status: 404,
        headers: { "Content-Type": "application/json" },
      }),
    );

    await expect(
      fetchUserInventory(`${userId}`, "token-prueba"),
    ).rejects.toThrow("Usuario no encontrado");
  });

  test("normaliza respuestas sin una lista de contenido", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(JSON.stringify({ data: [] }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }),
    );

    await expect(
      fetchUserInventory(`${userId}`, "token-prueba"),
    ).resolves.toEqual({ data: [], content: [] });
  });

  test("rechaza ids que no sean UUID válidos antes de llamar a fetch", async () => {
    const fetchMock = vi.spyOn(globalThis, "fetch");

    await expect(fetchUserInventory("no-es-un-uuid", "token")).rejects.toThrow(
      "Id de usuario inválido.",
    );
    expect(fetchMock).not.toHaveBeenCalled();
  });

  test("envía la paginación solicitada y omite Authorization sin token", async () => {
    const fetchMock = vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(JSON.stringify({ content: [{ id: "item-1" }] }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }),
    );

    await expect(
      fetchUserTransactions(userId, undefined, { page: 3, size: 25 }),
    ).resolves.toEqual({ content: [{ id: "item-1" }] });

    expect(fetchMock).toHaveBeenCalledWith(
      `http://localhost:8080/api/users/${userId}/transactions?page=3&size=25`,
      { headers: { Accept: "application/json" } },
    );
  });

  test("normaliza respuestas paginadas sin content", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(JSON.stringify({ totalElements: 0 }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }),
    );

    await expect(fetchUserInventory(userId)).resolves.toEqual({
      totalElements: 0,
      content: [],
    });
  });

  test("usa el error anidado de la API o el mensaje genérico", async () => {
    vi.spyOn(globalThis, "fetch")
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ error: { message: "Acceso denegado" } }), {
          status: 403,
        }),
      )
      .mockResolvedValueOnce(new Response("{}", { status: 500 }));

    await expect(fetchUserInventory(userId)).rejects.toThrow(
      "Acceso denegado",
    );
    await expect(fetchUserInventory(userId)).rejects.toThrow(
      "No se pudieron cargar los datos del usuario.",
    );
  });

  test("rechaza un cuerpo exitoso que no sea JSON ni un objeto/lista", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response('"texto"', { status: 200 }),
    );

    await expect(fetchUserInventory(userId)).rejects.toThrow(
      "La API devolvió un formato de datos inválido.",
    );
  });

  test("normaliza una respuesta exitosa con JSON inválido como lista vacía", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response("no es JSON", { status: 200 }),
    );

    await expect(fetchUserInventory(userId)).resolves.toEqual({
      content: [],
    });
  });
});
