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

const DEFAULT_PAGE_SIZE = 20;

function formatNumber(value) {
  if (value === null || value === undefined || value === "") return "—";
  const number = Number(value);
  return Number.isFinite(number) ? numberFormatter.format(number) : "—";
}

const extraerContenido = (response) => {
  if (Array.isArray(response.content)) return response.content;
  if (Array.isArray(response.listings)) return response.subastas;
  return [];
};

function normalizeListingsResponse(response) {
  if (Array.isArray(response)) {
    return {
      content: response,
      page: 1,
      size: DEFAULT_PAGE_SIZE,
      totalElements: response.length,
      totalPages: 1,
      hasNext: false,
      hasPrevious: false,
    };
  }

  const content = extraerContenido(response);

  return {
    content,
    page: Number(response?.page ?? 1),
    size: Number(response?.size ?? DEFAULT_PAGE_SIZE),
    totalElements: Number(response?.totalElements ?? content.length),
    totalPages: Number(response?.totalPages ?? 1),
    hasNext: Boolean(response?.hasNext),
    hasPrevious: Boolean(response?.hasPrevious),
  };
}

const MercadoPage = () => {
  const { token } = useAuth();
  const navigate = useNavigate();
  const [pageData, setPageData] = useState({
    content: [],
    page: 1,
    size: DEFAULT_PAGE_SIZE,
    totalElements: 0,
    totalPages: 1,
    hasNext: false,
    hasPrevious: false,
  });
  const [page, setPage] = useState(1);
  const [search, setSearch] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [retry, setRetry] = useState(0);

  useEffect(() => {
    let active = true;

    setLoading(true);
    setError("");

    fetchListings(token, { page, size: DEFAULT_PAGE_SIZE })
      .then((response) => {
        if (!active) return;
        setPageData(normalizeListingsResponse(response));
      })
      .catch((fetchError) => {
        console.error("Error al cargar los anuncios:", fetchError);

        if (active) {
          setPageData({
            content: [],
            page: 1,
            size: DEFAULT_PAGE_SIZE,
            totalElements: 0,
            totalPages: 1,
            hasNext: false,
            hasPrevious: false,
          });
          setError(fetchError.message || "No se pudieron cargar los anuncios.");
        }
      })
      .finally(() => {
        if (active) setLoading(false);
      });

    return () => {
      active = false;
    };
  }, [page, retry, token]);

  const pageItems = pageData.content;

  const otherUsersListings = useMemo(() => {
    const term = search.trim().toLocaleLowerCase("es");

    return pageItems.filter((listing) => {
      if (!term) return true;

      return [listing.nombreJugador, listing.vendedorNombre]
        .filter(Boolean)
        .some((value) => value.toLocaleLowerCase("es").includes(term));
    });
  }, [pageItems, search]);

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
            <output className="catalog-state">Cargando anuncios...</output>
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

          {!loading &&
            !error &&
            search.trim() &&
            otherUsersListings.length === 0 && (
              <div className="catalog-state">
                No se encontraron anuncios para esta búsqueda.
              </div>
            )}

          {!loading && !error && !search.trim() && pageItems.length === 0 && (
            <div className="catalog-state">
              No existen anuncios publicados actualmente.
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

              {pageData.totalPages > 1 && (
                <div className="market-pagination">
                  <button
                    type="button"
                    className="pager-button"
                    disabled={!pageData.hasPrevious || page <= 1}
                    onClick={() =>
                      setPage((current) => Math.max(1, current - 1))
                    }
                  >
                    Anterior
                  </button>
                  <span className="pager-status">
                    Página {pageData.page} de {pageData.totalPages}
                  </span>
                  <button
                    type="button"
                    className="pager-button"
                    disabled={!pageData.hasNext || page >= pageData.totalPages}
                    onClick={() =>
                      setPage((current) =>
                        Math.min(pageData.totalPages, current + 1),
                      )
                    }
                  >
                    Siguiente
                  </button>
                </div>
              )}
            </>
          )}
        </section>
      </div>
    </main>
  );
};

export default MercadoPage;
