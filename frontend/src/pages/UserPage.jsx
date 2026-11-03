import { useEffect, useState } from "react";
import { Blobatar } from "@blobatar/react";
import "blobatar/motion.css";
import { fetchUserInventory, fetchUserTransactions } from "../api/userApi";
import { useAuth } from "../context/AuthContext";
import "../styles/userPage.css";

const numberFormatter = new Intl.NumberFormat("es-AR", {
  maximumFractionDigits: 2,
});

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
  const [inventory, setInventory] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [retry, setRetry] = useState(0);

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
          fetchUserInventory(user.id, token),
          fetchUserTransactions(user.id, token),
        ]);

        if (active) {
          setInventory(inventoryResponse);
          setTransactions(transactionsResponse);
        }
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
  }, [retry, token, user?.id]);

  const currentUser = inventory.find(
    (item) => item.usuario?.id === user?.id,
  )?.usuario;
  const credits = currentUser?.creditosDisponibles ?? user?.creditosDisponibles;

  return (
    <section className="user-page-container">
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
          <p className="user-page-message" role="status">
            Cargando inventario...
          </p>
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
                  <th scope="col">Cantidad</th>
                  <th scope="col">Valor por token</th>
                  <th scope="col">Vender</th>
                </tr>
              </thead>
              <tbody>
                {inventory.map((item) => {
                  const player = item.jugador || {};
                  const totalTokens =
                    (Number(item.tokensDisponibles) || 0) +
                    (Number(item.tokensEnVenta) || 0);

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
                      <td>{formatNumber(totalTokens)}</td>
                      <td>{formatNumber(player.cotizacion)}</td>
                      <td>
                        <button
                          className="user-page-sell-button"
                          type="button"
                          aria-label={`Vender tokens de ${player.nombre || "jugador"}`}
                        >
                          Vender
                        </button>
                      </td>
                    </tr>
                  );
                })}
                {!error && inventory.length === 0 && (
                  <tr>
                    <td colSpan="8" className="user-page-message">
                      Todavía no tienes jugadores en tu inventario.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </article>

      <article className="historial-container user-page-card">
        <h2>Historial de operaciones</h2>
        {loading ? (
          <p className="user-page-message" role="status">
            Cargando historial...
          </p>
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
                      Todavía no tienes operaciones en tu historial.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        )}
      </article>
    </section>
  );
}

export default UserPage;
