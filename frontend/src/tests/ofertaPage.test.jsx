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
  toastError: vi.fn(),
  toastSuccess: vi.fn(),
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

vi.mock("react-toastify", () => ({
  toast: { error: mocks.toastError, success: mocks.toastSuccess },
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

const userInventory = {content: [
  {
    usuario: { id: "user-1", creditosDisponibles: 75 },
    jugador: { nombre: "Jude Bellingham" },
    tokensDisponibles: 1,
  },
]};

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
        <Route
          path="/profile"
          element={<div data-testid="profile-route">Perfil</div>}
        />
      </Routes>
    </MemoryRouter>,
  );
}

beforeEach(() => {
  vi.clearAllMocks();
  vi.stubGlobal(
    "confirm",
    vi.fn(() => true),
  );
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

  test("permite elegir la cantidad, confirma la compra y redirige al perfil", async () => {
    renderOffer();

    fireEvent.click(await screen.findByRole("button", { name: "Comprar" }));

    expect(await screen.findByRole("dialog")).toBeInTheDocument();
    expect(screen.getByText("Total:")).toBeInTheDocument();
    fireEvent.change(screen.getByLabelText(/Cantidad \(máximo 4\)/), {
      target: { value: "2" },
    });
    expect(screen.getByText("25 créditos")).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "Confirmar compra" }));

    expect(mocks.buyListing).toHaveBeenCalledWith("listing-1", 2, "token-test");
    expect(await screen.findByTestId("profile-route")).toBeInTheDocument();
  });

  test("no permite comprar más tokens que los anunciados", async () => {
    renderOffer();

    fireEvent.click(await screen.findByRole("button", { name: "Comprar" }));
    fireEvent.change(screen.getByLabelText(/Cantidad \(máximo 4\)/), {
      target: { value: "5" },
    });

    expect(
      screen.getByRole("button", { name: "Confirmar compra" }),
    ).toBeDisabled();
    expect(mocks.buyListing).not.toHaveBeenCalled();
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

test("muestra un error cuando la API no encuentra la oferta", async () => {
  mocks.fetchListings.mockResolvedValue([]);

  renderOffer({ withNavigationState: false });

  expect(
    await screen.findByText("No se encontró el anuncio solicitado."),
  ).toBeInTheDocument();
});

test("muestra el mensaje del error cuando falla la carga de la oferta", async () => {
  mocks.fetchListings.mockRejectedValue(new Error("Error del servidor"));

  renderOffer({ withNavigationState: false });

  expect(await screen.findByText("Error del servidor")).toBeInTheDocument();
});

test("muestra el mensaje predeterminado cuando falla la carga sin un mensaje de error", async () => {
  mocks.fetchListings.mockRejectedValue({});

  renderOffer({ withNavigationState: false });

  expect(
    await screen.findByText("No se pudo cargar la información del anuncio."),
  ).toBeInTheDocument();
});

test("permite reintentar la carga de una oferta", async () => {
  mocks.fetchListings
    .mockRejectedValueOnce(new Error("Error temporal"))
    .mockResolvedValueOnce([listing]);

  renderOffer({ withNavigationState: false });

  expect(await screen.findByText("Error temporal")).toBeInTheDocument();

  fireEvent.click(screen.getByRole("button", { name: "Reintentar" }));

  expect(await screen.findByText("Lionel Messi")).toBeInTheDocument();

  expect(mocks.fetchListings).toHaveBeenCalledTimes(2);
});

test("muestra un error cuando no se puede identificar al usuario", async () => {
  mocks.useAuth.mockReturnValue({
    user: { nombre: "Usuario" },
    token: "token-test",
  });

  renderOffer();

  expect(
    await screen.findByText("No se pudo identificar al usuario."),
  ).toBeInTheDocument();

  expect(mocks.fetchUserInventory).not.toHaveBeenCalled();

  expect(screen.getByText("No disponible")).toBeInTheDocument();
});

test("utiliza los créditos del usuario cuando no aparece en el inventario", async () => {
    mocks.fetchUserInventory.mockResolvedValue({
        content: []
    });

  renderOffer();

  expect(await screen.findByText("100 créditos")).toBeInTheDocument();
});

test("muestra un error cuando falla la carga de créditos y permite reintentar", async () => {
  mocks.fetchUserInventory
    .mockRejectedValueOnce(new Error("Error temporal"))
    .mockResolvedValueOnce(userInventory);

  renderOffer();

  expect(
    await screen.findByText("No se pudieron cargar los créditos actuales."),
  ).toBeInTheDocument();

  fireEvent.click(screen.getByRole("button", { name: "Reintentar" }));

  expect(await screen.findByText("75 créditos")).toBeInTheDocument();

  expect(mocks.fetchUserInventory).toHaveBeenCalledTimes(2);
});

test("rechaza cantidades que no sean números enteros", async () => {
  renderOffer();

  fireEvent.click(await screen.findByRole("button", { name: "Comprar" }));

  const input = screen.getByLabelText(/Cantidad \(máximo 4\)/);

  fireEvent.change(input, {
    target: { value: "1.5" },
  });

  fireEvent.submit(input.closest("form"));

  await waitFor(() =>
    expect(mocks.toastError).toHaveBeenCalledWith(
      "Ingresa una cantidad entre 1 y 4.",
    ),
  );

  expect(mocks.buyListing).not.toHaveBeenCalled();
});

test("rechaza la compra cuando el usuario no tiene créditos suficientes", async () => {
    mocks.fetchUserInventory.mockResolvedValue({
        content: [
            {
                usuario: { id: "user-1", creditosDisponibles: 10 },
            },
        ],
    });

  renderOffer();

  await screen.findByText("10 créditos");

  fireEvent.click(screen.getByRole("button", { name: "Comprar" }));

  const input = screen.getByLabelText(/Cantidad \(máximo 4\)/);

  fireEvent.submit(input.closest("form"));

  await waitFor(() =>
    expect(mocks.toastError).toHaveBeenCalledWith(
      "No tienes créditos suficientes para esa cantidad",
    ),
  );

  expect(mocks.buyListing).not.toHaveBeenCalled();
});

test("muestra el error cuando falla la compra", async () => {
  mocks.buyListing.mockRejectedValue(
    new Error("No se pudo procesar la compra"),
  );

  renderOffer();

  fireEvent.click(await screen.findByRole("button", { name: "Comprar" }));

  const input = screen.getByLabelText(/Cantidad \(máximo 4\)/);

  fireEvent.submit(input.closest("form"));

  await waitFor(() =>
    expect(mocks.toastError).toHaveBeenCalledWith(
      "No se pudo procesar la compra",
    ),
  );

  expect(mocks.buyListing).toHaveBeenCalledTimes(1);
});

test("muestra el mensaje predeterminado cuando falla la compra sin un mensaje de error", async () => {
  mocks.buyListing.mockRejectedValue({});

  renderOffer();

  fireEvent.click(await screen.findByRole("button", { name: "Comprar" }));

  fireEvent.click(screen.getByRole("button", { name: "Confirmar compra" }));

  await waitFor(() =>
    expect(mocks.toastError).toHaveBeenCalledWith(
      "No se pudo completar la compra",
    ),
  );
});

test("no cancela la oferta cuando el usuario rechaza la confirmación", async () => {
  vi.stubGlobal(
    "confirm",
    vi.fn(() => false),
  );

  renderOffer({ asOwner: true });

  fireEvent.click(
    await screen.findByRole("button", { name: "Cancelar oferta" }),
  );

  expect(window.confirm).toHaveBeenCalled();
  expect(mocks.cancelListing).not.toHaveBeenCalled();
});

test("muestra un error cuando falla la cancelación", async () => {
  mocks.cancelListing.mockRejectedValue(new Error("No se pudo cancelar"));

  renderOffer({ asOwner: true });

  fireEvent.click(
    await screen.findByRole("button", { name: "Cancelar oferta" }),
  );

  await waitFor(() =>
    expect(mocks.toastError).toHaveBeenCalledWith("No se pudo cancelar"),
  );

  expect(mocks.cancelListing).toHaveBeenCalledTimes(1);
});

test("muestra el mensaje predeterminado cuando falla la cancelación sin un mensaje de error", async () => {
  mocks.cancelListing.mockRejectedValue({});

  renderOffer({ asOwner: true });

  fireEvent.click(
    await screen.findByRole("button", { name: "Cancelar oferta" }),
  );

  await waitFor(() =>
    expect(mocks.toastError).toHaveBeenCalledWith(
      "No se pudo cancelar la oferta",
    ),
  );
});

test("muestra que la oferta no está disponible cuando fue cancelada", async () => {
  const cancelledListing = {
    ...listing,
    estado: "CANCELADA",
  };

  render(
    <MemoryRouter
      initialEntries={[
        {
          pathname: "/mercado/listing-1",
          state: { listing: cancelledListing },
        },
      ]}
    >
      <Routes>
        <Route path="/mercado/:id" element={<OfertaPage />} />
      </Routes>
    </MemoryRouter>,
  );

  expect(
    await screen.findByText("Esta oferta ya no está disponible."),
  ).toBeInTheDocument();

  expect(
    screen.queryByRole("button", { name: "Comprar" }),
  ).not.toBeInTheDocument();

  expect(
    screen.queryByRole("button", { name: "Cancelar oferta" }),
  ).not.toBeInTheDocument();
});

test("permite cerrar el diálogo de compra con el botón Volver", async () => {
  renderOffer();

  fireEvent.click(await screen.findByRole("button", { name: "Comprar" }));

  expect(await screen.findByRole("dialog")).toBeInTheDocument();

  fireEvent.click(screen.getByRole("button", { name: "Volver" }));

  expect(screen.queryByRole("dialog")).not.toBeInTheDocument();
});
