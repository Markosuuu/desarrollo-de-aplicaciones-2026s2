import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, test, expect, vi, beforeEach } from 'vitest';
import BuscadorCotizacionPorFecha from '../components/BuscadorCotizacionPorFecha';
import { getQuoteByDate } from '../api/playerApi';

vi.mock('../api/playerApi', () => ({
    getQuoteByDate: vi.fn(),
}));

describe('BuscadorCotizacionPorFecha', () => {

    const playerId = 1;
    const token = 'token-test';

    beforeEach(() => {
        vi.clearAllMocks();
    });

    const renderComponent = () => render(
        <BuscadorCotizacionPorFecha
            playerId={playerId}
            token={token}
        />
    );

    test('debe renderizar el buscador inicialmente', () => {
        renderComponent();

        expect(screen.getByText('Consultar cotización histórica')).toBeInTheDocument();
        expect(screen.getByRole('button', { name: 'Consultar' })).toBeInTheDocument();
        expect(screen.queryByText('Cotización:')).not.toBeInTheDocument();
    });

    test('debe actualizar la fecha seleccionada', () => {
        const { container } = renderComponent();

        const input = container.querySelector('input[type="date"]');

        fireEvent.change(input, {
            target: { value: '2026-11-17' },
        });

        expect(input).toHaveValue('2026-11-17');
    });

    test('debe consultar y mostrar la cotización histórica correctamente', async () => {
        const fecha = '2026-11-17';

        getQuoteByDate.mockResolvedValue({
            precio: 125.5,
            fecha: '2026-11-17T12:00:00',
        });

        const { container } = renderComponent();

        const input = container.querySelector('input[type="date"]');

        fireEvent.change(input, {
            target: { value: fecha },
        });

        fireEvent.click(screen.getByRole('button', { name: 'Consultar' }));

        await waitFor(() => {
            expect(getQuoteByDate).toHaveBeenCalledWith({
                id: playerId,
                fecha,
                token,
            });
        });

        expect(await screen.findByText('125.50')).toBeInTheDocument();

        expect(screen.getByText('Cotización:')).toBeInTheDocument();
    });

    test('debe mostrar el estado de carga mientras consulta', async () => {
        let resolver;

        getQuoteByDate.mockReturnValue(
            new Promise((resolve) => {
                resolver = resolve;
            })
        );

        const { container } = renderComponent();

        const input = container.querySelector('input[type="date"]');

        fireEvent.change(input, {
            target: { value: '2026-11-17' },
        });

        fireEvent.click(screen.getByRole('button', { name: 'Consultar' }));

        expect(screen.getByRole('button', { name: 'Buscando...' })).toBeDisabled();

        resolver({
            precio: 100,
            fecha: '2026-11-17T12:00:00',
        });

        await waitFor(() => {
            expect(screen.getByRole('button', { name: 'Consultar' })).toBeEnabled();
        });
    });

    test('debe mostrar el error si falla la consulta', async () => {
        getQuoteByDate.mockRejectedValue(
            new Error('No se encontró la cotización')
        );

        const { container } = renderComponent();

        const input = container.querySelector('input[type="date"]');

        fireEvent.change(input, {
            target: { value: '2026-11-17' },
        });

        fireEvent.click(screen.getByRole('button', { name: 'Consultar' }));

        expect(
            await screen.findByText('No se encontró la cotización')
        ).toBeInTheDocument();

        expect(screen.queryByText('Cotización:')).not.toBeInTheDocument();
    });

    test('no debe realizar la consulta si no hay una fecha seleccionada', () => {
        const { container } = renderComponent();

        const form = container.querySelector('form');

        fireEvent.submit(form);

        expect(getQuoteByDate).not.toHaveBeenCalled();
    });

    test('debe limpiar el resultado anterior al realizar una nueva consulta', async () => {
        getQuoteByDate
            .mockResolvedValueOnce({
                precio: 150,
                fecha: '2026-11-16T12:00:00',
            })
            .mockResolvedValueOnce({
                precio: 200,
                fecha: '2026-11-17T12:00:00',
            });

        const { container } = renderComponent();

        const input = container.querySelector('input[type="date"]');

        fireEvent.change(input, {
            target: { value: '2026-11-16' },
        });

        fireEvent.click(screen.getByRole('button', { name: 'Consultar' }));

        expect(await screen.findByText('150.00')).toBeInTheDocument();

        fireEvent.change(input, {
            target: { value: '2026-11-17' },
        });

        fireEvent.click(screen.getByRole('button', { name: 'Consultar' }));

        await waitFor(() => {
            expect(screen.getByText('200.00')).toBeInTheDocument();
        });

        expect(screen.queryByText('150.00')).not.toBeInTheDocument();
    });

});