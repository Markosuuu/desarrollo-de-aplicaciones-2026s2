package com.prontaentrega.controllers.dtos;

import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.Liga;
import com.prontaentrega.models.Posicion;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO de salida para exponer jugadores persistidos localmente.
 */
public record PlayerResponse(
        UUID id,
        String nombre,
        String equipo,
        Liga liga,
        Integer edad,
        Integer altura,
        Integer peso,
        Posicion posicion,
        Boolean activo,
        Integer goles,
        Integer asistencias,
        BigDecimal disparosPorPartido,
        BigDecimal porcentajePasesExitosos,
        BigDecimal keyPassesPorPartido,
        BigDecimal dribblesGanadosPorPartido,
        BigDecimal faltasCometidasPorPartido,
        BigDecimal rating,
        Integer minutosJugados,
        BigDecimal cotizacion,
        LocalDateTime fechaActualizacion,
        Integer tokensRestantes
) {

    /**
     * Construye la respuesta HTTP a partir de la entidad de dominio.
     */
    public static PlayerResponse from(BolsaDeJugador bolsa) {
        var estadisticas = bolsa.getEstadisticasDeportivas();
        var jugador = estadisticas.getJugador();
        return new PlayerResponse(
                jugador.getId(),
                jugador.getNombre(),
                jugador.getEquipo(),
                jugador.getLiga(),
                jugador.getEdad(),
                jugador.getAltura(),
                jugador.getPeso(),
                jugador.getPosicion(),
                estadisticas.getActivo(),
                estadisticas.getGoles(),
                estadisticas.getAsistencias(),
                estadisticas.getDisparosPorPartido(),
                estadisticas.getPorcentajePasesExitosos(),
                estadisticas.getKeyPassesPorPartido(),
                estadisticas.getDribblesGanadosPorPartido(),
                estadisticas.getFaltasCometidasPorPartido(),
                estadisticas.getRating(),
                estadisticas.getMinutosJugados(),
                bolsa.getCotizacionActual(),
                bolsa.getFechaActualizacion(),
                bolsa.getTokensRestantes()
        );
    }
}
