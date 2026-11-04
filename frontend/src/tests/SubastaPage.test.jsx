import { beforeEach, describe, expect, test, vi } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import SubastaPage from "../pages/SubastaPage";

const mocks = vi.hoisted(() => ({
  fetchSubasta: vi.fn(),
  cerrarSubasta: vi.fn(),
  ofertarSubasta: vi.fn(),
  useAuth: vi.fn(),
}));

vi.mock("../api/subastaApi", () => ({
  fetchSubasta: mocks.fetchSubasta,
  cerrarSubasta: mocks.cerrarSubasta,
  ofertarSubasta: mocks.ofertarSubasta,
}));

vi.mock("../context/AuthContext", () => ({
  useAuth: mocks.useAuth,
}));

const subasta = {
  id: "auction-1",
  propietario: {
    id: "owner-1",
    nombre: "Maite",
    correo: "maite@gmail.com",
    creditosDisponibles: 75.04,
  },
  jugador: {
    id: "player-1",
    nombre: "Michael Olise",
    equipo: "Bayern",
    liga: "Bundesliga",
    edad: 24,
    altura: 180,
    peso: 78,
    posicion: "Midfielder",
    activo: true,
    goles: 6,
    asistencias: 3,
    disparosPorPartido: 4.38,
    keyPassesPorPartido: 3.63,
    dribblesGanadosPorPartido: 2.13,
    faltasCometidasPorPartido: 2.25,
    rating: 8.18,
    minutosJugados: 657,
    cotizacion: 5.21,
    fechaActualizacion: "2026-11-04T14:55:01",
    tokensRestantes: 98,
  },
  cantidadOfrecida: 1,
  ofertaActual: 2.5,
  ofertasPrevias: [
    {
      id: "offer-1",
      subastaId: "auction-1",
      usuario: {
        id: "user-2",
        nombre: "Usuario Dos",
        correo: "usuario2@gmail.com",
        creditosDisponibles: 50,
      },
      puja: 2.5,
      timestamp: "2026-11-04T14:30:00",
      estado: true,
    },
  ],
  estado: true,
  vencimiento: "2026-11-15T15:35:00",
};

function renderAuction(user = { id: "user-1", nombre: "Usuario" }) {
  return render(
    <MemoryRouter initialEntries={["/subasta/auction-1"]}>
      <Routes>
        <Route path="/subasta/:id" element={<SubastaPage />} />
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

  mocks.useAuth.mockReturnValue({
    user: { id: "user-1", nombre: "Usuario" },
    token: "token-test",
  });

  mocks.fetchSubasta.mockResolvedValue(subasta);
  mocks.cerrarSubasta.mockResolvedValue({});
  mocks.ofertarSubasta.mockResolvedValue({});
});

describe("SubastaPage", () => {
  test("muestra la información de la subasta y del jugador", async () => {
    renderAuction();

    expect(await screen.findByText("Michael Olise")).toBeInTheDocument();
    expect(screen.getByText("Maite")).toBeInTheDocument();
  });

  test("muestra las estadísticas del jugador", async () => {
    renderAuction();

    await screen.findByText("Michael Olise");

    expect(screen.getByText("Goles")).toBeInTheDocument();
    expect(screen.getByText("Asistencias")).toBeInTheDocument();
    expect(screen.getByText("Disparos por partido")).toBeInTheDocument();
    expect(screen.getByText("Regates ganados por partido")).toBeInTheDocument();
    expect(screen.getByText("Faltas por partido")).toBeInTheDocument();
    expect(screen.getByText("Minutos jugados")).toBeInTheDocument();
  });

  test("muestra el historial de ofertas", async () => {
    renderAuction();

    expect(await screen.findByText("Usuario Dos")).toBeInTheDocument();
    expect(screen.getByText("Puja")).toBeInTheDocument();
    expect(screen.getByText("Fecha")).toBeInTheDocument();
    expect(screen.getByText("Estado")).toBeInTheDocument();
  });

  test("muestra un mensaje cuando todavía no hay ofertas", async () => {
    mocks.fetchSubasta.mockResolvedValue({
      ...subasta,
      ofertasPrevias: [],
      ofertaActual: null,
    });

    renderAuction();

    expect(
      await screen.findByText("Todavía no hubo ofertas para esta subasta."),
    ).toBeInTheDocument();
  });

  test("permite al propietario iniciar el cierre de la subasta", async () => {
    mocks.useAuth.mockReturnValue({
      user: { id: "owner-1", nombre: "Maite" },
      token: "token-test",
    });

    renderAuction();

    fireEvent.click(
      await screen.findByRole("button", { name: "Cerrar subasta" }),
    );

    expect(
      screen.getByText("¿Deseas cerrar la subasta?"),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("button", { name: "Aceptar" }),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("button", { name: "Cancelar" }),
    ).toBeInTheDocument();
  });

  test("permite al propietario confirmar el cierre y navega al perfil", async () => {
    mocks.useAuth.mockReturnValue({
      user: { id: "owner-1", nombre: "Maite" },
      token: "token-test",
    });

    renderAuction();

    fireEvent.click(
      await screen.findByRole("button", { name: "Cerrar subasta" }),
    );

    fireEvent.click(screen.getByRole("button", { name: "Aceptar" }));

    await waitFor(() => {
      expect(mocks.cerrarSubasta).toHaveBeenCalledWith(
        "auction-1",
        "token-test",
      );
    });

    expect(await screen.findByTestId("profile-route")).toBeInTheDocument();
  });

  test("permite cancelar la confirmación de cierre", async () => {
    mocks.useAuth.mockReturnValue({
      user: { id: "owner-1", nombre: "Maite" },
      token: "token-test",
    });

    renderAuction();

    fireEvent.click(
      await screen.findByRole("button", { name: "Cerrar subasta" }),
    );

    fireEvent.click(screen.getByRole("button", { name: "Cancelar" }));

    expect(
      screen.queryByText("¿Deseas cerrar la subasta?"),
    ).not.toBeInTheDocument();

    expect(mocks.cerrarSubasta).not.toHaveBeenCalled();
  });

  test("muestra un error si falla el cierre de la subasta", async () => {
    mocks.useAuth.mockReturnValue({
      user: { id: "owner-1", nombre: "Maite" },
      token: "token-test",
    });

    mocks.cerrarSubasta.mockRejectedValue(
      new Error("No se pudo cerrar la subasta"),
    );

    renderAuction();

    fireEvent.click(
      await screen.findByRole("button", { name: "Cerrar subasta" }),
    );

    fireEvent.click(screen.getByRole("button", { name: "Aceptar" }));

    expect(
      await screen.findByText("No se pudo cerrar la subasta"),
    ).toBeInTheDocument();
  });

  test("no permite al propietario realizar ofertas", async () => {
    mocks.useAuth.mockReturnValue({
      user: { id: "owner-1", nombre: "Maite" },
      token: "token-test",
    });

    renderAuction();

    await screen.findByText("Michael Olise");

    expect(
      screen.queryByRole("button", { name: "Realizar una oferta" }),
    ).not.toBeInTheDocument();

    expect(
      screen.getByRole("button", { name: "Cerrar subasta" }),
    ).toBeInTheDocument();
  });

  test("permite a otro usuario abrir el formulario de oferta", async () => {
    renderAuction();

    fireEvent.click(
      await screen.findByRole("button", { name: "Realizar una oferta" }),
    );

    expect(screen.getByLabelText("Tu oferta")).toBeInTheDocument();

    expect(
      screen.getByRole("button", { name: "Confirmar oferta" }),
    ).toBeInTheDocument();
  });

  test("permite realizar una oferta válida y actualiza la subasta", async () => {
    renderAuction();

    fireEvent.click(
      await screen.findByRole("button", { name: "Realizar una oferta" }),
    );

    fireEvent.change(screen.getByLabelText("Tu oferta"), {
      target: { value: "2.51" },
    });

    fireEvent.click(
      screen.getByRole("button", { name: "Confirmar oferta" }),
    );

    await waitFor(() => {
      expect(mocks.ofertarSubasta).toHaveBeenCalledWith({
        subastaId: "auction-1",
        puja: 2.51,
        token: "token-test",
      });
    });

    await waitFor(() => {
      expect(mocks.fetchSubasta).toHaveBeenCalledTimes(2);
    });

    expect(
      await screen.findByText("¡Tu oferta se realizó correctamente!"),
    ).toBeInTheDocument();
  });

  test("muestra un error si falla la realización de una oferta", async () => {
    mocks.ofertarSubasta.mockRejectedValue(
      new Error("No tenés créditos suficientes"),
    );

    renderAuction();

    fireEvent.click(
      await screen.findByRole("button", { name: "Realizar una oferta" }),
    );

    fireEvent.change(screen.getByLabelText("Tu oferta"), {
      target: { value: "3" },
    });

    fireEvent.click(
      screen.getByRole("button", { name: "Confirmar oferta" }),
    );

    expect(
      await screen.findByText("No tenés créditos suficientes"),
    ).toBeInTheDocument();
  });

  test("oculta las acciones cuando la subasta está finalizada", async () => {
    mocks.fetchSubasta.mockResolvedValue({
      ...subasta,
      estado: false,
    });

    renderAuction();

    await screen.findByText("Michael Olise");

    expect(
      screen.queryByRole("button", { name: "Cerrar subasta" }),
    ).not.toBeInTheDocument();

    expect(
      screen.queryByRole("button", { name: "Realizar una oferta" }),
    ).not.toBeInTheDocument();
  });

  test("muestra un error de API al cargar la subasta", async () => {
    mocks.fetchSubasta.mockRejectedValue(
      new Error("No se pudo cargar la subasta"),
    );

    renderAuction();

    expect(
      await screen.findByText("No se pudo cargar la subasta"),
    ).toBeInTheDocument();
  });

  test("permite reintentar la carga después de un error", async () => {
    mocks.fetchSubasta
      .mockRejectedValueOnce(new Error("Error temporal"))
      .mockResolvedValueOnce(subasta);

    renderAuction();

    expect(await screen.findByText("Error temporal")).toBeInTheDocument();

    fireEvent.click(screen.getByRole("button", { name: "Reintentar" }));

    expect(await screen.findByText("Michael Olise")).toBeInTheDocument();

    expect(mocks.fetchSubasta).toHaveBeenCalledTimes(2);
  });
});