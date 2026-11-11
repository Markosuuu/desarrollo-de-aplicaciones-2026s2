import { describe, expect, test, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter, Routes, Route } from 'react-router-dom';

import PlayerPage from '../pages/PlayerPage';

const mocks = vi.hoisted(() => ({
    fetchPlayer: vi.fn(),
    fetchPlayerQuotesHistory: vi.fn(),
    comprarSuper: vi.fn(),
    useAuth: vi.fn(),
    PlayerRadar: vi.fn(),
    QuoteHistoryChart: vi.fn(),
    toastError: vi.fn(),
    toastSuccess: vi.fn(),
}));

vi.mock('../api/marketApi', () => ({
    comprarSuper: mocks.comprarSuper,
}));

vi.mock('../api/playerApi', () => ({
    fetchPlayer: mocks.fetchPlayer,
    fetchPlayerQuotesHistory: mocks.fetchPlayerQuotesHistory,
}));

vi.mock('../context/AuthContext', () => ({
    useAuth: mocks.useAuth,
}));

vi.mock('../components/PlayerRadarStats', () => ({
    default: mocks.PlayerRadar,
}));

vi.mock('../components/QuoteHistoryChart', () => ({
    default: mocks.QuoteHistoryChart,
}));

vi.mock('react-toastify', () => ({
    toast: {
        error: mocks.toastError,
        success: mocks.toastSuccess,
    },
}));

const jugadorPrueba = {
    id: 1,
    nombre: 'Lionel Messi',
    equipo: 'Inter Miami',
    posicion: 'Delantero',
    edad: 39,
    altura: 170,
    peso: 72,
    minutosJugados: 1200,

    disparosPorPartido: 3,
    keyPassesPorPartido: 2,
    dribblesGanadosPorPartido: 4,
    faltasCometidasPorPartido: 1,
    goles: 10,
    asistencias: 5,

    cotizacion: 250,
    tokensRestantes: 100,
    fechaActualizacion: '2026-10-19T12:00:00',
};

function renderPlayerPage(id = '1') {
    return render(
        <MemoryRouter initialEntries={[`/player/${id}`]}>
            <Routes>
                <Route path="/player/:id" element={<PlayerPage />} />
                <Route path="/profile" element={<div>Perfil</div>} />
            </Routes>
        </MemoryRouter>
    );
}

beforeEach(() => {
    vi.clearAllMocks();

    mocks.useAuth.mockReturnValue({
        token: 'token-prueba',
    });

    mocks.fetchPlayer.mockResolvedValue(jugadorPrueba);

    mocks.fetchPlayerQuotesHistory.mockResolvedValue([]);
    mocks.comprarSuper.mockResolvedValue({ success: true });

    mocks.PlayerRadar.mockImplementation(() => (
        <div data-testid="player-radar" />
    ));

    mocks.QuoteHistoryChart.mockImplementation(() => (
        <div data-testid="quote-history-chart" />
    ));
});

// Test de carga inicial
describe('PlayerPage - carga', () => {
    test('muestra el mensaje de carga inicialmente', () => {
        mocks.fetchPlayer.mockReturnValue(new Promise(() => {}));

        renderPlayerPage();

        expect(screen.getByText('Cargando jugador...')).toBeTruthy();
    });

    test('llama a fetchPlayer con el id y el token', async () => {
        renderPlayerPage('15');

        await waitFor(() => {
            expect(mocks.fetchPlayer).toHaveBeenCalledWith(
                '15',
                'token-prueba'
            );
        });
    });

    test('muestra la información del jugador cuando la petición tiene éxito', async () => {
        renderPlayerPage();

        expect(await screen.findByText('Lionel Messi')).toBeTruthy();
        expect(screen.getByText('Equipo: Inter Miami')).toBeTruthy();
    });
});

// Tests de renderizado de la informacion del jugador
describe('PlayerPage - información', () => {
    test('muestra los datos personales del jugador', async () => {
        renderPlayerPage();

        expect(await screen.findByText('Lionel Messi')).toBeTruthy();
        expect(screen.getByText('Delantero')).toBeTruthy();
        expect(screen.getByText('39 años')).toBeTruthy();
        expect(screen.getByText('170 cm')).toBeTruthy();
        expect(screen.getByText('72 kg')).toBeTruthy();
        expect(screen.getByText('1200')).toBeTruthy();
    });

    test('muestra los datos de cotización', async () => {
        renderPlayerPage();

        expect(await screen.findByText('Cotización actual:')).toBeTruthy();
        expect(screen.getByText('250')).toBeTruthy();

        expect(screen.getByText('Tokens restantes:')).toBeTruthy();
        expect(screen.getByText('100')).toBeTruthy();

        expect(screen.getByText('19/10/2026')).toBeTruthy();
    });

    test('muestra Sin datos cuando no existe fecha de actualización', async () => {
        mocks.fetchPlayer.mockResolvedValue({
            ...jugadorPrueba,
            fechaActualizacion: null,
        });

        renderPlayerPage();

        expect(await screen.findByText('Sin datos')).toBeTruthy();
    });
});

// Tests de los graficos
describe('PlayerPage - gráficos', () => {
    test('renderiza los dos gráficos', async () => {
        renderPlayerPage();

        expect(await screen.findByTestId('player-radar')).toBeTruthy();
        expect(screen.getByTestId('quote-history-chart')).toBeTruthy();
    });

    test('pasa las estadísticas correctas a PlayerRadar', async () => {
        renderPlayerPage();

        await screen.findByTestId('player-radar');

        expect(mocks.PlayerRadar.mock.calls[0][0]).toEqual({
            disparos: 3,
            pases: 2,
            regates: 4,
            faltas: 1,
            goles: 10,
            asist: 5,
            max: 10,
        });
    });
});

//Tests de errores y reintentos
describe('PlayerPage - errores', () => {
    test('muestra el mensaje de error cuando falla la petición', async () => {
        const consoleError = vi
            .spyOn(console, 'error')
            .mockImplementation(() => {});

        mocks.fetchPlayer.mockRejectedValueOnce(
            new Error('Error interno del servidor')
        );

        renderPlayerPage();

        expect(
            await screen.findByText('No se pudo cargar el jugador')
        ).toBeTruthy();

        expect(screen.getByRole('button', {
            name: 'Reintentar',
        })).toBeTruthy();

        consoleError.mockRestore();
    });

    test('vuelve a realizar la petición al presionar Reintentar', async () => {
        const consoleError = vi
            .spyOn(console, 'error')
            .mockImplementation(() => {});

        mocks.fetchPlayer.mockRejectedValueOnce(
            new Error('Error interno del servidor')
        );

        renderPlayerPage();

        fireEvent.click(
            await screen.findByRole('button', {
                name: 'Reintentar',
            })
        );

        expect(await screen.findByText('Lionel Messi')).toBeTruthy();

        expect(mocks.fetchPlayer).toHaveBeenCalledTimes(2);

        consoleError.mockRestore();
    });
    test('muestra un mensaje cuando la API no devuelve un jugador', async () => {
        mocks.fetchPlayer.mockResolvedValue(null);

        renderPlayerPage();

        expect(
            await screen.findByText('No se encontró el jugador.')
        ).toBeTruthy();
    });
});

describe('PlayerPage - compra de tokens', () => {
    test('abre el diálogo, formatea los importes y reinicia la cantidad al volver a abrirlo', async () => {
        mocks.useAuth.mockReturnValue({
            token: 'token-prueba',
            user: { creditosDisponibles: 12345.5 },
        });
        mocks.fetchPlayer.mockResolvedValue({
            ...jugadorPrueba,
            cotizacion: 1234.56,
            tokensRestantes: 5,
        });

        renderPlayerPage();

        fireEvent.click(await screen.findByRole('button', { name: 'Comprar' }));

        const quantity = screen.getByLabelText('Cantidad (máximo 5)');
        const total = () =>
            screen.getByText(/créditos$/, {
                selector: '.offer-buy-total strong',
            });
        expect(quantity).toHaveValue(1);
        expect(total().textContent.replace(/\s+/g, ' ').trim()).toBe(
            '1.234,56 créditos'
        );
        expect(
            screen.getByText('Créditos disponibles: 12.345,5')
        ).toBeInTheDocument();

        fireEvent.change(quantity, { target: { value: '2' } });
        expect(total().textContent.replace(/\s+/g, ' ').trim()).toBe(
            '2.469,12 créditos'
        );

        fireEvent.click(screen.getByRole('button', { name: 'Volver' }));
        fireEvent.click(screen.getByRole('button', { name: 'Comprar' }));

        expect(screen.getByLabelText('Cantidad (máximo 5)')).toHaveValue(1);
    });

    test('rechaza cantidades no enteras, no positivas o superiores a los tokens disponibles', async () => {
        mocks.useAuth.mockReturnValue({
            token: 'token-prueba',
            user: { creditosDisponibles: 1000 },
        });

        renderPlayerPage();
        fireEvent.click(await screen.findByRole('button', { name: 'Comprar' }));

        const quantity = screen.getByLabelText('Cantidad (máximo 100)');
        const form = quantity.closest('form');

        for (const invalidQuantity of ['1.5', '0', '101']) {
            fireEvent.change(quantity, { target: { value: invalidQuantity } });
            fireEvent.submit(form);
        }

        expect(mocks.toastError).toHaveBeenNthCalledWith(
            1,
            'Ingresa una cantidad entre 1 y 100.'
        );
        expect(mocks.toastError).toHaveBeenCalledTimes(3);
        expect(mocks.comprarSuper).not.toHaveBeenCalled();
    });

    test('rechaza la compra cuando el total supera los créditos disponibles', async () => {
        mocks.useAuth.mockReturnValue({
            token: 'token-prueba',
            user: { creditosDisponibles: 100 },
        });

        renderPlayerPage();
        fireEvent.click(await screen.findByRole('button', { name: 'Comprar' }));
        fireEvent.submit(
            screen.getByLabelText('Cantidad (máximo 100)').closest('form')
        );

        expect(mocks.toastError).toHaveBeenCalledWith(
            'No tienes créditos suficientes para esa cantidad'
        );
        expect(mocks.comprarSuper).not.toHaveBeenCalled();
    });

    test('compra tokens y navega al perfil cuando la operación es exitosa', async () => {
        mocks.useAuth.mockReturnValue({
            token: 'token-prueba',
            user: { creditosDisponibles: 1000 },
        });

        renderPlayerPage();
        fireEvent.click(await screen.findByRole('button', { name: 'Comprar' }));
        fireEvent.submit(
            screen.getByLabelText('Cantidad (máximo 100)').closest('form')
        );

        expect(await screen.findByText('Perfil')).toBeInTheDocument();
        expect(mocks.comprarSuper).toHaveBeenCalledWith(
            1,
            1,
            'token-prueba'
        );
        expect(mocks.toastSuccess).toHaveBeenCalledWith('Compra exitosa!');
    });

    test('muestra el error de la API cuando falla la compra', async () => {
        const error = new Error('Compra rechazada');
        const consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});
        mocks.comprarSuper.mockRejectedValueOnce(error);
        mocks.useAuth.mockReturnValue({
            token: 'token-prueba',
            user: { creditosDisponibles: 1000 },
        });

        renderPlayerPage();
        fireEvent.click(await screen.findByRole('button', { name: 'Comprar' }));
        fireEvent.submit(
            screen.getByLabelText('Cantidad (máximo 100)').closest('form')
        );

        await waitFor(() => {
            expect(mocks.toastError).toHaveBeenCalledWith('Compra rechazada');
        });
        expect(consoleError).toHaveBeenCalledWith(
            'Error al comprar al jugador:',
            error
        );
        consoleError.mockRestore();
    });
});