const formatNumber = (value, decimals = 2) => {
  if (value == null) return "No disponible";

  return Number(value).toLocaleString("es-AR", {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
};

const EstadisticasJugador = ({ jugador }) => {
  return (
    <section className="subasta-section">
      <h2>Estadísticas del jugador</h2>

      <div className="subasta-stats-grid">
        <div className="subasta-stat">
          <span>Goles</span>
          <strong>{jugador?.goles ?? "-"}</strong>
        </div>

        <div className="subasta-stat">
          <span>Asistencias</span>
          <strong>{jugador?.asistencias ?? "-"}</strong>
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
          <strong>{jugador?.minutosJugados ?? "-"}</strong>
        </div>
      </div>
    </section>
  );
};

export default EstadisticasJugador;
