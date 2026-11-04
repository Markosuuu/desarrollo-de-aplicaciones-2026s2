
import { afterEach, beforeEach, describe, expect, test, vi } from 'vitest';
import { fetchPlayers, fetchPlayer, fetchPlayerQuotesHistory, getRanking } from '../api/playerApi.js';

describe('fetchPlayers', () => {
    beforeEach(() => {
        vi.stubGlobal('fetch', vi.fn());
    });

    afterEach(() => {
        vi.unstubAllGlobals();
    });

    test('realiza la petición con los parámetros y headers correspondientes', async () => {
        const jugadores = [{ id: 1, nombre: 'Lionel Messi' }];

        fetch.mockResolvedValue({
            ok: true,
            json: vi.fn().mockResolvedValue(jugadores),
        });

        const result = await fetchPlayers({
            page: 2,
            perPage: 10,
            nombre: 'Messi',
            equipo: 'Inter Miami',
            liga: 'MLS',
            token: 'token-test',
        });

        const [url, options] = fetch.mock.calls[0];
        const parsedUrl = new URL(url);

        expect(parsedUrl.searchParams.get('page')).toBe('2');
        expect(parsedUrl.searchParams.get('perPage')).toBe('10');
        expect(parsedUrl.searchParams.get('nombre')).toBe('Messi');
        expect(parsedUrl.searchParams.get('equipo')).toBe('Inter%20Miami');
        expect(parsedUrl.searchParams.get('liga')).toBe('MLS');

        expect(options.headers).toEqual({
            Accept: 'application/json',
            Authorization: 'Bearer token-test',
        });

        expect(result).toEqual(jugadores);
    });

    test('ignora términos de búsqueda que no sean seguros', async () => {
        fetch.mockResolvedValue({
            ok: true,
            json: vi.fn().mockResolvedValue([]),
        });

        await fetchPlayers({
            nombre: '<script>alert(1)</script>',
            equipo: 123,
            token: 'token-test',
        });

        const [url] = fetch.mock.calls[0];
        const parsedUrl = new URL(url);

        expect(parsedUrl.searchParams.has('nombre')).toBe(false);
        expect(parsedUrl.searchParams.has('equipo')).toBe(false);
    });

    test('lanza un error cuando la respuesta HTTP no es exitosa', async () => {
        fetch.mockResolvedValue({
            ok: false,
            json: vi.fn().mockResolvedValue({
                error: {
                    message: 'Error al consultar jugadores',
                },
            }),
        });

        await expect(fetchPlayers()).rejects.toThrow(
            'Error al consultar jugadores',
        );
    });

    test('utiliza el mensaje por defecto cuando la respuesta no contiene un error', async () => {
        fetch.mockResolvedValue({
            ok: false,
            json: vi.fn().mockResolvedValue({}),
        });

        await expect(fetchPlayers()).rejects.toThrow(
            'No se pudo cargar el catálogo de jugadores.',
        );
    });

    test('continúa aunque el servidor no devuelva JSON válido', async () => {
        fetch.mockResolvedValue({
            ok: true,
            json: vi.fn().mockRejectedValue(new Error('Invalid JSON')),
        });

        const result = await fetchPlayers();

        expect(result).toEqual({});
    });
});

describe('fetchPlayer', () => {
    beforeEach(() => {
        vi.stubGlobal('fetch', vi.fn());
    });

    afterEach(() => {
        vi.unstubAllGlobals();
    });

    test('obtiene un jugador utilizando su ID y token', async () => {
        const jugador = {
            id: 1,
            nombre: 'Lionel Messi',
            cotizacion: 300,
        };

        fetch.mockResolvedValue({
            ok: true,
            json: vi.fn().mockResolvedValue(jugador),
        });

        const result = await fetchPlayer(1, 'token-test');

        expect(fetch).toHaveBeenCalledWith(
            expect.stringContaining('/players/1'),
            {
                headers: {
                    Accept: 'application/json',
                    Authorization: 'Bearer token-test',
                },
            },
        );

        expect(result).toEqual(jugador);
    });

    test('devuelve un objeto vacío cuando la respuesta no contiene JSON válido', async () => {
        fetch.mockResolvedValue({
            ok: true,
            json: vi.fn().mockRejectedValue(new Error('Invalid JSON')),
        });

        const result = await fetchPlayer(1, 'token-test');

        expect(result).toEqual({});
    });
});

describe('fetchPlayerQuotesHistory', () => {
    beforeEach(() => {
        vi.stubGlobal('fetch', vi.fn());
    });

    afterEach(() => {
        vi.unstubAllGlobals();
    });

    test('obtiene el historial de cotizaciones utilizando el ID y token del jugador', async () => {
        const historial = [
            { fecha: '2026-10-01', precio: 250 },
            { fecha: '2026-10-08', precio: 275 },
        ];

        fetch.mockResolvedValue({
            json: vi.fn().mockResolvedValue(historial),
        });

        const result = await fetchPlayerQuotesHistory(1, 'token-test');

        expect(fetch).toHaveBeenCalledWith(
            expect.stringContaining('/players/1/quotes'),
            {
                headers: {
                    Accept: 'application/json',
                    Authorization: 'Bearer token-test',
                },
            },
        );

        expect(result).toEqual(historial);
    });

    test('devuelve un objeto vacío cuando la respuesta no contiene JSON válido', async () => {
        fetch.mockResolvedValue({
            json: vi.fn().mockRejectedValue(new Error('Invalid JSON')),
        });

        const result = await fetchPlayerQuotesHistory(1, 'token-test');

        expect(result).toEqual({});
    });
});

describe('getRanking', () => {

    beforeEach(() => {
        vi.stubGlobal('fetch', vi.fn());
    });

    afterEach(() => {
        vi.restoreAllMocks();
        vi.unstubAllGlobals();
    });

    test('debe obtener el ranking con parámetros por defecto', async () => {
        const payload = [{ id: 1, nombre: 'Jugador 1' }];

        fetch.mockResolvedValue({
            ok: true,
            json: vi.fn().mockResolvedValue(payload),
        });

        const result = await getRanking({ token: 'token-test' });

        expect(fetch).toHaveBeenCalledWith(
            expect.stringContaining('/players/ranking?page=1&perPage=50'),
            {
                headers: {
                    Accept: 'application/json',
                    Authorization: 'Bearer token-test',
                },
            }
        );

        expect(result).toEqual(payload);
    });

    test('debe obtener el ranking con parámetros personalizados', async () => {
        const payload = [{ id: 2, nombre: 'Jugador 2' }];

        fetch.mockResolvedValue({
            ok: true,
            json: vi.fn().mockResolvedValue(payload),
        });

        const result = await getRanking({
            page: 3,
            perPage: 20,
            token: 'token-test',
        });

        expect(fetch).toHaveBeenCalledWith(
            expect.stringContaining('/players/ranking?page=3&perPage=20'),
            expect.any(Object)
        );

        expect(result).toEqual(payload);
    });

    test('debe devolver un objeto vacío si falla el parseo del JSON', async () => {
        fetch.mockResolvedValue({
            ok: true,
            json: vi.fn().mockRejectedValue(new Error('JSON inválido')),
        });

        const result = await getRanking({ token: 'token-test' });

        expect(result).toEqual({});
    });

    test('debe lanzar el error recibido en error.message', async () => {
        fetch.mockResolvedValue({
            ok: false,
            json: vi.fn().mockResolvedValue({
                error: {
                    message: 'Error al obtener el ranking',
                },
            }),
        });

        await expect(
            getRanking({ token: 'token-test' })
        ).rejects.toThrow('Error al obtener el ranking');
    });

    test('debe lanzar el error recibido en message si no existe error.message', async () => {
        fetch.mockResolvedValue({
            ok: false,
            json: vi.fn().mockResolvedValue({
                message: 'Acceso denegado',
            }),
        });

        await expect(
            getRanking({ token: 'token-test' })
        ).rejects.toThrow('Acceso denegado');
    });

    test('debe lanzar el mensaje por defecto si no existe un mensaje de error', async () => {
        fetch.mockResolvedValue({
            ok: false,
            json: vi.fn().mockResolvedValue({}),
        });

        await expect(
            getRanking({ token: 'token-test' })
        ).rejects.toThrow('No se pudo cargar el catálogo de jugadores.');
    });

    test('debe lanzar el mensaje por defecto si falla el parseo del JSON y la respuesta no es exitosa', async () => {
        fetch.mockResolvedValue({
            ok: false,
            json: vi.fn().mockRejectedValue(new Error('JSON inválido')),
        });

        await expect(
            getRanking({ token: 'token-test' })
        ).rejects.toThrow('No se pudo cargar el catálogo de jugadores.');
    });
});