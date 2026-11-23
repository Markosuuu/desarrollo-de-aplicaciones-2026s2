import DonutChart from "./DonutChart";
import { useEffect, useState } from "react";
import { useAuth } from "../context/AuthContext";
import { fetchInventoryTokensSummary, fetchInventoryMoneySummary } from "../api/userApi";

const currencyFormatter = new Intl.NumberFormat("es-AR", {
    style: "currency",
    currency: "ARS",
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
});

export default function PortfolioDonutCharts() {
    const { token } = useAuth();

    const [tokensSummary, setTokensSummary] = useState(null);
    const [moneySummary, setMoneySummary] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [retryCount, setRetryCount] = useState(0);

    useEffect(() => {
        let active = true;

        const cargarResumenes = async () => {
            setLoading(true);
            setError(null);

            try {
                const [tokensResponse, moneyResponse] = await Promise.all([
                    fetchInventoryTokensSummary(token),
                    fetchInventoryMoneySummary(token),
                ]);

                if (active) {
                    setTokensSummary(tokensResponse);
                    setMoneySummary(moneyResponse);
                }
            } catch (err) {
                if (active) {
                    setError(
                        err.message ||
                        "No se pudieron cargar los resúmenes del inventario."
                    );
                }
            } finally {
                if (active) {
                    setLoading(false);
                }
            }
        };

        void cargarResumenes();

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

    const tokensData = tokensSummary.jugadores.map((jugador) => ({
        id: jugador.jugadorId,
        label: jugador.jugadorNombre,
        value: jugador.cantidad,
    }));

    const moneyData = moneySummary.jugadores.map((jugador) => ({
        id: jugador.jugadorId,
        label: jugador.jugadorNombre,
        value: Number(jugador.valor),
    }));

    return (
        moneyData.length === 0 && tokensData.length === 0 ? (
            <></>
        ) : (
            <div style={{
                display: "flex",
                flexDirection: "column",
                gap: "15px",
                paddingLeft: "15px",
                borderLeft: "1px solid #dedede",
            }}>
                <DonutChart
                    data={tokensData}
                    centerValue={tokensSummary.total}
                    centerLabel="Tokens totales"
                />

                <DonutChart
                    data={moneyData}
                    centerValue={currencyFormatter.format(moneySummary.total)}
                    centerLabel="Valor total"
                    formatValue={(value) =>
                        currencyFormatter.format(value)
                    }
                />
            </div>
        )
    );

}