package com.prontaentrega.models.estrategias;

import com.prontaentrega.models.ConfiguracionEstrategiaPonderada;
import com.prontaentrega.models.ConfiguracionEstrategiaPonderadaPenalizacion;
import com.prontaentrega.models.DatosValoracion;
import com.prontaentrega.models.exception.ConfiguracionNoCorrespondiente;
import com.prontaentrega.models.exception.CotizacionInvalidaException;
import com.prontaentrega.models.exception.DatosValoracionInvalidaException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Verifica las fórmulas del dominio sin servicios ni persistencia. */
class EstrategiasPonderadaPenalizacionTest {
    private final EstrategiaPonderadaConPenalizacion estrategiaPenalizacion = new EstrategiaPonderadaConPenalizacion();


    @Test
    void unaEstrategiaSabeRetornarSuIDYSuNombre() {
        assertEquals("ponderada-con-penalizacion", estrategiaPenalizacion.estrategiaId());
        assertEquals("Métricas ponderadas con penalización", estrategiaPenalizacion.nombreEstrategia());
    }

    /** La estrategia ponderada usa la cotización vigente y factor de escala. */
    @Test
    void aplicaFormulaPonderadaSobreCotizacionVigente() {
        ConfiguracionEstrategiaPonderadaPenalizacion configuracion = configuracionPonderadaPenalizada(
                "1", "0", "0", "0", "0", "0", "2");

        BigDecimal cotizacion = estrategiaPenalizacion.calcular(datos("10", 3, 0, "1", "2",
                "30", "4", "5"), configuracion);

        assertThat(cotizacion).isEqualByComparingTo("13");
    }

    @Test
    void aplicaFormulaPonderadaConPenalizacionSobreCotizacionVigente() {
        ConfiguracionEstrategiaPonderadaPenalizacion configuracion = configuracionPonderadaPenalizada(
                "1", "0", "0", "40", "0", "0", "2");

        BigDecimal cotizacion = estrategiaPenalizacion.calcular(datos("10", 3, 0, "1", "2",
                "30", "4", "5"), configuracion);

        assertThat(cotizacion).isEqualByComparingTo("11");
    }

    /** Una métrica requerida ausente provoca que el servicio pueda omitir el jugador. */
    @Test
    void omiteMetricasRequeridasAusentes() {
        ConfiguracionEstrategiaPonderadaPenalizacion configuracion = configuracionPonderadaPenalizada(
                "1", "0", "0", "0", "0", "0", "2");

        assertThatThrownBy(() -> estrategiaPenalizacion.calcular(datos("10", 3, 0, null,
                "2", "3", "4", "5"), configuracion))
                .isInstanceOf(DatosValoracionInvalidaException.class);
    }

    /** Una métrica negativa persistida se considera inválida para ese jugador. */
    @Test
    void omiteMetricasNegativas() {
        ConfiguracionEstrategiaPonderadaPenalizacion configuracion = configuracionPonderadaPenalizada(
                "1", "0", "0", "0", "0", "0", "2");

        assertThatThrownBy(() -> estrategiaPenalizacion.calcular(datos("10", -1, 0, "1",
                "2", "3", "4", "5"), configuracion))
                .isInstanceOf(DatosValoracionInvalidaException.class);
    }

    /** Una cotización propuesta igual o inferior a uno invalida toda la operación. */
    @Test
    void rechazaCotizacionNoMayorQueUno() {
        ConfiguracionEstrategiaPonderadaPenalizacion configuracion = configuracionPonderadaPenalizada(
                "1.0" , "0", "0",  "0", "0", "0", "-3");

        assertThatThrownBy(() -> estrategiaPenalizacion.calcular(datos("1", 0, 0, "0",
                "0", "0", "0", "0"), configuracion))
                .isInstanceOf(CotizacionInvalidaException.class);
    }

    /** Rechaza coeficientes negativos o cuya suma difiere de uno. */
    @Test
    void rechazaConfiguracionPonderadaInvalida() {
        assertThatThrownBy(() -> configuracionPonderadaPenalizada(
                "-0.1", "0.2", "0.2", "0.2", "0.2", "0.1", "1"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> configuracionPonderadaPenalizada(
                "0.1", "0.1", "0.1", "0.1", "0.1", "0.1", "1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rechazaConfiguracionDeOtraEstrategia() {
        ConfiguracionEstrategiaPonderada config = new ConfiguracionEstrategiaPonderada(
                BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO
        );
        ConfiguracionNoCorrespondiente e = assertThrows(ConfiguracionNoCorrespondiente.class, () ->
                estrategiaPenalizacion.calcular(
                        datos("10", 3, 0, "1", "2",
                "3", "4", "5"), config)
        );
        assertEquals("La configuración no corresponde a la estrategia ponderada con penalización", e.getMessage());
    }

    /** Crea una proyección de jugador con las métricas indicadas para cada caso unitario. */
    private DatosValoracion datos(String cotizacion, Integer goles, Integer asistencias,
                                  String disparos, String pases, String regates,
                                  String faltas, String rating) {
        return new DatosValoracion(UUID.randomUUID(), new BigDecimal(cotizacion), goles,
                asistencias, decimal(disparos), decimal(pases), decimal(regates),
                decimal(faltas), decimal(rating));
    }

    /** Construye configuración válida o inválida de la estrategia ponderada para los tests. */
    private ConfiguracionEstrategiaPonderadaPenalizacion configuracionPonderadaPenalizada(
            String goles, String asistencias, String disparos,
            String regates, String faltas, String rating, String escala) {
        return new ConfiguracionEstrategiaPonderadaPenalizacion(
                new BigDecimal(goles),
                new BigDecimal(asistencias),
                new BigDecimal(disparos),
                new BigDecimal(rating),
                new BigDecimal(faltas),
                new BigDecimal(regates),
                new BigDecimal(escala)
        );
    }

    /** Convierte valores de fixture opcionales a BigDecimal. */
    private BigDecimal decimal(String valor) {
        return valor == null ? null : new BigDecimal(valor);
    }
}