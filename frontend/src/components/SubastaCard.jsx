
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
    minute: '2-digit',
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
  const handleClick = () => {
    console.log('Subasta seleccionada:', subasta);
  };

  return (
    <button
      type="button"
      className="subasta-card"
      onClick={handleClick}
    >
      <div className="subasta-card-header">
        <div className="subasta-info-item">
          <span className="subasta-info-label">Jugador</span>
          <span className="subasta-info-value">
            {subasta.jugadorId}
          </span>
        </div>

        <span
          className={`subasta-status ${
            subasta.activa ? 'active' : 'inactive'
          }`}
        >
          {subasta.activa ? 'Activa' : 'Finalizada'}
        </span>
      </div>

      <div className="subasta-card-info">
        <div className="subasta-info-item">
          <span className="subasta-info-label">Jugador</span>
          <span className="subasta-info-value">
            {subasta.jugadorId}
          </span>
        </div>

        <div className="subasta-info-item">
          <span className="subasta-info-label">Propietario</span>
          <span className="subasta-info-value">
            {subasta.propietarioId}
          </span>
        </div>
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
          <span className="subasta-info-value subasta-offer">
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
