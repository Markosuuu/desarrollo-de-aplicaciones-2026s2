package com.prontaentrega.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prontaentrega.models.DatosValoracion;
import com.prontaentrega.models.estrategias.ConfiguracionEstrategiaPonderada;
import com.prontaentrega.models.EstadoEstrategiaCotizacion;
import com.prontaentrega.models.EstrategiaCotizacion;
import com.prontaentrega.models.estrategias.EstrategiaPonderada;
import com.prontaentrega.persistence.repository.CotizacionRepository;
import com.prontaentrega.services.dto.ResultadoRecotizacion;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Verifica la coordinación del servicio sin infraestructura de persistencia. */
@ExtendWith(MockitoExtension.class)
@Tag("integracion")
class QuoteRecalculationServiceTest {
    @Mock
    private CotizacionRepository strategyRepository;

    /** Usa la configuración activa, actualiza cotización vigente y cuenta jugadores omitidos. */
    @Test
    void recalculaElegiblesYOmiteMetricasInvalidas() {
        EstrategiaCotizacion strategy = new EstrategiaPonderada();
        when(strategyRepository.estrategiaIdsConfigurables()).thenReturn(java.util.Set.of("ponderada"));
        org.mockito.Mockito.doReturn(ConfiguracionEstrategiaPonderada.class)
                .when(strategyRepository).tipoConfiguracion("ponderada");
        CotizacionService recalculationService = new CotizacionService(
                strategyRepository, List.of(strategy));
        ConfiguracionEstrategiaPonderada config = new ConfiguracionEstrategiaPonderada(
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("2"));
        DatosValoracion valid = new DatosValoracion(UUID.randomUUID(), new BigDecimal("10"),
                3, 0, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);
        DatosValoracion invalid = new DatosValoracion(UUID.randomUUID(), new BigDecimal("20"),
                1, 0, null, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE);

        when(strategyRepository.bloquearEstado())
                .thenReturn(Optional.of(new EstadoEstrategiaCotizacion("ponderada")));
        when(strategyRepository.buscarConfiguracion("ponderada")).thenReturn(Optional.of(config));
        when(strategyRepository.buscarPagina(null, 200)).thenReturn(List.of(valid, invalid));
        when(strategyRepository.buscarPagina(invalid.bolsaId(), 200)).thenReturn(List.of());
        when(strategyRepository.actualizarCotizaciones(anyList())).thenAnswer(invocation ->
                ((List<?>) invocation.getArgument(0)).size());

        ResultadoRecotizacion result = recalculationService.recalcularEstrategiaActiva();

        assertThat(result).isEqualTo(new ResultadoRecotizacion("ponderada", 1, 1));
        verify(strategyRepository).actualizarCotizaciones(argThat(updates -> updates.size() == 1
                && updates.getFirst().cotizacionCalculada().compareTo(new BigDecimal("16")) == 0));
    }
}