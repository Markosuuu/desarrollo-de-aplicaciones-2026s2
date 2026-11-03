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
      "http://localhost:8080/api/marketplace/listings",
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

  test("compra una oferta por su id", async () => {
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValue(mockResponse({ success: true }));

    await buyListing("listing-1", "token-test");

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/orders/buy",
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ anuncioId: "listing-1" }),
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
});
