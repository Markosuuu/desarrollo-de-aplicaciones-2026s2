package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.ConfiguracionEstrategiaCotizacion;
import com.prontaentrega.models.DatosValoracion;
import com.prontaentrega.models.EstadoEstrategiaCotizacion;
import com.prontaentrega.persistence.dto.EstadoEstrategiaCotizacionDTOSQL;
import com.prontaentrega.persistence.repository.ConfiguracionEstrategiaProvider;
import com.prontaentrega.persistence.repository.CotizacionRepository;
import com.prontaentrega.persistence.repository.dao.QuoteRecalculationDAOSQL;
import com.prontaentrega.persistence.repository.dao.EstadoEstrategiaCotizacionDAOSQL;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Repository;

/** Adapta las entidades SQL al modelo de configuración de estrategias. */
@Repository
public class CotizacionRepositoryImpl implements CotizacionRepository {
    private final EstadoEstrategiaCotizacionDAOSQL estadoDAO;
    private final QuoteRecalculationDAOSQL recalculationDAO;
    private final Map<String, ConfiguracionEstrategiaProvider> configuracionesPorEstrategia;

    /** Construye el adaptador con sus DAOs relacionales. */
    public CotizacionRepositoryImpl(
            EstadoEstrategiaCotizacionDAOSQL estadoDAO,
            QuoteRecalculationDAOSQL recalculationDAO,
            List<ConfiguracionEstrategiaProvider> providers) {
        this.estadoDAO = estadoDAO;
        this.recalculationDAO = recalculationDAO;
        this.configuracionesPorEstrategia = new HashMap<>();
        for (ConfiguracionEstrategiaProvider provider : providers) {
            ConfiguracionEstrategiaProvider previo =
                    this.configuracionesPorEstrategia.put(provider.estrategiaId(), provider);
            if (previo != null) {
                throw new IllegalStateException(
                        "Hay más de un proveedor de configuración para " + provider.estrategiaId());
            }
        }
    }


    /** Se encarga de obtener el id de la estrategia activa lockeando */
    @Override
    public Optional<EstadoEstrategiaCotizacion> bloquearEstado() {
        return estadoDAO.buscarBloqueando(EstadoEstrategiaCotizacionDTOSQL.ID_GLOBAL)
                .map(EstadoEstrategiaCotizacionDTOSQL::aModelo);
    }

    /** Se encarga de obtener el id de la estrategia activa sin un lock */
    @Override
    public Optional<EstadoEstrategiaCotizacion> buscarEstado() {
        return estadoDAO.findById(EstadoEstrategiaCotizacionDTOSQL.ID_GLOBAL)
                .map(EstadoEstrategiaCotizacionDTOSQL::aModelo);
    }

    /** Se encarga de guardar y persistir la seleccion de estrategia */
    @Override
    public void guardarEstado(EstadoEstrategiaCotizacion estado) {
        EstadoEstrategiaCotizacionDTOSQL entidad = estadoDAO.findById(
                        EstadoEstrategiaCotizacionDTOSQL.ID_GLOBAL)
                .orElseGet(EstadoEstrategiaCotizacionDTOSQL::estadoInicial);
        entidad.seleccionar(estado.estrategiaActivaId());
        estadoDAO.save(entidad);
    }

    /** Se encarga de buscar la configuracion correspondiente a la estrategia dada por parametro. */
    @Override
    public Optional<ConfiguracionEstrategiaCotizacion> buscarConfiguracion(String estrategiaId) {
        return proveedor(estrategiaId).buscar();
    }

    /** Devuelve los IDs de configuración que poseen provider registrado.*/
    @Override
    public Set<String> estrategiaIdsConfigurables() {
        return Set.copyOf(configuracionesPorEstrategia.keySet());
    }

    /**  */
    @Override
    public Class<? extends ConfiguracionEstrategiaCotizacion> tipoConfiguracion(String estrategiaId) {
        return proveedor(estrategiaId).tipoConfiguracion();
    }

    /** Se encarga de guardar/actualizar la configuracion de la estrategia guardada por parametro*/
    @Override
    public void guardarConfiguracion(String estrategiaId,
                                     ConfiguracionEstrategiaCotizacion configuracion) {
        proveedor(estrategiaId).guardar(configuracion);
    }

    /** Recupera una página keyset de valoraciones para el recálculo. */
    @Override
    public List<DatosValoracion> buscarPagina(UUID cursorExclusivo, int limite) {
        return recalculationDAO.buscarPagina(cursorExclusivo, limite);
    }

    /** Actualiza en lote las cotizaciones calculadas. */
    @Override
    public int actualizarCotizaciones(List<ActualizacionCotizacion> actualizaciones) {
        return recalculationDAO.actualizarCotizaciones(actualizaciones);
    }

    /** Resuelve el provider tipado o rechaza un ID que no fue registrado. */
    private ConfiguracionEstrategiaProvider proveedor(String estrategiaId) {
        ConfiguracionEstrategiaProvider provider = configuracionesPorEstrategia.get(estrategiaId);
        if (provider == null) {
            throw new IllegalArgumentException("No existe proveedor para estrategia " + estrategiaId);
        }
        return provider;
    }
}