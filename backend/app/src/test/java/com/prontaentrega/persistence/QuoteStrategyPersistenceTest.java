package com.prontaentrega.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.prontaentrega.models.estrategias.ConfiguracionEstrategiaPonderada;
import com.prontaentrega.models.EstadoEstrategiaCotizacion;
import com.prontaentrega.persistence.repository.CotizacionRepository;
import com.prontaentrega.persistence.repository.dao.EstadoEstrategiaCotizacionDAOSQL;
import com.prontaentrega.utils.AbstractIntegrationTest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/** Verifica persistencia separada y lock del estado sobre PostgreSQL real. */
@SpringBootTest
class QuoteStrategyPersistenceTest extends AbstractIntegrationTest {
    @Autowired
    private CotizacionRepository strategyRepository;

    @Autowired
    private EstadoEstrategiaCotizacionDAOSQL estadoDAO;

    @Test
    @Transactional
    void persisteConfiguracionYSeleccionGlobal() {
        ConfiguracionEstrategiaPonderada configuracion = new ConfiguracionEstrategiaPonderada(
                new BigDecimal("0.10"), new BigDecimal("0.10"), new BigDecimal("0.10"),
                new BigDecimal("0.10"), new BigDecimal("0.10"), new BigDecimal("0.10"),
                new BigDecimal("0.40"), new BigDecimal("2.5"));

        strategyRepository.guardarConfiguracion("ponderada", configuracion);
        strategyRepository.guardarEstado(new EstadoEstrategiaCotizacion("ponderada"));

        assertThat(strategyRepository.buscarConfiguracion("ponderada"))
            .get()
            .isInstanceOf(ConfiguracionEstrategiaPonderada.class)
            .satisfies(persistida -> {
                ConfiguracionEstrategiaPonderada ponderada =
                    (ConfiguracionEstrategiaPonderada) persistida;
                assertThat(ponderada.pesoGoles())
                    .isEqualByComparingTo(configuracion.pesoGoles());
                assertThat(ponderada.factorEscala())
                    .isEqualByComparingTo(configuracion.factorEscala());
            });
        assertThat(strategyRepository.bloquearEstado())
                .contains(new EstadoEstrategiaCotizacion("ponderada"));
        assertThat(estadoDAO.findAllById(java.util.List.of("global"))).hasSize(1);
    }
}