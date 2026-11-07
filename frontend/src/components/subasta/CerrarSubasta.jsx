const CerrarSubasta = ({
  showCloseConfirmation,
  setShowCloseConfirmation,
  closing,
  handleCloseAuction,
}) => {
  return (
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
              {closing ? "Cerrando..." : "Aceptar"}
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
  );
};

export default CerrarSubasta;
