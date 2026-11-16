package com.prontaentrega.services;

import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.HistorialCotizacion;
import com.prontaentrega.persistence.repository.HistorialCotizacionRepository;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.services.exceptions.HistorialNoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class HistorialCotizacionService {

    private final HistorialCotizacionRepository historialCotizacionRepository;
    private final JugadorRepository jugadorRepository;

    public List<HistorialCotizacion> buscarHistorial(UUID jugadorId) {
        BolsaDeJugador bolsa = jugadorRepository.findById(jugadorId);

        return historialCotizacionRepository.buscarPorBolsaId(bolsa.getId());
    }

    public HistorialCotizacion buscarHistorialPorFecha(
            UUID jugadorId,
            LocalDate fecha) {
        BolsaDeJugador bolsa = jugadorRepository.findById(jugadorId);

        LocalDateTime inicioDia = fecha.atStartOfDay();

        Optional<HistorialCotizacion> anterior =
                historialCotizacionRepository
                        .findFirstByBolsaIdAndFechaLessThanOrderByFechaDesc(
                                bolsa.getId(),
                                inicioDia
                        );

        Optional<HistorialCotizacion> posterior =
                historialCotizacionRepository
                        .findFirstByBolsaIdAndFechaGreaterThanEqualOrderByFechaAsc(
                                bolsa.getId(),
                                inicioDia
                        );

        if (anterior.isEmpty() && posterior.isEmpty()) {
            throw new HistorialNoEncontradoException("El jugador no tiene historial de cotizaciones");
        }

        if (anterior.isEmpty()) {
            return posterior.get();
        }

        if (posterior.isEmpty()) {
            return anterior.get();
        }

        long distanciaAnterior = Math.abs(
                ChronoUnit.DAYS.between(
                        fecha,
                        anterior.get().getFecha().toLocalDate()
                )
        );

        long distanciaPosterior = Math.abs(
                ChronoUnit.DAYS.between(
                        fecha,
                        posterior.get().getFecha().toLocalDate()
                )
        );

        return distanciaAnterior <= distanciaPosterior
                ? anterior.get()
                : posterior.get();
    }
}
