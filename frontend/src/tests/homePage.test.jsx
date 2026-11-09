import { describe, expect, test, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, waitFor, cleanup, act } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { fetchPlayers } from '../api/playerApi';
import HomePage from '../pages/HomePage';

const mocks = vi.hoisted(() => ({
    fetchPlayers: vi.fn(),
    useAuth: vi.fn(),
    navigate: vi.fn(),
}));

vi.mock('../api/playerApi', () => ({
    fetchPlayers: mocks.fetchPlayers,
}));

vi.mock('../context/AuthContext', () => ({
    useAuth: mocks.useAuth,
}));

vi.mock('react-router-dom', async (importOriginal) => {
    const actual = await importOriginal();

    return {
        ...actual,
        useNavigate: () => mocks.navigate,
        Link: ({ children, onClick, className }) => (
            <a href="#" className={className} onClick={(event) => {
                event.preventDefault();
                onClick?.(event);
            }}>
                {children}
            </a>
        ),
    };
});

vi.mock('@blobatar/react', () => ({
    Blobatar: () => <div data-testid="blobatar" />,
}));

function createDeferred() {
    let resolve;
    let reject;

    const promise = new Promise((res, rej) => {
        resolve = res;
        reject = rej;
    });

    return { promise, resolve, reject };
}

describe('HomePage', () => {
    const jugadores = [
        {
            id: 1,
            nombre: 'Lionel Messi',
            equipo: 'Inter Miami',
            liga: 'MLS',
            posicion: 'Delantero',
        },
        {
            id: 2,
            nombre: 'Jude Bellingham',
            equipo: 'Real Madrid',
            liga: 'LaLiga',
            posicion: 'Mediocampista',
        },
    ];

    const paginacion = {
        pagina: 1,
        porPagina: 20,
        total: 2,
        totalPaginas: 1,
    };

    const renderHome = () =>
        render(
            <MemoryRouter>
                <HomePage />
            </MemoryRouter>,
        );

    beforeEach(() => {
        vi.clearAllMocks();

        mocks.useAuth.mockReturnValue({
            user: { nombre: 'Ana' },
            token: 'token-test',
        });

        mocks.fetchPlayers.mockResolvedValue({
            jugadores,
            paginacion,
        });
    });

    afterEach(() => {
        cleanup();
    });

    test('muestra el nombre del usuario y el catálogo', async () => {
        renderHome();

        expect(screen.getByText('Catálogo de jugadores')).toBeTruthy();

        await waitFor(() => {
            expect(screen.getByText('Lionel Messi')).toBeTruthy();
            expect(screen.getByText('Jude Bellingham')).toBeTruthy();
        });
    });

    test('solicita los jugadores utilizando el token y los parámetros iniciales', async () => {
        renderHome();

        await waitFor(() => {
            expect(fetchPlayers).toHaveBeenCalledWith({
                page: 1,
                perPage: 20,
                nombre: undefined,
                token: 'token-test',
            });
        });
    });

    test('muestra un mensaje cuando no hay jugadores', async () => {
        mocks.fetchPlayers.mockResolvedValue({
            jugadores: [],
            paginacion,
        });

        renderHome();

        expect(
            await screen.findByText(
                'No se encontraron jugadores para esta búsqueda.',
            ),
        ).toBeTruthy();
    });

    test('muestra un mensaje cuando falla la petición', async () => {
        mocks.fetchPlayers.mockRejectedValue(
            new Error('Error al cargar jugadores'),
        );

        renderHome();

        expect(
            await screen.findByText('Error al cargar jugadores'),
        ).toBeTruthy();
    });

    test('utiliza el mensaje de error por defecto cuando no hay mensaje', async () => {
        mocks.fetchPlayers.mockRejectedValue({});

        renderHome();

        expect(
            await screen.findByText(
                'No se pudo cargar el catálogo de jugadores.',
            ),
        ).toBeTruthy();
    });

    test('filtra jugadores según el término de búsqueda', async () => {
        renderHome();

        await screen.findByText('Lionel Messi');

        const search = screen.getByPlaceholderText('Nombre');

        fireEvent.change(search, {
            target: { value: 'messi' },
        });

        await waitFor(() => {
            expect(screen.getByText('Lionel Messi')).toBeTruthy();
            expect(screen.queryByText('Jude Bellingham')).toBeNull();
        });

        expect(fetchPlayers).toHaveBeenLastCalledWith({
            page: 1,
            perPage: 20,
            nombre: 'messi',
            token: 'token-test',
        });
    });

    test('permite navegar al perfil de un jugador', async () => {
        renderHome();

        const player = await screen.findByText('Lionel Messi');

        fireEvent.click(player.closest('a'));

        expect(mocks.navigate).toHaveBeenCalledWith('/player/1');
    });

    test('muestra los datos alternativos cuando faltan datos del jugador', async () => {
        mocks.fetchPlayers.mockResolvedValue({
            jugadores: [
                {
                    id: 3,
                    nombre: null,
                    equipo: null,
                    liga: null,
                    posicion: null,
                },
            ],
            paginacion,
        });

        renderHome();

        await screen.findByText('Jugador sin nombre');

        expect(screen.getByText('Sin posición')).toBeTruthy();
        expect(screen.getAllByText('—')).toHaveLength(3);
        expect(screen.getByTestId('blobatar')).toBeTruthy();
    });

    test.each([
        'LaLiga',
        'Bundesliga',
        'Ligue 1',
        'Serie A',
        'Premier League',
        'Liga desconocida',
    ])('procesa correctamente la liga %s', async (liga) => {
        mocks.fetchPlayers.mockResolvedValue({
            jugadores: [
                {
                    id: 1,
                    nombre: 'Jugador de prueba',
                    equipo: 'Equipo',
                    liga,
                    posicion: 'Delantero',
                },
            ],
            paginacion,
        });

        renderHome();

        await screen.findByText('Jugador de prueba');

        const image = screen.getByAltText('icono-liga');

        if (liga === 'Liga desconocida') {
            expect(image.getAttribute('src')).toBeNull();
        } else {
            expect(image.getAttribute('src')).toBeTruthy();
        }
    });
    test('utiliza una paginación por defecto cuando la API no la devuelve', async () => {
        mocks.fetchPlayers.mockResolvedValue({
            jugadores,
        });

        renderHome();

        await screen.findByText('Lionel Messi');

        expect(screen.getByText('Página 1 de 1')).toBeTruthy();
    });
    test('muestra nuevamente todos los jugadores cuando se limpia la búsqueda', async () => {
        renderHome();

        await screen.findByText('Lionel Messi');

        const search = screen.getByPlaceholderText('Nombre');

        fireEvent.change(search, {
            target: { value: 'messi' },
        });

        await waitFor(() => {
            expect(screen.queryByText('Jude Bellingham')).toBeNull();
        });

        fireEvent.change(search, {
            target: { value: '' },
        });

        await waitFor(() => {
            expect(screen.getByText('Lionel Messi')).toBeTruthy();
            expect(screen.getByText('Jude Bellingham')).toBeTruthy();
        });
    });
    test('permite avanzar y retroceder entre páginas', async () => {
        mocks.fetchPlayers
            .mockResolvedValueOnce({
                jugadores,
                paginacion: {
                    pagina: 1,
                    porPagina: 20,
                    total: 40,
                    totalPaginas: 2,
                },
            })
            .mockResolvedValue({
                jugadores,
                paginacion: {
                    pagina: 2,
                    porPagina: 20,
                    total: 40,
                    totalPaginas: 2,
                },
            });

        renderHome();

        await screen.findByText('Página 1 / 2');

        fireEvent.click(screen.getByRole('button', { name: 'Siguiente' }));

        await waitFor(() => {
            expect(fetchPlayers).toHaveBeenLastCalledWith({
                page: 2,
                perPage: 20,
                nombre: undefined,
                token: 'token-test',
            });
        });

        await screen.findByText('Página 2 / 2');

        fireEvent.click(screen.getByRole('button', { name: 'Anterior' }));

        await waitFor(() => {
            expect(fetchPlayers).toHaveBeenLastCalledWith({
                page: 1,
                perPage: 20,
                nombre: undefined,
                token: 'token-test',
            });
        });
    });

    test('ignora una respuesta exitosa cuando el efecto ya fue limpiado', async () => {
        const firstRequest = createDeferred();

        mocks.fetchPlayers
            .mockReturnValueOnce(firstRequest.promise)
            .mockResolvedValueOnce({
                jugadores: [],
                paginacion,
            });

        renderHome();

        const search = screen.getByPlaceholderText('Nombre');

        // Cambiar la búsqueda limpia el efecto anterior.
        fireEvent.change(search, {
            target: { value: 'messi' },
        });

        // Resolver la petición anterior después de su limpieza.
        await act(async () => {
            firstRequest.resolve({
                jugadores: [
                    {
                        id: 99,
                        nombre: 'Jugador antiguo',
                        equipo: 'Equipo antiguo',
                        liga: 'LaLiga',
                        posicion: 'Delantero',
                    },
                ],
                paginacion,
            });
        });

        expect(screen.queryByText('Jugador antiguo')).toBeNull();
    });

    test('ignora un error cuando el efecto ya fue limpiado', async () => {
        const firstRequest = createDeferred();

        mocks.fetchPlayers
            .mockReturnValueOnce(firstRequest.promise)
            .mockResolvedValueOnce({
                jugadores: [],
                paginacion,
            });

        renderHome();

        const search = screen.getByPlaceholderText('Nombre');

        // Limpiar el efecto anterior.
        fireEvent.change(search, {
            target: { value: 'messi' },
        });

        // Rechazar la petición anterior después de su limpieza.
        await act(async () => {
            firstRequest.reject(new Error('Error de petición antigua'));
        });

        expect(
            screen.queryByText('Error de petición antigua'),
        ).toBeNull();
    });
});