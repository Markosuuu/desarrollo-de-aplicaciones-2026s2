import { describe, expect, test, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import { LineChart } from '@mui/x-charts/LineChart';
import { useTheme } from '../context/ThemeContext';
import QuoteHistoryChart from '../components/QuoteHistoryChart';

vi.mock('@mui/x-charts/LineChart', () => ({
    LineChart: vi.fn(() => <div data-testid="line-chart" />),
}));

vi.mock('../context/ThemeContext', () => ({
    useTheme: vi.fn(),
}));

describe('QuoteHistoryChart', () => {
    const data = [
        { fecha: '2026-10-20', precio: 300 },
        { fecha: '2026-10-18', precio: 250 },
        { fecha: '2026-10-19', precio: 275 },
    ];

    beforeEach(() => {
        vi.clearAllMocks();
    });

    afterEach(() => {
        cleanup();
    });

    test('muestra un mensaje cuando no hay datos', () => {
        useTheme.mockReturnValue({ isDark: false });

        render(<QuoteHistoryChart data={[]} />);

        expect(
            screen.getByText('No hay historial de cotizaciones disponible.'),
        ).toBeTruthy();

        expect(LineChart).not.toHaveBeenCalled();
    });

    test('muestra el gráfico cuando existen datos válidos', () => {
        useTheme.mockReturnValue({ isDark: false });

        render(<QuoteHistoryChart data={data} />);

        expect(screen.getByTestId('line-chart')).toBeTruthy();
        expect(LineChart).toHaveBeenCalledOnce();
    });

    test('filtra datos inválidos y ordena las cotizaciones por fecha', () => {
        useTheme.mockReturnValue({ isDark: false });

        const input = [
            { fecha: '2026-10-20', precio: 300 },
            { fecha: '2026-10-18', precio: 250 },
            { fecha: '2026-10-19', precio: 275 },
            { fecha: '2026-10-21', precio: 'invalido' },
            { fecha: '', precio: 400 },
            { fecha: '2026-10-22', precio: 350 },
            { fecha: 'fecha-invalida', precio: 500 },
        ];

        render(<QuoteHistoryChart data={input} />);

        const props = LineChart.mock.calls[0][0];
        const points = props.dataset;

        expect(points).toHaveLength(4);

        expect(points.map((point) => point.precio)).toEqual([
            250, 275, 300, 350
        ]);

        expect(points.every((point) => point.fecha instanceof Date)).toBe(true);

        expect(points.map((point) => point.fecha.getTime())).toEqual(
            [...points.map((point) => point.fecha.getTime())].sort(
                (a, b) => a - b,
            ),
        );
    });

    test('utiliza los colores correspondientes al tema claro', () => {
        useTheme.mockReturnValue({ isDark: false });

        render(<QuoteHistoryChart data={data} />);

        const props = LineChart.mock.calls[0][0];

        expect(props.series[0].color).toBe('#006e0781');
        expect(props.series[0].shape).toBe('diamond');
        expect(props.yAxis[0].tickLabelStyle.fill).toBe('#666');
        expect(props.xAxis[0].tickLabelStyle.fill).toBe('#666');
    });

    test('utiliza los colores correspondientes al tema oscuro', () => {
        useTheme.mockReturnValue({ isDark: true });

        render(<QuoteHistoryChart data={data} />);

        const props = LineChart.mock.calls[0][0];

        expect(props.series[0].color).toBe('#d219694c');
        expect(props.series[0].shape).toBe('star');
        expect(props.yAxis[0].tickLabelStyle.fill).toBe('#bdbdbd');
        expect(props.xAxis[0].tickLabelStyle.fill).toBe('#bdbdbd');
    });

    test('formatea correctamente los precios de los ejes y los puntos', () => {
        useTheme.mockReturnValue({ isDark: false });

        render(<QuoteHistoryChart data={data} />);

        const props = LineChart.mock.calls[0][0];

        const formattedAxisPrice = props.yAxis[0].valueFormatter(1234.5);
        const formattedPointPrice = props.series[0].valueFormatter(1234.5);

        expect(formattedAxisPrice).toBe('1.234,5');
        expect(formattedPointPrice).toBe('$1.234,5');
        expect(props.series[0].valueFormatter(null)).toBe('');
    });

    test('formatea las fechas del eje horizontal', () => {
        useTheme.mockReturnValue({ isDark: false });

        render(<QuoteHistoryChart data={data} />);

        const props = LineChart.mock.calls[0][0];

        const formatter = props.xAxis[0].valueFormatter;
        const date = new Date('2026-10-20T12:00:00');

        expect(formatter(date)).toBe('20/10/2026');
    });

    test('utiliza un arreglo vacío cuando no se proporciona data', () => {
        useTheme.mockReturnValue({ isDark: false });

        render(<QuoteHistoryChart />);

        expect(
            screen.getByText('No hay historial de cotizaciones disponible.'),
        ).toBeTruthy();

        expect(LineChart).not.toHaveBeenCalled();
    });
});