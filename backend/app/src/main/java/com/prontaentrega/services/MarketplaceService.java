package com.prontaentrega.services;

import com.prontaentrega.models.AnuncioVenta;
import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.EstadoAnuncioVenta;
import com.prontaentrega.models.EstadoOperacion;
import com.prontaentrega.models.Operacion;
import com.prontaentrega.models.TipoOperacion;
import com.prontaentrega.models.Usuario;
import com.prontaentrega.models.UsuarioJugadorToken;
import com.prontaentrega.persistence.repository.AnuncioVentaRepository;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.persistence.repository.OperacionRepository;
import com.prontaentrega.persistence.repository.UsuarioJugadorTokenRepository;
import com.prontaentrega.persistence.repository.UsuarioRepository;
import com.prontaentrega.services.dto.MarketplaceListingResponse;
import com.prontaentrega.services.dto.OperacionResponse;
import com.prontaentrega.services.exceptions.ValidationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
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
public class MarketplaceService {
    private final AnuncioVentaRepository anuncioVentaRepository;
    private final UsuarioJugadorTokenRepository usuarioJugadorTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final JugadorRepository jugadorRepository;
    private final OperacionRepository operacionRepository;

    public MarketplaceService(AnuncioVentaRepository anuncioVentaRepository,
                             UsuarioJugadorTokenRepository usuarioJugadorTokenRepository,
                             UsuarioRepository usuarioRepository,
                             JugadorRepository jugadorRepository,
                             OperacionRepository operacionRepository) {
        this.anuncioVentaRepository = anuncioVentaRepository;
        this.usuarioJugadorTokenRepository = usuarioJugadorTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.jugadorRepository = jugadorRepository;
        this.operacionRepository = operacionRepository;
    }

    /**
     * Metodo para obtener los anuncios de venta activos en el marketplace.
     * @param page Nro de página
     * @param size Cantidad de anuncios por página
     * @return Lista de anuncios de venta
     */
    // TODO: Mejorar nombre
    public List<MarketplaceListingResponse> getListings(int page, int size) {
        int normalizedPage = Math.max(page, 1) - 1;
        int normalizedSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize);
        Page<AnuncioVenta> pageResult = anuncioVentaRepository.findByEstado(EstadoAnuncioVenta.PENDIENTE, pageable);

        // TODO: Esto debería ir a controller
        List<MarketplaceListingResponse> result = new ArrayList<>();
        for (AnuncioVenta anuncio : pageResult.getContent()) {
            result.add(toListingResponse(anuncio));
        }
        return result;
    }

    /**
     * Metodo para vender tokens en el marketplace.
     * @param usuarioId El ID del usuario que está vendiendo los tokens
     * @param jugadorId El ID del jugador cuyos tokens se están vendiendo
     * @param cantidad La cantidad de tokens a vender
     * @return El anuncio de venta creado
     */
    public MarketplaceListingResponse sellTokens(UUID usuarioId, UUID jugadorId, Integer cantidad) {
        // TODO: Cambiar a expcecion personalizada
        Usuario vendedor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ValidationException("USUARIO_NO_ENCONTRADO", "Usuario no encontrado"));

        // TODO: Cambiar a expcecion personalizada
        BolsaDeJugador bolsa = Optional.ofNullable(jugadorRepository.findById(jugadorId))
                .orElseThrow(() -> new ValidationException("JUGADOR_NO_ENCONTRADO", "Jugador no encontrado"));

        // TODO: Cambiar a expcecion personalizada
        if (cantidad == null || cantidad <= 0) {
            throw new ValidationException("CANTIDAD_INVALIDA", "La cantidad debe ser mayor a cero");
        }

        UsuarioJugadorToken saldo = usuarioJugadorTokenRepository.findByUsuarioIdAndJugadorId(usuarioId, jugadorId)
                .orElse(new UsuarioJugadorToken(usuarioId, jugadorId, 0, 0));

        int disponibles = saldo.getTokensDisponibles() == null ? 0 : saldo.getTokensDisponibles();

        if (cantidad > disponibles) {
            // TODO: Cambiar a expcecion personalizada
            throw new ValidationException("TOKENS_INSUFICIENTES", "No posees suficientes tokens para vender");
        }

        // Quizás cambiar a excepción por si es nulo
        BigDecimal precioPorToken = bolsa.getCotizacionActual() == null ? BigDecimal.ONE : bolsa.getCotizacionActual();
        BigDecimal precioTotal = precioPorToken.multiply(BigDecimal.valueOf(cantidad));


        AnuncioVenta anuncio = new AnuncioVenta(usuarioId, jugadorId, cantidad, precioPorToken, precioTotal);
        anuncioVentaRepository.save(anuncio);

        // Estos setters son medio raretes
        saldo.setTokensDisponibles(disponibles - cantidad);
        saldo.setTokensEnVenta((saldo.getTokensEnVenta() == null ? 0 : saldo.getTokensEnVenta()) + cantidad);
        saldo.setActualizadoEn(java.time.LocalDateTime.now());
        usuarioJugadorTokenRepository.save(saldo);

        // Metodo que rompe todo el encapsulamiento
        return toListingResponse(anuncio);
    }

    /**
     * Metodo para comprar tokens en el marketplace.
     * @param compradorId El ID del usuario que está comprando los tokens
     * @param anuncioId El ID del anuncio de venta que se está comprando
     */
    @Transactional
    public void buyTokens(UUID compradorId, UUID anuncioId) {
        // TODO: Cambiar a expcecion personalizada
        AnuncioVenta anuncio = anuncioVentaRepository.findByIdForUpdate(anuncioId)
                .orElseThrow(() -> new ValidationException("ANUNCIO_NO_ENCONTRADO", "El anuncio no existe"));

        if (anuncio.getEstado() != EstadoAnuncioVenta.PENDIENTE) {
            // TODO: Cambiar a expcecion personalizada
            throw new ValidationException("ANUNCIO_NO_DISPONIBLE", "La venta ya no está disponible");
        }

        if (anuncio.getVendedorId().equals(compradorId)) {
            // TODO: Cambiar a expcecion personalizada
            throw new ValidationException("COMPRA_PROPIA", "No puedes comprar tus propios tokens");
        }

        // TODO: Cambiar a expcecion personalizada
        Usuario comprador = usuarioRepository.findById(compradorId)
                .orElseThrow(() -> new ValidationException("USUARIO_NO_ENCONTRADO", "Usuario no encontrado"));

        // TODO: Cambiar a expcecion personalizada
        Usuario vendedor = usuarioRepository.findById(anuncio.getVendedorId())
                .orElseThrow(() -> new ValidationException("VENDEDOR_NO_ENCONTRADO", "Vendedor no encontrado"));

        // TODO: Cambiar a expcecion personalizada
        if (comprador.getCreditosDisponibles() == null || comprador.getCreditosDisponibles().compareTo(anuncio.getPrecioTotal()) < 0) {
            throw new ValidationException("CREDITOS_INSUFICIENTES", "No tienes suficientes créditos");
        }

        // Ver la vuelta de esto
        UsuarioJugadorToken tokenVendedor = usuarioJugadorTokenRepository.findByUsuarioIdAndJugadorId(
                anuncio.getVendedorId(), anuncio.getJugadorId())
                .orElse(new UsuarioJugadorToken(anuncio.getVendedorId(), anuncio.getJugadorId(), 0, 0));

        int enVenta = tokenVendedor.getTokensEnVenta() == null ? 0 : tokenVendedor.getTokensEnVenta();

        if (anuncio.getCantidadTokens() > enVenta) {
            // TODO: Cambiar a expcecion personalizada
            throw new ValidationException("ANUNCIO_NO_DISPONIBLE", "La publicación ya no tiene tokens disponibles");
        }

        // Actualización de los usuarios
        comprador.setCreditosDisponibles(comprador.getCreditosDisponibles().subtract(anuncio.getPrecioTotal()));
        vendedor.setCreditosDisponibles(vendedor.getCreditosDisponibles().add(anuncio.getPrecioTotal()));
        usuarioRepository.save(comprador);
        usuarioRepository.save(vendedor);

        // Actualización de los tokens
        UsuarioJugadorToken tokenComprador = usuarioJugadorTokenRepository.findByUsuarioIdAndJugadorId(compradorId, anuncio.getJugadorId())
                .orElse(new UsuarioJugadorToken(compradorId, anuncio.getJugadorId(), 0, 0));
        tokenComprador.setTokensDisponibles((tokenComprador.getTokensDisponibles() == null ? 0 : tokenComprador.getTokensDisponibles()) + anuncio.getCantidadTokens());
        tokenComprador.setActualizadoEn(java.time.LocalDateTime.now());
        usuarioJugadorTokenRepository.save(tokenComprador);

        tokenVendedor.setTokensEnVenta(enVenta - anuncio.getCantidadTokens());
        tokenVendedor.setActualizadoEn(java.time.LocalDateTime.now());
        usuarioJugadorTokenRepository.save(tokenVendedor);

        // Actualización del anuncio
        anuncio.setEstado(EstadoAnuncioVenta.COMPLETADA);
        anuncio.setActualizadoEn(java.time.LocalDateTime.now());
        anuncioVentaRepository.save(anuncio);

        // TODO: Cambiar a expcecion personalizada
        BolsaDeJugador bolsaJugador = Optional.ofNullable(jugadorRepository.findById(anuncio.getJugadorId()))
                .orElseThrow(() -> new ValidationException("JUGADOR_NO_ENCONTRADO", "Jugador no encontrado"));

        String nombreJugador = bolsaJugador.getEstadisticasDeportivas() == null
                ? "Jugador"
                : bolsaJugador.getEstadisticasDeportivas().getJugador().getNombre();

        // Actualización de las operaciones
        // No es como data repetida?
        Operacion compra = new Operacion(compradorId, anuncio.getVendedorId(), anuncio.getJugadorId(),
                anuncio.getCantidadTokens(), anuncio.getPrecioPorToken(), anuncio.getPrecioTotal(),
                TipoOperacion.COMPRA, EstadoOperacion.COMPLETADA, nombreJugador);

        Operacion venta = new Operacion(anuncio.getVendedorId(), compradorId, anuncio.getJugadorId(),
                anuncio.getCantidadTokens(), anuncio.getPrecioPorToken(), anuncio.getPrecioTotal(),
                TipoOperacion.VENTA, EstadoOperacion.COMPLETADA, nombreJugador);

        operacionRepository.save(compra);
        operacionRepository.save(venta);
    }

    /**
     * Metodo para cancelar una venta en el marketplace.
     * @param vendedorId El ID del usuario que está cancelando la venta
     * @param anuncioId El ID del anuncio de venta que se está cancelando
     *                  TODO: Falta endpoint en capa controller
     */
    public void cancelSale(UUID vendedorId, UUID anuncioId) {
        AnuncioVenta anuncio = anuncioVentaRepository.findById(anuncioId)
                .orElseThrow(() -> new ValidationException("ANUNCIO_NO_ENCONTRADO", "El anuncio no existe"));

        if (!anuncio.getVendedorId().equals(vendedorId)) {
            throw new ValidationException("NO_AUTORIZADO", "No puedes cancelar una venta ajena");
        }
        if (anuncio.getEstado() != EstadoAnuncioVenta.PENDIENTE) {
            throw new ValidationException("ANUNCIO_NO_CANCELABLE", "No se puede cancelar una venta completada");
        }

        UsuarioJugadorToken saldo = usuarioJugadorTokenRepository.findByUsuarioIdAndJugadorId(vendedorId, anuncio.getJugadorId())
                .orElse(new UsuarioJugadorToken(vendedorId, anuncio.getJugadorId(), 0, 0));
        saldo.setTokensDisponibles((saldo.getTokensDisponibles() == null ? 0 : saldo.getTokensDisponibles()) + anuncio.getCantidadTokens());
        saldo.setTokensEnVenta((saldo.getTokensEnVenta() == null ? 0 : saldo.getTokensEnVenta()) - anuncio.getCantidadTokens());
        saldo.setActualizadoEn(java.time.LocalDateTime.now());
        usuarioJugadorTokenRepository.save(saldo);

        anuncio.setEstado(EstadoAnuncioVenta.CANCELADA);
        anuncio.setActualizadoEn(java.time.LocalDateTime.now());
        anuncioVentaRepository.save(anuncio);
    }

    /**
     * Metodo para obtener las transacciones de un usuario.
     * @param usuarioId El ID del usuario
     * @return Una lista de las transacciones del usuario
     */
    public List<OperacionResponse> getUserTransactions(UUID usuarioId) {
        List<Operacion> operaciones = operacionRepository.findByUsuarioId(usuarioId);

        // TODO: Esto debería ir a controller
        List<OperacionResponse> result = new ArrayList<>();

        for (Operacion operacion : operaciones) {
            Usuario usuario = usuarioRepository.findById(operacion.getUsuarioId()).orElse(new Usuario());
            Usuario contraparte = usuarioRepository.findById(operacion.getContraparteId()).orElse(new Usuario());
            result.add(OperacionResponse.from(operacion, usuario.getNombre(), contraparte.getNombre()));
        }

        result.sort(Comparator.comparing(OperacionResponse::creadaEn).reversed());

        return result;
    }

    // TODO: Hay que matar este método
    private MarketplaceListingResponse toListingResponse(AnuncioVenta anuncio) {
        Usuario vendedor = usuarioRepository.findById(anuncio.getVendedorId())
                .orElse(new Usuario());
        BolsaDeJugador bolsa = Optional.ofNullable(jugadorRepository.findById(anuncio.getJugadorId()))
                .orElse(new BolsaDeJugador());
        String nombreJugador = bolsa.getEstadisticasDeportivas() == null
                ? "Jugador"
                : bolsa.getEstadisticasDeportivas().getJugador().getNombre();
        return MarketplaceListingResponse.from(anuncio, vendedor.getNombre(), nombreJugador);
    }
}
