const DatosJugador = ({ jugador }) => {
  return (
    <section className="subasta-section">
      <h2>Información del jugador</h2>

      <div className="subasta-player-details">
        <p>
          <span>Edad</span>
          <strong>{jugador?.edad ?? "-"} años</strong>
        </p>

        <p>
          <span>Altura</span>
          <strong>{jugador?.altura ?? "-"} cm</strong>
        </p>

        <p>
          <span>Peso</span>
          <strong>{jugador?.peso ?? "-"} kg</strong>
        </p>

        <p>
          <span>Tokens restantes</span>
          <strong>{jugador?.tokensRestantes ?? "-"}</strong>
        </p>
      </div>
    </section>
  );
};

export default DatosJugador;
