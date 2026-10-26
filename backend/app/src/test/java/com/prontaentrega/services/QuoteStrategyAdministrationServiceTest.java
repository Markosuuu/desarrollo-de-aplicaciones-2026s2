package com.prontaentrega.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.prontaentrega.models.ConfiguracionEstrategiaCotizacion;
import com.prontaentrega.models.estrategias.ConfiguracionEstrategiaPonderada;
import com.prontaentrega.models.EstadoEstrategiaCotizacion;
import com.prontaentrega.models.estrategias.ConfiguracionEstrategiaPonderadaPenalizacion;
import com.prontaentrega.models.estrategias.EstrategiaPonderada;
import com.prontaentrega.models.estrategias.EstrategiaPonderadaConPenalizacion;
import com.prontaentrega.persistence.repository.QuoteStrategyRepository;
import com.prontaentrega.services.dto.ResultadoRecotizacion;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.prontaentrega.services.exceptions.QuoteOperationConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Verifica la coordinación administrativa sin cargar base de datos. */
@ExtendWith(MockitoExtension.class)
class QuoteStrategyAdministrationServiceTest {
    @Mock
    private QuoteStrategyRepository strategyRepository;

    @Mock
    private QuoteRecalculationService recalculationService;

    private QuoteStrategyAdministrationService administrationService;

    /** Registra las dos estrategias y valida su asociación tipada con los providers. */
    @BeforeEach
    void crearServicio() {
        when(strategyRepository.estrategiaIdsConfigurables())
                .thenReturn(java.util.Set.of("ponderada", "ponderada-con-penalizacion"));
        doReturn(ConfiguracionEstrategiaPonderada.class)
                .when(strategyRepository).tipoConfiguracion("ponderada");
        doReturn(ConfiguracionEstrategiaPonderadaPenalizacion.class)
                .when(strategyRepository).tipoConfiguracion("ponderada-con-penalizacion");
        when(strategyRepository.estrategiaIdsConfigurables())
                .thenReturn(java.util.Set.of("ponderada", "ponderada-con-penalizacion"));
        administrationService = new QuoteStrategyAdministrationService(strategyRepository,
                recalculationService,
                List.of(new EstrategiaPonderada(), new EstrategiaPonderadaConPenalizacion()));
    }

    /** Actualizar una configuración no selecciona ni recalcula la estrategia. */
    @Test
    void actualizaConfiguracionSinIniciarRecotizacion() {
        ConfiguracionEstrategiaPonderada config = configuracionPonderada();
        when(strategyRepository.bloquearEstado())
                .thenReturn(Optional.of(new EstadoEstrategiaCotizacion(null)));

        ConfiguracionEstrategiaCotizacion guardada =
                administrationService.actualizarConfiguracion("ponderada", config);

        assertThat(guardada).isSameAs(config);
        verify(strategyRepository).guardarConfiguracion("ponderada", config);
        verify(recalculationService, never()).recalcularEstrategiaActiva();
    }

    /** Activar persiste primero la selección y delega el cálculo al servicio compartido. */
    @Test
    void activaEstrategiaYRecotizaEnElMismoFlujo() {
        ConfiguracionEstrategiaPonderada config = configuracionPonderada();
        when(strategyRepository.bloquearEstado())
                .thenReturn(Optional.of(new EstadoEstrategiaCotizacion(null)));
        when(strategyRepository.buscarConfiguracion("ponderada")).thenReturn(Optional.of(config));
        ResultadoRecotizacion resultado = new ResultadoRecotizacion("ponderada", 2, 1);
        when(recalculationService.recalcularEstrategiaActiva()).thenReturn(resultado);

        assertThat(administrationService.activarEstrategia("ponderada")).isEqualTo(resultado);

        InOrder orden = inOrder(strategyRepository, recalculationService);
        orden.verify(strategyRepository).guardarEstado(new EstadoEstrategiaCotizacion("ponderada"));
        orden.verify(recalculationService).recalcularEstrategiaActiva();
    }

    /** Una estrategia registrada sin configuración completa no puede activarse. */
    @Test
    void noActivaEstrategiaSinConfiguracion() {
        when(strategyRepository.bloquearEstado())
                .thenReturn(Optional.of(new EstadoEstrategiaCotizacion(null)));
        when(strategyRepository.buscarConfiguracion("ponderada")).thenReturn(Optional.empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> administrationService.activarEstrategia("ponderada"))
                .isInstanceOf(com.prontaentrega.services.exceptions.QuoteOperationConflictException.class);
        verify(strategyRepository, never()).guardarEstado(
                new EstadoEstrategiaCotizacion("ponderada"));
        verify(recalculationService, never()).recalcularEstrategiaActiva();
    }

    @Test
    void consultarActivaSinConfigurarArrojaExcepcion() {
        QuoteOperationConflictException error = assertThrows(QuoteOperationConflictException.class,
                () -> administrationService.consultarActiva());
        assertEquals(error.getMessage(),"No hay una estrategia activa configurada");
    }

    @Test
    void consultarActivaConEstrategiaActivaNoDisponibleArrojaExcepcion() {
        when(strategyRepository.buscarEstado()).thenReturn(
                Optional.of(new EstadoEstrategiaCotizacion("estrategia inexistente"))
        );
        QuoteOperationConflictException error = assertThrows(QuoteOperationConflictException.class,
                () -> administrationService.consultarActiva());
        assertEquals(error.getMessage(),"La estrategia activa no está disponible");
    }

    @Test
    void consultarActivaDevuelveEstrategiaActiva() {
        when(strategyRepository.buscarEstado()).thenReturn(
                Optional.of(new EstadoEstrategiaCotizacion("ponderada"))
        );
        when(strategyRepository.buscarConfiguracion("ponderada")).thenReturn(
                Optional.of(configuracionPonderada())
        );
        QuoteStrategyAdministrationService.QuoteStrategyView response = administrationService.consultarActiva();

        assertEquals(response.strategyId(), "ponderada");
        assertTrue(response.active());
    }

    @Test
    void listarEstrategiasRetornaLasEstrategiasPersistidas() {
        List<QuoteStrategyAdministrationService.QuoteStrategyView> lista = administrationService.listarEstrategias();
        assertTrue(!lista.isEmpty());
        assertEquals(lista.size(), 2);
        assertTrue(lista.stream().anyMatch(str -> Objects.equals(str.strategyId(), "ponderada")));
        assertTrue(lista.stream().anyMatch(str -> Objects.equals(str.strategyId(), "ponderada-con-penalizacion")));
    }

    private ConfiguracionEstrategiaPonderada configuracionPonderada() {
        return new ConfiguracionEstrategiaPonderada(BigDecimal.ONE, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ONE);
    }
}