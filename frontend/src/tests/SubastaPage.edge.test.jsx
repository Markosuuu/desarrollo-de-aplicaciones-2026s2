import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, test, vi } from "vitest";
import SubastaPage from "../pages/SubastaPage";

const mocks = vi.hoisted(() => ({
  fetchSubasta: vi.fn(),
  cerrarSubasta: vi.fn(),
  ofertarSubasta: vi.fn(),
  useAuth: vi.fn(),
  toastError: vi.fn(),
  toastSuccess: vi.fn(),
}));

vi.mock("../api/subastaApi", () => ({
  fetchSubasta: mocks.fetchSubasta,
  cerrarSubasta: mocks.cerrarSubasta,
  ofertarSubasta: mocks.ofertarSubasta,
}));

vi.mock("../context/AuthContext", () => ({
  useAuth: mocks.useAuth,
}));

vi.mock("../components/subasta/RealizarOferta", () => ({
  default: ({ handleOffer, puja, setPuja }) => (
    <form onSubmit={handleOffer}>
      <label htmlFor="test-bid">Puja de prueba</label>
      <input
        id="test-bid"
        value={puja}
        onChange={(event) => setPuja(event.target.value)}
      />
      <button type="submit">Enviar puja de prueba</button>
    </form>
  ),
}));

vi.mock("react-toastify", () => ({
  toast: { error: mocks.toastError, success: mocks.toastSuccess },
}));

const auction = {
  id: "auction-1",
  estado: true,
  ofertaActual: 2.5,
  propietario: { id: "owner-1", nombre: "Propietario" },
  jugador: { nombre: "Jugador de prueba" },
};

function renderAuction() {
  return render(
    <MemoryRouter initialEntries={["/subasta/auction-1"]}>
      <Routes>
        <Route path="/subasta/:id" element={<SubastaPage />} />
        <Route path="/profile" element={<p>Perfil</p>} />
      </Routes>
    </MemoryRouter>,
  );
}

beforeEach(() => {
  vi.clearAllMocks();
  vi.useRealTimers();
  mocks.useAuth.mockReturnValue({
    user: { id: "user-1" },
    token: "token-test",
  });
  mocks.fetchSubasta.mockResolvedValue(auction);
  mocks.cerrarSubasta.mockResolvedValue({});
  mocks.ofertarSubasta.mockResolvedValue({});
});

describe("SubastaPage - ramas alternativas", () => {
  test("mantiene el estado de carga mientras la petición sigue pendiente", () => {
    mocks.fetchSubasta.mockReturnValue(new Promise(() => {}));

    renderAuction();

    expect(screen.getByText("Cargando subasta...")).toBeInTheDocument();
  });

  test("muestra el error predeterminado cuando la carga falla sin mensaje", async () => {
    mocks.fetchSubasta.mockRejectedValue({});

    renderAuction();

    expect(
      await screen.findByText("No se pudo cargar la subasta."),
    ).toBeInTheDocument();
  });

  test("no renderiza contenido si la respuesta no incluye una subasta", async () => {
    mocks.fetchSubasta.mockResolvedValue(null);
    const { container } = renderAuction();

    await waitFor(() => {
      expect(mocks.fetchSubasta).toHaveBeenCalledOnce();
      expect(screen.queryByText("Cargando subasta...")).not.toBeInTheDocument();
    });

    expect(container.querySelector("main")).toBeNull();
  });

  test("usa el título alternativo cuando no hay datos del jugador", async () => {
    mocks.fetchSubasta.mockResolvedValue({
      ...auction,
      jugador: null,
      estado: false,
    });

    renderAuction();

    expect(
      await screen.findByRole("heading", { name: "Subasta" }),
    ).toBeInTheDocument();
    expect(screen.getByText("Subasta finalizada")).toBeInTheDocument();
  });

  test("muestra el error predeterminado si falla el cierre sin mensaje", async () => {
    mocks.useAuth.mockReturnValue({
      user: { id: "owner-1" },
      token: "token-test",
    });
    mocks.cerrarSubasta.mockRejectedValue({});

    renderAuction();

    fireEvent.click(
      await screen.findByRole("button", { name: "Cerrar subasta" }),
    );
    fireEvent.click(screen.getByRole("button", { name: "Aceptar" }));

    await waitFor(() =>
      expect(mocks.toastError).toHaveBeenCalledWith(
        "No se pudo cerrar la subasta",
      ),
    );
  });

  test("rechaza pujas no numéricas y menores al mínimo", async () => {
    renderAuction();

    await screen.findByText("Jugador de prueba");
    const bidInput = screen.getByLabelText("Puja de prueba");

    fireEvent.change(bidInput, { target: { value: "no-es-numérico" } });
    fireEvent.click(
      screen.getByRole("button", { name: "Enviar puja de prueba" }),
    );

    await waitFor(() =>
      expect(mocks.toastError).toHaveBeenCalledWith(
        "La puja mínima debe ser 2,51",
      ),
    );

    expect(mocks.ofertarSubasta).not.toHaveBeenCalled();

    fireEvent.change(bidInput, { target: { value: "1" } });
    fireEvent.click(
      screen.getByRole("button", { name: "Enviar puja de prueba" }),
    );

    expect(mocks.ofertarSubasta).not.toHaveBeenCalled();
  });

  test("muestra el error predeterminado si falla la oferta sin mensaje", async () => {
    mocks.ofertarSubasta.mockRejectedValue({});
    renderAuction();

    await screen.findByText("Jugador de prueba");
    fireEvent.change(screen.getByLabelText("Puja de prueba"), {
      target: { value: "3" },
    });
    fireEvent.click(
      screen.getByRole("button", { name: "Enviar puja de prueba" }),
    );

    await waitFor(() =>
      expect(mocks.toastError).toHaveBeenCalledWith(
        "No se pudo realizar la oferta",
      ),
    );
  });
});
