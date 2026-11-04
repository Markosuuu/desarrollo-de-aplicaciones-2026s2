import { beforeEach, describe, expect, test, vi } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import MercadoPage from "../pages/MercadoPage";

const mocks = vi.hoisted(() => ({
  fetchListings: vi.fn(),
  useAuth: vi.fn(),
}));

vi.mock("../api/marketApi", () => ({
  fetchListings: mocks.fetchListings,
}));

vi.mock("../context/AuthContext", () => ({
  useAuth: mocks.useAuth,
}));

vi.mock("@blobatar/react", () => ({
  Blobatar: () => <div data-testid="blobatar" />,
}));

const listings = [
  {
    id: "listing-1",
    vendedorId: "seller-1",
    vendedorNombre: "Vendedor Uno",
    jugadorId: "player-1",
    nombreJugador: "Lionel Messi",
    cantidadTokens: 4,
    precioPorToken: 12.5,
    precioTotal: 50,
    estado: "PENDIENTE",
  },
  {
    id: "listing-own",
    vendedorId: "user-1",
    vendedorNombre: "Usuario",
    nombreJugador: "Jude Bellingham",
    cantidadTokens: 2,
    precioPorToken: 20,
    precioTotal: 40,
    estado: "PENDIENTE",
  },
];

function renderMarket() {
  return render(
    <MemoryRouter initialEntries={["/mercado"]}>
      <Routes>
        <Route path="/mercado" element={<MercadoPage />} />
        <Route
          path="/mercado/:id"
          element={<div data-testid="offer-route">Detalle de oferta</div>}
        />
      </Routes>
    </MemoryRouter>,
  );
}

beforeEach(() => {
  vi.clearAllMocks();
  mocks.useAuth.mockReturnValue({
    user: { id: "user-1", nombre: "Usuario" },
    token: "token-test",
  });
  mocks.fetchListings.mockResolvedValue(listings);
});

describe("MercadoPage", () => {
  test("muestra anuncios de otros usuarios y los propios", async () => {
    renderMarket();

    expect(await screen.findByText("Lionel Messi")).toBeInTheDocument();
    expect(screen.getByText("Vendedor Uno")).toBeInTheDocument();
    expect(screen.getByText("Jude Bellingham")).toBeInTheDocument();
    expect(screen.getByText("50 créditos")).toBeInTheDocument();
  });

  test("navega al detalle de la oferta al elegir Comprar", async () => {
    renderMarket();

    let botones = await screen.findAllByRole("button", { name: "Comprar" });
    fireEvent.click(botones[0]);

    expect(await screen.findByTestId("offer-route")).toBeInTheDocument();
  });

  test("filtra las ofertas por jugador o vendedor", async () => {
    renderMarket();

    await screen.findByText("Lionel Messi");
    fireEvent.change(
      screen.getByRole("searchbox", { name: "Buscar anuncios" }),
      {
        target: { value: "inexistente" },
      },
    );

    await waitFor(() => {
      expect(
        screen.getByText("No se encontraron anuncios para esta búsqueda."),
      ).toBeInTheDocument();
    });
  });

  test("muestra el error de API y permite reintentar", async () => {
    mocks.fetchListings
      .mockRejectedValueOnce(new Error("No disponible"))
      .mockResolvedValueOnce([listings[0]]);

    renderMarket();

    expect(await screen.findByText("No disponible")).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "Reintentar" }));

    expect(await screen.findByText("Lionel Messi")).toBeInTheDocument();
  });
});
