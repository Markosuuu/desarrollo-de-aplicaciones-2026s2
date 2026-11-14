import { useParams, useNavigate } from "react-router-dom";
import { fetchPlayer, fetchPlayerQuotesHistory } from "../api/playerApi";
import { useAuth } from "../context/AuthContext";
import { useEffect, useState } from "react";
import PlayerRadar from "../components/PlayerRadarStats";
import "../styles/playerPage.css";

import { comprarSuper } from "../api/marketApi";

import { toast } from "react-toastify";

import QuoteHistoryChart from "../components/QuoteHistoryChart";

export default function PlayerPage() {
  const [player, setPlayer] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const { user, token } = useAuth();
  const { id } = useParams();
  const [retry, setRetry] = useState(0);
  const [quoteHistory, setQuoteHistory] = useState([]);

  const [submitting, setSubmitting] = useState(false);
  const [modalCompra, setModalCompra] = useState(false);
  const [cantidad, setCantidad] = useState("1");
  const [creditos] = useState(user?.creditosDisponibles || 0);

  const navigate = useNavigate();

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

  const cantidadAComprar = Number(cantidad);
  const tokensDisponibles = Number(player?.tokensRestantes) || 0;
  const precioFinal =
    (Number(player?.cotizacion) || 0) * (Number(cantidad) || 0);

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
    setCantidad("1");
    setModalCompra(true);
  };

  const handleBuy = async (event) => {
    event.preventDefault();
    if (submitting) return;

    if (
      !Number.isInteger(cantidadAComprar) ||
      cantidadAComprar <= 0 ||
      cantidadAComprar > tokensDisponibles
    ) {
      toast.error(`Ingresa una cantidad entre 1 y ${tokensDisponibles}.`);
      return;
    }

    if (creditos !== null && precioFinal > Number(creditos)) {
      toast.error("No tienes créditos suficientes para esa cantidad");
      return;
    }

    setSubmitting(true);
    try {
      await comprarSuper(player?.id, cantidadAComprar, token);
      void navigate("/profile", { replace: true });
      toast.success("Compra exitosa!");
    } catch (buyError) {
      console.error("Error al comprar al jugador:", buyError);
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
            disabled={submitting}
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
      {modalCompra && (
        <div className="offer-dialog-backdrop">
          <dialog
            open
            className="offer-buy-dialog"
            aria-modal="true"
            aria-labelledby="offer-buy-title"
          >
            <h2 id="offer-buy-title">Comprar tokens</h2>
            <form onSubmit={handleBuy}>
              <label htmlFor="offer-buy-quantity">
                Cantidad (máximo {tokensDisponibles})
              </label>
              <input
                autoFocus
                id="offer-buy-quantity"
                type="number"
                min="1"
                max={tokensDisponibles}
                step="1"
                value={cantidad}
                onChange={(event) => {
                  setCantidad(event.target.value);
                }}
                required
              />
              <p className="offer-buy-total">
                Total: <strong>{formatNumber(precioFinal)} créditos</strong>
              </p>
              <p className="offer-buy-balance">
                Créditos disponibles: {formatNumber(creditos)}
              </p>
              <div className="offer-buy-actions">
                <button
                  type="button"
                  className="offer-buy-cancel"
                  onClick={() => setModalCompra(false)}
                  disabled={submitting}
                >
                  Volver
                </button>
                <button
                  type="submit"
                  className="market-action-button"
                  disabled={
                    submitting ||
                    !Number.isInteger(cantidadAComprar) ||
                    cantidadAComprar <= 0 ||
                    cantidadAComprar > tokensDisponibles
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
