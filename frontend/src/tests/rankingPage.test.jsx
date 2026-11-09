import {
    describe,
    expect,
    test,
    vi,
    beforeEach,
    afterEach,
} from 'vitest';

import {
    render,
    screen,
    fireEvent,
    waitFor,
    act,
    cleanup,
} from '@testing-library/react';

import RankingPage from '../pages/RankingPage';

const mocks = vi.hoisted(() => ({
    getRanking: vi.fn(),
    useAuth: vi.fn(),
    navigate: vi.fn(),
}));

vi.mock('../api/playerApi', () => ({
    getRanking: mocks.getRanking,
}));

vi.mock('../context/AuthContext', () => ({
    useAuth: mocks.useAuth,
}));

vi.mock('react-router-dom', () => ({
    useNavigate: () => mocks.navigate,
}));

let observers;

class MockIntersectionObserver {
    constructor(callback) {
        this.callback = callback;
        this.observe = vi.fn();
        this.disconnect = vi.fn();

        observers.push(this);
    }

    trigger(isIntersecting) {
        this.callback([{ isIntersecting }]);
    }
}

const crearRespuesta = (
    jugadores = [],
    pagina = 1,
    totalPaginas = 1
) => ({
    jugadores,
    paginacion: {
        pagina,
        totalPaginas,
    },
});

const crearJugador = (id, overrides = {}) => ({
    id,
    nombre: `Jugador ${id}`,
    equipo: `Equipo ${id}`,
    liga: 'LaLiga',
    cotizacion: 1500.50,
    ...overrides,
});

describe('RankingPage', () => {

    beforeEach(() => {
        vi.clearAllMocks();

        observers = [];

        vi.stubGlobal(
            'IntersectionObserver',
            MockIntersectionObserver
        );

        mocks.useAuth.mockReturnValue({
            token: 'token-test',
        });
    });

    afterEach(() => {
        cleanup();
        vi.unstubAllGlobals();
    });

    test('debe mostrar el título y el estado de carga inicial', () => {
        mocks.getRanking.mockReturnValue(new Promise(() => {}));

        render(<RankingPage />);

        expect(
            screen.getByRole('heading', {
                name: 'Ranking de jugadores',
            })
        ).toBeInTheDocument();

        expect(
            screen.getByText('Cargando jugadores...')
        ).toBeInTheDocument();

        expect(mocks.getRanking).toHaveBeenCalledWith({
            page: 1,
            perPage: 50,
            nombre: '',
            token: 'token-test',
        });
    });

    test('debe mostrar los jugadores y sus cotizaciones', async () => {
        const jugadores = [
            crearJugador(1),
            crearJugador(2, {
                nombre: 'Lionel Messi',
                equipo: 'Inter Miami',
                cotizacion: 2500000,
            }),
        ];

        mocks.getRanking.mockResolvedValue(
            crearRespuesta(jugadores)
        );

        render(<RankingPage />);

        expect(
            await screen.findByText('Lionel Messi')
        ).toBeInTheDocument();

        expect(screen.getByText('Jugador 1')).toBeInTheDocument();
        expect(screen.getByText('Inter Miami')).toBeInTheDocument();

        expect(screen.getByText('2.500.000')).toBeInTheDocument();
        expect(screen.getByText('1.500,5')).toBeInTheDocument();

        expect(
            screen.getByText('Ya se cargaron todos los jugadores.')
        ).toBeInTheDocument();

        expect(
            screen.queryByText('Cargando jugadores...')
        ).not.toBeInTheDocument();
    });

    test('debe mostrar los iconos correspondientes a cada liga', async () => {
        const ligas = [
            'LaLiga',
            'Bundesliga',
            'Ligue 1',
            'Serie A',
            'Premier League',
            'Liga desconocida',
        ];

        const jugadores = ligas.map((liga, index) =>
            crearJugador(index + 1, { liga })
        );

        mocks.getRanking.mockResolvedValue(
            crearRespuesta(jugadores)
        );

        render(<RankingPage />);

        for (const liga of ligas) {
            const icono = await screen.findByRole('img', {
                name: liga,
            });

            expect(icono).toHaveAttribute('alt', liga);
            expect(icono).toHaveAttribute('title', liga);
        }

        const ligaDesconocida = screen.getByRole('img', {
            name: 'Liga desconocida',
        });

        expect(ligaDesconocida).not.toHaveAttribute('src');
    });


    test('debe mostrar un mensaje cuando no hay jugadores', async () => {
        mocks.getRanking.mockResolvedValue(
            crearRespuesta([])
        );

        render(<RankingPage />);

        expect(
            await screen.findByText('No hay jugadores disponibles.')
        ).toBeInTheDocument();
    });

    test('debe mostrar un error cuando falla la carga', async () => {
        mocks.getRanking.mockRejectedValue(
            new Error('Error al obtener el ranking')
        );

        render(<RankingPage />);

        expect(
            await screen.findByText('Error al obtener el ranking')
        ).toBeInTheDocument();

        expect(
            screen.getByRole('button', { name: 'Reintentar' })
        ).toBeInTheDocument();

        expect(
            screen.queryByText('No hay jugadores disponibles.')
        ).not.toBeInTheDocument();
    });

    test('debe mostrar el mensaje por defecto si el error no tiene mensaje', async () => {
        mocks.getRanking.mockRejectedValue(
            new Error('')
        );

        render(<RankingPage />);

        expect(
            await screen.findByText('No se pudo cargar el ranking.')
        ).toBeInTheDocument();
    });

    test('debe reintentar la carga inicial cuando ocurre un error', async () => {
        mocks.getRanking
            .mockRejectedValueOnce(new Error('Error de conexión'))
            .mockResolvedValueOnce(
                crearRespuesta([
                    crearJugador(30),
                ])
            );

        render(<RankingPage />);

        expect(
            await screen.findByText('Error de conexión')
        ).toBeInTheDocument();

        fireEvent.click(
            screen.getByRole('button', { name: 'Reintentar' })
        );

        expect(
            await screen.findByText('Jugador 30')
        ).toBeInTheDocument();

        expect(mocks.getRanking).toHaveBeenCalledTimes(2);

        expect(mocks.getRanking).toHaveBeenLastCalledWith({
            page: 1,
            perPage: 50,
            nombre: '',
            token: 'token-test',
        });
    });

    test('no debe realizar solicitudes si no hay token', async () => {
        mocks.useAuth.mockReturnValue({
            token: null,
        });

        render(<RankingPage />);

        expect(
            await screen.findByText('No hay jugadores disponibles.')
        ).toBeInTheDocument();

        expect(mocks.getRanking).not.toHaveBeenCalled();
    });

    test('debe cargar la siguiente página cuando se alcanza el final de la lista', async () => {
        mocks.getRanking
            .mockResolvedValueOnce(
                crearRespuesta([
                    crearJugador(1),
                ], 1, 2)
            )
            .mockResolvedValueOnce(
                crearRespuesta([
                    crearJugador(2),
                ], 2, 2)
            );

        render(<RankingPage />);

        expect(
            await screen.findByText('Jugador 1')
        ).toBeInTheDocument();

        await waitFor(() => {
            expect(observers.length).toBeGreaterThan(0);
        });

        const observer = observers[observers.length - 1];

        expect(observer.observe).toHaveBeenCalled();

        act(() => {
            observer.trigger(true);
        });

        expect(
            await screen.findByText('Jugador 2')
        ).toBeInTheDocument();

        expect(mocks.getRanking).toHaveBeenCalledTimes(2);

        expect(mocks.getRanking).toHaveBeenLastCalledWith({
            page: 2,
            perPage: 50,
            nombre: '',
            token: 'token-test',
        });

        expect(
            screen.getByText('Ya se cargaron todos los jugadores.')
        ).toBeInTheDocument();
    });

    test('no debe cargar otra página si el sentinel no está intersectando', async () => {
        mocks.getRanking.mockResolvedValue(
            crearRespuesta([
                crearJugador(1),
            ], 1, 2)
        );

        render(<RankingPage />);

        expect(
            await screen.findByText('Jugador 1')
        ).toBeInTheDocument();

        await waitFor(() => {
            expect(observers.length).toBeGreaterThan(0);
        });

        const observer = observers[observers.length - 1];

        act(() => {
            observer.trigger(false);
        });

        expect(mocks.getRanking).toHaveBeenCalledTimes(1);
    });

    test('debe buscar por nombre y reiniciar la paginación', async () => {
        mocks.getRanking.mockResolvedValue(
            crearRespuesta([crearJugador(1, { nombre: 'Lionel Messi' })])
        );

        render(<RankingPage />);

        await screen.findByText('Lionel Messi');

        fireEvent.change(screen.getByRole('searchbox'), {
            target: { value: 'Messi' },
        });

        await waitFor(() => {
            expect(mocks.getRanking).toHaveBeenLastCalledWith({
                page: 1,
                perPage: 50,
                nombre: 'Messi',
                token: 'token-test',
            });
        });

        expect(mocks.getRanking).toHaveBeenCalledTimes(2);
    });

    test('debe desconectar el observer cuando se desmonta el componente', async () => {
        mocks.getRanking.mockResolvedValue(
            crearRespuesta([
                crearJugador(1),
            ], 1, 2)
        );

        const { unmount } = render(<RankingPage />);

        await screen.findByText('Jugador 1');

        await waitFor(() => {
            expect(observers.length).toBeGreaterThan(0);
        });

        const observer = observers[observers.length - 1];

        unmount();

        expect(observer.disconnect).toHaveBeenCalled();
    });

});