import { afterEach, describe, expect, test, vi } from "vitest";
import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import DatosJugador from "../components/subasta/DatosJugador";
import EstadisticasJugador from "../components/subasta/EstadisticasJugador";
import HistorialOfertas from "../components/subasta/HistorialOfertas";
import InformacionSubasta from "../components/subasta/InformacionSubasta";
import RealizarOferta from "../components/subasta/RealizarOferta";

afterEach(() => {
  cleanup();
});

describe("DatosJugador", () => {
  test("muestra valores alternativos cuando faltan datos del jugador", () => {
    render(<DatosJugador />);

    expect(screen.getByText("- años")).toBeInTheDocument();
    expect(screen.getByText("- cm")).toBeInTheDocument();
    expect(screen.getByText("- kg")).toBeInTheDocument();
  });

  test("muestra los datos disponibles del jugador", () => {
    render(
      <DatosJugador
        jugador={{ edad: 24, altura: 180, peso: 78, tokensRestantes: 98 }}
      />,
    );

    expect(screen.getByText("24 años")).toBeInTheDocument();
    expect(screen.getByText("180 cm")).toBeInTheDocument();
    expect(screen.getByText("78 kg")).toBeInTheDocument();
    expect(screen.getByText("98")).toBeInTheDocument();
  });
});

describe("EstadisticasJugador", () => {
  test("muestra valores alternativos cuando faltan estadísticas", () => {
    render(<EstadisticasJugador />);

    expect(screen.getAllByText("-")).toHaveLength(3);
    expect(screen.getAllByText("No disponible")).toHaveLength(5);
  });

  test("formatea los datos numéricos disponibles", () => {
    render(
      <EstadisticasJugador
        jugador={{
          goles: 6,
          asistencias: 3,
          cotizacion: 5.21,
          disparosPorPartido: 4.38,
          keyPassesPorPartido: 3.63,
          dribblesGanadosPorPartido: 2.13,
          faltasCometidasPorPartido: 2.25,
          minutosJugados: 657,
        }}
      />,
    );

    expect(screen.getByText("6")).toBeInTheDocument();
    expect(screen.getByText("3")).toBeInTheDocument();
    expect(screen.getByText("657")).toBeInTheDocument();
    expect(screen.getAllByText("5,21")).toHaveLength(1);
  });
});

describe("HistorialOfertas", () => {
  test("muestra el estado vacío cuando no se pasan ofertas", () => {
    render(<HistorialOfertas />);

    expect(
      screen.getByText("Todavía no hubo ofertas para esta subasta."),
    ).toBeInTheDocument();
  });

  test("acepta la propiedad ofertas y reemplaza una lista no válida por una vacía", () => {
    const { rerender } = render(
      <HistorialOfertas
        ofertasPrevias={[{ id: "ignored" }]}
        ofertas="inválidas"
      />,
    );

    expect(
      screen.getByText("Todavía no hubo ofertas para esta subasta."),
    ).toBeInTheDocument();

    rerender(
      <HistorialOfertas
        ofertas={[
          {
            id: "offer-1",
            usuario: { nombre: "Ana" },
            puja: 10,
            timestamp: "2026-11-04T14:30:00",
            estado: true,
          },
        ]}
      />,
    );

    expect(screen.getByText("Ana")).toBeInTheDocument();
    expect(screen.getByText("Activa")).toBeInTheDocument();
    expect(screen.getByText("10,00")).toBeInTheDocument();
    expect(screen.getByText(/04\/11\/2026/)).toBeInTheDocument();
  });

  test("muestra datos alternativos para ofertas incompletas o con fecha inválida", () => {
    render(
      <HistorialOfertas
        ofertasPrevias={[
          { id: "offer-1", puja: null, timestamp: null, estado: false },
          {
            id: "offer-2",
            puja: 1.5,
            timestamp: "fecha-invalida",
            estado: false,
          },
        ]}
      />,
    );

    expect(screen.getAllByText("Desconocido")).toHaveLength(2);
    expect(screen.getByText("Sin fecha")).toBeInTheDocument();
    expect(screen.getByText("Fecha inválida")).toBeInTheDocument();
    expect(screen.getAllByText("Superada")).toHaveLength(2);
    expect(screen.getByText("No disponible")).toBeInTheDocument();
  });
});

describe("InformacionSubasta", () => {
  test("muestra valores alternativos cuando faltan datos", () => {
    render(<InformacionSubasta />);

    expect(screen.getByText("Desconocido")).toBeInTheDocument();
    expect(screen.getAllByText("No disponible")).toHaveLength(1);
    expect(screen.getByText("Sin ofertas")).toBeInTheDocument();
    expect(screen.getByText("-")).toBeInTheDocument();
    expect(screen.getByText("Sin fecha")).toBeInTheDocument();
  });

  test("muestra los datos proporcionados y una fecha inválida", () => {
    render(
      <InformacionSubasta
        propietario={{ nombre: "Ana" }}
        jugador={{ cotizacion: 5.21 }}
        subasta={{
          ofertaActual: 2.5,
          cantidadOfrecida: 3,
          vencimiento: "fecha-invalida",
        }}
      />,
    );

    expect(screen.getByText("Ana")).toBeInTheDocument();
    expect(screen.getByText("5,21")).toBeInTheDocument();
    expect(screen.getByText("2,50")).toBeInTheDocument();
    expect(screen.getByText("3")).toBeInTheDocument();
    expect(screen.getByText("Fecha inválida")).toBeInTheDocument();
  });
});

describe("RealizarOferta", () => {
  const props = {
    showOfferForm: false,
    setShowOfferForm: vi.fn(),
    handleOffer: vi.fn(),
    subasta: { ofertaActual: 2.5 },
    minimumBid: 2.51,
    puja: "",
    setPuja: vi.fn(),
    offering: false,
    setActionError: vi.fn(),
  };

  test("abre el formulario y permite cancelar la oferta", () => {
    const { rerender } = render(<RealizarOferta {...props} />);

    fireEvent.click(
      screen.getByRole("button", { name: "Realizar una oferta" }),
    );
    expect(props.setShowOfferForm).toHaveBeenCalledWith(true);

    rerender(<RealizarOferta {...props} showOfferForm />);
    fireEvent.click(screen.getByRole("button", { name: "Cancelar" }));

    expect(props.setShowOfferForm).toHaveBeenCalledWith(false);
    rerender(<RealizarOferta {...props} showOfferForm={false} />);
  });

  test("muestra el estado sin ofertas, actualiza la puja y deshabilita acciones al enviar", () => {
    const { rerender } = render(
      <RealizarOferta
        {...props}
        showOfferForm
        subasta={{}}
        minimumBid={0.01}
        offering
        puja="3"
      />,
    );

    expect(screen.getByText("Sin ofertas")).toBeInTheDocument();
    expect(
      screen.getByRole("button", { name: "Enviando oferta..." }),
    ).toBeDisabled();
    expect(screen.getByRole("button", { name: "Cancelar" })).toBeDisabled();
    expect(screen.getByLabelText("Tu oferta")).toBeDisabled();

    rerender(
      <RealizarOferta
        {...props}
        showOfferForm
        subasta={{}}
        minimumBid={0.01}
        puja="3"
      />,
    );
    fireEvent.change(screen.getByLabelText("Tu oferta"), {
      target: { value: "4" },
    });

    expect(props.setPuja).toHaveBeenCalledWith("4");
    expect(screen.getByText("0,01")).toBeInTheDocument();

    rerender(
      <RealizarOferta
        {...props}
        showOfferForm
        minimumBid={null}
        subasta={{}}
      />,
    );
    expect(screen.getByText("No disponible")).toBeInTheDocument();
  });
});
