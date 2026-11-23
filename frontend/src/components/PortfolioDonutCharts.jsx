
import DonutChart from "./DonutChart";

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
    const tokensData = portfolioMockData.map((jugador) => ({
        id: jugador.jugadorId,
        label: jugador.jugadorNombre,
        value: jugador.cantidadTokens,
    }));

    const totalTokens = tokensData.reduce(
        (total, jugador) => total + jugador.value,
        0
    );

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
                centerValue={totalTokens}
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
