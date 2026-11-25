import { describe, test, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";

import PortfolioDonutCharts from "../components/PortfolioDonutCharts";
import { useAuth } from "../context/AuthContext";
import {
    fetchInventoryTokensSummary,
    fetchInventoryMoneySummary,
} from "../api/userApi";

vi.mock("../context/AuthContext", () => ({
    useAuth: vi.fn(),
}));

vi.mock("../api/userApi", () => ({
    fetchInventoryTokensSummary: vi.fn(),
    fetchInventoryMoneySummary: vi.fn(),
}));

vi.mock("../components/DonutChart", () => ({
    default: ({ title, data, centerValue, centerLabel, formatValue }) => (
        <div data-testid="donut-chart">
            <h3>{title}</h3>
            <span>{centerValue}</span>
            <span>{centerLabel}</span>

            {data.map((jugador) => (
                <div key={jugador.id}>
                    <span>{jugador.label}</span>
                    <span>
                        {formatValue
                            ? formatValue(jugador.value)
                            : jugador.value}
                    </span>
                </div>
            ))}
        </div>
    ),
}));

describe("PortfolioDonutCharts", () => {
    const mockTokensSummary = {
        total: 33,
        jugadores: [
            {
                jugadorId: "1",
                jugadorNombre: "Lionel Messi",
                cantidad: 10,
            },
            {
                jugadorId: "2",
                jugadorNombre: "Raphinha",
                cantidad: 8,
            },
            {
                jugadorId: "3",
                jugadorNombre: "Neymar",
                cantidad: 15,
            },
        ],
    };

    const mockMoneySummary = {
        total: 25000.50,
        jugadores: [
            {
                jugadorId: "1",
                jugadorNombre: "Lionel Messi",
                cantidad: 10,
                valor: 15000.25,
            },
            {
                jugadorId: "2",
                jugadorNombre: "Raphinha",
                cantidad: 8,
                valor: 5000.15,
            },
            {
                jugadorId: "3",
                jugadorNombre: "Neymar",
                cantidad: 15,
                valor: 5000.1,
            },
        ],
    };

    beforeEach(() => {
        vi.clearAllMocks();

        useAuth.mockReturnValue({
            token: "mock-token",
        });
    });

    test("muestra el estado de carga mientras obtiene ambos resúmenes", () => {
        fetchInventoryTokensSummary.mockReturnValue(new Promise(() => {}));
        fetchInventoryMoneySummary.mockReturnValue(new Promise(() => {}));

        render(<PortfolioDonutCharts />);

        expect(
            screen.getByText("Cargando resumen...")
        ).toBeInTheDocument();
    });

    test("obtiene ambos resúmenes utilizando el token del usuario", async () => {
        fetchInventoryTokensSummary.mockResolvedValue(mockTokensSummary);
        fetchInventoryMoneySummary.mockResolvedValue(mockMoneySummary);

        render(<PortfolioDonutCharts />);

        await waitFor(() => {
            expect(fetchInventoryTokensSummary).toHaveBeenCalledWith(
                "mock-token"
            );

            expect(fetchInventoryMoneySummary).toHaveBeenCalledWith(
                "mock-token"
            );
        });
    });

    test("renderiza ambos gráficos con sus respectivos datos", async () => {
        fetchInventoryTokensSummary.mockResolvedValue(mockTokensSummary);
        fetchInventoryMoneySummary.mockResolvedValue(mockMoneySummary);

        render(<PortfolioDonutCharts />);

        const charts = await screen.findAllByTestId("donut-chart");

        expect(charts).toHaveLength(2);

        expect(screen.getByText("Tokens totales")).toBeInTheDocument();
        expect(screen.getByText("33")).toBeInTheDocument();

        expect(screen.getByText("Valor total")).toBeInTheDocument();
        expect(screen.getByText("$ 25.000,50")).toBeInTheDocument();
    });

    test("muestra un mensaje de error cuando falla la consulta de tokens", async () => {
        fetchInventoryTokensSummary.mockRejectedValue(
            new Error("Error al obtener los tokens.")
        );
        fetchInventoryMoneySummary.mockResolvedValue(mockMoneySummary);

        render(<PortfolioDonutCharts />);

        expect(
            await screen.findByText("Error al obtener los tokens.")
        ).toBeInTheDocument();

        expect(
            screen.getByRole("button", { name: "Reintentar" })
        ).toBeInTheDocument();

        expect(screen.queryByTestId("donut-chart")).not.toBeInTheDocument();
    });

    test("muestra un mensaje de error cuando falla la consulta de dinero", async () => {
        fetchInventoryTokensSummary.mockResolvedValue(mockTokensSummary);
        fetchInventoryMoneySummary.mockRejectedValue(
            new Error("Error al obtener el dinero.")
        );

        render(<PortfolioDonutCharts />);

        expect(
            await screen.findByText("Error al obtener el dinero.")
        ).toBeInTheDocument();

        expect(
            screen.getByRole("button", { name: "Reintentar" })
        ).toBeInTheDocument();

        expect(screen.queryByTestId("donut-chart")).not.toBeInTheDocument();
    });

    test("permite reintentar ambas consultas después de un error", async () => {
        fetchInventoryTokensSummary
            .mockRejectedValueOnce(
                new Error("Error al obtener los tokens.")
            )
            .mockResolvedValueOnce(mockTokensSummary);

        fetchInventoryMoneySummary
            .mockResolvedValueOnce(mockMoneySummary)
            .mockResolvedValueOnce(mockMoneySummary);

        render(<PortfolioDonutCharts />);

        fireEvent.click(
            await screen.findByRole("button", {
                name: "Reintentar",
            })
        );

        await waitFor(() => {
            expect(fetchInventoryTokensSummary).toHaveBeenCalledTimes(2);
            expect(fetchInventoryMoneySummary).toHaveBeenCalledTimes(2);
        });

        expect(await screen.findAllByTestId("donut-chart")).toHaveLength(2);
    });
});
