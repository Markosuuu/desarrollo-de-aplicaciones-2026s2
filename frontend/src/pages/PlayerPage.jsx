import { useParams, useLocation, navigate } from "react-router-dom";
import { fetchPlayer, fetchPlayerQuotesHistory } from "../api/playerApi";
import { useAuth } from "../context/AuthContext";
import { useEffect, useState } from "react";
import PlayerRadar from "../components/PlayerRadarStats";
import "../styles/playerPage.css";

import { buyListing } from "../api/marketApi";

import { toast } from "react-toastify";

import QuoteHistoryChart from "../components/QuoteHistoryChart";

export default function PlayerPage() {
  const [player, setPlayer] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const { token } = useAuth();
  const { id } = useParams();
  const [retry, setRetry] = useState(0);
  const [quoteHistory, setQuoteHistory] = useState([]);

  const { state } = useLocation();
  const [submitting, setSubmitting] = useState(false);
  const [buyDialogOpen, setBuyDialogOpen] = useState(false);
  const [buyQuantity, setBuyQuantity] = useState("1");
  const initialListing = state?.listing?.id === id ? state.listing : null;
  const [listing] = useState(initialListing);
  const [creditsLoading] = useState(true);
  const [credits] = useState(null);

  const quantityToBuy = Number(buyQuantity);
  const availableTokens = Number(listing?.cantidadTokens) || 0;
  const purchaseTotal =
    (Number(listing?.precioPorToken) || 0) * (Number(buyQuantity) || 0);

  useEffect(() => {
    let active = true;

    const loadPlayer = async () => {
      setLoading(true);
      setError("");

      try {
        const response = await fetchPlayer(id, token);
        const history = await fetchPlayerQuotesHistory(id, token);
        if (active) {
          setPlayer(response);
          setQuoteHistory(history);
        }
      } catch (error) {
        if (active) {
          setError(
            error.message || "No se pudo cargar la información del jugador.",
          );
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    };

    void loadPlayer();

    return () => {
      active = false;
    };
  }, [id, token, retry]);

  if (loading) {
    return <div>Cargando jugador...</div>;
  }

  if (error) {
    return (
      <div className="playerPage-error">
        <h2>No se pudo cargar el jugador</h2>
        <button onClick={() => setRetry((prev) => prev + 1)}>Reintentar</button>
      </div>
    );
  }

  if (!player) {
    return <div>No se encontró el jugador.</div>;
  }

  const openBuyDialog = () => {
    if (submitting) return;
    setBuyQuantity("1");
    setBuyDialogOpen(true);
  };

  const handleBuy = async (event) => {
    event.preventDefault();
    if (submitting) return;

    if (
      !Number.isInteger(quantityToBuy) ||
      quantityToBuy <= 0 ||
      quantityToBuy > availableTokens
    ) {
      toast.error(`Ingresa una cantidad entre 1 y ${availableTokens}.`);
      return;
    }

    if (credits !== null && purchaseTotal > Number(credits)) {
      toast.error("No tienes créditos suficientes para esa cantidad");
      return;
    }

    setSubmitting(true);
    try {
      await buyListing(listing.id, quantityToBuy, token);
      void navigate("/profile", { replace: true });
      toast.success("Compra exitosa!");
    } catch (buyError) {
      console.error("Error al comprar el anuncio:", buyError);
      toast.error(buyError.message || "No se pudo completar la compra");
    } finally {
      setSubmitting(false);
    }
  };

  const numberFormatter = new Intl.NumberFormat("es-AR", {
    maximumFractionDigits: 2,
  });

  function formatNumber(value) {
    if (value === null || value === undefined || value === "") return "—";
    const number = Number(value);
    return Number.isFinite(number) ? numberFormatter.format(number) : "—";
  }

  return (
    <div className="playerPage-container">
      <header className="">
        <h1 className="eyebrow texto-locura playerPage-titulo">
          {player.nombre}
        </h1>
        <div>
          <button
            type="button"
            className="market-action-button"
            onClick={openBuyDialog}
            disabled={submitting || creditsLoading}
          >
            Comprar
          </button>
        </div>
      </header>

      <div className="playerPage-stats">
        <section className="playerPage-card playerPage-info">
          <h2 className="playerPage-equipo">Equipo: {player.equipo}</h2>

          <div className="playerPage-details">
            <p>
              <span>Posición</span>
              <strong>{player.posicion}</strong>
            </p>
            <p>
              <span>Edad</span>
              <strong>{player.edad} años</strong>
            </p>
            <p>
              <span>Altura</span>
              <strong>{player.altura} cm</strong>
            </p>
            <p>
              <span>Peso</span>
              <strong>{player.peso} kg</strong>
            </p>
            <p>
              <span>Minutos jugados</span>
              <strong>{player.minutosJugados}</strong>
            </p>
          </div>
        </section>

        <section className="playerPage-card playerPage-chart">
          <h2 className="playerPage-equipo">Estadísticas por partido</h2>
          <PlayerRadar
            disparos={player.disparosPorPartido}
            pases={player.keyPassesPorPartido}
            regates={player.dribblesGanadosPorPartido}
            faltas={player.faltasCometidasPorPartido}
            goles={player.goles}
            asist={player.asistencias}
            max={Math.max(
              player.disparosPorPartido,
              player.keyPassesPorPartido,
              player.dribblesGanadosPorPartido,
              player.faltasCometidasPorPartido,
              player.goles,
              player.asistencias,
              1,
            )}
          />
        </section>
      </div>
      <section className="playerPage-card playerPage-info">
        <h2 className="playerPage-quote-section-title">Datos de cotización</h2>

        <div className="playerPage-quote-content">
          <div className="playerPage-quote-content">
            <div className="playerPage-quote-details">
              <p>
                <span>Cotización actual:</span>
                <strong>{player.cotizacion}</strong>
              </p>

              <p>
                <span>Tokens restantes:</span>
                <strong>{player.tokensRestantes}</strong>
              </p>

              <p>
                <span>Ultima actualizacion:</span>
                <strong>
                  {player.fechaActualizacion
                    ? new Date(player.fechaActualizacion).toLocaleDateString(
                        "es-AR",
                        {
                          day: "2-digit",
                          month: "2-digit",
                          year: "numeric",
                        },
                      )
                    : "Sin datos"}
                </strong>
              </p>
            </div>

            <div className="playerPage-quote-chart">
              <div className="playerPage-quote-chart">
                <QuoteHistoryChart data={quoteHistory} />
              </div>
            </div>
          </div>
        </div>
      </section>
      {buyDialogOpen && listing && (
        <div className="offer-dialog-backdrop">
          <dialog
            open
            className="offer-buy-dialog"
            aria-modal="true"
            aria-labelledby="offer-buy-title"
          >
            <h2 id="offer-buy-title">Comprar tokens</h2>
            <p>
              Oferta de {listing.vendedorNombre || "vendedor"} para{" "}
              {listing.nombreJugador}.
            </p>
            <form onSubmit={handleBuy}>
              <label htmlFor="offer-buy-quantity">
                Cantidad (máximo {availableTokens})
              </label>
              <input
                autoFocus
                id="offer-buy-quantity"
                type="number"
                min="1"
                max={availableTokens}
                step="1"
                value={buyQuantity}
                onChange={(event) => {
                  setBuyQuantity(event.target.value);
                }}
                required
              />
              <p className="offer-buy-total">
                Total: <strong>{formatNumber(purchaseTotal)} créditos</strong>
              </p>
              <p className="offer-buy-balance">
                Créditos disponibles: {formatNumber(credits)}
              </p>
              <div className="offer-buy-actions">
                <button
                  type="button"
                  className="offer-buy-cancel"
                  onClick={() => setBuyDialogOpen(false)}
                  disabled={submitting}
                >
                  Volver
                </button>
                <button
                  type="submit"
                  className="market-action-button"
                  disabled={
                    submitting ||
                    !Number.isInteger(quantityToBuy) ||
                    quantityToBuy <= 0 ||
                    quantityToBuy > availableTokens
                  }
                >
                  {submitting ? "Procesando compra..." : "Confirmar compra"}
                </button>
              </div>
            </form>
          </dialog>
        </div>
      )}
    </div>
  );
}
