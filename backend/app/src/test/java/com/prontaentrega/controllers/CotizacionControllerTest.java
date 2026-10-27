package com.prontaentrega.controllers;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prontaentrega.services.CotizacionService;
import com.prontaentrega.services.exceptions.QuoteOperationConflictException;
import com.prontaentrega.services.exceptions.QuoteStrategyNotFoundException;
import com.prontaentrega.services.dto.ResultadoRecotizacion;
import com.prontaentrega.models.estrategias.ConfiguracionEstrategiaPonderada;
import com.prontaentrega.services.CotizacionService.QuoteStrategyView;
import java.math.BigDecimal;
import java.util.List;
import com.prontaentrega.utils.AbstractIntegrationTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

/** Verifica el contrato y la clave maestra del endpoint manual de recotización. */
@SpringBootTest
@AutoConfigureMockMvc
@Tag("integracion")
class CotizacionControllerTest extends AbstractIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CotizacionService administrationService;

    @Value("${api-key.master-secret}")
    private String masterSecret;

    /** Acepta clave válida, no requiere cuerpo y devuelve conteos del servicio común. */
    @Test
    void recotizaSinParametrosConClaveMaestraValida() throws Exception {
        when(administrationService.recalcularEstrategiaActiva())
                .thenReturn(new ResultadoRecotizacion("ponderada", 4, 1));

        mockMvc.perform(post("/api/quotes/recalculate").header("X-Master-Key", masterSecret))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.strategyId").value("ponderada"))
                .andExpect(jsonPath("$.processedPlayers").value(4))
                .andExpect(jsonPath("$.skippedPlayers").value(1));

        verify(administrationService).recalcularEstrategiaActiva();
    }

    /** Rechaza claves ausentes o incorrectas sin iniciar una recotización. */
    @Test
    void rechazaClaveMaestraAusenteOIncorrecta() throws Exception {
        mockMvc.perform(post("/api/quotes/recalculate"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("CLAVE_MAESTRA_INVALIDA"));
        mockMvc.perform(post("/api/quotes/recalculate").header("X-Master-Key", "incorrecta"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());

        verify(administrationService, never()).recalcularEstrategiaActiva();
    }

    /** Devuelve estrategia activa y catálogo con sus configuraciones persistidas. */
    @Test
        @WithMockUser
    void consultaEstrategiaActivaYCatalogo() throws Exception {
        ConfiguracionEstrategiaPonderada config = configuracionPonderada();
        when(administrationService.consultarActiva()).thenReturn(
                new QuoteStrategyView("ponderada", "Métricas ponderadas", true, config));
        when(administrationService.listarEstrategias()).thenReturn(List.of(
                new QuoteStrategyView("ponderada", "Métricas ponderadas", true, config),
                new QuoteStrategyView("ponderada-con-penalizacion",
                        "Métricas ponderadas con penalización", false, null)));

        mockMvc.perform(get("/api/quotes/strategy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.strategyId").value("ponderada"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.config.pesoGoles").value(1.0));
        mockMvc.perform(get("/api/quotes/strategies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.strategies.length()").value(2))
                .andExpect(jsonPath("$.strategies[1].config").doesNotExist());
    }

    /** Actualiza la configuración sin ejecutar recálculo ni modificar la estrategia activa. */
    @Test
    void actualizaConfiguracionSinRecalcular() throws Exception {
        when(administrationService.actualizarConfiguracion(
                org.mockito.ArgumentMatchers.eq("ponderada"),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(configuracionPonderada());

        mockMvc.perform(put("/api/quotes/strategies/ponderada/config")
                        .header("X-Master-Key", masterSecret)
                        .contentType("application/json")
                        .content(configuracionJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.strategyId").value("ponderada"))
                .andExpect(jsonPath("$.config.pesoGoles").value(1.0));

        verify(administrationService, never()).recalcularEstrategiaActiva();
    }

    /** La activación devuelve el resultado solo después de que el servicio concluye. */
    @Test
    void activaEstrategiaYDevuelveConteos() throws Exception {
        when(administrationService.activarEstrategia("ponderada"))
                .thenReturn(new ResultadoRecotizacion("ponderada", 3, 2));

        mockMvc.perform(put("/api/quotes/strategy")
                        .header("X-Master-Key", masterSecret)
                        .contentType("application/json")
                        .content("{\"strategyId\":\"ponderada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.strategyId").value("ponderada"))
                .andExpect(jsonPath("$.processedPlayers").value(3))
                .andExpect(jsonPath("$.skippedPlayers").value(2));
    }

    /** Mapea configuración inválida, estrategia inexistente y estado no disponible al contrato. */
    @Test
    void devuelveErroresQuotePlanosConCodigoHttpCorrecto() throws Exception {
        mockMvc.perform(put("/api/quotes/strategies/ponderada/config")
                        .header("X-Master-Key", masterSecret)
                        .contentType("application/json")
                        .content(configuracionJson().replace("\"factorEscala\":1", "\"factorEscala\":1,\"extra\":1")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CONFIGURACION_INVALIDA"));

        when(administrationService.activarEstrategia("missing"))
                .thenThrow(new QuoteStrategyNotFoundException("La estrategia solicitada no existe"));
        mockMvc.perform(put("/api/quotes/strategy").header("X-Master-Key", masterSecret)
                        .contentType("application/json").content("{\"strategyId\":\"missing\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ESTRATEGIA_NO_ENCONTRADA"));

        when(administrationService.consultarActiva())
                .thenThrow(new QuoteOperationConflictException("No hay una estrategia activa configurada"));
        mockMvc.perform(get("/api/quotes/strategy").with(
                        org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("tester")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    private ConfiguracionEstrategiaPonderada configuracionPonderada() {
        return new ConfiguracionEstrategiaPonderada(BigDecimal.ONE, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ONE);
    }

    private String configuracionJson() {
        return """
                {"pesoGoles":1,"pesoAsistencias":0,"pesoDisparosPorPartido":0,
                 "pesoPasesClavePorPartido":0,"pesoRegatesGanadosPorPartido":0,
                 "pesoFaltasCometidasPorPartido":0,"pesoRating":0,"factorEscala":1}
                """;
    }
}