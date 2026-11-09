import { afterEach, describe, expect, test, vi } from "vitest";
import {
  buyListing,
  cancelListing,
  createSellOrder,
  fetchListings,
} from "../api/marketApi";

afterEach(() => {
  vi.restoreAllMocks();
});

function mockResponse(payload, status = 200) {
  return new Response(JSON.stringify(payload), {
    status,
    headers: { "Content-Type": "application/json" },
  });
}

describe("marketApi", () => {
  test("obtiene la lista de ofertas con autenticación", async () => {
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValue(mockResponse([]));

    await fetchListings("token-test");

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/marketplace/listings?page=1&size=20",
      {
        method: "GET",
        headers: {
          Accept: "application/json",
          Authorization: "Bearer token-test",
        },
      },
    );
  });

  test("crea una oferta con jugador y cantidad", async () => {
    const listing = { id: "listing-1" };
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValue(mockResponse(listing, 201));

    await expect(
      createSellOrder({ jugadorId: "player-1", cantidad: 4 }, "token-test"),
    ).resolves.toEqual(listing);

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/orders/sell",
      {
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
          Authorization: "Bearer token-test",
        },
        body: JSON.stringify({ jugadorId: "player-1", cantidad: 4 }),
      },
    );
  });

  test("compra una cantidad concreta de una oferta", async () => {
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValue(mockResponse({ success: true }));

    await buyListing("listing-1", 2, "token-test");

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/orders/buy",
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ anuncioId: "listing-1", cantidad: 2 }),
      }),
    );
  });

  test("cancela una oferta por su id", async () => {
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValue(mockResponse({ success: true }));

    await cancelListing("listing-1", "token-test");

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/orders/listing-1",
      expect.objectContaining({ method: "DELETE" }),
    );
  });

  test("propaga errores HTTP de la API", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      mockResponse({ message: "Sin créditos suficientes" }, 400),
    );

    await expect(buyListing("listing-1", "token-test")).rejects.toThrow(
      "Sin créditos suficientes",
    );
  });

  test("normaliza respuestas paginadas y acepta el formato listings", async () => {
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValueOnce(
        mockResponse({ content: [{ id: "listing-1" }], page: 2 }),
      )
      .mockResolvedValueOnce(mockResponse({ listings: [{ id: "listing-2" }] }))
      .mockResolvedValueOnce(mockResponse({ data: "sin anuncios" }));

    await expect(
      fetchListings("token-test", { page: 2, size: 5 }),
    ).resolves.toEqual({
      content: [{ id: "listing-1" }],
      listings: [{ id: "listing-1" }],
      page: 2,
    });
    await expect(fetchListings("token-test")).resolves.toEqual({
      listings: [{ id: "listing-2" }],
      content: [{ id: "listing-2" }],
    });
    await expect(fetchListings("token-test")).resolves.toEqual({
      data: "sin anuncios",
      content: [],
      listings: [],
    });

    expect(fetchMock).toHaveBeenNthCalledWith(
      1,
      "http://localhost:8080/api/marketplace/listings?page=2&size=5",
      expect.any(Object),
    );
  });

  test("rechaza respuestas de anuncios que no sean arreglos u objetos", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(mockResponse(null));

    await expect(fetchListings("token-test")).rejects.toThrow(
      "La API devolvió un listado de anuncios inválido.",
    );
  });

  test("rechaza la creación cuando la API no devuelve un id", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(mockResponse({}));

    await expect(
      createSellOrder({ jugadorId: "player-1", cantidad: 1 }, "token-test"),
    ).rejects.toThrow("La API no devolvió el anuncio creado.");
  });

  test("usa el error anidado de la API y el mensaje genérico si falta", async () => {
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValueOnce(
        mockResponse({ error: { message: "Orden rechazada" } }, 400),
      )
      .mockResolvedValueOnce(mockResponse({}, 500));

    await expect(buyListing("listing-1", 1, "token-test")).rejects.toThrow(
      "Orden rechazada",
    );
    await expect(buyListing("listing-1", 1, "token-test")).rejects.toThrow(
      "No se pudo completar la operación del mercado.",
    );
    expect(fetchMock).toHaveBeenCalledTimes(2);
  });

  test("tolera un cuerpo de respuesta JSON inválido", async () => {
    vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response("no es json", { status: 200 }),
    );

    await expect(buyListing("listing-1", 1)).resolves.toEqual({});
  });

  test("codifica el id de la oferta al cancelarla", async () => {
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValue(mockResponse({ success: true }));

    await cancelListing("listing/con espacios", "token-test");

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/orders/listing%2Fcon%20espacios",
      expect.any(Object),
    );
  });
});
