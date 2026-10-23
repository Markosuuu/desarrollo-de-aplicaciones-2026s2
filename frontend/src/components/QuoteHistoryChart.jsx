
import { LineChart } from '@mui/x-charts/LineChart';
import { useTheme } from '../context/ThemeContext';

const formatPrice = (price) =>
    new Intl.NumberFormat('es-AR', {
        maximumFractionDigits: 2,
    }).format(price);

export default function QuoteHistoryChart({ data = [] }) {
    const { isDark } = useTheme();

    const points = data
        .filter((item) => item.fecha && Number.isFinite(Number(item.precio)))
        .map((item) => ({
            fecha: new Date(`${item.fecha}`.length === 10
                ? `${item.fecha}T12:00:00`
                : item.fecha),
            precio: Number(item.precio),
        }))
        .filter((item) => !Number.isNaN(item.fecha.getTime()))
        .sort((a, b) => a.fecha - b.fecha);

    if (points.length === 0) {
        return <p>No hay historial de cotizaciones disponible.</p>;
    }

    return (
        <div sx={{'width': '100%'}}>
            <LineChart
                dataset={points}
                height={300}
                width={undefined}
                xAxis={[
                    {
                        dataKey: 'fecha',
                        scaleType: 'time',
                        valueFormatter: (date) =>
                            date.toLocaleDateString('es-AR', {
                                day: '2-digit',
                                month: '2-digit',
                                year: 'numeric',
                            }),
                        tickLabelStyle: {
                            fill: isDark ? '#bdbdbd' : '#666',
                        },
                    },
                ]}
                yAxis={[
                    {
                        valueFormatter: (value) => formatPrice(value),
                        tickLabelStyle: {
                            fill: isDark ? '#bdbdbd' : '#666',
                        },
                    },
                ]}
                series={[
                    {
                        type: 'line',
                        dataKey: 'precio',
                        label: 'Historial de cotizaciones',
                        color: isDark ? "#d219694c" : '#006e0781',
                        area: true,
                        showMark: true,
                        shape:isDark ? 'star' : 'diamond',
                        curve: 'linear',
                        valueFormatter: (value) =>
                            value == null ? '' : `$${formatPrice(value)}`,
                    },
                ]}
                grid={{ horizontal: true }}
                slotProps={{
                    legend: { hidden: true },
                }}
                sx={{
                    '& .MuiAreaElement-series-auto-generated-id-0': {
                        fillOpacity: isDark ? 0.25 : 0.12,
                    },
                    '& .MuiChartsLegend-root': {
                        color: isDark ? '#f5f5f5' : 'black',
                        fontSize: '1rem'
                    },
                    '& .MuiChartsAxis-line, & .MuiChartsAxis-tick': {
                        stroke: isDark ? '#666' : '#ccc',
                    },
                    '& .MuiChartsAxis-tickLabel': {
                        fill: isDark ? '#bdbdbd' : '#666',
                    },
                    '& .MuiChartsGrid-line': {
                        stroke: isDark ? '#444' : '#e5e5e5',
                    },
                }}
            />
        </div>
    );
}
