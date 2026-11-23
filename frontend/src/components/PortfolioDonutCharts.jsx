
import DonutChart from "./DonutChart";
import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { fetchInventoryTokensSummary } from "../api/userApi";

const currencyFormatter = new Intl.NumberFormat('es-AR', {
    style: 'currency',
    currency: 'ARS',
    maximumFractionDigits: 0,
});

const portfolioMockData = [
    {
        jugadorId: '1',
        jugadorNombre: 'Lionel Messi',
        cantidadTokens: 10,
        precioUnitario: 5,
    },
    {
        jugadorId: '2',
        jugadorNombre: 'Raphinha',
        cantidadTokens: 8,
        precioUnitario: 10,
    },
    {
        jugadorId: '3',
        jugadorNombre: 'Neymar',
        cantidadTokens: 15,
        precioUnitario: 10,
    },
    {
        jugadorId: '4',
        jugadorNombre: 'Mussi',
        cantidadTokens: 20,
        precioUnitario: 10,
    },
    {
        jugadorId: '5',
        jugadorNombre: 'Riquelme',
        cantidadTokens: 12,
        precioUnitario: 11,
    },
];


export default function PortfolioDonutCharts() {
    const { token } = useAuth();

    const [summary, setSummary] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [retryCount, setRetryCount] = useState(0);

    useEffect(() => {
        let active = true;

        const cargarResumen = async () => {
            setLoading(true);
            setError(null);

            try {
                const response = await fetchInventoryTokensSummary(token);

                if (active) {
                    setSummary(response);
                }
            } catch (err) {
                if (active) {
                    setError(
                        err.message ||
                        "No se pudo cargar el resumen del inventario."
                    );
                }
            } finally {
                if (active) {
                    setLoading(false);
                }
            }
        };

        void cargarResumen();

        return () => {
            active = false;
        };
    }, [token, retryCount]);

    if (loading) {
        return <p>Cargando resumen...</p>;
    }

    if (error) {
        return (
            <div className="portfolio-charts-error">
                <p>{error}</p>

                <button
                    type="button"
                    onClick={() => setRetryCount((previous) => previous + 1)}
                >
                    Reintentar
                </button>
            </div>
        );
    }

    const tokensData = summary.jugadores.map((jugador) => ({
        id: jugador.jugadorId,
        label: jugador.jugadorNombre,
        value: jugador.cantidad,
    }));

    const salesData = portfolioMockData.map((jugador) => ({
        id: jugador.jugadorId,
        label: jugador.jugadorNombre,
        value: jugador.cantidadTokens * jugador.precioUnitario,
    }));

    const totalSales = salesData.reduce(
        (total, jugador) => total + jugador.value,
        0
    );

    return (
        <div style={{
            display: 'flex', 
            flexDirection: 'column', 
            gap: '15px', 
            paddingLeft: '15px', 
            borderLeft: '1px solid #dedede'
        }}>
            <DonutChart
                data={tokensData}
                centerValue={summary.total}
                centerLabel="Tokens totales"
            />

            <DonutChart
                data={salesData}
                centerValue={currencyFormatter.format(totalSales)}
                centerLabel="Valor total"
                formatValue={(value) =>
                    currencyFormatter.format(value)
                }
            />
        </div>
    );
}
