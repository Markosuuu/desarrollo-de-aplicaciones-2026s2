package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.cotizacion.HistorialCotizacion;
import com.prontaentrega.persistence.dto.jugadores.BolsaDeJugadorDTOSQL;
import com.prontaentrega.persistence.dto.cotizacion.HistorialCotizacionDTOSQL;
import com.prontaentrega.persistence.repository.HistorialCotizacionRepository;
import com.prontaentrega.persistence.repository.dao.HistorialCotizacionDAOSQL;
import com.prontaentrega.persistence.repository.dao.JugadorDAOSQL;
import com.prontaentrega.services.exceptions.cotizacion.NoHayEstrategiaActivaConfiguradaException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class HistorialCotizacionRepositoryImpl implements HistorialCotizacionRepository {

    private final HistorialCotizacionDAOSQL historialDAO;
    private final JugadorDAOSQL jugadorDAO;
    @Override
    public void guardarHistorial(List<HistorialCotizacion> historial) {
        if (historial.isEmpty()) {
            return;
        }

        List<UUID> bolsaIds = historial.stream()
                .map(HistorialCotizacion::getBolsaId)
                .distinct()
                .toList();

        Map<UUID, BolsaDeJugadorDTOSQL> bolsasPorId = jugadorDAO.findAllById(bolsaIds)
                .stream()
                .collect(Collectors.toMap(
                        BolsaDeJugadorDTOSQL::getId,
                        Function.identity()
                ));

        List<HistorialCotizacionDTOSQL> entidades = historial.stream()
                .map(registro -> {
                    BolsaDeJugadorDTOSQL bolsa = bolsasPorId.get(registro.getBolsaId());

                    if (bolsa == null) {
                        throw new NoHayEstrategiaActivaConfiguradaException(
                                "No existe la bolsa asociada al historial de cotización"
                        );
                    }

                    return HistorialCotizacionDTOSQL.desdeModelo(registro, bolsa);
                })
                .toList();

        historialDAO.saveAll(entidades);
    }

    @Override
    public List<HistorialCotizacion> buscarHistorialById(UUID jugadorId) {
        return historialDAO.buscarHistorialById(jugadorId)
                .stream()
                .map(HistorialCotizacionDTOSQL::aModelo)
                .toList();
    }

    @Override
    public void deleteAll() {
        historialDAO.deleteAll();
    }
}
