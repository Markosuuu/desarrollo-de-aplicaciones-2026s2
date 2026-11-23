
import { PieChart } from '@mui/x-charts/PieChart';
import { useDrawingArea } from '@mui/x-charts/hooks';
import { styled } from '@mui/material/styles';

const StyledText = styled('text')(({ theme }) => ({
    fill: theme.palette.text.primary,
    textAnchor: 'middle',
    dominantBaseline: 'central',
}));

function PieCenterLabel({ value, label }) {
    const { width, height, left, top } = useDrawingArea();

    const centerX = left + width / 2;
    const centerY = top + height / 2;

    return (
        <StyledText x={centerX} y={centerY}>
            <tspan
                x={centerX}
                dy="-0.3em"
                fontSize={19}
                fontWeight={600}
            >
                {value}
            </tspan>

            <tspan
                x={centerX}
                dy="1.8em"
                fontSize={12}
                fill="gray"
            >
                {label}
            </tspan>
        </StyledText>
    );
}

export default function DonutChart({
    title,
    data,
    centerValue,
    centerLabel,
    formatValue = (value) =>
        value.toLocaleString('es-AR'),
}) {
    const validData = data.filter(
        (item) => item.value > 0
    );

    return (
        <section className="donut-chart">
            {title && (
                <h3 className="donut-chart-title">
                    {title}
                </h3>
            )}

            {validData.length === 0 ? (
                <p>No hay datos disponibles.</p>
            ) : (
                <PieChart
                    width={200}
                    height={200}
                    skipAnimation
                    series={[
                        {
                            data: validData,
                            innerRadius: 60,
                            outerRadius: 99,
                            paddingAngle: 1,
                            cornerRadius: 5,
                            valueFormatter: ({ value }) =>
                                formatValue(value),
                        },
                    ]}
                    hideLegend
                >
                    <PieCenterLabel
                        value={centerValue}
                        label={centerLabel}
                    />
                </PieChart>
            )}
        </section>
    );
}
