function formatNumber(value, decimals = 2) {
  if (value == null) return "No disponible";

  return Number(value).toLocaleString("es-AR", {
    minimumFractionDigits: decimals,
    maximumFractionDigits: decimals,
  });
}

const RealizarOferta = ({
  showOfferForm,
  setShowOfferForm,
  handleOffer,
  subasta,
  minimumBid,
  puja,
  setPuja,
  offering,
}) => {
  return (
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
        <form className="subasta-offer-form" onSubmit={handleOffer}>
          <h3>Realizar una oferta</h3>

          <p>
            Oferta más alta actual:{" "}
            <strong>
              {subasta.ofertaActual != null
                ? formatNumber(subasta.ofertaActual)
                : "Sin ofertas"}
            </strong>
          </p>

          <p>
            Puja mínima: <strong>{formatNumber(minimumBid)}</strong>
          </p>

          <label htmlFor="subasta-puja">Tu oferta</label>

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
            <button type="submit" disabled={offering}>
              {offering ? "Enviando oferta..." : "Confirmar oferta"}
            </button>

            <button
              type="button"
              onClick={() => {
                setShowOfferForm(false);
              }}
              disabled={offering}
            >
              Cancelar
            </button>
          </div>
        </form>
      )}
    </div>
  );
};

export default RealizarOferta;
