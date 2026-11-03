package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.HistorialCotizacion;
import com.prontaentrega.persistence.dto.BolsaDeJugadorDTOSQL;
import com.prontaentrega.persistence.dto.HistorialCotizacionDTOSQL;
import com.prontaentrega.persistence.repository.CotizacionRepository;
import com.prontaentrega.persistence.repository.HistorialCotizacionRepository;
import com.prontaentrega.persistence.repository.dao.HistorialCotizacionDAOSQL;
import com.prontaentrega.persistence.repository.dao.JugadorDAOSQL;
import com.prontaentrega.services.exceptions.QuoteOperationConflictException;
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
                        throw new QuoteOperationConflictException(
                                "No existe la bolsa asociada al historial de cotización"
                        );
                    }

                    return HistorialCotizacionDTOSQL.desdeModelo(registro, bolsa);
                })
                .toList();

        historialDAO.saveAll(entidades);
    }

    @Override
    public List<HistorialCotizacion> buscarPorBolsaId(UUID bolsaId) {
        return historialDAO
                .findByBolsaDeJugador_IdOrderByFechaAsc(bolsaId)
                .stream()
                .map(HistorialCotizacionDTOSQL::aModelo)
                .toList();
    }

    @Override
    public void deleteAll() {
        historialDAO.deleteAll();
    }
}
