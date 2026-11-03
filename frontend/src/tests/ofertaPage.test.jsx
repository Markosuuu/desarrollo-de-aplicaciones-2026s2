import { beforeEach, describe, expect, test, vi } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import OfertaPage from "../pages/OfertaPage";

const mocks = vi.hoisted(() => ({
  buyListing: vi.fn(),
  cancelListing: vi.fn(),
  fetchListings: vi.fn(),
  fetchUserInventory: vi.fn(),
  useAuth: vi.fn(),
}));

vi.mock("../api/marketApi", () => ({
  buyListing: mocks.buyListing,
  cancelListing: mocks.cancelListing,
  fetchListings: mocks.fetchListings,
}));

vi.mock("../api/userApi", () => ({
  fetchUserInventory: mocks.fetchUserInventory,
}));

vi.mock("../context/AuthContext", () => ({
  useAuth: mocks.useAuth,
}));

const listing = {
  id: "listing-1",
  vendedorId: "seller-1",
  vendedorNombre: "Vendedor Uno",
  jugadorId: "player-1",
  nombreJugador: "Lionel Messi",
  cantidadTokens: 4,
  precioPorToken: 12.5,
  precioTotal: 50,
  estado: "PENDIENTE",
};

const userInventory = [
  {
    usuario: { id: "user-1", creditosDisponibles: 75 },
    jugador: { nombre: "Jude Bellingham" },
    tokensDisponibles: 1,
  },
];

function renderOffer({ asOwner = false, withNavigationState = true } = {}) {
  const currentListing = asOwner
    ? { ...listing, vendedorId: "user-1" }
    : listing;

  return render(
    <MemoryRouter
      initialEntries={[
        {
          pathname: "/mercado/listing-1",
          state: withNavigationState ? { listing: currentListing } : null,
        },
      ]}
    >
      <Routes>
        <Route path="/mercado/:id" element={<OfertaPage />} />
        <Route path="/profile" element={<div data-testid="profile-route">Perfil</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

beforeEach(() => {
  vi.clearAllMocks();
  vi.stubGlobal("confirm", vi.fn(() => true));
  mocks.useAuth.mockReturnValue({
    user: { id: "user-1", nombre: "Usuario", creditosDisponibles: 100 },
    token: "token-test",
  });
  mocks.fetchListings.mockResolvedValue([listing]);
  mocks.fetchUserInventory.mockResolvedValue(userInventory);
  mocks.buyListing.mockResolvedValue({ success: true });
  mocks.cancelListing.mockResolvedValue({ success: true });
});

describe("OfertaPage", () => {
  test("muestra la oferta y el saldo actual del comprador", async () => {
    renderOffer();

    expect(await screen.findByText("Lionel Messi")).toBeInTheDocument();
    expect(screen.getByText("Vendedor Uno")).toBeInTheDocument();
    expect(screen.getByText("75 créditos")).toBeInTheDocument();
  });

  test("confirma la compra, llama a la API y redirige al perfil", async () => {
    renderOffer();

    fireEvent.click(await screen.findByRole("button", { name: "Comprar" }));

    expect(window.confirm).toHaveBeenCalled();
    expect(mocks.buyListing).toHaveBeenCalledWith("listing-1", "token-test");
    expect(await screen.findByTestId("profile-route")).toBeInTheDocument();
  });

  test("muestra el botón de cancelación para el dueño y actualiza el estado", async () => {
    renderOffer({ asOwner: true });

    fireEvent.click(
      await screen.findByRole("button", { name: "Cancelar oferta" }),
    );

    expect(mocks.cancelListing).toHaveBeenCalledWith("listing-1", "token-test");
    await waitFor(() => {
      expect(screen.getByText("CANCELADA")).toBeInTheDocument();
    });
    expect(
      screen.getByText("Esta oferta ya no está disponible."),
    ).toBeInTheDocument();
  });

  test("carga la oferta desde la API al abrir su ruta directamente", async () => {
    renderOffer({ withNavigationState: false });

    expect(await screen.findByText("Lionel Messi")).toBeInTheDocument();
    expect(mocks.fetchListings).toHaveBeenCalledWith("token-test");
  });

  test("no permite comprar una oferta propia", async () => {
    renderOffer({ asOwner: true });

    expect(
      await screen.findByRole("button", { name: "Cancelar oferta" }),
    ).toBeInTheDocument();
    expect(screen.queryByRole("button", { name: "Comprar" })).toBeNull();
  });
});
