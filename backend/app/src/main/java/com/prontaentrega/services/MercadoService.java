package com.prontaentrega.services;

import com.prontaentrega.models.AnuncioVenta;
import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.Jugador;
import com.prontaentrega.models.enums.EstadoAnuncioVenta;
import com.prontaentrega.models.enums.EstadoOperacion;
import com.prontaentrega.models.Operacion;
import com.prontaentrega.models.enums.TipoOperacion;
import com.prontaentrega.models.Usuario;
import com.prontaentrega.models.InventarioUsuario;
import com.prontaentrega.persistence.repository.AnuncioVentaRepository;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.persistence.repository.OperacionRepository;
import com.prontaentrega.persistence.repository.InventarioUsuarioRepository;
import com.prontaentrega.persistence.repository.UsuarioRepository;
import com.prontaentrega.services.exceptions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
        int normalizedPage = Math.max(page, 1) - 1;
        int normalizedSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize);

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
        Usuario vendedor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con el id" + usuarioId.toString()));

        BolsaDeJugador bolsa = Optional.ofNullable(jugadorRepository.findById(jugadorId))
                .orElseThrow(() -> new JugadorNoEncontradoException("Jugador no encontrado con el id" + jugadorId.toString()));

        if (cantidad == null || cantidad <= 0) {
            throw new CantidadDeTokensException("La cantidad debe ser mayor a cero");
        }

        InventarioUsuario saldo = inventarioUsuarioRepository.findByUsuarioIdAndJugadorId(usuarioId, jugadorId)
                .orElse(new InventarioUsuario(vendedor, bolsa, 0, 0));

        int disponibles = saldo.getTokensDisponibles() == null ? 0 : saldo.getTokensDisponibles();

        if (cantidad > disponibles) {
            throw new CantidadDeTokensException("No posees suficientes tokens para vender");
        }

        AnuncioVenta anuncio = new AnuncioVenta(vendedor, bolsa, cantidad);
        anuncioVentaRepository.save(anuncio);

        // Estos setters son medio raretes, quizás se podría pasar a service?
        saldo.setTokensDisponibles(disponibles - cantidad);
        saldo.setTokensEnVenta((saldo.getTokensEnVenta() == null ? 0 : saldo.getTokensEnVenta()) + cantidad);
        saldo.setActualizadoEn(LocalDateTime.now());
        inventarioUsuarioRepository.save(saldo);

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
    public void comprar(UUID compradorId, UUID anuncioId) {
        comprar(compradorId, anuncioId, null);
    }

    @Transactional
    public void comprar(UUID compradorId, UUID anuncioId, Integer cantidadSolicitada) {
        AnuncioVenta anuncio = anuncioVentaRepository.findByIdForUpdate(anuncioId)
                .orElseThrow(() -> new AnuncioNoEncontradoException("El anuncio no existe"));

        if (anuncio.getEstado() != EstadoAnuncioVenta.PENDIENTE) {
            throw new AnuncioNoPendienteException("La venta ya no está disponible");
        }

        int cantidadCompra = cantidadSolicitada == null
                ? anuncio.getCantidadTokens()
                : cantidadSolicitada;
        if (cantidadCompra <= 0 || cantidadCompra > anuncio.getCantidadTokens()) {
            throw new CantidadDeTokensException(
                    "La cantidad debe ser mayor a cero y no superar los tokens disponibles en la oferta");
        }

        if (anuncio.getVendedor().getId().equals(compradorId)) {
            throw new VendedorComprandoTokensPropiosException("No puedes comprar tus propios tokens");
        }

        Usuario comprador = usuarioRepository.findById(compradorId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Comprador no encontrado con el id: " + compradorId.toString()));

        Usuario vendedor = usuarioRepository.findById(anuncio.getVendedor().getId())
                .orElseThrow(() -> new UsuarioNoEncontradoException("Vendedor no encontrado con el id: " + anuncio.getVendedor().getId().toString()));

        UUID jugadorId = anuncio.getBolsaDeJugador()
                .getEstadisticasDeportivas()
                .getJugador()
                .getId();

        BolsaDeJugador bolsaJugador = Optional.ofNullable(jugadorRepository.findById(jugadorId))
                .orElseThrow(() -> new JugadorNoEncontradoException(
                        "Jugador no encontrado con el id" + jugadorId));

        BigDecimal precioCompra = anuncio.getPrecioPorToken()
                .multiply(BigDecimal.valueOf(cantidadCompra));
        if (comprador.getCreditosDisponibles() == null || comprador.getCreditosDisponibles().compareTo(precioCompra) < 0) {
            throw new CreditosInsuficientesException("No tienes suficientes créditos");
        }

        InventarioUsuario tokenVendedor = inventarioUsuarioRepository.findByUsuarioIdAndJugadorId(
                vendedor.getId(), jugadorId)
                .orElse(new InventarioUsuario(vendedor, bolsaJugador, 0, 0));

        int enVenta = tokenVendedor.getTokensEnVenta() == null ? 0 : tokenVendedor.getTokensEnVenta();

        if (anuncio.getCantidadTokens() > enVenta) {
            throw new CantidadDeTokensException("La publicación ya no tiene tokens disponibles");
        }

        // Actualización de los usuarios
        comprador.restarCreditos(precioCompra);
        vendedor.sumarCreditos(precioCompra);
        usuarioRepository.save(comprador);
        usuarioRepository.save(vendedor);

        // Actualización de los tokens
        InventarioUsuario tokenComprador = inventarioUsuarioRepository.findByUsuarioIdAndJugadorId(compradorId, jugadorId)
                .orElse(new InventarioUsuario(comprador, bolsaJugador, 0, 0));
        tokenComprador.agregarTokens(cantidadCompra);
        tokenComprador.setActualizadoEn(LocalDateTime.now());
        inventarioUsuarioRepository.save(tokenComprador);

        tokenVendedor.quitarTokens(cantidadCompra);
        tokenVendedor.setActualizadoEn(LocalDateTime.now());
        inventarioUsuarioRepository.save(tokenVendedor);

        int cantidadRestante = anuncio.getCantidadTokens() - cantidadCompra;
        anuncio.setCantidadTokens(cantidadRestante);
        anuncio.setPrecioTotal(
                anuncio.getPrecioPorToken().multiply(BigDecimal.valueOf(cantidadRestante)));
        if (cantidadRestante == 0) {
            anuncio.setEstado(EstadoAnuncioVenta.COMPLETADA);
        }
        anuncio.setActualizadoEn(LocalDateTime.now());
        anuncioVentaRepository.save(anuncio);

        Jugador jugador = bolsaJugador.getEstadisticasDeportivas().getJugador();

        // Actualización de las operaciones
        // No es como data repetida? Quizás conviene solo al consultar
        // En cualquier caso se puede suprimir a una Operacion como "transaccion" y quitar el TipoOperaicon

        Operacion compra = new Operacion(comprador, vendedor, jugador,
                cantidadCompra, anuncio.getPrecioPorToken(), precioCompra,
                TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA);

        Operacion venta = new Operacion(vendedor, comprador, jugador,
                cantidadCompra, anuncio.getPrecioPorToken(), precioCompra,
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
    public void cancelarVenta(UUID vendedorId, UUID anuncioId) {
        AnuncioVenta anuncio = anuncioVentaRepository.findById(anuncioId)
                .orElseThrow(() -> new AnuncioNoEncontradoException("El anuncio no existe"));

        if (!anuncio.getVendedor().getId().equals(vendedorId)) {
            throw new SinAutorizacionExpcetion("No puedes cancelar una venta ajena");
        }

        if (anuncio.getEstado() != EstadoAnuncioVenta.PENDIENTE) {
            throw new AnuncioNoPendienteException("No se puede cancelar una venta completada");
        }

        Usuario vendedor = usuarioRepository.findById(vendedorId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(
                        "Usuario no encontrado con el id" + vendedorId));

        UUID jugadorId = anuncio.getBolsaDeJugador()
                .getEstadisticasDeportivas()
                .getJugador()
                .getId();

        BolsaDeJugador bolsaJugador = Optional.ofNullable(jugadorRepository.findById(jugadorId))
                .orElseThrow(() -> new JugadorNoEncontradoException(
                        "Jugador no encontrado con el id" + jugadorId));

        InventarioUsuario saldo = inventarioUsuarioRepository.findByUsuarioIdAndJugadorId(vendedorId, jugadorId)
                .orElse(new InventarioUsuario(vendedor, bolsaJugador, 0, 0));

        saldo.setTokensDisponibles(saldo.getTokensDisponibles() + anuncio.getCantidadTokens());
        saldo.setTokensEnVenta(saldo.getTokensEnVenta() - anuncio.getCantidadTokens());
        saldo.setActualizadoEn(LocalDateTime.now());
        inventarioUsuarioRepository.save(saldo);

        anuncio.setEstado(EstadoAnuncioVenta.CANCELADA);
        anuncio.setActualizadoEn(LocalDateTime.now());
        anuncioVentaRepository.save(anuncio);
    }

    /**
     * Metodo para obtener las transacciones de un usuario.
     * @param usuarioId El ID del usuario
     * @return Una lista de las transacciones del usuario
     */
    public List<Operacion> getTransaccionesById(UUID usuarioId) {
        return operacionRepository.findByUsuarioId(usuarioId);
    }

    public void comprarSuper(UUID compradorId, UUID jugadorId, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new CantidadDeTokensException("La cantidad debe ser mayor a cero");
        }

        Usuario comprador = usuarioRepository.findById(compradorId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Comprador no encontrado con el id: " + compradorId.toString()));

        BolsaDeJugador bolsaJugador = Optional.ofNullable(jugadorRepository.findById(jugadorId))
                .orElseThrow(() -> new JugadorNoEncontradoException(
                        "Jugador no encontrado con el id" + jugadorId));

        if (bolsaJugador.getTokensRestantes() < cantidad) {
            throw new CantidadDeTokensException("El jugador ya no tiene suficientes tokens disponibles");
        }

        BigDecimal precioFinal = bolsaJugador.getCotizacionActual().multiply(BigDecimal.valueOf(cantidad));

        if (comprador.getCreditosDisponibles() == null || comprador.getCreditosDisponibles().compareTo(precioFinal) < 0) {
            throw new CreditosInsuficientesException("No tienes suficientes créditos");
        }

        comprador.restarCreditos(precioFinal);
        usuarioRepository.save(comprador);

        InventarioUsuario tokenComprador = inventarioUsuarioRepository.findByUsuarioIdAndJugadorId(compradorId, jugadorId)
                .orElse(new InventarioUsuario(comprador, bolsaJugador, 0, 0));
        tokenComprador.agregarTokens(cantidad);
        tokenComprador.setActualizadoEn(LocalDateTime.now());
        inventarioUsuarioRepository.save(tokenComprador);


        bolsaJugador.setTokensRestantes(bolsaJugador.getTokensRestantes() - cantidad);
        jugadorRepository.save(bolsaJugador);

        Jugador jugador = bolsaJugador.getEstadisticasDeportivas().getJugador();

        // Mala operacion, porque el comprador y vendedor son el mismo id
        // habría que quitar lógica o ver que onda
        Operacion compra = new Operacion(comprador, comprador, jugador,
                cantidad, bolsaJugador.getCotizacionActual(), precioFinal,
                TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA);

        operacionRepository.save(compra);
    }

    public List<InventarioUsuario> getInventarioById(UUID usuarioId) {
        return inventarioUsuarioRepository.findAllByUsuarioId(usuarioId);
    }
}
