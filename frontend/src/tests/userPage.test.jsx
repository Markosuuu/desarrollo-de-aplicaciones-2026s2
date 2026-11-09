import { beforeEach, describe, expect, test, vi } from "vitest";
import { fireEvent, render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import UserPage from "../pages/UserPage";

const mocks = vi.hoisted(() => ({
  createSellOrder: vi.fn(),
  fetchUserInventory: vi.fn(),
  fetchUserTransactions: vi.fn(),
  useAuth: vi.fn(),
}));

vi.mock("../api/marketApi", () => ({
  createSellOrder: mocks.createSellOrder,
}));

vi.mock("../api/userApi", () => ({
  fetchUserInventory: mocks.fetchUserInventory,
  fetchUserTransactions: mocks.fetchUserTransactions,
}));

vi.mock("../context/AuthContext", () => ({
  useAuth: mocks.useAuth,
}));

vi.mock("@blobatar/react", () => ({
  Blobatar: () => <div data-testid="blobatar" />,
}));

const inventory = [
  {
    id: "inventory-1",
    usuario: { id: "user-1", creditosDisponibles: 75.5 },
    jugador: {
      id: "player-1",
      nombre: "Lionel Messi",
      liga: "MLS",
      equipo: "Inter Miami",
      posicion: "Forward",
      cotizacion: 12.25,
    },
    tokensDisponibles: 7,
    tokensEnVenta: 3,
  },
];

const transactions = [
  {
    id: "transaction-sale",
    tipo: "VENTA",
    estado: "COMPLETADA",
    nombreJugador: "Lionel Messi",
    cantidad: 2,
    precioPorToken: 12.25,
    total: 24.5,
    contraparteNombre: "Comprador",
    creadaEn: "2026-10-19T12:00:00",
  },
  {
    id: "transaction-buy",
    tipo: "COMPRA",
    estado: "PENDIENTE",
    nombreJugador: "Kylian Mbappé",
    cantidad: 1,
    precioPorToken: 18,
    total: 18,
    contraparteNombre: "Vendedor",
    creadaEn: "2026-10-20T12:00:00",
  },
];

function renderUserPage() {
  return render(
    <MemoryRouter initialEntries={["/profile"]}>
      <Routes>
        <Route path="/profile" element={<UserPage />} />
        <Route
          path="/mercado/:id"
          element={<div data-testid="offer-route">Oferta creada</div>}
        />
      </Routes>
    </MemoryRouter>,
  );
}

beforeEach(() => {
  vi.clearAllMocks();
  mocks.useAuth.mockReturnValue({
    user: {
      id: "user-1",
      nombre: "Usuario de prueba",
      creditosDisponibles: 100,
    },
    token: "token-prueba",
  });
  mocks.fetchUserInventory.mockResolvedValue(inventory);
  mocks.fetchUserTransactions.mockResolvedValue(transactions);
  mocks.createSellOrder.mockResolvedValue({ id: "listing-1" });
});

describe("UserPage", () => {
  test("carga y muestra el inventario, el historial y los créditos actuales", async () => {
    renderUserPage();

    expect(await screen.findAllByText("Lionel Messi")).toHaveLength(2);
    expect(screen.getByText("75,5")).toBeInTheDocument();
    expect(screen.getByText("Inter Miami")).toBeInTheDocument();
    expect(screen.getByText("7")).toBeInTheDocument();
    expect(screen.getByText("3")).toBeInTheDocument();
    expect(screen.getByText("Comprador")).toBeInTheDocument();
    expect(screen.getByText("Vendedor")).toBeInTheDocument();
    expect(screen.getByText("+24,5")).toBeInTheDocument();
    expect(screen.getByText("-18")).toBeInTheDocument();
    expect(screen.getAllByTestId("blobatar")).toHaveLength(1);
    expect(mocks.fetchUserInventory).toHaveBeenCalledWith(
      "user-1",
      "token-prueba",
      { page: 1, size: 10 },
    );
    expect(mocks.fetchUserTransactions).toHaveBeenCalledWith(
      "user-1",
      "token-prueba",
      { page: 1, size: 10 },
    );
  });

  test("abre el formulario de creación sin publicar hasta confirmar cantidad", async () => {
    renderUserPage();

    fireEvent.click(
      await screen.findByRole("button", {
        name: "Vender tokens de Lionel Messi",
      }),
    );

    expect(await screen.findByRole("dialog")).toBeInTheDocument();
    expect(mocks.createSellOrder).not.toHaveBeenCalled();
    expect(mocks.fetchUserInventory).toHaveBeenCalledTimes(1);
    expect(mocks.fetchUserTransactions).toHaveBeenCalledTimes(1);
  });

  test("crea una oferta con la cantidad elegida y navega a su detalle", async () => {
    renderUserPage();

    fireEvent.click(
      await screen.findByRole("button", {
        name: "Vender tokens de Lionel Messi",
      }),
    );
    fireEvent.change(screen.getByLabelText(/Cantidad de tokens/), {
      target: { value: "3" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Publicar oferta" }));

    expect(mocks.createSellOrder).toHaveBeenCalledWith(
      { jugadorId: "player-1", cantidad: 3 },
      "token-prueba",
    );
    expect(await screen.findByTestId("offer-route")).toBeInTheDocument();
  });

  test("muestra estados vacíos cuando la API no devuelve registros", async () => {
    mocks.fetchUserInventory.mockResolvedValue([]);
    mocks.fetchUserTransactions.mockResolvedValue([]);

    renderUserPage();

    expect(await screen.findByText("Inventario vacío")).toBeInTheDocument();
    expect(
      screen.getByText("No hay operaciones registradas"),
    ).toBeInTheDocument();
    expect(screen.getByText("100")).toBeInTheDocument();
  });
});
