
import { describe, test, expect, vi } from "vitest";
import { render, screen } from "@testing-library/react";
import DonutChart from "../components/DonutChart";

vi.mock("@mui/x-charts/PieChart", () => ({
    PieChart: ({ children, skipAnimation, series }) => (
        <div
            data-testid="pie-chart"
            data-skip-animation={String(skipAnimation)}
            data-series={JSON.stringify(series)}
        >
            {children}
        </div>
    ),
}));

vi.mock("@mui/x-charts/hooks", () => ({
    useDrawingArea: () => ({
        width: 300,
        height: 260,
        left: 0,
        top: 0,
    }),
}));

describe("DonutChart", () => {
    const mockData = [
        { id: "1", label: "Messi", value: 10 },
        { id: "2", label: "Raphinha", value: 8 },
        { id: "3", label: "Neymar", value: 15 },
    ];

    test("renderiza el título cuando se proporciona", () => {
        render(
            <DonutChart
                title="Mis tokens"
                data={mockData}
                centerValue={33}
                centerLabel="Tokens totales"
            />
        );

        expect(
            screen.getByRole("heading", { name: "Mis tokens" })
        ).toBeInTheDocument();
    });

    test("renderiza el valor y la etiqueta del centro", () => {
        render(
            <DonutChart
                data={mockData}
                centerValue={33}
                centerLabel="Tokens totales"
            />
        );

        expect(screen.getByText("33")).toBeInTheDocument();
        expect(
            screen.getByText("Tokens totales")
        ).toBeInTheDocument();
    });

    test("renderiza el gráfico con los datos recibidos", () => {
        render(
            <DonutChart
                data={mockData}
                centerValue={33}
                centerLabel="Tokens totales"
            />
        );

        const chart = screen.getByTestId("pie-chart");
        const series = JSON.parse(
            chart.getAttribute("data-series")
        );

        expect(series[0].data).toEqual(mockData);
        expect(series[0].innerRadius).toBe(60);
        expect(series[0].outerRadius).toBe(99);
    });

    test("desactiva las animaciones del gráfico", () => {
        render(
            <DonutChart
                data={mockData}
                centerValue={33}
                centerLabel="Tokens totales"
            />
        );

        expect(
            screen.getByTestId("pie-chart")
        ).toHaveAttribute("data-skip-animation", "true");
    });

    test("muestra un mensaje cuando no hay datos", () => {
        render(
            <DonutChart
                data={[]}
                centerValue={0}
                centerLabel="Tokens totales"
            />
        );

        expect(
            screen.getByText("No hay datos disponibles.")
        ).toBeInTheDocument();

        expect(
            screen.queryByTestId("pie-chart")
        ).not.toBeInTheDocument();
    });

    test("filtra los jugadores que tienen valor cero", () => {
        render(
            <DonutChart
                data={[
                    { id: "1", label: "Messi", value: 10 },
                    { id: "2", label: "Neymar", value: 0 },
                ]}
                centerValue={10}
                centerLabel="Tokens totales"
            />
        );

        const chart = screen.getByTestId("pie-chart");
        const series = JSON.parse(
            chart.getAttribute("data-series")
        );

        expect(series[0].data).toHaveLength(1);
        expect(series[0].data[0].label).toBe("Messi");
    });
});
