package com.prontaentrega.models;

import com.prontaentrega.models.auth.Usuario;
import com.prontaentrega.models.exception.*;
import com.prontaentrega.models.jugadores.*;
import com.prontaentrega.models.subastas.Oferta;
import com.prontaentrega.models.subastas.Subasta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("modelo")
public class SubastaTest {
    private Usuario propietario;
    private Usuario postor;
    private BolsaDeJugador bolsa;
    private Subasta subasta;

    @BeforeEach
    void setUp() {
        propietario = mock(Usuario.class);
        postor = mock(Usuario.class);

        Jugador jugador = new Jugador(1, "Messi", "Barcelona FC", Liga.LALIGA, Posicion.FORWARD,
                40, 170, 70);

        EstadisticasDeportivas estadisticas = new EstadisticasDeportivas(jugador,
                true, 10, 10,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, 120);

        bolsa = new BolsaDeJugador(estadisticas, new BigDecimal("2.5"), LocalDateTime.now(), "Fuente", 100);

        subasta = new Subasta(propietario, bolsa, 10, new BigDecimal("100"),
                LocalDateTime.now().plusDays(1));
    }

    private Oferta oferta(String puja) {
        return new Oferta(subasta, postor, new BigDecimal(puja), "clave-" + puja);
    }

    private Subasta subastaVencida() {
        LocalDateTime ahora = LocalDateTime.now();
        return new Subasta(propietario, bolsa, 10, new BigDecimal("100"),
                ahora.minusDays(1), ahora.minusDays(2));
    }

    // Constructor: casos exitosos

    @Test
    void constructor_deberiaCrearUnaSubastaActivaSinOfertas() {
        assertSame(propietario, subasta.getPropietario());
        assertSame(bolsa, subasta.getBolsaDeJugador());
        assertEquals(10, subasta.getCantidadOfrecida());
        assertThat(subasta.getPrecioInicial()).isEqualByComparingTo("100");
        assertTrue(subasta.getActiva());
        assertNull(subasta.getOfertaActual());
    }

    @Test
    void constructor_deberiaInicializarCreadoEnYActualizadoEnConElMismoValor() {
        assertNotNull(subasta.getCreadoEn());
        assertEquals(subasta.getCreadoEn(), subasta.getActualizadoEn());
    }

    @Test
    void constructor_deberiaUsarLaFechaDeCreacionIndicada() {
        LocalDateTime creadoEn = LocalDateTime.now().minusDays(1);

        Subasta conFecha = new Subasta(propietario, bolsa, 10, new BigDecimal("100"),
                LocalDateTime.now().plusDays(1), creadoEn);

        assertEquals(creadoEn, conFecha.getCreadoEn());
        assertEquals(creadoEn, conFecha.getActualizadoEn());
    }

// Constructor: casos fallidos

    @Test
    void constructor_deberiaRechazarSiElPropietarioEsNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Subasta(null, bolsa, 10, new BigDecimal("100"),
                        LocalDateTime.now().plusDays(1)));

        assertEquals("propietario is marked non-null but is null", ex.getMessage());
    }

    @Test
    void constructor_deberiaRechazarSiLaBolsaDeJugadorEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Subasta(propietario, null, 10, new BigDecimal("100"),
                        LocalDateTime.now().plusDays(1)));

        assertEquals("bolsaDeJugador is marked non-null but is null", ex.getMessage());
    }

    @Test
    void constructor_deberiaRechazarSiLaCantidadOfrecidaEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Subasta(propietario, bolsa, null, new BigDecimal("100"),
                        LocalDateTime.now().plusDays(1)));

        assertEquals("cantidadOfrecida is marked non-null but is null", ex.getMessage());
    }

    @Test
    void constructor_deberiaRechazarSiElPrecioInicialEsNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Subasta(propietario, bolsa, 10, null,
                        LocalDateTime.now().plusDays(1)));

        assertEquals("precioInicial is marked non-null but is null", ex.getMessage());
    }

    @Test
    void constructor_deberiaRechazarSiElVencimientoEsNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Subasta(propietario, bolsa, 10, new BigDecimal("100"), null));

        assertEquals("vencimiento is marked non-null but is null", ex.getMessage());
    }

    @Test
    void constructor_deberiaRechazarSiLaFechaDeCreacionEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Subasta(propietario, bolsa, 10, new BigDecimal("100"),
                        LocalDateTime.now().plusDays(1), null));

        assertEquals("creadoEn is marked non-null but is null", ex.getMessage());
    }

    @Test
    void constructor_deberiaRechazarSiLaCantidadOfrecidaEsCero() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new Subasta(propietario, bolsa, 0, new BigDecimal("100"),
                        LocalDateTime.now().plusDays(1)));

        assertEquals("La cantidad ofrecida debe ser mayor que 0", ex.getMessage());
    }

    @Test
    void constructor_deberiaRechazarSiLaCantidadOfrecidaEsNegativa() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new Subasta(propietario, bolsa, -3, new BigDecimal("100"),
                        LocalDateTime.now().plusDays(1)));

        assertEquals("La cantidad ofrecida debe ser mayor que 0", ex.getMessage());
    }

    @Test
    void constructor_deberiaRechazarSiElPrecioInicialEsCero() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new Subasta(propietario, bolsa, 10, BigDecimal.ZERO,
                        LocalDateTime.now().plusDays(1)));

        assertEquals("El precio inicial debe ser mayor que 0", ex.getMessage());
    }

    @Test
    void constructor_deberiaRechazarSiElPrecioInicialEsNegativo() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new Subasta(propietario, bolsa, 10, new BigDecimal("-1"),
                        LocalDateTime.now().plusDays(1)));

        assertEquals("El precio inicial debe ser mayor que 0", ex.getMessage());
    }

    @Test
    void constructor_deberiaRechazarSiElVencimientoEsIgualALaCreacion() {
        LocalDateTime creadoEn = LocalDateTime.now();

        VencimientoInvalidoException ex = assertThrows(VencimientoInvalidoException.class,
                () -> new Subasta(propietario, bolsa, 10, new BigDecimal("100"),
                        creadoEn, creadoEn));

        assertEquals("El vencimiento debe ser posterior a la creación", ex.getMessage());
    }

    @Test
    void constructor_deberiaRechazarSiElVencimientoEsAnteriorALaCreacion() {
        LocalDateTime creadoEn = LocalDateTime.now();

        VencimientoInvalidoException ex = assertThrows(VencimientoInvalidoException.class,
                () -> new Subasta(propietario, bolsa, 10, new BigDecimal("100"),
                        creadoEn.minusHours(1), creadoEn));

        assertEquals("El vencimiento debe ser posterior a la creación", ex.getMessage());
    }

// obtenerOfertaMinima

    @Test
    void obtenerOfertaMinima_deberiaDevolverElPrecioInicialSiNoHayOfertas() {
        assertThat(subasta.obtenerOfertaMinima()).isEqualByComparingTo("100");
    }

    @Test
    void obtenerOfertaMinima_deberiaDevolverLaPujaActualSiHayOfertas() {
        subasta.actualizarPostorMasGrande(oferta("150"));

        assertThat(subasta.obtenerOfertaMinima()).isEqualByComparingTo("150");
    }

// actualizarPostorMasGrande: casos exitosos

    @Test
    void actualizarPostorMasGrande_deberiaAceptarUnaPrimeraOfertaIgualAlPrecioInicial() {
        Oferta oferta = oferta("100");

        subasta.actualizarPostorMasGrande(oferta);

        assertSame(oferta, subasta.getOfertaActual());
    }

    @Test
    void actualizarPostorMasGrande_deberiaAceptarUnaPrimeraOfertaMayorAlPrecioInicial() {
        Oferta oferta = oferta("120");

        subasta.actualizarPostorMasGrande(oferta);

        assertSame(oferta, subasta.getOfertaActual());
    }

    @Test
    void actualizarPostorMasGrande_deberiaReemplazarLaOfertaActualPorUnaMayor() {
        subasta.actualizarPostorMasGrande(oferta("120"));
        Oferta mayor = oferta("130");

        subasta.actualizarPostorMasGrande(mayor);

        assertSame(mayor, subasta.getOfertaActual());
    }

    @Test
    void actualizarPostorMasGrande_deberiaActualizarLaFechaDeActualizacion() {
        LocalDateTime antes = subasta.getActualizadoEn();

        subasta.actualizarPostorMasGrande(oferta("120"));

        assertFalse(subasta.getActualizadoEn().isBefore(antes));
    }

// actualizarPostorMasGrande: casos fallidos

    @Test
    void actualizarPostorMasGrande_deberiaRechazarSiLaOfertaEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> subasta.actualizarPostorMasGrande(null));

        assertEquals("oferta is marked non-null but is null", ex.getMessage());
    }

    @Test
    void actualizarPostorMasGrande_deberiaRechazarSiLaSubastaEstaCerrada() {
        subasta.cerrarSubasta();

        SubastaNoActivaException ex = assertThrows(SubastaNoActivaException.class,
                () -> subasta.actualizarPostorMasGrande(oferta("120")));

        assertEquals("La subasta no está activa", ex.getMessage());
        assertNull(subasta.getOfertaActual());
    }

    @Test
    void actualizarPostorMasGrande_deberiaRechazarSiLaSubastaYaVencio() {
        Subasta vencida = subastaVencida();

        SubastaNoActivaException ex = assertThrows(SubastaNoActivaException.class,
                () -> vencida.actualizarPostorMasGrande(oferta("120")));

        assertEquals("La subasta ya venció", ex.getMessage());
        assertNull(vencida.getOfertaActual());
    }

    @Test
    void actualizarPostorMasGrande_deberiaRechazarUnaPrimeraOfertaMenorAlPrecioInicial() {
        OfertaInvalidaException ex = assertThrows(OfertaInvalidaException.class,
                () -> subasta.actualizarPostorMasGrande(oferta("99")));

        assertEquals("La oferta debe superar la oferta mínima", ex.getMessage());
        assertNull(subasta.getOfertaActual());
    }

    @Test
    void actualizarPostorMasGrande_deberiaRechazarUnaOfertaIgualALaOfertaActual() {
        Oferta actual = oferta("120");
        subasta.actualizarPostorMasGrande(actual);

        OfertaInvalidaException ex = assertThrows(OfertaInvalidaException.class,
                () -> subasta.actualizarPostorMasGrande(oferta("120")));

        assertEquals("La oferta debe superar la oferta mínima", ex.getMessage());
        assertSame(actual, subasta.getOfertaActual());
    }

    @Test
    void actualizarPostorMasGrande_deberiaRechazarUnaOfertaMenorALaOfertaActual() {
        Oferta actual = oferta("120");
        subasta.actualizarPostorMasGrande(actual);

        OfertaInvalidaException ex = assertThrows(OfertaInvalidaException.class,
                () -> subasta.actualizarPostorMasGrande(oferta("110")));

        assertEquals("La oferta debe superar la oferta mínima", ex.getMessage());
        assertSame(actual, subasta.getOfertaActual());
    }

    // cerrarSubasta
    @Test
    void cerrarSubasta_deberiaActualizarLaFechaDeActualizacion() {
        LocalDateTime antes = subasta.getActualizadoEn();

        subasta.cerrarSubasta();

        assertFalse(subasta.getActualizadoEn().isBefore(antes));
    }

    @Test
    void cerrarSubasta_deberiaPermitirCerrarUnaSubastaVencida() {
        Subasta vencida = subastaVencida();

        vencida.cerrarSubasta();

        assertFalse(vencida.getActiva());
    }

    @Test
    void cerrarSubasta_deberiaConservarLaOfertaActualAlCerrar() {
        Oferta ganadora = oferta("120");
        subasta.actualizarPostorMasGrande(ganadora);

        subasta.cerrarSubasta();

        assertSame(ganadora, subasta.getOfertaActual());
    }

    @Test
    void cerrarSubasta_deberiaRechazarSiLaSubastaYaEstaCerrada() {
        subasta.cerrarSubasta();

        SubastaNoActivaException ex = assertThrows(SubastaNoActivaException.class,
                () -> subasta.cerrarSubasta());

        assertEquals("La subasta ya está cerrada", ex.getMessage());
    }

    // Oferta: constructor, casos exitosos

    @Test
    void oferta_constructor_deberiaCrearUnaOfertaConLosDatosIndicados() {
        Oferta oferta = new Oferta(subasta, postor, new BigDecimal("120"), "clave-1");

        assertSame(subasta, oferta.getSubasta());
        assertSame(postor, oferta.getUsuario());
        assertThat(oferta.getPuja()).isEqualByComparingTo("120");
        assertEquals("clave-1", oferta.getIdempotencyKey());
    }

    @Test
    void oferta_constructor_deberiaCrearLaOfertaComoSuperior() {
        Oferta oferta = new Oferta(subasta, postor, new BigDecimal("120"), "clave-1");

        assertTrue(oferta.getEsSuperior());
    }

    @Test
    void oferta_constructor_deberiaInicializarLaFechaDeCreacion() {
        LocalDateTime antes = LocalDateTime.now();

        Oferta oferta = new Oferta(subasta, postor, new BigDecimal("120"), "clave-1");

        assertNotNull(oferta.getCreadoEn());
        assertFalse(oferta.getCreadoEn().isBefore(antes));
    }

    @Test
    void oferta_constructor_deberiaUsarLaFechaDeCreacionIndicada() {
        LocalDateTime creadoEn = LocalDateTime.now().minusHours(3);

        Oferta oferta = new Oferta(subasta, postor, new BigDecimal("120"), creadoEn, "clave-1");

        assertEquals(creadoEn, oferta.getCreadoEn());
    }

    @Test
    void oferta_constructor_deberiaDejarElIdSinAsignar() {
        Oferta oferta = new Oferta(subasta, postor, new BigDecimal("120"), "clave-1");

        assertNull(oferta.getId());
    }

// Oferta: constructor, casos fallidos

    @Test
    void oferta_constructor_deberiaRechazarSiLaSubastaEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Oferta(null, postor, new BigDecimal("120"), "clave-1"));

        assertEquals("subasta is marked non-null but is null", ex.getMessage());
    }

    @Test
    void oferta_constructor_deberiaRechazarSiElUsuarioEsNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Oferta(subasta, null, new BigDecimal("120"), "clave-1"));

        assertEquals("usuario is marked non-null but is null", ex.getMessage());
    }

    @Test
    void oferta_constructor_deberiaRechazarSiLaPujaEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Oferta(subasta, postor, null, "clave-1"));

        assertEquals("puja is marked non-null but is null", ex.getMessage());
    }

    @Test
    void oferta_constructor_deberiaRechazarSiLaClaveDeIdempotenciaEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Oferta(subasta, postor, new BigDecimal("120"), null));

        assertEquals("idempotencyKey is marked non-null but is null", ex.getMessage());
    }

    @Test
    void oferta_constructor_deberiaRechazarSiLaFechaDeCreacionEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Oferta(subasta, postor, new BigDecimal("120"), null, "clave-1"));

        assertEquals("creadoEn is marked non-null but is null", ex.getMessage());
    }

    @Test
    void oferta_constructor_deberiaRechazarSiLaPujaEsCero() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new Oferta(subasta, postor, BigDecimal.ZERO, "clave-1"));

        assertEquals("La puja debe ser mayor que 0", ex.getMessage());
    }

    @Test
    void oferta_constructor_deberiaRechazarSiLaPujaEsNegativa() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new Oferta(subasta, postor, new BigDecimal("-10"), "clave-1"));

        assertEquals("La puja debe ser mayor que 0", ex.getMessage());
    }

    @Test
    void oferta_constructor_deberiaRechazarSiLaClaveDeIdempotenciaEstaVacia() {
        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> new Oferta(subasta, postor, new BigDecimal("120"), ""));

        assertEquals("La clave de idempotencia es obligatoria", ex.getMessage());
    }

    @Test
    void oferta_constructor_deberiaRechazarSiLaClaveDeIdempotenciaSoloTieneEspacios() {
        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> new Oferta(subasta, postor, new BigDecimal("120"), "   "));

        assertEquals("La clave de idempotencia es obligatoria", ex.getMessage());
    }

// Oferta: setters

    @Test
    void oferta_setEsSuperior_deberiaActualizarElEstadoDeLaOferta() {
        Oferta oferta = new Oferta(subasta, postor, new BigDecimal("120"), "clave-1");

        oferta.setEsSuperior(false);

        assertFalse(oferta.getEsSuperior());
    }

    // Subasta: constructor completo, parámetros nulos

    @Test
    void constructorCompleto_deberiaRechazarSiElPropietarioEsNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Subasta(null, bolsa, 10, new BigDecimal("100"),
                        LocalDateTime.now().plusDays(1), LocalDateTime.now()));

        assertEquals("propietario is marked non-null but is null", ex.getMessage());
    }

    @Test
    void constructorCompleto_deberiaRechazarSiLaBolsaDeJugadorEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Subasta(propietario, null, 10, new BigDecimal("100"),
                        LocalDateTime.now().plusDays(1), LocalDateTime.now()));

        assertEquals("bolsaDeJugador is marked non-null but is null", ex.getMessage());
    }

    @Test
    void constructorCompleto_deberiaRechazarSiLaCantidadOfrecidaEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Subasta(propietario, bolsa, null, new BigDecimal("100"),
                        LocalDateTime.now().plusDays(1), LocalDateTime.now()));

        assertEquals("cantidadOfrecida is marked non-null but is null", ex.getMessage());
    }

    @Test
    void constructorCompleto_deberiaRechazarSiElPrecioInicialEsNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Subasta(propietario, bolsa, 10, null,
                        LocalDateTime.now().plusDays(1), LocalDateTime.now()));

        assertEquals("precioInicial is marked non-null but is null", ex.getMessage());
    }

    @Test
    void constructorCompleto_deberiaRechazarSiElVencimientoEsNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Subasta(propietario, bolsa, 10, new BigDecimal("100"),
                        null, LocalDateTime.now()));

        assertEquals("vencimiento is marked non-null but is null", ex.getMessage());
    }

// Oferta: constructor completo, parámetros nulos

    @Test
    void oferta_constructorCompleto_deberiaRechazarSiLaSubastaEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Oferta(null, postor, new BigDecimal("120"),
                        LocalDateTime.now(), "clave-1"));

        assertEquals("subasta is marked non-null but is null", ex.getMessage());
    }

    @Test
    void oferta_constructorCompleto_deberiaRechazarSiElUsuarioEsNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Oferta(subasta, null, new BigDecimal("120"),
                        LocalDateTime.now(), "clave-1"));

        assertEquals("usuario is marked non-null but is null", ex.getMessage());
    }

    @Test
    void oferta_constructorCompleto_deberiaRechazarSiLaPujaEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Oferta(subasta, postor, null,
                        LocalDateTime.now(), "clave-1"));

        assertEquals("puja is marked non-null but is null", ex.getMessage());
    }

    @Test
    void oferta_constructorCompleto_deberiaRechazarSiLaClaveDeIdempotenciaEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Oferta(subasta, postor, new BigDecimal("120"),
                        LocalDateTime.now(), null));

        assertEquals("idempotencyKey is marked non-null but is null", ex.getMessage());
    }

// Oferta: getSubastaId y getUsuarioId

    @Test
    void oferta_getSubastaId_deberiaDevolverElIdDeLaSubasta() {
        UUID subastaId = UUID.randomUUID();
        subasta.setId(subastaId);
        Oferta oferta = new Oferta(subasta, postor, new BigDecimal("120"), "clave-1");

        assertEquals(subastaId, oferta.getSubastaId());
    }

    @Test
    void oferta_getSubastaId_deberiaDevolverNuloSiLaSubastaNoTieneId() {
        Oferta oferta = new Oferta(subasta, postor, new BigDecimal("120"), "clave-1");

        assertNull(oferta.getSubastaId());
    }

    @Test
    void oferta_getUsuarioId_deberiaDevolverElIdDelUsuario() {
        UUID usuarioId = UUID.randomUUID();
        when(postor.getId()).thenReturn(usuarioId);
        Oferta oferta = new Oferta(subasta, postor, new BigDecimal("120"), "clave-1");

        assertEquals(usuarioId, oferta.getUsuarioId());
    }


    @Test
    void asignarOfertaGanadora_deberiaAsignarLaOfertaComoOfertaActual() {
        Oferta ganadora = oferta("120");

        subasta.asignarOfertaGanadora(ganadora);

        assertSame(ganadora, subasta.getOfertaActual());
    }

    @Test
    void asignarOfertaGanadora_deberiaPermitirReasignarUnaOfertaConLaMismaPuja() {
        subasta.actualizarPostorMasGrande(oferta("120"));
        Oferta ganadora = oferta("120");

        subasta.asignarOfertaGanadora(ganadora);

        assertSame(ganadora, subasta.getOfertaActual());
    }

    @Test
    void asignarOfertaGanadora_deberiaPermitirAsignarSiLaSubastaYaVencio() {
        Subasta vencida = subastaVencida();
        Oferta ganadora = oferta("120");

        vencida.asignarOfertaGanadora(ganadora);

        assertSame(ganadora, vencida.getOfertaActual());
    }

    @Test
    void asignarOfertaGanadora_deberiaActualizarLaFechaDeActualizacion() {
        LocalDateTime antes = subasta.getActualizadoEn();

        subasta.asignarOfertaGanadora(oferta("120"));

        assertFalse(subasta.getActualizadoEn().isBefore(antes));
    }

    @Test
    void asignarOfertaGanadora_deberiaRechazarSiLaOfertaEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> subasta.asignarOfertaGanadora(null));

        assertEquals("oferta is marked non-null but is null", ex.getMessage());
    }
}
