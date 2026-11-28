
import { describe, test, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import CrearSubastaModal from '../components/subasta/CrearSubastaModal';
import { createAuction } from '../api/subastaApi';
import { useNavigate } from 'react-router-dom';

vi.mock('../api/subastaApi', () => ({
    createAuction: vi.fn(),
}));

vi.mock('react-router-dom', async (importOriginal) => {
    const actual = await importOriginal();

    return {
        ...actual,
        useNavigate: vi.fn(),
    };
});

describe('CrearSubastaModal', () => {
    const mockNavigate = vi.fn();
    const mockOnClose = vi.fn();
    const mockOnCreated = vi.fn();

    const props = {
        jugadorId: 'jugador-123',
        nombreJugador: 'Lionel Messi',
        tokensDisponibles: 10,
        cotizacion: 150.50,
        token: 'token-test',
        onClose: mockOnClose,
        onCreated: mockOnCreated,
    };

    beforeEach(() => {
        vi.clearAllMocks();
        useNavigate.mockReturnValue(mockNavigate);
    });

    const renderModal = (customProps = {}) =>
        render(
            <CrearSubastaModal
                {...props}
                {...customProps}
            />
        );

    const obtenerFechaFutura = () => {
        const fecha = new Date();
        fecha.setDate(fecha.getDate() + 1);

        const formato = (valor) => String(valor).padStart(2, '0');

        return [
            fecha.getFullYear(),
            formato(fecha.getMonth() + 1),
            formato(fecha.getDate()),
        ].join('-') + 'T12:00';
    };

    const completarFormulario = ({
        cantidad = '2',
        precio = '200',
        vencimiento = obtenerFechaFutura(),
    } = {}) => {
        fireEvent.change(screen.getByLabelText('Cantidad de tokens'), {
            target: { value: cantidad },
        });

        fireEvent.change(screen.getByLabelText('Precio inicial'), {
            target: { value: precio },
        });

        fireEvent.change(
            screen.getByLabelText('Fecha y hora de vencimiento'),
            {
                target: { value: vencimiento },
            }
        );
    };

    const enviarFormulario = () => {
        fireEvent.submit(screen.getByRole('button', {
            name: 'Crear subasta',
        }).closest('form'));
    };

    test('muestra los datos del jugador y los tokens disponibles', () => {
        renderModal();

        expect(screen.getByRole('heading', {
            name: 'Crear subasta',
        })).toBeInTheDocument();

        expect(screen.getByText('Lionel Messi')).toBeInTheDocument();
        expect(screen.getByText('Disponibles: 10')).toBeInTheDocument();

        expect(screen.getByLabelText('Precio inicial')).toHaveAttribute(
            'placeholder',
            'Cotizacion actual: $150.5'
        );
    });

    test('cierra el modal al presionar Cancelar', () => {
        renderModal();

        fireEvent.click(screen.getByRole('button', {
            name: 'Cancelar',
        }));

        expect(mockOnClose).toHaveBeenCalledTimes(1);
    });

    test('valida que la cantidad sea un entero mayor a cero', async () => {
        renderModal();

        completarFormulario({ cantidad: '0' });
        enviarFormulario();

        expect(await screen.findByRole('alert')).toHaveTextContent(
            'La cantidad debe ser un número entero mayor a cero.'
        );

        expect(createAuction).not.toHaveBeenCalled();
    });

    test('valida que la cantidad no supere los tokens disponibles', async () => {
        renderModal();

        completarFormulario({ cantidad: '11' });
        enviarFormulario();

        expect(await screen.findByRole('alert')).toHaveTextContent(
            'No tenés suficientes tokens disponibles.'
        );

        expect(createAuction).not.toHaveBeenCalled();
    });

    test('valida que el precio inicial sea mayor a cero', async () => {
        renderModal();

        completarFormulario({ precio: '0' });
        enviarFormulario();

        expect(await screen.findByRole('alert')).toHaveTextContent(
            'El precio inicial debe ser mayor a cero.'
        );

        expect(createAuction).not.toHaveBeenCalled();
    });

    test('valida que el vencimiento sea una fecha futura', async () => {
        renderModal();

        completarFormulario({
            vencimiento: '2020-01-01T12:00',
        });

        enviarFormulario();

        expect(await screen.findByRole('alert')).toHaveTextContent(
            'El vencimiento debe ser una fecha y hora futura.'
        );

        expect(createAuction).not.toHaveBeenCalled();
    });

    test('crea la subasta y navega al detalle cuando la operación es exitosa', async () => {
        createAuction.mockResolvedValue({
            id: 'subasta-456',
        });

        renderModal();
        completarFormulario();
        enviarFormulario();

        await waitFor(() => {
            expect(createAuction).toHaveBeenCalledWith({
                jugadorId: 'jugador-123',
                cantidad: 2,
                precioInicial: 200,
                vencimiento: `${obtenerFechaFutura()}:00`,
                token: 'token-test',
            });
        });

        expect(mockOnCreated).toHaveBeenCalledTimes(1);
        expect(mockOnClose).toHaveBeenCalledTimes(1);
        expect(mockNavigate).toHaveBeenCalledWith('/subasta/subasta-456');
    });

    test('muestra el error del servidor si falla la creación', async () => {
        createAuction.mockRejectedValue(
            new Error('No se pudo crear la subasta')
        );

        renderModal();
        completarFormulario();
        enviarFormulario();

        expect(await screen.findByRole('alert')).toHaveTextContent(
            'No se pudo crear la subasta'
        );

        expect(mockOnCreated).not.toHaveBeenCalled();
        expect(mockOnClose).not.toHaveBeenCalled();
        expect(mockNavigate).not.toHaveBeenCalled();
    });

    test('muestra un mensaje predeterminado si el error no tiene detalle', async () => {
        createAuction.mockRejectedValue({});

        renderModal();
        completarFormulario();
        enviarFormulario();

        expect(await screen.findByRole('alert')).toHaveTextContent(
            'No se pudo crear la subasta.'
        );
    });

    test('muestra el estado de carga mientras se crea la subasta', async () => {
        let resolverCreacion;

        createAuction.mockReturnValue(
            new Promise((resolve) => {
                resolverCreacion = resolve;
            })
        );

        renderModal();
        completarFormulario();
        enviarFormulario();

        expect(await screen.findByRole('button', {
            name: 'Creando...',
        })).toBeDisabled();

        expect(screen.getByRole('button', {
            name: 'Cancelar',
        })).toBeDisabled();

        resolverCreacion({ id: 'subasta-456' });

        await waitFor(() => {
            expect(mockNavigate).toHaveBeenCalledWith(
                '/subasta/subasta-456'
            );
        });
    });
});
