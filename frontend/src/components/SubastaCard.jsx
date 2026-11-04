
import { useNavigate } from 'react-router-dom';
import '../styles/subastaCard.css';

function formatDate(date) {
  if (!date) return 'Sin fecha';
  const parsedDate = new Date(date);

  if (Number.isNaN(parsedDate.getTime())) {
    return 'Fecha inválida';
  }

  return new Intl.DateTimeFormat('es-AR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
  }).format(parsedDate);
}

function formatOffer(offer) {
  if (offer == null) return 'Sin ofertas';

  if (typeof offer === 'number') {
    return offer.toLocaleString('es-AR');
  }

  return String(offer);
}

export default function SubastaCard({ subasta }) {
    const navigate = useNavigate()

  const handleClick = () => {
    console.log(`/subasta/${subasta.id}`)
    void navigate(`/subasta/${subasta.id}`)
  };

  return (
    <button
      type="button"
      className="subasta-card"
      onClick={handleClick}
    >
      <div className="subasta-card-header">
        <div className="subasta-card-title">
          <span className="subasta-player-name">
            {subasta.jugador?.nombre || 'Jugador desconocido'}
          </span>
          <span className="subasta-player-team">
            {subasta.jugador?.equipo || 'Equipo desconocido'}
            {subasta.jugador?.liga && ` · ${subasta.jugador.liga}`}
          </span>
        </div>
        <div className="subasta-info-item">
          <span className="subasta-info-label">Cotización actual</span>
          <span className="subasta-info-value subasta-offer">
            {subasta.jugador?.cotizacion != null
              ? subasta.jugador.cotizacion.toLocaleString('es-AR', {
                  minimumFractionDigits: 2,
                  maximumFractionDigits: 2,
                })
              : 'No disponible'}
          </span>
        </div>
        <div className="subasta-card-info">
        <div className="subasta-info-item">
          <span className="subasta-info-label">Propietario</span>
          <span className="subasta-info-value">
            {subasta.propietario?.nombre || 'Desconocido'}
          </span>
        </div>
      </div>
        <span
          className={`subasta-status ${
            subasta.activa ? 'active' : 'inactive'
          }`}
        >
          {subasta.activa ? 'Activa' : 'Finalizada'}
        </span>
      </div>

      <div className="subasta-card-footer">
        <div className="subasta-info-item">
          <span className="subasta-info-label">Cantidad ofrecida</span>
          <span className="subasta-info-value">
            {subasta.cantidadOfrecida}
          </span>
        </div>

        <div className="subasta-info-item">
          <span className="subasta-info-label">Oferta actual</span>
          <span className="subasta-info-value">
            {formatOffer(subasta.ofertaActual)}
          </span>
        </div>

        <div className="subasta-info-item">
          <span className="subasta-info-label">Vencimiento</span>
          <span className="subasta-info-value">
            {formatDate(subasta.vencimiento)}
          </span>
        </div>
      </div>
    </button>
  );
}
