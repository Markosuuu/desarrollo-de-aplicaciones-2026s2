import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import {
  act,
  cleanup,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";

const mocks = vi.hoisted(() => ({
  fetchSubastas: vi.fn(),
  buscarSubastasPorNombre: vi.fn(),
  useAuth: vi.fn(),
}));

vi.mock("../api/subastaApi", () => ({
  fetchSubastas: mocks.fetchSubastas,
  buscarSubastasPorNombre: mocks.buscarSubastasPorNombre,
}));

vi.mock("../context/AuthContext", () => ({
  useAuth: mocks.useAuth,
}));

vi.mock("../components/SubastaCard", () => ({
  default: ({ subasta }) => (
    <article data-testid="subasta-card">{subasta.nombre}</article>
  ),
}));

import ListaSubastasPage from "../pages/ListaSubastaPage";

const crearSubastas = (cantidad, inicio = 1) =>
  Array.from({ length: cantidad }, (_, index) => ({
    id: `subasta-${inicio + index}`,
    nombre: `Subasta ${inicio + index}`,
  }));

let observers = [];

class MockIntersectionObserver {
  constructor(callback, options) {
    this.callback = callback;
    this.options = options;
    this.observe = vi.fn();
    this.disconnect = vi.fn();

    observers.push(this);
  }

  trigger(isIntersecting = true) {
    this.callback([
      {
        isIntersecting,
      },
    ]);
  }
}

function renderListaSubastas() {
  return render(<ListaSubastasPage />);
}

describe("ListaSubastasPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    observers = [];

    mocks.useAuth.mockReturnValue({
      token: "token-test",
    });

    vi.stubGlobal("IntersectionObserver", MockIntersectionObserver);
  });

  afterEach(() => {
    cleanup();
    vi.unstubAllGlobals();
  });

  test("muestra el estado de carga inicial", () => {
    mocks.fetchSubastas.mockReturnValue(new Promise(() => {}));

    renderListaSubastas();

    expect(screen.getByText("Subastas")).toBeInTheDocument();
    expect(
      screen.getByText("Explorá las subastas disponibles en el mercado."),
    ).toBeInTheDocument();

    expect(screen.getByText("Cargando subastas...")).toBeInTheDocument();

    expect(mocks.fetchSubastas).toHaveBeenCalledWith({
      page: 1,
      size: 10,
      token: "token-test",
    });
  });

  test("muestra las subastas obtenidas correctamente", async () => {
    mocks.fetchSubastas.mockResolvedValue({
      subastas: crearSubastas(3),
    });

    renderListaSubastas();

    expect(await screen.findAllByTestId("subasta-card")).toHaveLength(3);

    expect(screen.getByText("Subasta 1")).toBeInTheDocument();
    expect(screen.getByText("Subasta 2")).toBeInTheDocument();
    expect(screen.getByText("Subasta 3")).toBeInTheDocument();

    expect(
      screen.getByText("Fin de las subastas activas"),
    ).toBeInTheDocument();
  });

  test("busca subastas por el nombre del jugador", async () => {
    mocks.fetchSubastas.mockResolvedValue({
      subastas: crearSubastas(1),
    });
    mocks.buscarSubastasPorNombre.mockResolvedValue({
      subastas: [{ id: "messi", nombre: "Lionel Messi" }],
    });

    renderListaSubastas();
    await screen.findByText("Subasta 1");

    fireEvent.change(screen.getByPlaceholderText("Nombre del jugador"), {
      target: { value: "Lionel Messi" },
    });

    expect(await screen.findByText("Lionel Messi")).toBeInTheDocument();
    expect(mocks.buscarSubastasPorNombre).toHaveBeenCalledWith({
      page: 1,
      size: 10,
      user: "Lionel Messi",
      jugador: "Lionel Messi",
      token: "token-test",
    });
    expect(screen.queryByText("Subasta 1")).not.toBeInTheDocument();
  });

  test("ignora resultados de una búsqueda anterior que termina más tarde", async () => {
    let resolveInitialRequest;
    mocks.fetchSubastas.mockReturnValue(
      new Promise((resolve) => {
        resolveInitialRequest = resolve;
      }),
    );
    mocks.buscarSubastasPorNombre.mockResolvedValue({
      subastas: [{ id: "messi", nombre: "Lionel Messi" }],
    });

    renderListaSubastas();

    fireEvent.change(screen.getByPlaceholderText("Nombre del jugador"), {
      target: { value: "Messi" },
    });

    expect(await screen.findByText("Lionel Messi")).toBeInTheDocument();

    await act(async () => {
      resolveInitialRequest({ subastas: crearSubastas(1) });
    });

    expect(screen.getByText("Lionel Messi")).toBeInTheDocument();
    expect(screen.queryByText("Subasta 1")).not.toBeInTheDocument();
  });

  test("muestra un mensaje cuando no hay subastas disponibles", async () => {
    mocks.fetchSubastas.mockResolvedValue({
      subastas: [],
    });

    renderListaSubastas();

    expect(
      await screen.findByText("No existen subastas activas actualmente"),
    ).toBeInTheDocument();

    expect(screen.queryAllByTestId("subasta-card")).toHaveLength(0);
  });

  test("maneja una respuesta sin la propiedad subastas", async () => {
    mocks.fetchSubastas.mockResolvedValue({});

    renderListaSubastas();

    expect(
      await screen.findByText("No existen subastas activas actualmente"),
    ).toBeInTheDocument();
  });

  test("muestra un error cuando falla la carga de subastas", async () => {
    mocks.fetchSubastas.mockRejectedValue(
      new Error("Error al conectar con el servidor"),
    );

    renderListaSubastas();

    expect(
      await screen.findByText("Error al conectar con el servidor"),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("button", { name: "Reintentar" }),
    ).toBeInTheDocument();

    expect(screen.queryByText("Cargando subastas...")).not.toBeInTheDocument();
  });

  test("muestra el mensaje predeterminado cuando ocurre un error sin mensaje", async () => {
    mocks.fetchSubastas.mockRejectedValue({});

    renderListaSubastas();

    expect(
      await screen.findByText("Ocurrió un error al cargar las subastas."),
    ).toBeInTheDocument();
  });

  test("permite reintentar la carga después de un error", async () => {
    mocks.fetchSubastas
      .mockRejectedValueOnce(new Error("Error temporal"))
      .mockResolvedValueOnce({
        subastas: crearSubastas(2),
      });

    renderListaSubastas();

    fireEvent.click(await screen.findByRole("button", { name: "Reintentar" }));

    expect(await screen.findAllByTestId("subasta-card")).toHaveLength(2);

    expect(mocks.fetchSubastas).toHaveBeenCalledTimes(2);

    expect(mocks.fetchSubastas).toHaveBeenLastCalledWith({
      page: 1,
      size: 10,
      token: "token-test",
    });
  });

  test("configura el IntersectionObserver para detectar el scroll", async () => {
    mocks.fetchSubastas.mockResolvedValue({
      subastas: crearSubastas(10),
      hasNext: true,
    });

    renderListaSubastas();

    await screen.findAllByTestId("subasta-card");

    await waitFor(() => {
      expect(observers).toHaveLength(1);
    });

    expect(observers[0].observe).toHaveBeenCalledTimes(1);
    expect(observers[0].options).toEqual({
      rootMargin: "200px",
      threshold: 0,
    });
  });

  test("carga la siguiente página cuando el sentinel es visible", async () => {
    mocks.fetchSubastas
      .mockResolvedValueOnce({
        subastas: crearSubastas(10),
        hasNext: true,
      })
      .mockResolvedValueOnce({
        subastas: crearSubastas(5, 11),
        hasNext: false,
      });

    renderListaSubastas();

    expect(await screen.findAllByTestId("subasta-card")).toHaveLength(10);

    await waitFor(() => {
      expect(observers.length).toBeGreaterThan(0);
    });

    act(() => {
      observers[0].trigger(true);
    });

    await waitFor(() => {
      expect(mocks.fetchSubastas).toHaveBeenCalledTimes(2);
    });

    expect(mocks.fetchSubastas).toHaveBeenLastCalledWith({
      page: 2,
      size: 10,
      token: "token-test",
    });

    expect(await screen.findAllByTestId("subasta-card")).toHaveLength(15);

    expect(
      screen.getByText("Fin de las subastas activas"),
    ).toBeInTheDocument();
  });

  test("no carga otra página cuando el sentinel no es visible", async () => {
    mocks.fetchSubastas.mockResolvedValue({
      subastas: crearSubastas(10),
      hasNext: true,
    });

    renderListaSubastas();

    await screen.findAllByTestId("subasta-card");

    await waitFor(() => {
      expect(observers).toHaveLength(1);
    });

    act(() => {
      observers[0].trigger(false);
    });

    expect(mocks.fetchSubastas).toHaveBeenCalledTimes(1);
  });

  test("desconecta el observer cuando deja de utilizarse", async () => {
    mocks.fetchSubastas.mockResolvedValue({
      subastas: crearSubastas(10),
      hasNext: true,
    });

    const { unmount } = renderListaSubastas();

    await screen.findAllByTestId("subasta-card");

    await waitFor(() => {
      expect(observers).toHaveLength(1);
    });

    const observer = observers[0];

    unmount();

    expect(observer.disconnect).toHaveBeenCalled();
  });

  test("no crea un observer cuando no hay más subastas", async () => {
    mocks.fetchSubastas.mockResolvedValue({
      subastas: crearSubastas(5),
    });

    renderListaSubastas();

    await screen.findAllByTestId("subasta-card");

    expect(
      screen.getByText("Fin de las subastas activas"),
    ).toBeInTheDocument();

    expect(observers).toHaveLength(0);
  });

  test("no vuelve a solicitar páginas cuando ya se alcanzó el final", async () => {
    mocks.fetchSubastas.mockResolvedValue({
      subastas: crearSubastas(5),
    });

    renderListaSubastas();

    await screen.findAllByTestId("subasta-card");

    expect(mocks.fetchSubastas).toHaveBeenCalledTimes(1);

    expect(observers).toHaveLength(0);
  });
});
