import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { createAuction } from '../../api/subastaApi';
import "../../styles/userPage.css";

function obtenerFechaMinima() {
    const ahora = new Date();

    const fecha = [
        ahora.getFullYear(),
        String(ahora.getMonth() + 1).padStart(2, '0'),
        String(ahora.getDate()).padStart(2, '0'),
    ].join('-');

    const hora = [
        String(ahora.getHours()).padStart(2, '0'),
        String(ahora.getMinutes()).padStart(2, '0'),
    ].join(':');

    return `${fecha}T${hora}`;
}

export default function CrearSubastaModal({
    jugadorId,
    nombreJugador,
    tokensDisponibles,
    cotizacion,
    token,
    onClose,
    onCreated,
}) {
    const [cantidad, setCantidad] = useState('1');
    const [precioInicial, setPrecioInicial] = useState('');
    const [vencimiento, setVencimiento] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const fechaMinima = obtenerFechaMinima();

    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        const cantidadNumerica = Number(cantidad);
        const precioNumerico = Number(precioInicial);

        if (!Number.isInteger(cantidadNumerica) || cantidadNumerica < 1) {
            setError('La cantidad debe ser un número entero mayor a cero.');
            return;
        }

        if (cantidadNumerica > tokensDisponibles) {
            setError('No tenés suficientes tokens disponibles.');
            return;
        }

        if (!Number.isFinite(precioNumerico) || precioNumerico <= 0) {
            setError('El precio inicial debe ser mayor a cero.');
            return;
        }

        if (!vencimiento || vencimiento <= obtenerFechaMinima()) {
            setError('El vencimiento debe ser una fecha y hora futura.');
            return;
        }

        setLoading(true);

        try {
            const response = await createAuction({
                jugadorId,
                cantidad: cantidadNumerica,
                precioInicial: precioNumerico,
                vencimiento: `${vencimiento}:00`,
                token,
            });

            if (response) {
                onCreated?.();
                onClose();
                void navigate("/subasta/"+response.id)
            }
        } catch (err) {
            setError(err.message || 'No se pudo crear la subasta.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div
            className="user-page-dialog-backdrop"
        >
            <dialog
                open
                className="user-page-sell-dialog"
                aria-modal="true"
                aria-labelledby="crear-subasta-title"
                onMouseDown={(e) => {
                    if (e.target === e.currentTarget && !loading) {
                        onClose();
                    }
                }}
            >
                <h2 id="crear-subasta-title">Crear subasta</h2>

                <p>
                    Estás creando una subasta para <strong>{nombreJugador}</strong>.
                </p>

                <form onSubmit={handleSubmit}>
                    <div className="crear-subasta-field">
                        <label htmlFor="cantidad-subasta">
                            Cantidad de tokens
                        </label>

                        <input
                            id="cantidad-subasta"
                            type="number"
                            min="1"
                            max={tokensDisponibles}
                            step="1"
                            value={cantidad}
                            onChange={(e) => setCantidad(e.target.value)}
                            required
                        />

                        <small>
                            Disponibles: {tokensDisponibles}
                        </small>
                    </div>

                    <div className="crear-subasta-field">
                        <label htmlFor="precio-subasta">
                            Precio inicial
                        </label>

                        <input
                            id="precio-subasta"
                            type="number"
                            min="0.01"
                            step="0.01"
                            placeholder={'Cotizacion actual: $'+cotizacion} 
                            value={precioInicial}
                            onChange={(e) => setPrecioInicial(e.target.value)}
                            required
                        />
                    </div>

                    <div className="crear-subasta-field">
                        <label htmlFor="vencimiento-subasta">
                            Fecha y hora de vencimiento
                        </label>

                        <input
                            id="vencimiento-subasta"
                            type="datetime-local"
                            min={fechaMinima}
                            value={vencimiento}
                            onChange={(e) => setVencimiento(e.target.value)}
                            required
                        />
                    </div>

                    {error && (
                        <p className="user-page-modal-error" role="alert">
                            {error}
                        </p>
                    )}

                    <div className="user-page-dialog-actions">
                        <button
                            type="button"
                            onClick={onClose}
                            disabled={loading}
                            className='user-page-cancel-button'
                        >
                            Cancelar
                        </button>

                        <button
                            type="submit"
                            disabled={loading}
                        >
                            {loading ? 'Creando...' : 'Crear subasta'}
                        </button>
                    </div>
                </form>
            </dialog>
        </div>
    );
}