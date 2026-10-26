package com.prontaentrega.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prontaentrega.models.estrategias.ConfiguracionEstrategiaPonderada;
import com.prontaentrega.models.DatosValoracion;
import com.prontaentrega.models.EstadoEstrategiaCotizacion;
import com.prontaentrega.models.EstrategiaCotizacion;
import com.prontaentrega.models.estrategias.EstrategiaPonderada;
import com.prontaentrega.persistence.repository.QuoteRecalculationRepository;
import com.prontaentrega.persistence.repository.QuoteStrategyRepository;
import com.prontaentrega.services.dto.ResultadoRecotizacion;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Verifica la coordinación del servicio sin infraestructura de persistencia. */
@ExtendWith(MockitoExtension.class)
class QuoteRecalculationServiceTest {
    @Mock
    private QuoteStrategyRepository strategyRepository;

    @Mock
    private QuoteRecalculationRepository quoteRepository;

    /** Usa la configuración activa, actualiza cotización vigente y cuenta jugadores omitidos. */
    @Test
    void recalculaElegiblesYOmiteMetricasInvalidas() {
        EstrategiaCotizacion strategy = new EstrategiaPonderada();
        QuoteRecalculationService recalculationService = new QuoteRecalculationService(
                strategyRepository, quoteRepository, List.of(strategy));
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
        when(quoteRepository.buscarPagina(null, 200)).thenReturn(List.of(valid, invalid));
        when(quoteRepository.buscarPagina(invalid.bolsaId(), 200)).thenReturn(List.of());
        when(quoteRepository.actualizarCotizaciones(anyList())).thenAnswer(invocation ->
                ((List<?>) invocation.getArgument(0)).size());

        ResultadoRecotizacion result = recalculationService.recalcularEstrategiaActiva();

        assertThat(result).isEqualTo(new ResultadoRecotizacion("ponderada", 1, 1));
        verify(quoteRepository).actualizarCotizaciones(argThat(updates -> updates.size() == 1
                && updates.getFirst().cotizacionCalculada().compareTo(new BigDecimal("16")) == 0));
    }
}