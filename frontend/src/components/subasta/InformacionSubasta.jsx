function formatNumber(value, decimals = 2) {
  if (value == null) return "No disponible";

  return Number(value).toLocaleString("es-AR", {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}

function formatDate(date, includeTime = true) {
  if (!date) return "Sin fecha";

  const parsedDate = new Date(date);

  if (Number.isNaN(parsedDate.getTime())) {
    return "Fecha inválida";
  }

  return new Intl.DateTimeFormat("es-AR", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    ...(includeTime && {
      hour: "2-digit",
    }),
  }).format(parsedDate);
}

const InformacionSubasta = ({ propietario, jugador, subasta = {} }) => {
  return (
    <section className="subasta-overview">
      <div className="subasta-overview-item">
        <span>Propietario</span>
        <strong>{propietario?.nombre || "Desconocido"}</strong>
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
            : "Sin ofertas"}
        </strong>
      </div>

      <div className="subasta-overview-item">
        <span>Cantidad ofrecida</span>
        <strong>{subasta.cantidadOfrecida ?? "-"}</strong>
      </div>

      <div className="subasta-overview-item">
        <span>Vencimiento</span>
        <strong>{formatDate(subasta.vencimiento)}</strong>
      </div>
    </section>
  );
};

export default InformacionSubasta;
