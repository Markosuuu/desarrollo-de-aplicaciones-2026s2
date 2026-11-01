import { describe, expect, test, vi, beforeEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import { RadarChart } from '@mui/x-charts';
import { useTheme } from '../context/ThemeContext';
import PlayerRadar from '../components/PlayerRadarStats';

vi.mock('@mui/x-charts', () => ({
    RadarChart: vi.fn(() => <div data-testid="radar-chart" />),
}));

vi.mock('../context/ThemeContext', () => ({
    useTheme: vi.fn(),
}));

describe('PlayerRadar', () => {
    const stats = {
        disparos: 80,
        pases: 75,
        regates: 90,
        faltas: 30,
        goles: 60,
        asist: 50,
        max: 100,
    };

    beforeEach(() => {
        cleanup();
        vi.clearAllMocks();
    });

    test('renderiza el gráfico de estadísticas', () => {
        useTheme.mockReturnValue({ isDark: false });

        render(<PlayerRadar {...stats} />);

        expect(screen.getByTestId('radar-chart')).toBeTruthy();
        expect(RadarChart).toHaveBeenCalledOnce();
    });

    test('pasa correctamente las estadísticas al gráfico', () => {
        useTheme.mockReturnValue({ isDark: false });

        render(<PlayerRadar {...stats} />);

        const props = RadarChart.mock.calls[0][0];

        expect(props.radar.max).toBe(100);
        expect(props.series[0].data).toEqual([
            80, 75, 90, 30, 50, 60,
        ]);
        expect(props.radar.metrics).toEqual([
            'Goles',
            'Asistencias',
            '%-Disparos',
            '%-Pases',
            '%-Regates',
            '%-Faltas',
        ]);
    });

    test('utiliza los colores del tema claro', () => {
        useTheme.mockReturnValue({ isDark: false });

        render(<PlayerRadar {...stats} />);

        const props = RadarChart.mock.calls[0][0];

        expect(props.colors).toEqual(['#d21969']);
        expect(props.sx.color).toBe('#212121');
        expect(props.sx['& text'].fill).toBe('#212121');
    });

    test('utiliza los colores del tema oscuro', () => {
        useTheme.mockReturnValue({ isDark: true });

        render(<PlayerRadar {...stats} />);

        const props = RadarChart.mock.calls[0][0];

        expect(props.colors).toEqual(['#61b4f8']);
        expect(props.sx.color).toBe('#fa0606');
        expect(props.sx['& text'].fill).toBe('#ffffff');
    });
});