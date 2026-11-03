import { beforeEach, describe, expect, test, vi } from "vitest";
import { fireEvent, render, screen } from "@testing-library/react";
import UserPage from "../pages/UserPage";

const mocks = vi.hoisted(() => ({
  fetchUserInventory: vi.fn(),
  fetchUserTransactions: vi.fn(),
  useAuth: vi.fn(),
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

beforeEach(() => {
  vi.clearAllMocks();
  mocks.useAuth.mockReturnValue({
    user: { id: "user-1", nombre: "Usuario de prueba", creditosDisponibles: 100 },
    token: "token-prueba",
  });
  mocks.fetchUserInventory.mockResolvedValue(inventory);
  mocks.fetchUserTransactions.mockResolvedValue(transactions);
});

describe("UserPage", () => {
  test("carga y muestra el inventario, el historial y los créditos actuales", async () => {
    render(<UserPage />);

    expect(await screen.findAllByText("Lionel Messi")).toHaveLength(2);
    expect(screen.getByText("75,5")).toBeInTheDocument();
    expect(screen.getByText("Inter Miami")).toBeInTheDocument();
    expect(screen.getByText("10")).toBeInTheDocument();
    expect(screen.getByText("Comprador")).toBeInTheDocument();
    expect(screen.getByText("Vendedor")).toBeInTheDocument();
    expect(screen.getByText("+24,5")).toBeInTheDocument();
    expect(screen.getByText("-18")).toBeInTheDocument();
    expect(screen.getAllByTestId("blobatar")).toHaveLength(1);
    expect(mocks.fetchUserInventory).toHaveBeenCalledWith(
      "user-1",
      "token-prueba",
    );
    expect(mocks.fetchUserTransactions).toHaveBeenCalledWith(
      "user-1",
      "token-prueba",
    );
  });

  test("mantiene el botón Vender sin iniciar una operación", async () => {
    render(<UserPage />);

    fireEvent.click(
      await screen.findByRole("button", { name: "Vender tokens de Lionel Messi" }),
    );

    expect(mocks.fetchUserInventory).toHaveBeenCalledTimes(1);
    expect(mocks.fetchUserTransactions).toHaveBeenCalledTimes(1);
  });

  test("muestra estados vacíos cuando la API no devuelve registros", async () => {
    mocks.fetchUserInventory.mockResolvedValue([]);
    mocks.fetchUserTransactions.mockResolvedValue([]);

    render(<UserPage />);

    expect(
      await screen.findByText("Todavía no tienes jugadores en tu inventario."),
    ).toBeInTheDocument();
    expect(
      screen.getByText("Todavía no tienes operaciones en tu historial."),
    ).toBeInTheDocument();
    expect(screen.getByText("100")).toBeInTheDocument();
  });
});
