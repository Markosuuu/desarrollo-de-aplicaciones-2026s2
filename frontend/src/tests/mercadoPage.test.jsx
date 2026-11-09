import { beforeEach, describe, expect, test, vi } from "vitest";
import { act, fireEvent, render, screen, waitFor } from "@testing-library/react";
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
    const search = screen.getByRole("searchbox", { name: "Buscar anuncios" });
    fireEvent.change(search, { target: { value: "  vendedor UNO " } });

    await waitFor(() => {
      expect(screen.getByText("Vendedor Uno")).toBeInTheDocument();
      expect(screen.queryByText("Jude Bellingham")).not.toBeInTheDocument();
    });

    fireEvent.change(
      search,
      { target: { value: "inexistente" } },
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

  test("muestra el estado vacío cuando no hay anuncios", async () => {
    mocks.fetchListings.mockResolvedValue([]);

    renderMarket();

    expect(
      await screen.findByText("No existen anuncios publicados actualmente."),
    ).toBeInTheDocument();
  });

  test("muestra valores de reemplazo para datos incompletos o inválidos", async () => {
    mocks.fetchListings.mockResolvedValue([
      {
        id: "listing-incomplete",
        cantidadTokens: "",
        precioPorToken: "no-numérico",
        precioTotal: null,
      },
    ]);

    renderMarket();

    expect(await screen.findByText("Jugador")).toBeInTheDocument();
    expect(screen.getByText("—")).toBeInTheDocument();
    expect(screen.getByText("— tokens")).toBeInTheDocument();
    expect(screen.getAllByText("— créditos")).toHaveLength(2);
  });

  test("normaliza el formato paginado y permite avanzar y retroceder", async () => {
    const firstPage = { ...listings[0], id: "page-1" };
    const secondPage = { ...listings[1], id: "page-2" };
    mocks.fetchListings
      .mockResolvedValueOnce({
        content: [firstPage],
        page: 1,
        totalPages: 2,
        hasNext: true,
        hasPrevious: false,
      })
      .mockResolvedValueOnce({
        listings: [secondPage],
        page: 2,
        totalPages: 2,
        hasNext: false,
        hasPrevious: true,
      })
      .mockResolvedValueOnce({
        content: [firstPage],
        page: 1,
        totalPages: 2,
        hasNext: true,
        hasPrevious: false,
      });

    renderMarket();

    expect(await screen.findByText("Lionel Messi")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Anterior" })).toBeDisabled();
    fireEvent.click(screen.getByRole("button", { name: "Siguiente" }));

    expect(await screen.findByText("Jude Bellingham")).toBeInTheDocument();
    expect(screen.getByText("Página 2 de 2")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Siguiente" })).toBeDisabled();

    fireEvent.click(screen.getByRole("button", { name: "Anterior" }));
    expect(await screen.findByText("Lionel Messi")).toBeInTheDocument();
    expect(mocks.fetchListings).toHaveBeenLastCalledWith("token-test", {
      page: 1,
      size: 20,
    });
  });

  test("usa el mensaje de error predeterminado si el rechazo no tiene mensaje", async () => {
    mocks.fetchListings.mockRejectedValue({});

    renderMarket();

    expect(
      await screen.findByText("No se pudieron cargar los anuncios."),
    ).toBeInTheDocument();
  });

  test("normaliza respuestas sin listados ni metadatos de paginación", async () => {
    mocks.fetchListings
      .mockResolvedValueOnce({ content: null, listings: null })
      .mockResolvedValueOnce({ content: [listings[0]] });

    const { unmount } = renderMarket();
    expect(
      await screen.findByText("No existen anuncios publicados actualmente."),
    ).toBeInTheDocument();
    unmount();

    renderMarket();
    expect(await screen.findByText("Lionel Messi")).toBeInTheDocument();
    expect(screen.queryByText(/Página \d+ de \d+/)).not.toBeInTheDocument();
  });

  test("ignora resultados de carga recibidos después de desmontarse", async () => {
    let resolveListings;
    mocks.fetchListings.mockReturnValue(
      new Promise((resolve) => {
        resolveListings = resolve;
      }),
    );

    const { unmount } = renderMarket();
    unmount();

    await act(async () => {
      resolveListings(listings);
    });
  });

  test("ignora errores recibidos después de desmontarse", async () => {
    let rejectListings;
    mocks.fetchListings.mockReturnValue(
      new Promise((_resolve, reject) => {
        rejectListings = reject;
      }),
    );
    vi.spyOn(console, "error").mockImplementation(() => {});

    const { unmount } = renderMarket();
    unmount();

    await act(async () => {
      rejectListings(new Error("Fallo posterior al desmontaje"));
    });
  });
});
