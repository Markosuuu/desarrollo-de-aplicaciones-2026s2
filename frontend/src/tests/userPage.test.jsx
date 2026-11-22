import { beforeEach, describe, expect, test, vi } from "vitest";
import {
  act,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import UserPage from "../pages/UserPage";

const mocks = vi.hoisted(() => ({
  createSellOrder: vi.fn(),
  fetchUserInventory: vi.fn(),
  fetchUserTransactions: vi.fn(),
  useAuth: vi.fn(),
  toastError: vi.fn(),
  toastSuccess: vi.fn(),
  createAuction: vi.fn()
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

vi.mock("react-toastify", () => ({
  toast: { error: mocks.toastError, success: mocks.toastSuccess },
}));

vi.mock("../api/subastaApi", () => ({
  createAuction: mocks.createAuction,
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
        <Route
          path="/subasta/:id"
          element={<div data-testid="auction-route">Subasta creada</div>}
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
  mocks.createAuction.mockResolvedValue({ id: "auction-1" });
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
        name: "Opciones de venta para Lionel Messi",
      }),
    );
    fireEvent.click(
      await screen.findByRole("button", {
        name: "Crear oferta",
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
        name: "Opciones de venta para Lionel Messi",
      }),
    );

    fireEvent.click(
      await screen.findByRole("button", {
        name: "Crear oferta",
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

  test("muestra los datos faltantes con valores de reemplazo", async () => {
    mocks.fetchUserInventory.mockResolvedValue([
      { id: "incomplete-item", tokensDisponibles: null, tokensEnVenta: "" },
      {
        id: "unnamed-player",
        tokensDisponibles: 2,
        jugador: { cotizacion: "no-numérico" },
      },
    ]);
    mocks.fetchUserTransactions.mockResolvedValue([
      {
        id: "incomplete-transaction",
        tipo: "COMPRA",
        creadaEn: "fecha-invalid",
      },
      { id: "missing-transaction-fields", creadaEn: "" },
    ]);

    renderUserPage();

    expect((await screen.findAllByText("—")).length).toBeGreaterThan(0);
    const unnamedPlayerButtons = screen.getAllByRole("button", {
      name: "Opciones de venta para jugador",
    });
    expect(unnamedPlayerButtons[0]).toBeDisabled();
    fireEvent.click(unnamedPlayerButtons[1]);
    fireEvent.click(
      await screen.findByRole("button", {
        name: "Crear oferta",
      }),
    );
    expect(
      await screen.findByText(/Jugador — — créditos por token/),
    ).toBeInTheDocument();
  });

  test("muestra el error de carga y permite volver a cargar los datos", async () => {
    mocks.fetchUserInventory
      .mockRejectedValueOnce(new Error("No se pudo cargar"))
      .mockResolvedValueOnce(inventory);

    renderUserPage();

    expect(await screen.findByText("No se pudo cargar")).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "Reintentar" }));

    expect(await screen.findByText("Inter Miami")).toBeInTheDocument();
    expect(mocks.fetchUserInventory).toHaveBeenCalledTimes(2);
  });

  test("muestra el error de carga genérico y la falta de usuario", async () => {
    mocks.useAuth.mockReturnValue({ token: "token-prueba" });
    const fetchInventory = vi
      .spyOn(console, "error")
      .mockImplementation(() => {});

    renderUserPage();

    expect(
      await screen.findByText("No se pudo identificar al usuario."),
    ).toBeInTheDocument();
    expect(mocks.fetchUserInventory).not.toHaveBeenCalled();

    fetchInventory.mockRestore();
    mocks.useAuth.mockReturnValue({
      token: "token-prueba",
      user: { id: "user-1" },
    });
    mocks.fetchUserInventory.mockRejectedValue({});
    vi.spyOn(console, "error").mockImplementation(() => {});

    renderUserPage();

    expect(
      await screen.findByText(
        "No se pudo cargar el inventario y el historial de operaciones.",
      ),
    ).toBeInTheDocument();
  });

  test("acepta respuestas paginadas sin metadatos adicionales", async () => {
    mocks.fetchUserInventory.mockResolvedValue({});
    mocks.fetchUserTransactions.mockResolvedValue({});

    renderUserPage();

    expect(await screen.findByText("Inventario vacío")).toBeInTheDocument();
    expect(
      screen.getByText("No hay operaciones registradas"),
    ).toBeInTheDocument();
    expect(
      screen.queryByRole("button", { name: "Siguiente" }),
    ).not.toBeInTheDocument();
  });

  test("ignora resultados de carga recibidos después de desmontarse", async () => {
    let resolveInventory;
    let resolveTransactions;
    mocks.fetchUserInventory.mockReturnValue(
      new Promise((resolve) => {
        resolveInventory = resolve;
      }),
    );
    mocks.fetchUserTransactions.mockReturnValue(
      new Promise((resolve) => {
        resolveTransactions = resolve;
      }),
    );
    const consoleErrorSpy = vi
      .spyOn(console, "error")
      .mockImplementation(() => {});

    const { unmount } = renderUserPage();
    unmount();

    await act(async () => {
      resolveInventory(inventory);
      resolveTransactions(transactions);
    });

    expect(mocks.fetchUserInventory).toHaveBeenCalledTimes(1);
    expect(mocks.fetchUserTransactions).toHaveBeenCalledTimes(1);
    expect(consoleErrorSpy).not.toHaveBeenCalled();
  });

  test("ignora errores de carga recibidos después de desmontarse", async () => {
    let rejectInventory;
    let resolveTransactions;
    mocks.fetchUserInventory.mockReturnValue(
      new Promise((_resolve, reject) => {
        rejectInventory = reject;
      }),
    );
    mocks.fetchUserTransactions.mockReturnValue(
      new Promise((resolve) => {
        resolveTransactions = resolve;
      }),
    );
    const consoleErrorSpy = vi
      .spyOn(console, "error")
      .mockImplementation(() => {});
    const loadError = new Error("Fallo posterior al desmontaje");

    const { unmount } = renderUserPage();
    unmount();

    await act(async () => {
      rejectInventory(loadError);
      resolveTransactions(transactions);
    });

    expect(consoleErrorSpy).toHaveBeenCalledTimes(1);
    expect(consoleErrorSpy).toHaveBeenCalledWith(
      "Error al cargar los datos del usuario:",
      loadError,
    );
  });

  test("página el inventario y el historial usando sus controles", async () => {
    const inventoryPage1 = {
      content: inventory,
      page: 1,
      totalPages: 2,
      hasNext: true,
      hasPrevious: false,
    };
    const inventoryPage2 = {
      content: [
        {
          ...inventory[0],
          id: "inventory-2",
          jugador: { ...inventory[0].jugador, nombre: "Página dos" },
        },
      ],
      page: 2,
      totalPages: 2,
      hasNext: false,
      hasPrevious: true,
    };
    const transactionsPage1 = {
      content: transactions,
      page: 1,
      totalPages: 2,
      hasNext: true,
      hasPrevious: false,
    };
    const transactionsPage2 = {
      content: [
        {
          ...transactions[0],
          id: "transaction-2",
          nombreJugador: "Operación dos",
        },
      ],
      page: 2,
      totalPages: 2,
      hasNext: false,
      hasPrevious: true,
    };
    mocks.fetchUserInventory.mockImplementation((_userId, _token, options) =>
      Promise.resolve(options.page === 1 ? inventoryPage1 : inventoryPage2),
    );
    mocks.fetchUserTransactions.mockImplementation((_userId, _token, options) =>
      Promise.resolve(
        options.page === 1 ? transactionsPage1 : transactionsPage2,
      ),
    );

    renderUserPage();

    expect(await screen.findAllByText("Lionel Messi")).toHaveLength(2);
    const nextButtons = screen.getAllByRole("button", { name: "Siguiente" });
    expect(
      screen.getAllByRole("button", { name: "Anterior" })[0],
    ).toBeDisabled();
    fireEvent.click(nextButtons[0]);

    expect(await screen.findByText("Página dos")).toBeInTheDocument();
    fireEvent.click(screen.getAllByRole("button", { name: "Siguiente" })[1]);

    expect(await screen.findByText("Operación dos")).toBeInTheDocument();
    expect(screen.getAllByText("2 / 2")).toHaveLength(2);
    expect(
      screen.getAllByRole("button", { name: "Siguiente" })[0],
    ).toBeDisabled();

    fireEvent.click(screen.getAllByRole("button", { name: "Anterior" })[0]);
    await waitFor(() => {
      expect(mocks.fetchUserInventory).toHaveBeenCalledTimes(4);
    });
    expect(mocks.fetchUserInventory).toHaveBeenLastCalledWith(
      "user-1",
      "token-prueba",
      { page: 1, size: 10 },
    );
    fireEvent.click(screen.getAllByRole("button", { name: "Anterior" })[1]);
    await waitFor(() => {
      expect(mocks.fetchUserTransactions).toHaveBeenCalledTimes(5);
    });
    expect(mocks.fetchUserTransactions).toHaveBeenLastCalledWith(
      "user-1",
      "token-prueba",
      { page: 1, size: 10 },
    );
  });

  test("valida cantidades inválidas y permite cerrar el diálogo", async () => {
    renderUserPage();

    fireEvent.click(
      await screen.findByRole("button", {
        name: "Opciones de venta para Lionel Messi",
      }),
    );

    fireEvent.click(
      await screen.findByRole("button", {
        name: "Crear oferta",
      }),
    );
    const quantity = screen.getByLabelText(/Cantidad de tokens/);
    const form = quantity.closest("form");

    for (const invalidQuantity of ["1.5", "0", "8"]) {
      fireEvent.change(quantity, { target: { value: invalidQuantity } });
      fireEvent.submit(form);

      await waitFor(() =>
        expect(mocks.toastError).toHaveBeenCalledWith(
          "Ingresa una cantidad entre 1 y 7",
        ),
      );
    }

    expect(mocks.createSellOrder).not.toHaveBeenCalled();
    fireEvent.click(screen.getByRole("button", { name: "Volver" }));
    expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
  });

  test("muestra el error del servidor al publicar una oferta", async () => {
    mocks.createSellOrder.mockRejectedValue(new Error("Oferta rechazada"));
    renderUserPage();

    fireEvent.click(
      await screen.findByRole("button", {
        name: "Opciones de venta para Lionel Messi",
      }),
    );

    fireEvent.click(
      screen.getByRole("button", {
        name: "Crear oferta",
      }),
    );

    fireEvent.submit(screen.getByRole("dialog").querySelector("form"));

    await waitFor(() =>
      expect(mocks.toastError).toHaveBeenCalledWith("Oferta rechazada"),
    );

    expect(screen.getByRole("dialog")).toBeInTheDocument();
  });

  test("usa el mensaje predeterminado si la creación falla sin detalle", async () => {
    mocks.createSellOrder.mockRejectedValue({});
    renderUserPage();

    fireEvent.click(
      await screen.findByRole("button", {
        name: "Opciones de venta para Lionel Messi",
      }),
    );

    fireEvent.click(
      screen.getByRole("button", {
        name: "Crear oferta",
      }),
    );

    fireEvent.submit(screen.getByRole("dialog").querySelector("form"));

    await waitFor(() =>
      expect(mocks.toastError).toHaveBeenCalledWith(
        "Error al crear el anuncio de venta",
      ),
    );
  });

  // Tests de crear subasta

  test("abre el formulario de creación de subasta con los datos del jugador", async () => {
    renderUserPage();

    fireEvent.click(
      await screen.findByRole("button", {
        name: "Opciones de venta para Lionel Messi",
      }),
    );

    fireEvent.click(
      screen.getByRole("button", {
        name: "Crear subasta",
      }),
    );

    expect(await screen.findByRole("dialog")).toBeInTheDocument();
    expect(screen.getByRole("heading", {
      name: "Crear subasta",
    })).toBeInTheDocument();

    expect(
      screen.getByText((_, element) =>
        element.tagName === "P" &&
        element.textContent.includes("Estás creando una subasta para") &&
        element.textContent.includes("Lionel Messi")
      )
    ).toBeInTheDocument();
    expect(screen.getByText("Disponibles: 7")).toBeInTheDocument();

    expect(mocks.createAuction).not.toHaveBeenCalled();
    expect(mocks.createSellOrder).not.toHaveBeenCalled();
  });

  test("crea una subasta y navega a su detalle", async () => {
    mocks.createAuction.mockResolvedValue({
      id: "auction-1",
    });

    renderUserPage();

    fireEvent.click(
      await screen.findByRole("button", {
        name: "Opciones de venta para Lionel Messi",
      }),
    );

    fireEvent.click(
      screen.getByRole("button", {
        name: "Crear subasta",
      }),
    );

    await screen.findByRole("dialog");

    fireEvent.change(screen.getByLabelText("Cantidad de tokens"), {
      target: { value: "2" },
    });

    fireEvent.change(screen.getByLabelText("Precio inicial"), {
      target: { value: "150" },
    });

    const fecha = new Date();
    fecha.setDate(fecha.getDate() + 1);

    const vencimiento = [
      fecha.getFullYear(),
      String(fecha.getMonth() + 1).padStart(2, "0"),
      String(fecha.getDate()).padStart(2, "0"),
    ].join("-") + "T12:00";

    fireEvent.change(
      screen.getByLabelText("Fecha y hora de vencimiento"),
      { target: { value: vencimiento } },
    );

    fireEvent.click(
      screen.getByRole("button", {
        name: "Crear subasta",
      }),
    );

    expect(await screen.findByTestId("auction-route")).toBeInTheDocument();

    expect(mocks.createAuction).toHaveBeenCalledWith({
      jugadorId: "player-1",
      cantidad: 2,
      precioInicial: 150,
      vencimiento: `${vencimiento}:00`,
      token: "token-prueba",
    });

    expect(mocks.createSellOrder).not.toHaveBeenCalled();
  });
});
