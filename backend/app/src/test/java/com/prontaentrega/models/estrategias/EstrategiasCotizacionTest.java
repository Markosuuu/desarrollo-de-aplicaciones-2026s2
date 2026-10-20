package com.prontaentrega.models.estrategias;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.prontaentrega.models.ConfiguracionEstrategiaPonderada;
import com.prontaentrega.models.ConfiguracionEstrategiaPonderadaPenalizacion;
import com.prontaentrega.models.DatosValoracion;
import com.prontaentrega.models.exception.CotizacionInvalidaException;
import com.prontaentrega.models.exception.DatosValoracionInvalidaException;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** Verifica las fórmulas del dominio sin servicios ni persistencia. */
class EstrategiasCotizacionTest {
    private final EstrategiaPonderada estrategiaPonderada = new EstrategiaPonderada();
    private final EstrategiaPonderadaConPenalizacion estrategiaPenalizada =
            new EstrategiaPonderadaConPenalizacion();

    /** La estrategia ponderada usa la cotización vigente y factor de escala. */
    @Test
    void aplicaFormulaPonderadaSobreCotizacionVigente() {
        ConfiguracionEstrategiaPonderada configuracion = configuracionPonderada(
                "1", "0", "0", "0", "0", "0", "0", "2");

        BigDecimal cotizacion = estrategiaPonderada.calcular(datos("10", 3, 0, "1", "2",
                "3", "4", "5"), configuracion);

        assertThat(cotizacion).isEqualByComparingTo("16");
    }

    /** La penalización se aplica solo cuando los regates quedan debajo del umbral. */
    @Test
    void aplicaUmbralYPenalizacionEnEstrategiaPenalizada() {
        ConfiguracionEstrategiaPonderadaPenalizacion configuracion = configuracionPenalizada(
                "1", "0", "0", "0", "0", "0.5", "0.25");

        BigDecimal cotizacion = estrategiaPenalizada.calcular(datos("10", 2, 0, "1", null,
                "0.4", "1", "5"), configuracion);

        assertThat(cotizacion).isEqualByComparingTo("11.75");
    }

    /** Las métricas no usadas por una estrategia no impiden su cálculo. */
    @Test
    void estrategiaPenalizadaNoRequierePasesClave() {
        ConfiguracionEstrategiaPonderadaPenalizacion configuracion = configuracionPenalizada(
                "1", "0", "0", "0", "0", "0.5", "0.25");

        assertThat(estrategiaPenalizada.calcular(datos("10", 2, 0, "1", null,
                "0.8", "1", "5"), configuracion)).isEqualByComparingTo("12");
    }

    /** Una métrica requerida ausente provoca que el servicio pueda omitir el jugador. */
    @Test
    void omiteMetricasRequeridasAusentes() {
        ConfiguracionEstrategiaPonderada configuracion = configuracionPonderada(
                "1", "0", "0", "0", "0", "0", "0", "2");

        assertThatThrownBy(() -> estrategiaPonderada.calcular(datos("10", 3, 0, null,
                "2", "3", "4", "5"), configuracion))
                .isInstanceOf(DatosValoracionInvalidaException.class);
    }

    /** Una métrica negativa persistida se considera inválida para ese jugador. */
    @Test
    void omiteMetricasNegativas() {
        ConfiguracionEstrategiaPonderada configuracion = configuracionPonderada(
                "1", "0", "0", "0", "0", "0", "0", "2");

        assertThatThrownBy(() -> estrategiaPonderada.calcular(datos("10", -1, 0, "1",
                "2", "3", "4", "5"), configuracion))
                .isInstanceOf(DatosValoracionInvalidaException.class);
    }

    /** Una cotización propuesta igual o inferior a uno invalida toda la operación. */
    @Test
    void rechazaCotizacionNoMayorQueUno() {
        ConfiguracionEstrategiaPonderada configuracion = configuracionPonderada(
                "1", "0", "0", "0", "0", "0", "0", "-2");

        assertThatThrownBy(() -> estrategiaPonderada.calcular(datos("2", 2, 0, "1",
                "2", "3", "4", "5"), configuracion))
                .isInstanceOf(CotizacionInvalidaException.class);
    }

    /** Rechaza coeficientes negativos o cuya suma difiere de uno. */
    @Test
    void rechazaConfiguracionPonderadaInvalida() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> configuracionPonderada(
                "-0.1", "0.2", "0.2", "0.2", "0.2", "0.2", "0.1", "1"))
                .isInstanceOf(IllegalArgumentException.class);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> configuracionPonderada(
                "0.1", "0.1", "0.1", "0.1", "0.1", "0.1", "0.1", "1"))
                .isInstanceOf(IllegalArgumentException.class);
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
    private ConfiguracionEstrategiaPonderada configuracionPonderada(
            String goles, String asistencias, String disparos, String pases,
            String regates, String faltas, String rating, String escala) {
        return new ConfiguracionEstrategiaPonderada(new BigDecimal(goles),
                new BigDecimal(asistencias), new BigDecimal(disparos), new BigDecimal(pases),
                new BigDecimal(regates), new BigDecimal(faltas), new BigDecimal(rating),
                new BigDecimal(escala));
    }

        /** Construye configuración de la estrategia penalizada para los tests. */
    private ConfiguracionEstrategiaPonderadaPenalizacion configuracionPenalizada(
            String goles, String asistencias, String disparos, String rating,
            String faltas, String umbral, String escala) {
        return new ConfiguracionEstrategiaPonderadaPenalizacion(new BigDecimal(goles),
                new BigDecimal(asistencias), new BigDecimal(disparos), new BigDecimal(rating),
                new BigDecimal(faltas), new BigDecimal(umbral), new BigDecimal(escala));
    }

        /** Convierte valores de fixture opcionales a BigDecimal. */
    private BigDecimal decimal(String valor) {
        return valor == null ? null : new BigDecimal(valor);
    }
}