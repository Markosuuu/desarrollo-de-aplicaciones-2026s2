import { useEffect, useState } from "react";
import { Blobatar } from "@blobatar/react";
import "blobatar/motion.css";
import { useNavigate } from "react-router-dom";
import { createSellOrder } from "../api/marketApi";
import { fetchUserInventory, fetchUserTransactions } from "../api/userApi";
import { useAuth } from "../context/AuthContext";
import "../styles/userPage.css";

const numberFormatter = new Intl.NumberFormat("es-AR", {
  maximumFractionDigits: 2,
});

const PAGE_SIZE = 10;

function normalizePageResponse(response) {
  if (Array.isArray(response)) {
    return {
      content: response,
      page: 1,
      size: PAGE_SIZE,
      totalElements: response.length,
      totalPages: 1,
      hasNext: false,
      hasPrevious: false,
    };
  }

  const content = Array.isArray(response?.content) ? response.content : [];

  return {
    content,
    page: Number(response?.page ?? 1),
    size: Number(response?.size ?? PAGE_SIZE),
    totalElements: Number(response?.totalElements ?? content.length),
    totalPages: Number(response?.totalPages ?? 1),
    hasNext: Boolean(response?.hasNext),
    hasPrevious: Boolean(response?.hasPrevious),
  };
}

function formatNumber(value) {
  if (value === null || value === undefined || value === "") return "—";
  const number = Number(value);
  return Number.isFinite(number) ? numberFormatter.format(number) : "—";
}

function formatDate(value) {
  if (!value) return "—";

  const date = new Date(value);
  return Number.isNaN(date.getTime())
    ? "—"
    : date.toLocaleString("es-AR", {
        dateStyle: "short",
        timeStyle: "short",
      });
}

function UserPage() {
  const { token, user } = useAuth();
  const navigate = useNavigate();
  const [inventoryPageData, setInventoryPageData] = useState({
    content: [],
    page: 1,
    size: PAGE_SIZE,
    totalElements: 0,
    totalPages: 1,
    hasNext: false,
    hasPrevious: false,
  });
  const [transactionsPageData, setTransactionsPageData] = useState({
    content: [],
    page: 1,
    size: PAGE_SIZE,
    totalElements: 0,
    totalPages: 1,
    hasNext: false,
    hasPrevious: false,
  });
  const [inventoryPage, setInventoryPage] = useState(1);
  const [transactionsPage, setTransactionsPage] = useState(1);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [retry, setRetry] = useState(0);
  const [selectedInventoryItem, setSelectedInventoryItem] = useState(null);
  const [sellQuantity, setSellQuantity] = useState("");
  const [sellError, setSellError] = useState("");
  const [creatingOffer, setCreatingOffer] = useState(false);

  useEffect(() => {
    let active = true;

    async function loadUserData() {
      if (!user?.id) {
        setError("No se pudo identificar al usuario.");
        setLoading(false);
        return;
      }

      setLoading(true);
      setError("");

      try {
        const [inventoryResponse, transactionsResponse] = await Promise.all([
          fetchUserInventory(user.id, token, { page: inventoryPage, size: PAGE_SIZE }),
          fetchUserTransactions(user.id, token, { page: transactionsPage, size: PAGE_SIZE }),
        ]);

        if (!active) return;

        setInventoryPageData(normalizePageResponse(inventoryResponse));
        setTransactionsPageData(normalizePageResponse(transactionsResponse));
      } catch (loadError) {
        console.error("Error al cargar los datos del usuario:", loadError);

        if (active) {
          setError(
            loadError.message ||
              "No se pudo cargar el inventario y el historial de operaciones.",
          );
        }
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    }

    void loadUserData();

    return () => {
      active = false;
    };
  }, [retry, token, user?.id, inventoryPage, transactionsPage]);

  const inventory = inventoryPageData.content;
  const transactions = transactionsPageData.content;

  const currentUser = inventory.find(
    (item) => item.usuario?.id === user?.id,
  )?.usuario;
  const credits = currentUser?.creditosDisponibles ?? user?.creditosDisponibles;
  const availableTokens = Number(selectedInventoryItem?.tokensDisponibles) || 0;

  const openSellDialog = (item) => {
    setSelectedInventoryItem(item);
    setSellQuantity(String(item.tokensDisponibles));
    setSellError("");
  };

  const closeSellDialog = () => {
    if (creatingOffer) return;
    setSelectedInventoryItem(null);
    setSellError("");
  };

  const handleCreateOffer = async (event) => {
    event.preventDefault();
    const quantity = Number(sellQuantity);

    if (
      !Number.isInteger(quantity) ||
      quantity <= 0 ||
      quantity > availableTokens
    ) {
      setSellError(`Ingresa una cantidad entre 1 y ${availableTokens}.`);
      return;
    }

    setCreatingOffer(true);
    setSellError("");
    try {
      const listing = await createSellOrder(
        {
          jugadorId: selectedInventoryItem.jugador.id,
          cantidad: quantity,
        },
        token,
      );
      void navigate(`/mercado/${listing.id}`, {
        state: { listing },
      });
    } catch (createError) {
      console.error("Error al crear el anuncio de venta:", createError);
      setSellError(createError.message || "No se pudo crear la oferta.");
    } finally {
      setCreatingOffer(false);
    }
  };

  return (
    <main className="user-page-container">
      <header className="user-page-header">
        <h1 className="texto-locura eyebrow">{user?.nombre || "Usuario"}</h1>
        <p className="user-page-credits">
          Créditos <strong>{formatNumber(credits)}</strong>
        </p>
      </header>

      {error && (
        <div className="user-page-error" role="alert">
          <p>{error}</p>
          <button type="button" onClick={() => setRetry((value) => value + 1)}>
            Reintentar
          </button>
        </div>
      )}

      <article className="inventario-container user-page-card">
        <h2>Inventario</h2>
        {loading ? (
          <output className="user-page-message">Cargando inventario...</output>
        ) : (
          <div className="user-page-table-scroll">
            <table>
              <thead>
                <tr>
                  <th scope="col">Avatar</th>
                  <th scope="col">Jugador</th>
                  <th scope="col">Liga</th>
                  <th scope="col">Equipo</th>
                  <th scope="col">Posición</th>
                  <th scope="col">Cantidad disponible</th>
                  <th scope="col">Cantidad en venta</th>
                  <th scope="col">Valor por token</th>
                  <th scope="col">Vender</th>
                </tr>
              </thead>
              <tbody>
                {inventory.map((item) => {
                  const player = item.jugador || {};

                  return (
                    <tr key={item.id}>
                      <td>
                        <Blobatar
                          name={player.nombre || "Jugador"}
                          animate="hover"
                          width={40}
                        />
                      </td>
                      <td>{player.nombre || "—"}</td>
                      <td>{player.liga || "—"}</td>
                      <td>{player.equipo || "—"}</td>
                      <td>{player.posicion || "—"}</td>
                      <td>{formatNumber(item.tokensDisponibles)}</td>
                      <td>{formatNumber(item.tokensEnVenta)}</td>
                      <td>{formatNumber(player.cotizacion)}</td>
                      <td>
                        <button
                          className="user-page-sell-button"
                          type="button"
                          aria-label={`Vender tokens de ${player.nombre || "jugador"}`}
                          disabled={(Number(item.tokensDisponibles) || 0) <= 0}
                          onClick={() => openSellDialog(item)}
                        >
                          Crear oferta
                        </button>
                      </td>
                    </tr>
                  );
                })}
                {!error && inventory.length === 0 && (
                  <tr>
                    <td colSpan="9" className="user-page-message">
                      Inventario vacío
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
            {inventoryPageData.totalPages > 1 && (
              <div className="user-page-pager">
                <button
                  type="button"
                  className="pager-button"
                  disabled={!inventoryPageData.hasPrevious || inventoryPage <= 1}
                  onClick={() => setInventoryPage((value) => Math.max(1, value - 1))}
                >
                  Anterior
                </button>
                <span className="pager-status">
                  {inventoryPageData.page} / {inventoryPageData.totalPages}
                </span>
                <button
                  type="button"
                  className="pager-button"
                  disabled={!inventoryPageData.hasNext || inventoryPage >= inventoryPageData.totalPages}
                  onClick={() => setInventoryPage((value) => Math.min(inventoryPageData.totalPages, value + 1))}
                >
                  Siguiente
                </button>
              </div>
            )}
          </div>
        )}
      </article>

      <article className="historial-container user-page-card">
        <h2>Historial de operaciones</h2>
        {loading ? (
          <output className="user-page-message">Cargando historial...</output>
        ) : (
          <div className="user-page-table-scroll">
            <table>
              <thead>
                <tr>
                  <th scope="col">Operación</th>
                  <th scope="col">Estado</th>
                  <th scope="col">Fecha</th>
                  <th scope="col">Asociado</th>
                  <th scope="col">Precio total</th>
                  <th scope="col">Precio por token</th>
                  <th scope="col">Cantidad</th>
                  <th scope="col">Jugador</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((transaction) => {
                  const isSale = transaction.tipo === "VENTA";

                  return (
                    <tr key={transaction.id}>
                      <td>{transaction.tipo || "—"}</td>
                      <td>{transaction.estado || "—"}</td>
                      <td>{formatDate(transaction.creadaEn)}</td>
                      <td>{transaction.contraparteNombre || "—"}</td>
                      <td
                        className={
                          isSale
                            ? "user-page-amount user-page-amount--sale"
                            : "user-page-amount user-page-amount--purchase"
                        }
                      >
                        {isSale ? "+" : "-"}
                        {formatNumber(transaction.total)}
                      </td>
                      <td>{formatNumber(transaction.precioPorToken)}</td>
                      <td>{formatNumber(transaction.cantidad)}</td>
                      <td>{transaction.nombreJugador || "—"}</td>
                    </tr>
                  );
                })}
                {!error && transactions.length === 0 && (
                  <tr>
                    <td colSpan="8" className="user-page-message">
                      No hay operaciones registradas
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
            {transactionsPageData.totalPages > 1 && (
              <div className="user-page-pager">
                <button
                  type="button"
                  className="pager-button"
                  disabled={!transactionsPageData.hasPrevious || transactionsPage <= 1}
                  onClick={() => setTransactionsPage((value) => Math.max(1, value - 1))}
                >
                  Anterior
                </button>
                <span className="pager-status">
                  {transactionsPageData.page} / {transactionsPageData.totalPages}
                </span>
                <button
                  type="button"
                  className="pager-button"
                  disabled={!transactionsPageData.hasNext || transactionsPage >= transactionsPageData.totalPages}
                  onClick={() => setTransactionsPage((value) => Math.min(transactionsPageData.totalPages, value + 1))}
                >
                  Siguiente
                </button>
              </div>
            )}
          </div>
        )}
      </article>

      {selectedInventoryItem && (
        <div className="user-page-dialog-backdrop">
          <dialog
            open
            className="user-page-sell-dialog"
            aria-modal="true"
            aria-labelledby="user-page-sell-title"
          >
            <h2 id="user-page-sell-title">Crear oferta</h2>
            <p>
              {selectedInventoryItem.jugador?.nombre || "Jugador"} — {formatNumber(selectedInventoryItem.jugador?.cotizacion)} créditos por token
            </p>
            <form onSubmit={handleCreateOffer}>
              <label htmlFor="user-page-sell-quantity">
                Cantidad de tokens (disponibles: {availableTokens})
              </label>
              <input
                autoFocus
                id="user-page-sell-quantity"
                type="number"
                min="1"
                max={availableTokens}
                step="1"
                value={sellQuantity}
                onChange={(event) => setSellQuantity(event.target.value)}
                required
              />
              {sellError && (
                <p className="user-page-sell-error" role="alert">
                  {sellError}
                </p>
              )}
              <div className="user-page-dialog-actions">
                <button
                  type="button"
                  className="user-page-cancel-button"
                  onClick={closeSellDialog}
                  disabled={creatingOffer}
                >
                  Volver
                </button>
                <button type="submit" disabled={creatingOffer}>
                  {creatingOffer ? "Creando..." : "Publicar oferta"}
                </button>
              </div>
            </form>
          </dialog>
        </div>
      )}
    </main>
  );
}

export default UserPage;
