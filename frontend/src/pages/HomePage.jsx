import { useEffect, useMemo, useState } from "react";
import { fetchPlayers } from "../api/playerApi";
import { useAuth } from "../context/AuthContext";
import { Blobatar } from "@blobatar/react";
import "blobatar/motion.css";
import liga_es from "../assets/liga-es.png";
import liga_al from "../assets/liga-al.png";
import liga_fr from "../assets/liga-fr.png";
import liga_it from "../assets/liga-it.png";
import liga_en from "../assets/liga-en.png";
import { useNavigate, Link } from "react-router-dom";

const PAGE_SIZE = 20;

export default function HomePage() {
  const { token } = useAuth();
  const [players, setPlayers] = useState([]);
  const [page, setPage] = useState(1);
  const [searchName, setSearchName] = useState("");
  const [searchEquipo, setSearchEquipo] = useState("");
  const [searchLiga, setSearchLiga] = useState("");

  const LIGAS = [
    "LaLiga",
    "Bundesliga",
    "Serie A",
    "Premier League",
    "Ligue 1",
  ];

  const [pagination, setPagination] = useState({
    pagina: 1,
    porPagina: PAGE_SIZE,
    total: 0,
    totalPaginas: 1,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    let active = true;

    setLoading(true);
    setError("");

    fetchPlayers({
      page,
      perPage: PAGE_SIZE,
      nombre: searchName.trim() || undefined,
      equipo: searchEquipo.trim() || undefined,
      liga: searchLiga || undefined,
      token: token,
    })
      .then((response) => {
        if (!active) {
          return;
        }

        const nextPlayers = response?.jugadores ?? [];
        setPlayers(nextPlayers);
        setPagination(
          response?.paginacion ?? {
            pagina: page,
            porPagina: PAGE_SIZE,
            total: nextPlayers.length,
            totalPaginas: 1,
          },
        );
      })
      .catch((fetchError) => {
        if (!active) {
          return;
        }

        setPlayers([]);
        setError(
          fetchError.message || "No se pudo cargar el catálogo de jugadores.",
        );
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [page, searchName, searchEquipo, searchLiga, token]);

  const filteredPlayers = useMemo(() => {
    const normalizedName = searchName.trim().toLowerCase();
    const normalizedEquipo = searchEquipo.trim().toLowerCase();

    return players.filter((player) => {
      const matchesName =
        !normalizedName ||
        player.nombre?.toLowerCase().includes(normalizedName);

      const matchesEquipo =
        !normalizedEquipo ||
        player.equipo?.toLowerCase().includes(normalizedEquipo);

      return matchesName && matchesEquipo;
    });
  }, [players, searchName, searchEquipo]);

  const totalPages = Math.max(pagination.totalPaginas || 1, 1);

  const handleSearchName = (event) => {
    setPage(1);
    setSearchName(event.target.value);
  };

  const handleSearchEquipo = (event) => {
    setPage(1);
    setSearchEquipo(event.target.value);
  };

  const handlePageChange = (nextPage) => {
    const safePage = Math.min(Math.max(nextPage, 1), totalPages);
    setPage(safePage);
  };

  const getIconoFromLiga = (liga) => {
    switch (liga) {
      case "LaLiga":
        return liga_es;
      case "Bundesliga":
        return liga_al;
      case "Ligue 1":
        return liga_fr;
      case "Serie A":
        return liga_it;
      case "Premier League":
        return liga_en;
      default:
        break;
    }
  };

  return (
    <main className="home-shell">
      <div className="home-panel">
        <header className="topbar">
          <div>
            <h1 className="eyebrow texto-locura">ProntaEntrega</h1>
          </div>
        </header>

        <section className="home-card catalog-card">
          <div>
            <h2>Catálogo de jugadores</h2>
          </div>
          <div className="catalog-header">
            

            <label className="search-field" aria-label="Buscar jugadores">
              <span className="search-label">Buscar por nombre</span>
              <input
                type="search"
                value={searchName}
                onChange={handleSearchName}
                placeholder="Nombre"
              />
            </label>
            <label className="search-field" aria-label="Buscar jugadores">
              <span className="search-label">Buscar por equipo</span>
              <input
                type="search"
                value={searchEquipo}
                onChange={handleSearchEquipo}
                placeholder="Equipo"
              />
            </label>
            <label className="search-field">
              <span className="search-label">Filtrar por Liga</span>

              <select
                value={searchLiga}
                onChange={(event) => {
                  setPage(1);
                  setSearchLiga(event.target.value);
                }}
              >
                <option value="">Todas las ligas</option>

                {LIGAS.map((liga) => (
                  <option key={liga} value={liga}>
                    {liga}
                  </option>
                ))}
              </select>
            </label>
          </div>

          {loading && (
            <div className="catalog-state">Cargando jugadores...</div>
          )}

          {error && <div className="catalog-state catalog-error">{error}</div>}

          {filteredPlayers.length === 0 && (
            <div className="catalog-state">
              No se encontraron jugadores para esta búsqueda.
            </div>
          )}

          {filteredPlayers.length > 0 && (
            <>
              <div className="catalog-summary">
                <span>{filteredPlayers.length} jugadores</span>
                <span>
                  Página {pagination.pagina} de {totalPages}
                </span>
              </div>

              <div className="player-grid">
                {filteredPlayers.map((player) => (
                  <Link
                    key={player.id ?? `${player.nombre}-${player.equipo}`}
                    className="player-card"
                    onClick={() => navigate('/player/'+player.id)}
                  >
                    <div className="player-card__top">
                      <span className="player-position">
                        {player.posicion || "Sin posición"}
                      </span>
                    </div>
                    <div className="player-card__name">
                      <Blobatar
                        name={player.nombre || "Jugador"}
                        animate="hover"
                        width={50}
                      />
                      <h3>{player.nombre || "Jugador sin nombre"}</h3>
                    </div>
                    <ul className="player-meta">
                      <li>
                        <span>Equipo</span>
                        <strong>{player.equipo || "—"}</strong>
                      </li>
                      <li>
                        <span>Liga</span>
                        <div className="player-meta__liga">
                          <img
                            src={getIconoFromLiga(player.liga)}
                            alt="icono-liga"
                            width={30}
                          />
                          <strong>{player.liga || "—"}</strong>
                        </div>
                      </li>
                      <li>
                        <span>Posición</span>
                        <strong>{player.posicion || "—"}</strong>
                      </li>
                    </ul>
                  </Link>
                ))}
              </div>

              <div className="pagination" aria-label="Paginación del catálogo">
                <button
                  type="button"
                  className="pager-button"
                  onClick={() => handlePageChange(page - 1)}
                  disabled={page <= 1}
                >
                  Anterior
                </button>

                <span>
                  Página {pagination.pagina} / {totalPages}
                </span>

                <button
                  type="button"
                  className="pager-button"
                  onClick={() => handlePageChange(page + 1)}
                  disabled={page >= totalPages}
                >
                  Siguiente
                </button>
              </div>
            </>
          )}
        </section>
      </div>
    </main>
  );
}
