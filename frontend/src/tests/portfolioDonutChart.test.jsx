
import { describe, test, expect, vi, beforeEach } from "vitest";
import { render, screen, waitFor, fireEvent } from "@testing-library/react";

import PortfolioDonutCharts from "../components/PortfolioDonutCharts";
import { useAuth } from "../context/AuthContext";
import { fetchInventoryTokensSummary } from "../api/userApi";

vi.mock("../context/AuthContext", () => ({
    useAuth: vi.fn(),
}));

vi.mock("../api/userApi", () => ({
    fetchInventoryTokensSummary: vi.fn(),
}));

vi.mock("../components/DonutChart", () => ({
    default: ({ title, data, centerValue, centerLabel }) => (
        <div data-testid="donut-chart">
            <h3>{title}</h3>
            <span>{centerValue}</span>
            <span>{centerLabel}</span>

            {data.map((jugador) => (
                <div key={jugador.id}>
                    <span>{jugador.label}</span>
                    <span>{jugador.value}</span>
                </div>
            ))}
        </div>
    ),
}));

describe("PortfolioDonutCharts", () => {
    const mockSummary = {
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

    beforeEach(() => {
        vi.clearAllMocks();

        useAuth.mockReturnValue({
            token: "mock-token",
        });
    });

    test("muestra el estado de carga mientras obtiene el resumen", () => {
        fetchInventoryTokensSummary.mockReturnValue(
            new Promise(() => {})
        );

        render(<PortfolioDonutCharts />);
        

        expect(
            screen.getByText("Cargando resumen...")
        ).toBeInTheDocument();
    });

    test("obtiene el resumen utilizando el token del usuario", async () => {
        fetchInventoryTokensSummary.mockResolvedValue(mockSummary);

        render(<PortfolioDonutCharts />);

        await waitFor(() => {
            expect(fetchInventoryTokensSummary).toHaveBeenCalledWith(
                "mock-token"
            );
        });
    });

    test("renderiza el gráfico con los datos recibidos", async () => {
        fetchInventoryTokensSummary.mockResolvedValue(mockSummary);

        render(<PortfolioDonutCharts />);

        await waitFor(() => {
            expect(fetchInventoryTokensSummary).toHaveBeenCalledWith(
                "mock-token"
            );
        });

        expect(screen.getByText("33")).toBeInTheDocument();
        expect(screen.getByText("Tokens totales")).toBeInTheDocument();
    });

    test("muestra un mensaje de error cuando falla la consulta", async () => {
        fetchInventoryTokensSummary.mockRejectedValue(
            new Error("Error al obtener el inventario.")
        );

        render(<PortfolioDonutCharts />);

        expect(
            await screen.findByText(
                "Error al obtener el inventario."
            )
        ).toBeInTheDocument();

        expect(
            screen.getByRole("button", { name: "Reintentar" })
        ).toBeInTheDocument();
    });

    test("permite reintentar la consulta después de un error", async () => {
        fetchInventoryTokensSummary
            .mockRejectedValueOnce(
                new Error("Error al obtener el inventario.")
            )
            .mockResolvedValueOnce(mockSummary);

        render(<PortfolioDonutCharts />);

        fireEvent.click(
            await screen.findByRole("button", {
                name: "Reintentar",
            })
        );

        expect(fetchInventoryTokensSummary).toHaveBeenCalledTimes(2);
    });
});
