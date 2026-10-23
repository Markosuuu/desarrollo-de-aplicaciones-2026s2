import { useParams } from "react-router-dom";
import { fetchPlayer } from "../api/playerApi";
import { useAuth } from "../context/AuthContext";
import { useEffect, useState } from "react";
import PlayerRadar from "../components/PlayerRadarStats";
import "../styles/playerPage.css"

import QuoteHistoryChart from '../components/QuoteHistoryChart';

const historialPrueba = [
    { fecha: '2026-09-21', precio: 250 },
    { fecha: '2026-09-28', precio: 275 },
    { fecha: '2026-10-05', precio: 265 },
    { fecha: '2026-10-12', precio: 250 },
    { fecha: '2026-10-19', precio: 190 },
    { fecha: '2026-10-30', precio: 250 },
    { fecha: '2026-11-10', precio: 350 },
    { fecha: '2026-11-21', precio: 50 },
    { fecha: '2026-11-28', precio: 95 },
    { fecha: '2026-12-05', precio: 265 },
    { fecha: '2026-12-12', precio: 310 },
    { fecha: '2026-12-19', precio: 290 },
];

export default function PlayerPage() {
    const [player, setPlayer] = useState(null);
    const { token } = useAuth();
    const { id } = useParams();

    useEffect(() => {
        const loadPlayer = async () => {
            const response = await fetchPlayer(id, token);
            setPlayer(response);
        };
        loadPlayer();
    }, [id, token]);

    if (!player) {
        return <div>Cargando...</div>;
    }
    return (
        <div className="playerPage-container">
            <h1 className="eyebrow texto-locura playerPage-titulo">
                {player.nombre}
            </h1>

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
                    <h2 className="playerPage-equipo">
                        Estadísticas por partido
                    </h2>
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
                            1
                        )}
                    />
                </section>
            </div>
            <section className="playerPage-card playerPage-info">
                <h2 className="playerPage-quote-section-title">
                    Datos de cotización
                </h2>

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
                                        ? new Date(player.fechaActualizacion).toLocaleDateString("es-AR", {
                                            day: "2-digit",
                                            month: "2-digit",
                                            year: "numeric",
                                        })
                                        : "Sin datos"}
                                </strong>
                            </p>
                        </div>

                        <div className="playerPage-quote-chart">
                            <div className="playerPage-quote-chart">
                                <QuoteHistoryChart data={historialPrueba} />
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </div>
    );

}
