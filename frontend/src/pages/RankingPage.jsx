import { useAuth } from "../context/AuthContext";
import { getRanking } from "../api/playerApi";
import { useNavigate } from "react-router-dom";
import { useCallback, useEffect, useRef, useState } from "react";
import liga_es from "../assets/liga-es.png";
import liga_al from "../assets/liga-al.png";
import liga_fr from "../assets/liga-fr.png";
import liga_it from "../assets/liga-it.png";
import liga_en from "../assets/liga-en.png";
import "../styles/rankingPage.css"

export default function RankingPage() {
    const { token } = useAuth();
    const navigate = useNavigate();

    const [jugadores, setJugadores] = useState([]);
    const [paginacion, setPaginacion] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    const sentinelRef = useRef(null);
    const loadingRef = useRef(false);
    const paginacionRef = useRef(null);

    const cargarPagina = useCallback(async (page) => {
        if (!token || loadingRef.current) return;

        loadingRef.current = true;
        setLoading(true);
        setError(null);

        try {
            const response = await getRanking({
                page,
                perPage: 50,
                token,
            });

            setJugadores((actuales) =>
                page === 1
                    ? response.jugadores
                    : [...actuales, ...response.jugadores]
            );

            setPaginacion(response.paginacion);
            paginacionRef.current = response.paginacion;

        } catch (err) {
            setError(err.message || "No se pudo cargar el ranking.");
        } finally {
            loadingRef.current = false;
            setLoading(false);
        }
    }, [token]);

    // Carga inicial
    useEffect(() => {
        setJugadores([]);
        setPaginacion(null);
        paginacionRef.current = null;

        cargarPagina(1);
    }, [cargarPagina]);

    // Detecta cuando el usuario llega al final de la tabla
    useEffect(() => {
        const sentinel = sentinelRef.current;

        if (!sentinel || loading || error) return;

        const observer = new IntersectionObserver((entries) => {
            if (!entries[0].isIntersecting || loadingRef.current) return;

            const actual = paginacionRef.current;

            if (actual && actual.pagina < actual.totalPaginas) {
                cargarPagina(actual.pagina + 1);
            }
        });

        observer.observe(sentinel);

        return () => observer.disconnect();
    }, [loading, error, paginacion, cargarPagina]);

    const irAlJugador = (id) => {
        navigate(`/player/${id}`);
    };

    const formatearCotizacion = (cotizacion) => {
        return Number(cotizacion).toLocaleString("es-AR", {
            maximumFractionDigits: 2,
        });
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
        <div className="ranking-page">
            <h1>Ranking de jugadores</h1>

            <table className="ranking-table">
                <thead>
                    <tr>
                        <th>Nombre</th>
                        <th>Equipo</th>
                        <th>Liga</th>
                        <th>Cotización</th>
                    </tr>
                </thead>

                <tbody>
                    {jugadores.map((jugador) => (
                        <tr
                            key={jugador.id}
                            onClick={() => irAlJugador(jugador.id)}
                            className="ranking-row"
                            role="link"
                            tabIndex={0}
                            onKeyDown={(event) => {
                                if (event.key === "Enter") {
                                    irAlJugador(jugador.id);
                                }
                            }}
                        >
                            <td>{jugador.nombre}</td>
                            <td>{jugador.equipo}</td>
                            <td>
                                <div className="ranking-liga">
                                    <img
                                        src={getIconoFromLiga(jugador.liga)}
                                        alt={jugador.liga}
                                        title={jugador.liga}
                                        className="ranking-league-icon"
                                    />
                                    {jugador.liga}
                                </div>
                            </td>
                            <td>{formatearCotizacion(jugador.cotizacion)}</td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {loading && <p>Cargando jugadores...</p>}

            {error && (
                <div>
                    <p>{error}</p>
                    <button
                        onClick={() => cargarPagina(
                            paginacion ? paginacion.pagina + 1 : 1
                        )}
                    >
                        Reintentar
                    </button>
                </div>
            )}

            {!loading && !error && jugadores.length === 0 && (
                <p>No hay jugadores disponibles.</p>
            )}

            {/* Elemento observado para detectar el final de la lista */}
            <div ref={sentinelRef} />

            {paginacion && paginacion.pagina === paginacion.totalPaginas && (
                <p>Ya se cargaron todos los jugadores.</p>
            )}
        </div>
    );
}