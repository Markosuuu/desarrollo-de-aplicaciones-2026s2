import { useParams } from "react-router-dom";
import { fetchPlayer } from "../api/playerApi";
import { useAuth } from "../context/AuthContext";
import { useEffect, useState } from "react";
import PlayerRadar from "../components/PlayerRadarStats";
import "../styles/playerPage.css"

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
        <div className="playerPage-container ">
            <h1 className="eyebrow texto-locura playerPage-titulo">{player.nombre}</h1>
            <div className="playerPage-stats">
                <div>
                    <h2>Equipo: {player.equipo}</h2>
                    <h4>Posición: {player.posicion}</h4>
                    <h4>Edad: {player.edad} años</h4>
                    <h4>altura: {player.altura}cm</h4>
                    <h4>Peso: {player.peso}kg</h4>
                    <h4>Minutos jugados: {player.minutosJugados}</h4>
                </div>
                <PlayerRadar 
                    disparos={player.disparosPorPartido}
                    pases={player.keyPassesPorPartido}
                    regates={player.dribblesGanadosPorPartido}
                    faltas={player.faltasCometidasPorPartido}
                    goles={player.goles}
                    asist={player.asistencias}
                    max={
                        Math.max(player.disparosPorPartido, 
                            player.keyPassesPorPartido, 
                            player.dribblesGanadosPorPartido,
                            player.faltasCometidasPorPartido, 
                            player.goles,
                            player.asistencias
                        )
                    }
                />
                
            </div>
        </div>
    );
}
