package com.prontaentrega.services;

import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.HistorialCotizacion;
import com.prontaentrega.persistence.repository.HistorialCotizacionRepository;
import com.prontaentrega.persistence.repository.JugadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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
}
