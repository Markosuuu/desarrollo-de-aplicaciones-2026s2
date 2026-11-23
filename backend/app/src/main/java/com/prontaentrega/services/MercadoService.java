package com.prontaentrega.services;

import com.prontaentrega.models.exception.AnuncioNoPendienteException;
import com.prontaentrega.models.mercado.AnuncioVenta;
import com.prontaentrega.models.jugadores.BolsaDeJugador;
import com.prontaentrega.models.jugadores.Jugador;
import com.prontaentrega.models.mercado.EstadoAnuncioVenta;
import com.prontaentrega.models.mercado.EstadoOperacion;
import com.prontaentrega.models.mercado.Operacion;
import com.prontaentrega.models.mercado.TipoOperacion;
import com.prontaentrega.models.auth.Usuario;
import com.prontaentrega.models.mercado.InventarioUsuario;
import com.prontaentrega.models.exception.CantidadDeTokensException;
import com.prontaentrega.persistence.repository.AnuncioVentaRepository;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.persistence.repository.OperacionRepository;
import com.prontaentrega.persistence.repository.InventarioUsuarioRepository;
import com.prontaentrega.persistence.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.prontaentrega.services.dto.JugadorResumenTokensInventario;
import com.prontaentrega.services.dto.ResumenInventarioTokensResponse;
import com.prontaentrega.services.exceptions.auth.UsuarioNoEncontradoException;
import com.prontaentrega.services.exceptions.jugadores.JugadorNoEncontradoException;
import com.prontaentrega.services.exceptions.mercado.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio para manejar la lógica del marketplace, incluyendo la compra y venta de tokens.
 */
@Service
public class MercadoService {
    private final AnuncioVentaRepository anuncioVentaRepository;
    private final InventarioUsuarioRepository inventarioUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final JugadorRepository jugadorRepository;
    private final OperacionRepository operacionRepository;

    private static final String JUGADOR_NO_ENCONTRADO = "Jugador no encontrado con el id: ";

    public MercadoService(AnuncioVentaRepository anuncioVentaRepository,
                          InventarioUsuarioRepository inventarioUsuarioRepository,
                          UsuarioRepository usuarioRepository,
                          JugadorRepository jugadorRepository,
                          OperacionRepository operacionRepository) {
        this.anuncioVentaRepository = anuncioVentaRepository;
        this.inventarioUsuarioRepository = inventarioUsuarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.jugadorRepository = jugadorRepository;
        this.operacionRepository = operacionRepository;
    }

    /**
     * Metodo para obtener los anuncios de venta pendientes en el mercado.
     * @param page Nro de página
     * @param size Cantidad de anuncios por página
     * @return Lista de anuncios de venta
     */
    public Page<AnuncioVenta> getAnuncios(int page, int size) {
        Pageable pageable = toPageable(page, size);
        return anuncioVentaRepository.findByEstado(EstadoAnuncioVenta.PENDIENTE, pageable);
    }


    /**
     * Metodo para vender tokens en el mercado.
     *
     * @param usuarioId El ID del usuario que está vendiendo los tokens
     * @param jugadorId El ID del jugador cuyos tokens se están vendiendo
     * @param cantidad La cantidad de tokens a vender
     * @throws UsuarioNoEncontradoException Si el usuario no existe
     * @throws JugadorNoEncontradoException Si el jugador no existe
     * @throws CantidadDeTokensException Si la cantidad de tokens es inválida o insuficiente para vender
     * @return El anuncio de venta creado
     */
    public AnuncioVenta vender(UUID usuarioId, UUID jugadorId, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new CantidadDeTokensException("La cantidad debe ser mayor a cero");
        }

        Usuario vendedor = buscarUsuario(usuarioId);

        BolsaDeJugador bolsa = buscarJugador(jugadorId);

        InventarioUsuario inventario = buscarInventario(usuarioId, jugadorId, vendedor, bolsa);

        inventario.ponerTokensEnVenta(cantidad);
        inventarioUsuarioRepository.save(inventario);

        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, cantidad);
        anuncioVentaRepository.save(anuncio);

        return anuncio;
    }

    /**
     * Metodo para comprar tokens en el mercado.
     * @param compradorId El ID del usuario que está comprando los tokens
     * @param anuncioId El ID del anuncio de venta que se está comprando
     * @throws AnuncioNoEncontradoException Si el anuncio no existe
     * @throws AnuncioNoPendienteException Si el anuncio no está pendiente
     * @throws VendedorComprandoTokensPropiosException Si el comprador es el mismo que el vendedor
     * @throws UsuarioNoEncontradoException Si el comprador o vendedor no existen
     * @throws CreditosInsuficientesException Si el comprador no tiene suficientes créditos
     * @throws CantidadDeTokensException Si la cantidad de tokens en el anuncio es mayor a la cantidad de tokens en venta del vendedor
     */
    @Transactional
    public void comprar(UUID compradorId, UUID anuncioId, Integer cantidadSolicitada) {
        AnuncioVenta anuncio = buscarAnuncio(anuncioId);

        validarCompra(compradorId, cantidadSolicitada, anuncio);

        Usuario comprador = buscarUsuario(compradorId);

        Usuario vendedor = buscarUsuario(anuncio.getVendedor().getId());

        UUID jugadorId = anuncio.getBolsaDeJugador()
                .getEstadisticasDeportivas()
                .getJugador()
                .getId();

        BolsaDeJugador bolsaJugador = buscarJugador(jugadorId);

        BigDecimal precioCompra = anuncio.getPrecioPorToken()
                .multiply(BigDecimal.valueOf(cantidadSolicitada));

        validarCreditos(comprador, precioCompra);

        // Actualización del inventario del vendedor
        InventarioUsuario inventarioVendedor = buscarInventario(vendedor.getId(), jugadorId, vendedor, bolsaJugador);
        inventarioVendedor.actualizarPorVenta(cantidadSolicitada, anuncio.getCantidadTokens());
        inventarioUsuarioRepository.save(inventarioVendedor);

        // Actualización del inventario del comprador
        InventarioUsuario inventarioComprador = buscarInventario(compradorId, jugadorId, comprador, bolsaJugador);
        inventarioComprador.actualizarPorCompra(cantidadSolicitada);
        inventarioUsuarioRepository.save(inventarioComprador);

        // Actualización de créditos de usuarios
        comprador.restarCreditos(precioCompra);
        vendedor.sumarCreditos(precioCompra);
        usuarioRepository.save(comprador);
        usuarioRepository.save(vendedor);


        anuncio.actualizarTokensEnVenta(cantidadSolicitada);
        anuncioVentaRepository.save(anuncio);

        Jugador jugador = bolsaJugador.getEstadisticasDeportivas().getJugador();

        // Actualización de las operaciones
        // No es como data repetida? Quizás conviene solo al consultar
        // En cualquier caso se puede suprimir a una Operacion como "transaccion" y quitar el TipoOperaicon

        Operacion compra = new Operacion(comprador, vendedor, jugador,
                cantidadSolicitada, anuncio.getPrecioPorToken(), precioCompra,
                TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA);

        Operacion venta = new Operacion(vendedor, comprador, jugador,
                cantidadSolicitada, anuncio.getPrecioPorToken(), precioCompra,
                TipoOperacion.VENTA, EstadoOperacion.COMPLETADA);

        operacionRepository.save(compra);
        operacionRepository.save(venta);
    }

    /**
     * Metodo para cancelar una venta en el marketplace.
     * @param vendedorId El ID del usuario que está cancelando la venta
     * @param anuncioId El ID del anuncio de venta que se está cancelando
     * @throws AnuncioNoEncontradoException Si el anuncio no existe
     * @throws SinAutorizacionExpcetion Si el usuario no es el vendedor
     * @throws AnuncioNoPendienteException Si el anuncio no está pendiente
     */
    @Transactional
    public void cancelarVenta(UUID vendedorId, UUID anuncioId) {
        AnuncioVenta anuncio = buscarAnuncio(anuncioId);

        verificarCancelacion(vendedorId, anuncio);

        Usuario vendedor = buscarUsuario(vendedorId);

        BolsaDeJugador bolsaJugador = anuncio.getBolsaDeJugador();
        UUID jugadorId = bolsaJugador.getEstadisticasDeportivas().getJugador().getId();

        InventarioUsuario inventario = buscarInventario(vendedorId, jugadorId, vendedor, bolsaJugador);
        inventario.cancelarVenta(anuncio.getCantidadTokens());
        inventarioUsuarioRepository.save(inventario);

        anuncio.cancelarAnuncio();
        anuncioVentaRepository.save(anuncio);
    }

    /**
     * Metodo para obtener las transacciones de un usuario.
     * @param usuarioId El ID del usuario
     * @return Una lista de las transacciones del usuario
     */
    public Page<Operacion> getTransaccionesById(UUID usuarioId, int page, int size) {
        Pageable pageable = toPageable(page, size);
        return operacionRepository.findByUsuarioId(usuarioId, pageable);
    }

    /**
     * Metodo para comprar al super usuario
     * <p>
     * Idea: quizás pasar un boolean al metodo solo para admitir el super y así tener un único metodo de compra?
     *
     * @param compradorId Id del comprador
     * @param jugadorId Id del jugador
     * @param cantidad Cantidad a comprar
     * @throws UsuarioNoEncontradoException Si el comprador o vendedor no existen
     * @throws JugadorNoEncontradoException Si el jugador no existen
     * @throws CreditosInsuficientesException Si el comprador no tiene suficientes créditos
     * @throws CantidadDeTokensException Si la cantidad de tokens en el anuncio es mayor a la cantidad de tokens en venta del vendedor o si la cantidad pedida es menor a 1
     */
    public void comprarSuper(UUID compradorId, UUID jugadorId, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new CantidadDeTokensException("La cantidad debe ser mayor a cero");
        }

        Usuario comprador = buscarUsuario(compradorId);

        BolsaDeJugador bolsaJugador = buscarJugador(jugadorId);

        BigDecimal precioFinal = bolsaJugador.getCotizacionActual().multiply(BigDecimal.valueOf(cantidad));

        validarCreditos(comprador, precioFinal);

        comprador.restarCreditos(precioFinal);
        usuarioRepository.save(comprador);

        InventarioUsuario tokenComprador = buscarInventario(compradorId, jugadorId, comprador, bolsaJugador);
        tokenComprador.agregarTokens(cantidad);
        inventarioUsuarioRepository.save(tokenComprador);

        bolsaJugador.restarTokens(cantidad);
        jugadorRepository.save(bolsaJugador);

        Jugador jugador = bolsaJugador.getEstadisticasDeportivas().getJugador();

        // Mala operacion, porque el comprador y vendedor son el mismo id
        // habría que quitar lógica o ver que onda
        Operacion compra = new Operacion(comprador, comprador, jugador,
                cantidad, bolsaJugador.getCotizacionActual(), precioFinal,
                TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA);

        operacionRepository.save(compra);
    }

    /**
     * Metodo para obtener el inventario de un usuario específico
     * @param usuarioId Un usuario
     * @return Una lista con el inventario del usuario dado.
     * @throws UsuarioNoEncontradoException Si el usuario dado no existe.
     */
    public Page<InventarioUsuario> getInventarioById(UUID usuarioId, int page, int size) {
        if (!usuarioRepository.existsById(usuarioId)) {
            throw new UsuarioNoEncontradoException("Usuario no encontrado con el id: " + usuarioId.toString());
        }

        return inventarioUsuarioRepository.findAllByUsuarioId(usuarioId, toPageable(page, size));
    }

    private Pageable toPageable(int page, int size) {
        int normalizedPage = Math.max(page, 1) - 1;
        int normalizedSize = Math.max(size, 1);
        return PageRequest.of(normalizedPage, normalizedSize);
    }

    @NonNull
    private InventarioUsuario buscarInventario(UUID usuarioId, UUID jugadorId, Usuario vendedor, BolsaDeJugador bolsa) {
        return inventarioUsuarioRepository.findByUsuarioIdAndJugadorId(usuarioId, jugadorId)
                .orElse(new InventarioUsuario(vendedor, bolsa, 0, 0));
    }

    @NonNull
    private BolsaDeJugador buscarJugador(UUID jugadorId) {
        return jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new JugadorNoEncontradoException(JUGADOR_NO_ENCONTRADO + jugadorId.toString()));
    }

    @NonNull
    private Usuario buscarUsuario(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con el id" + usuarioId.toString()));
    }

    @NonNull
    private AnuncioVenta buscarAnuncio(UUID anuncioId) {
        return anuncioVentaRepository.findByIdForUpdate(anuncioId)
                .orElseThrow(() -> new AnuncioNoEncontradoException("El anuncio no existe"));
    }

    private static void validarCompra(UUID compradorId, Integer cantidadSolicitada, AnuncioVenta anuncio) {
        if (anuncio.getEstado() != EstadoAnuncioVenta.PENDIENTE) {
            throw new AnuncioNoPendienteException("La venta ya no está disponible");
        }

        if (cantidadSolicitada == null || cantidadSolicitada <= 0 || cantidadSolicitada > anuncio.getCantidadTokens()) {
            throw new CantidadDeTokensException(
                    "La cantidad debe ser mayor a cero y no superar los tokens disponibles en la oferta");
        }

        if (anuncio.getVendedor().getId().equals(compradorId)) {
            throw new VendedorComprandoTokensPropiosException("No puedes comprar tus propios tokens");
        }
    }

    private static void validarCreditos(Usuario comprador, BigDecimal precioCompra) {
        if (comprador.getCreditosDisponibles() == null || comprador.getCreditosDisponibles().compareTo(precioCompra) < 0) {
            throw new CreditosInsuficientesException("No tienes suficientes créditos");
        }
    }

    private static void verificarCancelacion(UUID vendedorId, AnuncioVenta anuncio) {
        if (!anuncio.getVendedor().getId().equals(vendedorId)) {
            throw new SinAutorizacionExpcetion("No puedes cancelar una venta ajena");
        }

        if (anuncio.getEstado() != EstadoAnuncioVenta.PENDIENTE) {
            throw new AnuncioNoPendienteException("No se puede cancelar una venta completada");
        }
    }


    @Transactional(readOnly = true)
    public ResumenInventarioTokensResponse obtenerResumenTokens(UUID usuarioId) {

        Long totalTokens = inventarioUsuarioRepository.obtenerTotalTokens(usuarioId);

        List<JugadorResumenTokensInventario> jugadores =
                inventarioUsuarioRepository.obtenerResumenTokensPorJugador(usuarioId)
                        .stream()
                        .map(jugador -> new JugadorResumenTokensInventario(
                                jugador.getJugadorId(),
                                jugador.getJugadorNombre(),
                                jugador.getCantidadTokens()
                        ))
                        .toList();

        return new ResumenInventarioTokensResponse(totalTokens, jugadores);
    }
}
