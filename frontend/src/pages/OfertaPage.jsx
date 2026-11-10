import { useEffect, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import { buyListing, cancelListing, fetchListings } from "../api/marketApi";
import { fetchUserInventory } from "../api/userApi";
import { useAuth } from "../context/AuthContext";
import "../styles/mercadoPage.css";

const numberFormatter = new Intl.NumberFormat("es-AR", {
  maximumFractionDigits: 2,
});

function formatNumber(value) {
  if (value === null || value === undefined || value === "") return "—";
  const number = Number(value);
  return Number.isFinite(number) ? numberFormatter.format(number) : "—";
}

export default function OfertaPage() {
  const { id } = useParams();
  const { state } = useLocation();
  const navigate = useNavigate();
  const { user, token } = useAuth();
  const initialListing = state?.listing?.id === id ? state.listing : null;
  const [listing, setListing] = useState(initialListing);
  const [credits, setCredits] = useState(null);
  const [creditsLoading, setCreditsLoading] = useState(true);
  const [creditsError, setCreditsError] = useState("");
  const [loading, setLoading] = useState(!initialListing);
  const [error, setError] = useState("");
  const [actionError, setActionError] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [buyDialogOpen, setBuyDialogOpen] = useState(false);
  const [buyQuantity, setBuyQuantity] = useState("1");
  const [retry, setRetry] = useState(0);

  useEffect(() => {
    let active = true;

    if (initialListing) {
      setListing(initialListing);
      setLoading(false);
      setError("");
    } else {
      setLoading(true);
      setError("");

      fetchListings(token)
        .then((listings) => {
          if (!active) return;
          const foundListing = listings.find((item) => item.id === id);
          setListing(foundListing || null);
          if (!foundListing) setError("No se encontró el anuncio solicitado.");
        })
        .catch((fetchError) => {
          console.error("Error al cargar el anuncio:", fetchError);
          if (active) {
            setListing(null);
            setError(
              fetchError.message ||
                "No se pudo cargar la información del anuncio.",
            );
          }
        })
        .finally(() => {
          if (active) setLoading(false);
        });
    }

    if (!user?.id) {
      setCreditsLoading(false);
      setCreditsError("No se pudo identificar al usuario.");
    } else {
      setCreditsLoading(true);
      setCreditsError("");
      fetchUserInventory(user.id, token)
        .then((inventory) => {
          if (!active) return;
          console.log(inventory)
          const currentUser = inventory.content.find(
            (item) => item.usuario?.id === user.id,
          )?.usuario;
          console.log(currentUser)
          setCredits(
            currentUser?.creditosDisponibles ??
              user.creditosDisponibles ??
              null,
          );
        })
        .catch((fetchError) => {
          console.error(
            "Error al cargar los créditos del usuario:",
            fetchError,
          );
          if (active) {
            setCredits(null);
            setCreditsError("No se pudieron cargar los créditos actuales.");
          }
        })
        .finally(() => {
          if (active) setCreditsLoading(false);
        });
    }

    return () => {
      active = false;
    };
  }, [id, initialListing, retry, token, user, user?.id]);

  const isOwner = listing?.vendedorId === user?.id;
  const isPending = listing?.estado === "PENDIENTE";
  const availableTokens = Number(listing?.cantidadTokens) || 0;
  const quantityToBuy = Number(buyQuantity);
  const purchaseTotal =
    (Number(listing?.precioPorToken) || 0) * (Number(buyQuantity) || 0);

  const openBuyDialog = () => {
    if (!listing || isOwner || !isPending || submitting) return;
    setBuyQuantity("1");
    setActionError("");
    setBuyDialogOpen(true);
  };

  const handleBuy = async (event) => {
    event.preventDefault();
    if (!listing || isOwner || !isPending || submitting) return;

    if (
      !Number.isInteger(quantityToBuy) ||
      quantityToBuy <= 0 ||
      quantityToBuy > availableTokens
    ) {
      setActionError(`Ingresa una cantidad entre 1 y ${availableTokens}.`);
      return;
    }

    if (credits !== null && purchaseTotal > Number(credits)) {
      setActionError("No tienes créditos suficientes para esa cantidad.");
      return;
    }

    setSubmitting(true);
    setActionError("");
    try {
      await buyListing(listing.id, quantityToBuy, token);
      void navigate("/profile", { replace: true });
    } catch (buyError) {
      console.error("Error al comprar el anuncio:", buyError);
      setActionError(buyError.message || "No se pudo completar la compra.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = async () => {
    if (!listing || !isOwner || !isPending || submitting) return;

    const confirmed = window.confirm(
      `¿Quieres cancelar la oferta de ${listing.nombreJugador}?`,
    );
    if (!confirmed) return;

    setSubmitting(true);
    setActionError("");
    try {
      await cancelListing(listing.id, token);
      setListing((current) => ({ ...current, estado: "CANCELADA" }));
    } catch (cancelError) {
      console.error("Error al cancelar el anuncio:", cancelError);
      setActionError(cancelError.message || "No se pudo cancelar la oferta.");
    } finally {
      setSubmitting(false);
    }
  };

  // Para arreglar issues de Sonar
  let creditsLabel;

  if (creditsLoading) {
    creditsLabel = "Cargando...";
  } else if (creditsError) {
    creditsLabel = "No disponible";
  } else {
    creditsLabel = `${formatNumber(credits)} créditos`;
  }

  let offerAction;

  if (!isPending) {
    offerAction = (
      <p className="offer-not-available">Esta oferta ya no está disponible.</p>
    );
  } else if (isOwner) {
    offerAction = (
      <button
        type="button"
        className="market-action-button market-action-button--secondary"
        onClick={handleCancel}
        disabled={submitting}
      >
        {submitting ? "Cancelando..." : "Cancelar oferta"}
      </button>
    );
  } else {
    offerAction = (
      <button
        type="button"
        className="market-action-button"
        onClick={openBuyDialog}
        disabled={submitting || creditsLoading}
      >
        Comprar
      </button>
    );
  }

  return (
    <main className="home-shell mercado-shell">
      <div className="home-panel">
        <header className="topbar">
          <h1 className="eyebrow texto-locura">ProntaEntrega</h1>
        </header>

        <section className="home-card offer-detail-card">
          <Link className="offer-back-link" to="/mercado">
            Volver al mercado
          </Link>

          {loading && (
            <output className="catalog-state">Cargando oferta...</output>
          )}

          {!loading && error && (
            <div className="catalog-state catalog-error" role="alert">
              <p>{error}</p>
              <button
                type="button"
                className="pager-button"
                onClick={() => setRetry((value) => value + 1)}
              >
                Reintentar
              </button>
            </div>
          )}

          {!loading && listing && (
            <>
              <div className="offer-detail-heading">
                <div>
                  <p className="welcome">Detalle de la oferta</p>
                  <h2>{listing.nombreJugador}</h2>
                </div>
                <span className="offer-status">{listing.estado}</span>
              </div>

              <dl className="offer-detail-list">
                <div>
                  <dt>Vendedor</dt>
                  <dd>{listing.vendedorNombre || "—"}</dd>
                </div>
                <div>
                  <dt>Cantidad de tokens</dt>
                  <dd>{formatNumber(listing.cantidadTokens)}</dd>
                </div>
                <div>
                  <dt>Precio por token</dt>
                  <dd>{formatNumber(listing.precioPorToken)} créditos</dd>
                </div>
                <div>
                  <dt>Precio total</dt>
                  <dd>{formatNumber(listing.precioTotal)} créditos</dd>
                </div>
                <div>
                  <dt>Tus créditos actuales</dt>
                  <dd>{creditsLabel}</dd>
                </div>
              </dl>

              {creditsError && (
                <div className="offer-inline-error" role="alert">
                  <p>{creditsError}</p>
                  <button
                    type="button"
                    className="offer-retry-button"
                    onClick={() => setRetry((value) => value + 1)}
                  >
                    Reintentar
                  </button>
                </div>
              )}
              {actionError && (
                <p className="offer-inline-error" role="alert">
                  {actionError}
                </p>
              )}
              {offerAction}
            </>
          )}
        </section>
      </div>

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
                  setActionError("");
                }}
                required
              />
              <p className="offer-buy-total">
                Total: <strong>{formatNumber(purchaseTotal)} créditos</strong>
              </p>
              <p className="offer-buy-balance">
                Créditos disponibles: {formatNumber(credits)}
              </p>
              {actionError && (
                <p className="offer-inline-error" role="alert">
                  {actionError}
                </p>
              )}
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
    </main>
  );
}
