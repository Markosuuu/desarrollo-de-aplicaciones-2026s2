package com.prontaentrega.models;

import com.prontaentrega.models.auth.Usuario;
import com.prontaentrega.models.exception.AnuncioNoPendienteException;
import com.prontaentrega.models.exception.CampoFaltanteException;
import com.prontaentrega.models.exception.CantidadDeTokensException;
import com.prontaentrega.models.exception.ValorNegativoException;
import com.prontaentrega.models.jugadores.*;
import com.prontaentrega.models.mercado.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@Tag("modelo")
public class MercadoTest {

    private Usuario vendedor;
    private Usuario contraparte;
    private Jugador jugador;
    private BolsaDeJugador bolsa;

    @BeforeEach
    void setUp() {
        vendedor = mock(Usuario.class);
        contraparte = mock(Usuario.class);

        jugador = new Jugador(1, "Messi", "Barcelona FC", Liga.LALIGA, Posicion.FORWARD,
                40, 170, 70);

        EstadisticasDeportivas estadisticas = new EstadisticasDeportivas(jugador,
                true, 10, 10,
                BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE,
                BigDecimal.ONE, BigDecimal.ONE, 120);

        bolsa = new BolsaDeJugador(estadisticas, new BigDecimal("2.5"), LocalDateTime.now(), "Fuente", 100);
    }

    // Constructor: casos exitosos

    @Test
    void constructor_deberiaCrearAnuncioPendienteConLosDatosIndicados() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);

        assertSame(vendedor, anuncio.getVendedor());
        assertSame(bolsa, anuncio.getBolsaDeJugador());
        assertEquals(10, anuncio.getCantidadTokens());
        assertEquals(EstadoAnuncioVenta.PENDIENTE, anuncio.getEstado());
    }

    @Test
    void constructor_deberiaTomarElPrecioPorTokenDeLaCotizacionActualDeLaBolsa() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);

        assertThat(anuncio.getPrecioPorToken()).isEqualByComparingTo("2.5");
    }

    @Test
    void constructor_deberiaCalcularElPrecioTotalSegunLaCantidad() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);

        assertThat(anuncio.getPrecioTotal()).isEqualByComparingTo("25");
    }

    @Test
    void constructor_deberiaInicializarCreadoEnYActualizadoEnConElMismoValor() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);

        assertNotNull(anuncio.getCreadoEn());
        assertEquals(anuncio.getCreadoEn(), anuncio.getActualizadoEn());
    }

    // Constructor: casos fallidos

    @Test
    void constructor_deberiaRechazarSiLaCantidadEsCero() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new AnuncioVenta(vendedor, bolsa, 0));

        assertEquals("La cantidad a vender no puede ser 0 o negativa", ex.getMessage());
    }

    @Test
    void constructor_deberiaRechazarSiLaCantidadEsNegativa() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new AnuncioVenta(vendedor, bolsa, -5));

        assertEquals("La cantidad a vender no puede ser 0 o negativa", ex.getMessage());
    }

    @Test
    void constructor_deberiaRechazarSiElVendedorEsNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new AnuncioVenta(null, bolsa, 10));

        assertEquals("vendedor is marked non-null but is null", ex.getMessage());
    }

    @Test
    void constructor_deberiaRechazarSiLaBolsaDeJugadorEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new AnuncioVenta(vendedor, null, 10));

        assertEquals("bolsaDeJugador is marked non-null but is null", ex.getMessage());
    }

    @Test
    void constructor_deberiaRechazarSiLaCantidadDeTokensEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new AnuncioVenta(vendedor, bolsa, null));

        assertEquals("cantidadTokens is marked non-null but is null", ex.getMessage());
    }

    // actualizarTokensEnVenta: casos exitosos

    @Test
    void actualizarTokensEnVenta_deberiaDescontarLosTokensSolicitados() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);

        anuncio.actualizarTokensEnVenta(4);

        assertEquals(6, anuncio.getCantidadTokens());
    }

    @Test
    void actualizarTokensEnVenta_deberiaRecalcularElPrecioTotalConLosTokensRestantes() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);

        anuncio.actualizarTokensEnVenta(4);

        assertThat(anuncio.getPrecioTotal()).isEqualByComparingTo("15");
    }

    @Test
    void actualizarTokensEnVenta_deberiaMantenerElAnuncioPendienteSiQuedanTokens() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);

        anuncio.actualizarTokensEnVenta(9);

        assertEquals(EstadoAnuncioVenta.PENDIENTE, anuncio.getEstado());
    }

    @Test
    void actualizarTokensEnVenta_deberiaCompletarElAnuncioSiSeVendenTodosLosTokens() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);

        anuncio.actualizarTokensEnVenta(10);

        assertEquals(0, anuncio.getCantidadTokens());
        assertEquals(EstadoAnuncioVenta.COMPLETADA, anuncio.getEstado());
        assertThat(anuncio.getPrecioTotal()).isEqualByComparingTo("0");
    }

    @Test
    void actualizarTokensEnVenta_deberiaActualizarLaFechaDeActualizacion() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);
        LocalDateTime antes = anuncio.getActualizadoEn();

        anuncio.actualizarTokensEnVenta(1);

        assertFalse(anuncio.getActualizadoEn().isBefore(antes));
    }

    // actualizarTokensEnVenta: casos fallidos

    @Test
    void actualizarTokensEnVenta_deberiaRechazarSiLaCantidadEsNula() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);

        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> anuncio.actualizarTokensEnVenta(null));

        assertEquals("Cantidad inválida", ex.getMessage());
    }

    @Test
    void actualizarTokensEnVenta_deberiaRechazarSiLaCantidadEsCero() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);

        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> anuncio.actualizarTokensEnVenta(0));

        assertEquals("La cantidad no puede ser negativa", ex.getMessage());
    }

    @Test
    void actualizarTokensEnVenta_deberiaRechazarSiLaCantidadEsNegativa() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);

        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> anuncio.actualizarTokensEnVenta(-3));

        assertEquals("La cantidad no puede ser negativa", ex.getMessage());
    }

    @Test
    void actualizarTokensEnVenta_deberiaRechazarSiLaCantidadSuperaLosTokensEnVenta() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);

        CantidadDeTokensException ex = assertThrows(CantidadDeTokensException.class,
                () -> anuncio.actualizarTokensEnVenta(11));

        assertEquals("No hay suficientes tokens", ex.getMessage());
    }

    @Test
    void actualizarTokensEnVenta_noDeberiaModificarElAnuncioSiLaActualizacionFalla() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);

        assertThrows(CantidadDeTokensException.class, () -> anuncio.actualizarTokensEnVenta(11));

        assertEquals(10, anuncio.getCantidadTokens());
        assertThat(anuncio.getPrecioTotal()).isEqualByComparingTo("25");
        assertEquals(EstadoAnuncioVenta.PENDIENTE, anuncio.getEstado());
    }

    // cancelarAnuncio

    @Test
    void cancelarAnuncio_deberiaCancelarUnAnuncioPendiente() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);

        anuncio.cancelarAnuncio();

        assertEquals(EstadoAnuncioVenta.CANCELADA, anuncio.getEstado());
    }

    @Test
    void cancelarAnuncio_deberiaActualizarLaFechaDeActualizacion() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);
        LocalDateTime antes = anuncio.getActualizadoEn();

        anuncio.cancelarAnuncio();

        assertFalse(anuncio.getActualizadoEn().isBefore(antes));
    }

    @Test
    void cancelarAnuncio_deberiaRechazarSiElAnuncioYaEstaCancelado() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);
        anuncio.cancelarAnuncio();

        AnuncioNoPendienteException ex = assertThrows(AnuncioNoPendienteException.class,
                anuncio::cancelarAnuncio);

        assertEquals("El anuncio no está pendiente para cancelarlo", ex.getMessage());
    }

    @Test
    void cancelarAnuncio_deberiaRechazarSiElAnuncioYaEstaCompletado() {
        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, 10);
        anuncio.actualizarTokensEnVenta(10);

        AnuncioNoPendienteException ex = assertThrows(AnuncioNoPendienteException.class,
                anuncio::cancelarAnuncio);

        assertEquals("El anuncio no está pendiente para cancelarlo", ex.getMessage());
        assertEquals(EstadoAnuncioVenta.COMPLETADA, anuncio.getEstado());
    }

    // Tests de Inventario

    // InventarioUsuario: constructor

    @Test
    void inventario_constructor_deberiaCrearInventarioConLosDatosIndicados() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        assertSame(vendedor, inventario.getUsuario());
        assertSame(bolsa, inventario.getBolsaDeJugador());
        assertEquals(10, inventario.getTokensDisponibles());
        assertEquals(5, inventario.getTokensEnVenta());
        assertNotNull(inventario.getActualizadoEn());
    }

    @Test
    void inventario_constructor_deberiaRechazarSiLosTokensDisponiblesSonNegativos() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new InventarioUsuario(vendedor, bolsa, -1, 5));

        assertEquals("Los tokens no pueden ser negativos", ex.getMessage());
    }

    @Test
    void inventario_constructor_deberiaRechazarSiLosTokensEnVentaSonNegativos() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new InventarioUsuario(vendedor, bolsa, 10, -1));

        assertEquals("Los tokens no pueden ser negativos", ex.getMessage());
    }

    @Test
    void inventario_constructor_deberiaRechazarSiElUsuarioEsNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new InventarioUsuario(null, bolsa, 10, 5));

        assertEquals("usuario is marked non-null but is null", ex.getMessage());
    }

    @Test
    void inventario_constructor_deberiaRechazarSiLaBolsaDeJugadorEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new InventarioUsuario(vendedor, null, 10, 5));

        assertEquals("bolsaDeJugador is marked non-null but is null", ex.getMessage());
    }

    @Test
    void inventario_constructor_deberiaRechazarSiLosTokensDisponiblesSonNulos() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new InventarioUsuario(vendedor, bolsa, null, 5));

        assertEquals("tokensDisponibles is marked non-null but is null", ex.getMessage());
    }

    @Test
    void inventario_constructor_deberiaRechazarSiLosTokensEnVentaSonNulos() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new InventarioUsuario(vendedor, bolsa, 10, null));

        assertEquals("tokensEnVenta is marked non-null but is null", ex.getMessage());
    }

// InventarioUsuario: agregarTokens

    @Test
    void agregarTokens_deberiaSumarLosTokensAlosDisponibles() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        inventario.agregarTokens(3);

        assertEquals(13, inventario.getTokensDisponibles());
        assertEquals(5, inventario.getTokensEnVenta());
    }

    @Test
    void agregarTokens_deberiaActualizarLaFechaDeActualizacion() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);
        LocalDateTime antes = inventario.getActualizadoEn();

        inventario.agregarTokens(3);

        assertFalse(inventario.getActualizadoEn().isBefore(antes));
    }

    @Test
    void agregarTokens_deberiaRechazarSiLaCantidadEsNula() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> inventario.agregarTokens(null));

        assertEquals("Cantidad inválida", ex.getMessage());
    }

    @Test
    void agregarTokens_deberiaRechazarSiLaCantidadEsCero() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> inventario.agregarTokens(0));

        assertEquals("La cantidad debe ser mayor que 0", ex.getMessage());
    }

    @Test
    void agregarTokens_deberiaRechazarSiLaCantidadEsNegativa() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> inventario.agregarTokens(-2));

        assertEquals("La cantidad debe ser mayor que 0", ex.getMessage());
        assertEquals(10, inventario.getTokensDisponibles());
    }

// InventarioUsuario: quitarTokens

    @Test
    void quitarTokens_deberiaRestarLosTokensEnVenta() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        inventario.quitarTokens(2);

        assertEquals(3, inventario.getTokensEnVenta());
        assertEquals(10, inventario.getTokensDisponibles());
    }

    @Test
    void quitarTokens_deberiaPermitirQuitarTodosLosTokensEnVenta() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        inventario.quitarTokens(5);

        assertEquals(0, inventario.getTokensEnVenta());
    }

    @Test
    void quitarTokens_deberiaActualizarLaFechaDeActualizacion() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);
        LocalDateTime antes = inventario.getActualizadoEn();

        inventario.quitarTokens(1);

        assertFalse(inventario.getActualizadoEn().isBefore(antes));
    }

    @Test
    void quitarTokens_deberiaRechazarSiLaCantidadEsNula() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> inventario.quitarTokens(null));

        assertEquals("Cantidad inválida", ex.getMessage());
    }

    @Test
    void quitarTokens_deberiaRechazarSiLaCantidadEsCero() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> inventario.quitarTokens(0));

        assertEquals("La cantidad debe ser mayor que 0", ex.getMessage());
    }

    @Test
    void quitarTokens_deberiaRechazarSiLaCantidadEsNegativa() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> inventario.quitarTokens(-1));

        assertEquals("La cantidad debe ser mayor que 0", ex.getMessage());
    }

    @Test
    void quitarTokens_deberiaRechazarSiSePidenMasTokensDeLosQueHayEnVenta() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        CantidadDeTokensException ex = assertThrows(CantidadDeTokensException.class,
                () -> inventario.quitarTokens(6));

        assertEquals("No hay suficientes tokens en venta", ex.getMessage());
        assertEquals(5, inventario.getTokensEnVenta());
    }

// InventarioUsuario: ponerTokensEnVenta

    @Test
    void ponerTokensEnVenta_deberiaMoverTokensDeDisponiblesAEnVenta() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        inventario.ponerTokensEnVenta(4);

        assertEquals(6, inventario.getTokensDisponibles());
        assertEquals(9, inventario.getTokensEnVenta());
    }

    @Test
    void ponerTokensEnVenta_deberiaPermitirPonerTodosLosTokensDisponibles() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        inventario.ponerTokensEnVenta(10);

        assertEquals(0, inventario.getTokensDisponibles());
        assertEquals(15, inventario.getTokensEnVenta());
    }

    @Test
    void ponerTokensEnVenta_deberiaActualizarLaFechaDeActualizacion() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);
        LocalDateTime antes = inventario.getActualizadoEn();

        inventario.ponerTokensEnVenta(1);

        assertFalse(inventario.getActualizadoEn().isBefore(antes));
    }

    @Test
    void ponerTokensEnVenta_deberiaRechazarSiLaCantidadEsNula() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> inventario.ponerTokensEnVenta(null));

        assertEquals("Cantidad inválida", ex.getMessage());
    }

    @Test
    void ponerTokensEnVenta_deberiaRechazarSiLaCantidadEsCero() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> inventario.ponerTokensEnVenta(0));

        assertEquals("La cantidad debe ser mayor que 0", ex.getMessage());
    }

    @Test
    void ponerTokensEnVenta_deberiaRechazarSiLaCantidadEsNegativa() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> inventario.ponerTokensEnVenta(-4));

        assertEquals("La cantidad debe ser mayor que 0", ex.getMessage());
        assertEquals(10, inventario.getTokensDisponibles());
    }

    @Test
    void ponerTokensEnVenta_deberiaRechazarSiNoHayTokensDisponiblesSuficientes() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        CantidadDeTokensException ex = assertThrows(CantidadDeTokensException.class,
                () -> inventario.ponerTokensEnVenta(11));

        assertEquals("No posees suficientes tokens para la transacción", ex.getMessage());
        assertEquals(10, inventario.getTokensDisponibles());
        assertEquals(5, inventario.getTokensEnVenta());
    }

// InventarioUsuario: actualizarPorVenta

    @Test
    void actualizarPorVenta_deberiaDescontarLosTokensEnVenta() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        inventario.actualizarPorVenta(2, 5);

        assertEquals(3, inventario.getTokensEnVenta());
        assertEquals(10, inventario.getTokensDisponibles());
    }

    @Test
    void actualizarPorVenta_deberiaActualizarLaFechaDeActualizacion() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);
        LocalDateTime antes = inventario.getActualizadoEn();

        inventario.actualizarPorVenta(2, 5);

        assertFalse(inventario.getActualizadoEn().isBefore(antes));
    }

    @Test
    void actualizarPorVenta_deberiaRechazarSiElAnuncioTieneMasTokensQueLosEnVenta() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        CantidadDeTokensException ex = assertThrows(CantidadDeTokensException.class,
                () -> inventario.actualizarPorVenta(2, 6));

        assertEquals("La publicación ya no tiene tokens disponibles", ex.getMessage());
        assertEquals(5, inventario.getTokensEnVenta());
    }

    @Test
    void actualizarPorVenta_deberiaRechazarSiLaCantidadEsNula() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> inventario.actualizarPorVenta(null, 5));

        assertEquals("Cantidad inválida", ex.getMessage());
    }

    @Test
    void actualizarPorVenta_deberiaRechazarSiLaCantidadEsCero() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> inventario.actualizarPorVenta(0, 5));

        assertEquals("La cantidad debe ser mayor que 0", ex.getMessage());
    }

    @Test
    void actualizarPorVenta_deberiaRechazarSiSeVendenMasTokensDeLosQueHayEnVenta() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        CantidadDeTokensException ex = assertThrows(CantidadDeTokensException.class,
                () -> inventario.actualizarPorVenta(6, 5));

        assertEquals("No hay suficientes tokens en venta", ex.getMessage());
    }

// InventarioUsuario: actualizarPorCompra

    @Test
    void actualizarPorCompra_deberiaSumarLosTokensComprados() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        inventario.actualizarPorCompra(3);

        assertEquals(13, inventario.getTokensDisponibles());
        assertEquals(5, inventario.getTokensEnVenta());
    }

    @Test
    void actualizarPorCompra_deberiaRechazarSiLaCantidadEsNula() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> inventario.actualizarPorCompra(null));

        assertEquals("Cantidad inválida", ex.getMessage());
    }

    @Test
    void actualizarPorCompra_deberiaRechazarSiLaCantidadEsNegativa() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> inventario.actualizarPorCompra(-1));

        assertEquals("La cantidad debe ser mayor que 0", ex.getMessage());
    }

// InventarioUsuario: cancelarVenta

    @Test
    void cancelarVenta_deberiaDevolverLosTokensEnVentaADisponibles() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        inventario.cancelarVenta(3);

        assertEquals(13, inventario.getTokensDisponibles());
        assertEquals(2, inventario.getTokensEnVenta());
    }

    @Test
    void cancelarVenta_deberiaActualizarLaFechaDeActualizacion() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);
        LocalDateTime antes = inventario.getActualizadoEn();

        inventario.cancelarVenta(3);

        assertFalse(inventario.getActualizadoEn().isBefore(antes));
    }

    @Test
    void cancelarVenta_deberiaRechazarSiLaCantidadEsNula() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> inventario.cancelarVenta(null));

        assertEquals("Cantidad inválida", ex.getMessage());
    }

    @Test
    void cancelarVenta_deberiaRechazarSiLaCantidadEsCero() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> inventario.cancelarVenta(0));

        assertEquals("La cantidad debe ser mayor que 0", ex.getMessage());
    }

    @Test
    void cancelarVenta_deberiaRechazarSiLaCantidadEsNegativa() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> inventario.cancelarVenta(-2));

        assertEquals("La cantidad debe ser mayor que 0", ex.getMessage());
    }

    @Test
    void cancelarVenta_deberiaRechazarSiSeCancelanMasTokensDeLosQueHayEnVenta() {
        InventarioUsuario inventario = new InventarioUsuario(vendedor, bolsa, 10, 5);

        CantidadDeTokensException ex = assertThrows(CantidadDeTokensException.class,
                () -> inventario.cancelarVenta(6));

        assertEquals("No hay suficientes tokens en venta", ex.getMessage());
        assertEquals(10, inventario.getTokensDisponibles());
        assertEquals(5, inventario.getTokensEnVenta());
    }

    // Tests de Operación

    @Test
    void operacion_constructor_deberiaAsignarTodosLosDatosIndicados() {
        Operacion operacion = new Operacion(vendedor, contraparte, jugador, 4,
                new BigDecimal("2.5"), new BigDecimal("10"),
                TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA);

        assertSame(vendedor, operacion.getUsuario());
        assertSame(contraparte, operacion.getContraparte());
        assertSame(jugador, operacion.getJugador());
        assertEquals(4, operacion.getCantidad());
        assertThat(operacion.getPrecioPorToken()).isEqualByComparingTo("2.5");
        assertThat(operacion.getTotal()).isEqualByComparingTo("10");
        assertEquals(TipoOperacion.COMPRA, operacion.getTipo());
        assertEquals(EstadoOperacion.COMPLETADA, operacion.getEstado());
    }

    @Test
    void operacion_constructor_deberiaInicializarLaFechaDeCreacion() {
        LocalDateTime antes = LocalDateTime.now();

        Operacion operacion = new Operacion(vendedor, contraparte, jugador, 4,
                new BigDecimal("2.5"), new BigDecimal("10"),
                TipoOperacion.VENTA, EstadoOperacion.COMPLETADA);

        assertNotNull(operacion.getCreadaEn());
        assertFalse(operacion.getCreadaEn().isBefore(antes));
    }

    @Test
    void operacion_constructor_deberiaDejarElIdSinAsignar() {
        Operacion operacion = new Operacion(vendedor, contraparte, jugador, 4,
                new BigDecimal("2.5"), new BigDecimal("10"),
                TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA);

        assertNull(operacion.getId());
    }

    @Test
    void operacion_setId_deberiaAsignarElIdentificador() {
        Operacion operacion = new Operacion(vendedor, contraparte, jugador, 4,
                new BigDecimal("2.5"), new BigDecimal("10"),
                TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA);
        UUID id = UUID.randomUUID();

        operacion.setId(id);

        assertEquals(id, operacion.getId());
    }

    // Operacion: parámetros nulos

    @Test
    void operacion_constructor_deberiaRechazarSiElUsuarioEsNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Operacion(null, contraparte, jugador, 4,
                        new BigDecimal("2.5"), new BigDecimal("10"),
                        TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA));

        assertEquals("usuario is marked non-null but is null", ex.getMessage());
    }

    @Test
    void operacion_constructor_deberiaRechazarSiLaContraparteEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Operacion(vendedor, null, jugador, 4,
                        new BigDecimal("2.5"), new BigDecimal("10"),
                        TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA));

        assertEquals("contraparte is marked non-null but is null", ex.getMessage());
    }

    @Test
    void operacion_constructor_deberiaRechazarSiElJugadorEsNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Operacion(vendedor, contraparte, null, 4,
                        new BigDecimal("2.5"), new BigDecimal("10"),
                        TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA));

        assertEquals("jugador is marked non-null but is null", ex.getMessage());
    }

    @Test
    void operacion_constructor_deberiaRechazarSiLaCantidadEsNula() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Operacion(vendedor, contraparte, jugador, null,
                        new BigDecimal("2.5"), new BigDecimal("10"),
                        TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA));

        assertEquals("cantidad is marked non-null but is null", ex.getMessage());
    }

    @Test
    void operacion_constructor_deberiaRechazarSiElPrecioPorTokenEsNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Operacion(vendedor, contraparte, jugador, 4,
                        null, new BigDecimal("10"),
                        TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA));

        assertEquals("precioPorToken is marked non-null but is null", ex.getMessage());
    }

    @Test
    void operacion_constructor_deberiaRechazarSiElTotalEsNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Operacion(vendedor, contraparte, jugador, 4,
                        new BigDecimal("2.5"), null,
                        TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA));

        assertEquals("total is marked non-null but is null", ex.getMessage());
    }

    @Test
    void operacion_constructor_deberiaRechazarSiElTipoEsNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Operacion(vendedor, contraparte, jugador, 4,
                        new BigDecimal("2.5"), new BigDecimal("10"),
                        null, EstadoOperacion.COMPLETADA));

        assertEquals("tipo is marked non-null but is null", ex.getMessage());
    }

    @Test
    void operacion_constructor_deberiaRechazarSiElEstadoEsNulo() {
        NullPointerException ex = assertThrows(NullPointerException.class,
                () -> new Operacion(vendedor, contraparte, jugador, 4,
                        new BigDecimal("2.5"), new BigDecimal("10"),
                        TipoOperacion.COMPRA, null));

        assertEquals("estado is marked non-null but is null", ex.getMessage());
    }

    // Operacion: valores inválidos

    @Test
    void operacion_constructor_deberiaRechazarSiLaCantidadEsCero() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new Operacion(vendedor, contraparte, jugador, 0,
                        new BigDecimal("2.5"), new BigDecimal("10"),
                        TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA));

        assertEquals("La cantidad debe ser mayor que 0", ex.getMessage());
    }

    @Test
    void operacion_constructor_deberiaRechazarSiLaCantidadEsNegativa() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new Operacion(vendedor, contraparte, jugador, -4,
                        new BigDecimal("2.5"), new BigDecimal("10"),
                        TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA));

        assertEquals("La cantidad debe ser mayor que 0", ex.getMessage());
    }

    @Test
    void operacion_constructor_deberiaRechazarSiElTotalEsNegativo() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> new Operacion(vendedor, contraparte, jugador, 4,
                        new BigDecimal("2.5"), new BigDecimal("-10"),
                        TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA));

        assertEquals("El total no puede ser negativo", ex.getMessage());
    }

    @Test
    void operacion_constructor_deberiaAceptarUnTotalIgualACero() {
        Operacion operacion = new Operacion(vendedor, contraparte, jugador, 4,
                new BigDecimal("2.5"), BigDecimal.ZERO,
                TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA);

        assertThat(operacion.getTotal()).isEqualByComparingTo("0");
    }
}
