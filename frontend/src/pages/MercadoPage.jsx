import { useEffect, useMemo, useState } from "react";
import { Blobatar } from "@blobatar/react";
import "blobatar/motion.css";
import { useNavigate } from "react-router-dom";
import { fetchListings } from "../api/marketApi";
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

const MercadoPage = () => {
  const { token } = useAuth();
  const navigate = useNavigate();
  const [listings, setListings] = useState([]);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [retry, setRetry] = useState(0);

  useEffect(() => {
    let active = true;

    setLoading(true);
    setError("");

    fetchListings(token)
      .then((response) => {
        if (active) setListings(response);
      })
      .catch((fetchError) => {
        console.error("Error al cargar los anuncios:", fetchError);

        if (active) {
          setListings([]);
          setError(fetchError.message || "No se pudieron cargar los anuncios.");
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [retry, token]);

  const otherUsersListings = useMemo(() => {
    const term = search.trim().toLocaleLowerCase("es");

    return listings.filter((listing) => {
      if (!term) return true;

      return [listing.nombreJugador, listing.vendedorNombre]
        .filter(Boolean)
        .some((value) => value.toLocaleLowerCase("es").includes(term));
    });
  }, [listings, search]);

  return (
    <main className="home-shell mercado-shell">
      <div className="home-panel">
        <header className="topbar">
          <h1 className="eyebrow texto-locura">ProntaEntrega</h1>
        </header>

        <section className="home-card catalog-card">
          <div className="catalog-header">
            <div>
              <p className="welcome">Anuncios disponibles para comprar</p>
              <h2>Mercado de tokens</h2>
            </div>

            <label className="search-field" aria-label="Buscar anuncios">
              <span className="search-label">Buscar</span>
              <input
                type="search"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Jugador o vendedor"
              />
            </label>
          </div>

          {loading && (
            <div className="catalog-state" role="status">
              Cargando anuncios...
            </div>
          )}

          {error && (
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

          {!loading && !error && otherUsersListings.length === 0 && (
            <div className="catalog-state">
              {search.trim()
                ? "No se encontraron anuncios para esta búsqueda."
                : "Todavía no hay anuncios disponibles de otros usuarios."}
            </div>
          )}

          {!loading && !error && otherUsersListings.length > 0 && (
            <>
              <div className="catalog-summary">
                <span>
                  {otherUsersListings.length}{" "}
                  {otherUsersListings.length === 1 ? "anuncio" : "anuncios"}
                </span>
              </div>

              <div className="player-grid">
                {otherUsersListings.map((listing) => (
                  <article
                    className="player-card market-listing-card"
                    key={listing.id}
                  >
                    <div className="player-card__name">
                      <Blobatar
                        name={listing.nombreJugador || "Jugador"}
                        animate="hover"
                        width={50}
                      />
                      <h3>{listing.nombreJugador || "Jugador"}</h3>
                    </div>

                    <ul className="player-meta">
                      <li>
                        <span>Vendedor</span>
                        <strong>{listing.vendedorNombre || "—"}</strong>
                      </li>
                      <li>
                        <span>Cantidad</span>
                        <strong>
                          {formatNumber(listing.cantidadTokens)} tokens
                        </strong>
                      </li>
                      <li>
                        <span>Precio por token</span>
                        <strong>
                          {formatNumber(listing.precioPorToken)} créditos
                        </strong>
                      </li>
                      <li>
                        <span>Precio total</span>
                        <strong>
                          {formatNumber(listing.precioTotal)} créditos
                        </strong>
                      </li>
                    </ul>

                    <button
                      type="button"
                      className="market-action-button"
                      onClick={() =>
                        navigate(`/mercado/${listing.id}`, {
                          state: { listing },
                        })
                      }
                    >
                      Comprar
                    </button>
                  </article>
                ))}
              </div>
            </>
          )}
        </section>
      </div>
    </main>
  );
};

export default MercadoPage;
