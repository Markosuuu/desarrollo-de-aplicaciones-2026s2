const formatNumber = (value, decimals = 2) => {
  if (value == null) return "No disponible";

  return Number(value).toLocaleString("es-AR", {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
};

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

const HistorialOfertas = (ofertas) => {
  return (
    <section className="subasta-section">
      <h2>Historial de ofertas</h2>

      {ofertas.length === 0 ? (
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
              {ofertas.map((oferta) => (
                <tr key={oferta.id}>
                  <td>{oferta.usuario?.nombre || "Desconocido"}</td>
                  <td className="subasta-table-bid">
                    {formatNumber(oferta.puja)}
                  </td>
                  <td>{formatDate(oferta.timestamp)}</td>
                  <td>
                    <span
                      className={`offer-status ${oferta.estado ? "active" : "inactive"}`}
                    >
                      {oferta.estado ? "Activa" : "Superada"}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
};

export default HistorialOfertas;
