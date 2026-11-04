
import { useCallback, useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { useAuth } from '../context/AuthContext';
import {
  fetchSubasta,
  cerrarSubasta,
  ofertarSubasta,
} from '../api/subastaApi';

import '../styles/SubastaPage.css';

function formatNumber(value, decimals = 2) {
  if (value == null) return 'No disponible';

  return Number(value).toLocaleString('es-AR', {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}

function formatDate(date, includeTime = true) {
  if (!date) return 'Sin fecha';

  const parsedDate = new Date(date);

  if (Number.isNaN(parsedDate.getTime())) {
    return 'Fecha inválida';
  }

  return new Intl.DateTimeFormat('es-AR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    ...(includeTime && {
      hour: '2-digit',
    }),
  }).format(parsedDate);
}

function getMinimumBid(currentOffer) {
  return currentOffer != null
    ? Number((Number(currentOffer) + 0.01).toFixed(2))
    : 0.01;
}

export default function SubastaPage() {
  const { id } = useParams();
  const { user, token } = useAuth();
  const navigate = useNavigate();

  const [subasta, setSubasta] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const [showCloseConfirmation, setShowCloseConfirmation] = useState(false);
  const [showOfferForm, setShowOfferForm] = useState(false);

  const [puja, setPuja] = useState('');
  const [closing, setClosing] = useState(false);
  const [offering, setOffering] = useState(false);

  const [actionError, setActionError] = useState(null);
  const [successMessage, setSuccessMessage] = useState(null);

  // Carga la información de la subasta.
  const loadSubasta = useCallback(async () => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetchSubasta(id, token);
      setSubasta(response);
    } catch (err) {
      setError(err.message || 'No se pudo cargar la subasta.');
    } finally {
      setLoading(false);
    }
  }, [id, token]);

  useEffect(() => {
    void loadSubasta();
  }, [loadSubasta]);

  // Los errores y mensajes desaparecen automáticamente.
  useEffect(() => {
    if (!actionError && !successMessage) return;

    const timeout = setTimeout(() => {
      setActionError(null);
      setSuccessMessage(null);
    }, 5000);

    return () => clearTimeout(timeout);
  }, [actionError, successMessage]);

  // Cerrar la subasta.
  const handleCloseAuction = async () => {
    setClosing(true);
    setActionError(null);
    setSuccessMessage(null);

    try {
      await cerrarSubasta(id, token);
      void navigate('/profile');
    } catch (err) {
      setActionError(err.message || 'No se pudo cerrar la subasta.');
      setShowCloseConfirmation(false);
    } finally {
      setClosing(false);
    }
  };

  // Realizar una oferta.
  const handleOffer = async (event) => {
    event.preventDefault();

    const numericBid = Number(puja);
    const minimumBid = getMinimumBid(subasta.ofertaActual);

    if (!Number.isFinite(numericBid) || numericBid < minimumBid) {
      setActionError(
        `La puja mínima debe ser ${formatNumber(minimumBid)}.`
      );
      return;
    }

    setOffering(true);
    setActionError(null);
    setSuccessMessage(null);

    try {
      await ofertarSubasta({
        subastaId: id,
        puja: numericBid,
        token,
      });

      setPuja('');
      setShowOfferForm(false);
      setSuccessMessage('¡Tu oferta se realizó correctamente!');

      await loadSubasta();
    } catch (err) {
      setActionError(err.message || 'No se pudo realizar la oferta.');
    } finally {
      setOffering(false);
    }
  };

  if (loading) {
    return (
      <main className="subasta-detail-page">
        <p className="subasta-page-message">Cargando subasta...</p>
      </main>
    );
  }

  if (error) {
    return (
      <main className="subasta-detail-page">
        <div className="subasta-page-error">
          <p>{error}</p>
          <button type="button" onClick={loadSubasta}>
            Reintentar
          </button>
        </div>
      </main>
    );
  }

  if (!subasta) return null;

  const { jugador, propietario, ofertasPrevias = [] } = subasta;

  const isOwner = user?.id === propietario?.id;
  const isActive = subasta.estado;

  const minimumBid = getMinimumBid(subasta.ofertaActual);

  return (
    <main className="subasta-detail-page">

      {/* Encabezado */}
      <header className="subasta-detail-header">
        <div>
          <span className="subasta-detail-eyebrow">
            MERCADO DE JUGADORES
          </span>

          <h1>{jugador?.nombre || 'Subasta'}</h1>

          <p>
            {jugador?.equipo} · {jugador?.liga} · {jugador?.posicion}
          </p>
        </div>

        <span className={`subasta-detail-status ${isActive ? 'active' : 'inactive'}`}>
          {isActive ? 'Subasta activa' : 'Subasta finalizada'}
        </span>
      </header>

      {/* Información principal */}
      <section className="subasta-overview">

        <div className="subasta-overview-item">
          <span>Propietario</span>
          <strong>{propietario?.nombre || 'Desconocido'}</strong>
        </div>

        <div className="subasta-overview-item">
          <span>Cotización del jugador</span>
          <strong>{formatNumber(jugador?.cotizacion)}</strong>
        </div>

        <div className="subasta-overview-item highlight">
          <span>Oferta más alta</span>
          <strong>
            {subasta.ofertaActual != null
              ? formatNumber(subasta.ofertaActual)
              : 'Sin ofertas'}
          </strong>
        </div>

        <div className="subasta-overview-item">
          <span>Cantidad ofrecida</span>
          <strong>{subasta.cantidadOfrecida}</strong>
        </div>

        <div className="subasta-overview-item">
          <span>Vencimiento</span>
          <strong>{formatDate(subasta.vencimiento)}</strong>
        </div>

      </section>

      {/* Estadísticas del jugador */}
      <section className="subasta-section">
        <h2>Estadísticas del jugador</h2>

        <div className="subasta-stats-grid">

          <div className="subasta-stat">
            <span>Goles</span>
            <strong>{jugador?.goles ?? '-'}</strong>
          </div>

          <div className="subasta-stat">
            <span>Asistencias</span>
            <strong>{jugador?.asistencias ?? '-'}</strong>
          </div>

          <div className="subasta-stat">
            <span>Cotizacion</span>
            <strong>{formatNumber(jugador?.cotizacion)}</strong>
          </div>

          <div className="subasta-stat">
            <span>Disparos por partido</span>
            <strong>{formatNumber(jugador?.disparosPorPartido)}</strong>
          </div>

          <div className="subasta-stat">
            <span>Pases clave por partido</span>
            <strong>{formatNumber(jugador?.keyPassesPorPartido)}</strong>
          </div>

          <div className="subasta-stat">
            <span>Regates ganados por partido</span>
            <strong>{formatNumber(jugador?.dribblesGanadosPorPartido)}</strong>
          </div>

          <div className="subasta-stat">
            <span>Faltas por partido</span>
            <strong>{formatNumber(jugador?.faltasCometidasPorPartido)}</strong>
          </div>

          <div className="subasta-stat">
            <span>Minutos jugados</span>
            <strong>{jugador?.minutosJugados ?? '-'}</strong>
          </div>

        </div>
      </section>

      {/* Información adicional */}
      <section className="subasta-section">
        <h2>Información del jugador</h2>

        <div className="subasta-player-details">
          <p>
            <span>Edad</span>
            <strong>{jugador?.edad ?? '-'} años</strong>
          </p>

          <p>
            <span>Altura</span>
            <strong>{jugador?.altura ?? '-'} cm</strong>
          </p>

          <p>
            <span>Peso</span>
            <strong>{jugador?.peso ?? '-'} kg</strong>
          </p>

          <p>
            <span>Tokens restantes</span>
            <strong>{jugador?.tokensRestantes ?? '-'}</strong>
          </p>
        </div>
      </section>

      {/* Historial de ofertas */}
      <section className="subasta-section">
        <h2>Historial de ofertas</h2>

        {ofertasPrevias.length === 0 ? (
          <p className="subasta-empty">
            Todavía no hubo ofertas para esta subasta.
          </p>
        ) : (
          <div className="subasta-table-container">
            <table className="subasta-offers-table">
              <thead>
                <tr>
                  <th>Usuario</th>
                  <th>Puja</th>
                  <th>Fecha</th>
                  <th>Estado</th>
                </tr>
              </thead>

              <tbody>
                {ofertasPrevias.map((oferta) => (
                  <tr key={oferta.id}>
                    <td>{oferta.usuario?.nombre || 'Desconocido'}</td>
                    <td className="subasta-table-bid">
                      {formatNumber(oferta.puja)}
                    </td>
                    <td>{formatDate(oferta.timestamp)}</td>
                    <td>
                      <span className={`offer-status ${oferta.estado ? 'active' : 'inactive'}`}>
                        {oferta.estado ? 'Activa' : 'Superada'}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      {/* Mensajes de acciones */}
      {actionError && (
        <div className="subasta-action-feedback error" role="alert">
          {actionError}
        </div>
      )}

      {successMessage && (
        <div className="subasta-action-feedback success">
          {successMessage}
        </div>
      )}

      {/* Acciones del usuario */}
      {isActive && (
        <section className="subasta-actions">

          {isOwner ? (
            <div className="subasta-close-container">

              {!showCloseConfirmation ? (
                <button
                  type="button"
                  className="subasta-close-button"
                  onClick={() => setShowCloseConfirmation(true)}
                  disabled={closing}
                >
                  Cerrar subasta
                </button>
              ) : (
                <div className="subasta-confirmation">
                  <p>¿Deseas cerrar la subasta?</p>

                  <div>
                    <button
                      type="button"
                      className="subasta-confirm-button"
                      onClick={handleCloseAuction}
                      disabled={closing}
                    >
                      {closing ? 'Cerrando...' : 'Aceptar'}
                    </button>

                    <button
                      type="button"
                      className="subasta-cancel-button"
                      onClick={() => setShowCloseConfirmation(false)}
                      disabled={closing}
                    >
                      Cancelar
                    </button>
                  </div>
                </div>
              )}

            </div>
          ) : (
            <div className="subasta-offer-container">

              {!showOfferForm ? (
                <button
                  type="button"
                  className="subasta-offer-button"
                  onClick={() => setShowOfferForm(true)}
                >
                  Realizar una oferta
                </button>
              ) : (
                <form
                  className="subasta-offer-form"
                  onSubmit={handleOffer}
                >
                  <h3>Realizar una oferta</h3>

                  <p>
                    Oferta más alta actual:{' '}
                    <strong>
                      {subasta.ofertaActual != null
                        ? formatNumber(subasta.ofertaActual)
                        : 'Sin ofertas'}
                    </strong>
                  </p>

                  <p>
                    Puja mínima:{' '}
                    <strong>{formatNumber(minimumBid)}</strong>
                  </p>

                  <label htmlFor="subasta-puja">
                    Tu oferta
                  </label>

                  <input
                    id="subasta-puja"
                    type="number"
                    min={minimumBid}
                    step="0.01"
                    value={puja}
                    onChange={(event) => setPuja(event.target.value)}
                    placeholder="Ingresá tu puja"
                    required
                    disabled={offering}
                  />

                  <div className="subasta-offer-form-actions">
                    <button
                      type="submit"
                      disabled={offering}
                    >
                      {offering ? 'Enviando oferta...' : 'Confirmar oferta'}
                    </button>

                    <button
                      type="button"
                      onClick={() => {
                        setShowOfferForm(false);
                        setActionError(null);
                      }}
                      disabled={offering}
                    >
                      Cancelar
                    </button>
                  </div>
                </form>
              )}

            </div>
          )}

        </section>
      )}

    </main>
  );
}
