package com.prontaentrega.controllers.dtos;

import com.prontaentrega.models.Jugador;
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
        String liga,
        Integer edad,
        Integer altura,
        Integer peso,
        String posicion,
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
    public static PlayerResponse from(Jugador jugador) {
        return new PlayerResponse(
                jugador.getId(),
                jugador.getNombre(),
                jugador.getEquipo(),
                jugador.getLiga(),
                jugador.getEdad(),
                jugador.getAltura(),
                jugador.getPeso(),
                jugador.getPosicion(),
                jugador.getActivo(),
                jugador.getGoles(),
                jugador.getAsistencias(),
                jugador.getDisparosPorPartido(),
                jugador.getPorcentajePasesExitosos(),
                jugador.getKeyPassesPorPartido(),
                jugador.getDribblesGanadosPorPartido(),
                jugador.getFaltasCometidasPorPartido(),
                jugador.getRating(),
                jugador.getMinutosJugados(),
                jugador.getCotizacionActual(),
                jugador.getFechaActualizacion(),
                jugador.getTokensRestantes()
        );
    }
}
