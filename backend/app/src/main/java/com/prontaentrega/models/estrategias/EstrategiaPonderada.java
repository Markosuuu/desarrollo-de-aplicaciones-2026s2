package com.prontaentrega.models.estrategias;

import com.prontaentrega.models.ConfiguracionEstrategiaCotizacion;
import com.prontaentrega.models.ConfiguracionEstrategiaPonderada;
import com.prontaentrega.models.DatosValoracion;
import com.prontaentrega.models.exception.CotizacionInvalidaException;
import com.prontaentrega.models.exception.DatosValoracionInvalidaException;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

/** Calcula la cotización incremental mediante métricas ponderadas sin penalización. */
@Component
public class EstrategiaPonderada implements EstrategiaCotizacion {
    /** Devuelve el identificador persistido de la estrategia. */
    @Override
    public String estrategiaId() {
        return "ponderada";
    }

    /** Devuelve el nombre público en español. */
    @Override
    public String nombreEstrategia() {
        return "Métricas ponderadas";
    }

    /** Declara la configuración requerida y mantiene verificable la asociación del algoritmo. */
    @Override
    public Class<ConfiguracionEstrategiaPonderada> tipoConfiguracion() {
        return ConfiguracionEstrategiaPonderada.class;
    }

    /** Aplica la fórmula ponderada y exige una cotización resultante mayor que uno. */
    @Override
    public BigDecimal calcular(DatosValoracion datos,
                               ConfiguracionEstrategiaCotizacion configuracion) {
        if (!(configuracion instanceof ConfiguracionEstrategiaPonderada pesos)) {
            throw new IllegalArgumentException("La configuración no corresponde a la estrategia ponderada");
        }

        validar(datos);

        BigDecimal score = pesos.pesoGoles().multiply(BigDecimal.valueOf(datos.goles()))
                .add(pesos.pesoAsistencias().multiply(BigDecimal.valueOf(datos.asistencias())))
                .add(pesos.pesoDisparosPorPartido().multiply(datos.disparosPorPartido()))
                .add(pesos.pesoPasesClavePorPartido().multiply(datos.keyPassesPorPartido()))
                .add(pesos.pesoRegatesGanadosPorPartido().multiply(datos.dribblesGanadosPorPartido()))
                .add(pesos.pesoFaltasCometidasPorPartido().multiply(datos.faltasCometidasPorPartido()))
                .add(pesos.pesoRating().multiply(datos.rating()));

        return validarCotizacion(datos.cotizacionVigente()
                .add(score.multiply(pesos.factorEscala())));
    }

    /** Rechaza datos nulos o negativos para que el servicio omita esta bolsa. */
    private void validar(DatosValoracion datos) {

        if (datos == null || datos.cotizacionVigente() == null || datos.goles() == null
                || datos.asistencias() == null || datos.disparosPorPartido() == null || datos.keyPassesPorPartido() == null
                || datos.dribblesGanadosPorPartido() == null || datos.faltasCometidasPorPartido() == null || datos.rating() == null) {
            throw new DatosValoracionInvalidaException("Faltan métricas requeridas para valorar al jugador");
        }
        if (datos.goles() < 0 || datos.asistencias() < 0) {
            throw new DatosValoracionInvalidaException("Las métricas del jugador contienen valores inválidos");
        }

        List<BigDecimal> list = List.of(
                datos.disparosPorPartido(), datos.keyPassesPorPartido(), datos.dribblesGanadosPorPartido(),
                datos.faltasCometidasPorPartido(), datos.rating()
        );

        if  (list.stream().anyMatch(valor -> valor.compareTo(BigDecimal.ZERO) < 0)) {
            throw new DatosValoracionInvalidaException("Las métricas del jugador contienen valores inválidos");
        }
    }

    /** Rechaza un resultado que incumple el mínimo positivo del mercado. */
    private BigDecimal validarCotizacion(BigDecimal cotizacion) {
        if (cotizacion.compareTo(BigDecimal.ONE) <= 0) {
            throw new CotizacionInvalidaException("La cotización calculada debe ser mayor que 1");
        }
        return cotizacion;
    }
}