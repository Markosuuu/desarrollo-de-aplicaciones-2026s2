package com.prontaentrega.services;

import com.prontaentrega.models.jugadores.BolsaDeJugador;
import com.prontaentrega.models.mercado.InventarioUsuario;
import com.prontaentrega.models.subastas.Oferta;
import com.prontaentrega.models.mercado.Operacion;
import com.prontaentrega.models.subastas.Subasta;
import com.prontaentrega.models.auth.Usuario;
import com.prontaentrega.models.mercado.EstadoOperacion;
import com.prontaentrega.models.mercado.TipoOperacion;
import com.prontaentrega.models.exception.CantidadDeTokensException;
import com.prontaentrega.persistence.repository.InventarioUsuarioRepository;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.persistence.repository.OfertaRepository;
import com.prontaentrega.persistence.repository.OperacionRepository;
import com.prontaentrega.persistence.repository.SubastaRepository;
import com.prontaentrega.persistence.repository.UsuarioRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.prontaentrega.services.exceptions.auth.UsuarioNoEncontradoException;
import com.prontaentrega.services.exceptions.jugadores.JugadorNoEncontradoException;
import com.prontaentrega.services.exceptions.mercado.CreditosInsuficientesException;
import com.prontaentrega.services.exceptions.mercado.InventarioNoEncontradoException;
import com.prontaentrega.services.exceptions.mercado.SinAutorizacionExpcetion;
import com.prontaentrega.services.exceptions.subastas.IdempotencyKeyException;
import com.prontaentrega.models.exception.OfertaInvalidaException;
import com.prontaentrega.models.exception.SubastaNoActivaException;
import com.prontaentrega.services.exceptions.subastas.SubastaNoEncontradaException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubastaService {
    private final SubastaRepository subastaRepository;
    private final OfertaRepository ofertaRepository;
    private final UsuarioRepository usuarioRepository;
    private final JugadorRepository jugadorRepository;
    private final InventarioUsuarioRepository inventarioUsuarioRepository;
    private final OperacionRepository operacionRepository;

    public SubastaService(
            SubastaRepository subastaRepository,
            OfertaRepository ofertaRepository,
            UsuarioRepository usuarioRepository,
            JugadorRepository jugadorRepository,
            InventarioUsuarioRepository inventarioUsuarioRepository,
            OperacionRepository operacionRepository) {
        this.subastaRepository = subastaRepository;
        this.ofertaRepository = ofertaRepository;
        this.usuarioRepository = usuarioRepository;
        this.jugadorRepository = jugadorRepository;
        this.inventarioUsuarioRepository = inventarioUsuarioRepository;
        this.operacionRepository = operacionRepository;
    }

    /**
     * Lista de forma paginada todas las subastas que están activas
     *
     * @param page Nro de página
     * @param size Tamaño de la página
     * @return Todas las subastas paginadas
     */
    public Page<Subasta> listarSubastas(int page, int size) {
        int normalizedPage = Math.max(page, 1) - 1;
        int normalizedSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize);
        return subastaRepository.findByActiva(true, pageable);
    }

    /**
     * Obtiene una subasta específica
     * @param subastaId Id de una subasta
     * @return Una subasta
     * @throws SubastaNoEncontradaException Cuando no existe la subasta
     */
    public Subasta obtenerSubasta(UUID subastaId) {
        return subastaRepository.findById(subastaId)
                .orElseThrow(() -> new SubastaNoEncontradaException(subastaId));
    }

    /**
     * Obtiene la lista de ofertas que se hicieron en una subasta específica
     * @param subastaId Id de una subasta
     * @return Una lista de ofertas
     */
    public List<Oferta> obtenerOfertas(UUID subastaId) {
        return ofertaRepository.findBySubastaIdOrderByPujaDesc(subastaId);
    }

    /**
     * Metodo para crear subastas
     * @param usuarioId Id del usuario que quiere crear la subasta
     * @param jugadorId Id del jugador que se subasta
     * @param cantidad cantidad de tokens que se subastan de ese jugador
     * @param precioInicial Precio en el que arranca la subasta
     * @param vencimiento Fecha y hora en la cual se da por terminada la subasta
     * @throws UsuarioNoEncontradoException Cuando no existe el usuario
     * @throws JugadorNoEncontradoException Cuando no existe el jugador
     * @throws CantidadDeTokensException Cuando se quiere crear una subasta con cantidad negativa o nula
     * @throws OfertaInvalidaException Cuando se quiere crear una subasta con valores erroneos
     * @return Una subasta
     */
    @Transactional
    public Subasta crearSubasta(
            UUID usuarioId,
            UUID jugadorId,
            Integer cantidad,
            BigDecimal precioInicial,
            LocalDateTime vencimiento) {
        Usuario vendedor = buscarUsuario(usuarioId);

        BolsaDeJugador bolsa = buscarJugador(jugadorId);

        validarCreacionSubasta(cantidad, precioInicial, vencimiento, usuarioId, jugadorId);

        InventarioUsuario inventario = buscarInventario(usuarioId, jugadorId, vendedor, bolsa);

        inventario.ponerTokensEnVenta(cantidad);
        inventarioUsuarioRepository.save(inventario);

        Subasta subasta = new Subasta(vendedor, bolsa, cantidad, precioInicial, vencimiento);
        return subastaRepository.save(subasta);
    }

    /**
     * Crea una oferta en una subasta
     * @param usuarioId Id de un usuario
     * @param subastaId Id de una subasta
     * @param puja Cantidad que se oferta
     * @param idempotencyKey clave de idempotencia para evitar duplicados
     *
     * @throws SubastaNoEncontradaException Cuando no existe la subasta
     * @throws SubastaNoActivaException Cuando la subasta ya cerró
     * @throws OfertaInvalidaException Cuando la puja no supera el mínimo de la subasta o el propio dueño quiere ofertar o la cantidad no es positiva
     * @throws CreditosInsuficientesException Cuando el usuario no tiene créditos suficientes para pujar
     *
     * @return Una oferta
     */
    @Transactional
    public Oferta ofertar(UUID usuarioId, UUID subastaId, BigDecimal puja, String idempotencyKey) {

        Oferta ofertaPrevia = validarIdempotencia(usuarioId, subastaId, puja, idempotencyKey);
        if (ofertaPrevia != null) return ofertaPrevia;

        Subasta subasta = buscarSubasta(subastaId);

        Usuario comprador = buscarUsuario(usuarioId);

        validarOferta(usuarioId, puja, subasta, comprador);

        ofertaRepository.actualizarSuperior(subastaId);

        Oferta oferta = new Oferta(subasta, comprador, puja, idempotencyKey);
        ofertaRepository.save(oferta);

        subasta.actualizarPostorMasGrande(oferta);
        subastaRepository.save(subasta);

        return oferta;
    }

    /**
     * Cierra una subasta activa
     * @param usuarioId Id del propietario de la subasta
     * @param subastaId Id de la subasta
     * @param motivo Motivo por el que se cierra
     * @return Una subasta cerrada
     * @throws SubastaNoEncontradaException Cuando no existe la subasta
     * @throws SinAutorizacionExpcetion Cuando un usuario quiere cerrar una subasta que no le pertenece
     * @throws SubastaNoActivaException Cuando se quiere cerrar una subasta que no está activa
     */
    @Transactional
    public Subasta cerrarSubasta(UUID usuarioId, UUID subastaId, String motivo) {
        Subasta subasta = buscarSubasta(subastaId);

        validarCerrarSubasta(usuarioId, subasta);

        return resolverCierre(subasta, motivo);
    }

    @Transactional
    public void resolverSubastasVencidas() {
        List<Subasta> subastasVencidas = subastaRepository.getSubastasVencidasActivas();

        for (Subasta subasta : subastasVencidas) {
            resolverCierre(subasta, "POR_TIEMPO");
        }
    }

    @Transactional
    protected Subasta resolverCierre(Subasta subasta, String motivo) {
        ofertaRepository.findOfertaGanadora(subasta.getId())
                .ifPresentOrElse(
                        oferta -> {
                            oferta.setEsSuperior(true);
                            ofertaRepository.save(oferta);
                            liquidarVenta(subasta, oferta);
                        },
                        () -> devolverTokensAlPropietario(subasta)
                );

        subasta.cerrarSubasta(motivo);
        subastaRepository.save(subasta);
        return subasta;
    }

    private void devolverTokensAlPropietario(Subasta subasta) {
        Usuario vendedor = buscarUsuario(subasta.getPropietario().getId());

        UUID jugadorId = subasta.getBolsaDeJugador().getEstadisticasDeportivas().getJugador().getId();
        BolsaDeJugador bolsa = subasta.getBolsaDeJugador();
        InventarioUsuario inventario = buscarInventario(vendedor.getId(), jugadorId, vendedor, bolsa);

        inventario.cancelarVenta(subasta.getCantidadOfrecida());
        inventarioUsuarioRepository.save(inventario);
    }

    private void liquidarVenta(Subasta subasta, Oferta ofertaGanadora) {
        Usuario vendedor = buscarUsuario(subasta.getPropietario().getId());

        Usuario ganador = buscarUsuario(ofertaGanadora.getUsuarioId());

        ganador.restarCreditos(ofertaGanadora.getPuja());
        vendedor.sumarCreditos(ofertaGanadora.getPuja());
        usuarioRepository.save(ganador);
        usuarioRepository.save(vendedor);

        UUID jugadorId = subasta.getBolsaDeJugador().getEstadisticasDeportivas().getJugador().getId();
        BolsaDeJugador bolsa = subasta.getBolsaDeJugador();

        InventarioUsuario inventarioGanador = buscarInventario(ganador.getId(), jugadorId, ganador, bolsa);
        InventarioUsuario inventarioVendedor = buscarInventario(vendedor.getId(), jugadorId, vendedor, bolsa);

        inventarioGanador.agregarTokens(subasta.getCantidadOfrecida());
        inventarioVendedor.quitarTokens(subasta.getCantidadOfrecida());
        inventarioUsuarioRepository.save(inventarioGanador);
        inventarioUsuarioRepository.save(inventarioVendedor);

        subasta.asignarOfertaGanadora(ofertaGanadora);

        Operacion operacionCompra = new Operacion(
                ganador,
                vendedor,
                bolsa.getEstadisticasDeportivas().getJugador(),
                subasta.getCantidadOfrecida(),
                subasta.getBolsaDeJugador().getCotizacionActual(),
                ofertaGanadora.getPuja(),
                TipoOperacion.COMPRA,
                EstadoOperacion.COMPLETADA);

        Operacion operacionVenta = new Operacion(
                vendedor,
                ganador,
                bolsa.getEstadisticasDeportivas().getJugador(),
                subasta.getCantidadOfrecida(),
                subasta.getBolsaDeJugador().getCotizacionActual(),
                ofertaGanadora.getPuja(),
                TipoOperacion.VENTA,
                EstadoOperacion.COMPLETADA);

        operacionRepository.save(operacionCompra);
        operacionRepository.save(operacionVenta);
    }

    public Page<Subasta> listarSubastasPorNombre(int page, int size, String user, String jugador) {
        int normalizedPage = Math.max(page, 1) - 1;
        int normalizedSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize);
        return subastaRepository.findByNombre(user, jugador, pageable);
    }

    @NonNull
    private Usuario buscarUsuario(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));
    }

    @NonNull
    private InventarioUsuario buscarInventario(UUID usuarioId, UUID jugadorId, Usuario vendedor, BolsaDeJugador bolsa) {
        return inventarioUsuarioRepository.findByUsuarioIdAndJugadorId(usuarioId, jugadorId)
                .orElse(new InventarioUsuario(vendedor, bolsa, 0, 0));
    }

    @NonNull
    private BolsaDeJugador buscarJugador(UUID jugadorId) {
        return jugadorRepository.findById(jugadorId)
                .orElseThrow(() -> new JugadorNoEncontradoException("Jugador no encontrado con el id: " + jugadorId));
    }

    @NonNull
    private Subasta buscarSubasta(UUID subastaId) {
        return subastaRepository.findByIdForUpdate(subastaId)
                .orElseThrow(() -> new SubastaNoEncontradaException(subastaId));
    }

    private Oferta validarIdempotencia(UUID usuarioId, UUID subastaId, BigDecimal puja, String idempotencyKey) {
        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 255) {
            throw new OfertaInvalidaException("La clave de idempotencia es obligatoria y debe tener hasta 255 caracteres");
        }

        Optional<Oferta> ofertaPrevia = ofertaRepository.findByUsuarioIdAndIdempotencyKey(usuarioId, idempotencyKey);
        if (ofertaPrevia.isPresent()) {
            Oferta oferta = ofertaPrevia.get();

            if (!oferta.getSubastaId().equals(subastaId) || oferta.getPuja().compareTo(puja) != 0) {
                throw new IdempotencyKeyException(
                        "La clave de idempotencia ya fue utilizada con otros datos");
            }
            return oferta;
        }
        return null;
    }

    private void validarCreacionSubasta(Integer cantidad,
                                        BigDecimal precioInicial,
                                        LocalDateTime vencimiento,
                                        UUID userId, UUID jugadorID
    ) {
        if (cantidad == null || cantidad < 1) {
            throw new CantidadDeTokensException("La cantidad de tokens debe ser mayor o igual a 1");
        }

        if (precioInicial == null || precioInicial.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OfertaInvalidaException("El precio inicial debe ser mayor a cero");
        }

        if (vencimiento == null || !vencimiento.isAfter(LocalDateTime.now(ZoneId.systemDefault()))) {
            throw new OfertaInvalidaException("La fecha de cierre debe ser futura");
        }

        if (vencimiento.getSecond() != 0 || vencimiento.getNano() != 0) {
            throw new OfertaInvalidaException("La fecha de cierre solo admite precisión de minutos");
        }

        Optional<InventarioUsuario> inventory = inventarioUsuarioRepository.findByUsuarioIdAndJugadorId(userId, jugadorID);
        if (inventory.isEmpty()) {
            throw new InventarioNoEncontradoException("No puedes crear una subasta, no eres propietario de tokens del jugador.");
        }
    }

    private static void validarCerrarSubasta(UUID usuarioId, Subasta subasta) {
        if (!subasta.getPropietario().getId().equals(usuarioId)) {
            throw new SinAutorizacionExpcetion("No puedes cerrar una subasta ajena");
        }

        if (Boolean.FALSE.equals(subasta.getActiva())) {
            throw new SubastaNoActivaException("La subasta ya está cerrada");
        }
    }

    private static void validarOferta(UUID usuarioId, BigDecimal puja, Subasta subasta, Usuario comprador) {
        if (Boolean.FALSE.equals(subasta.getActiva())) {
            throw new SubastaNoActivaException("La subasta ya no está activa");
        }

        if (!subasta.getVencimiento().isAfter(LocalDateTime.now(ZoneId.systemDefault()))) {
            throw new SubastaNoActivaException("La subasta ya venció");
        }

        if (subasta.getPropietario().getId().equals(usuarioId)) {
            throw new OfertaInvalidaException("El propietario no puede ofertar en su propia subasta");
        }

        if (puja == null || puja.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OfertaInvalidaException("La puja debe ser mayor a cero");
        }

        if (puja.compareTo(subasta.obtenerOfertaMinima()) <= 0) {
            throw new OfertaInvalidaException("La puja debe superar la oferta actual");
        }

        if (comprador.getCreditosDisponibles() == null || comprador.getCreditosDisponibles().compareTo(puja) < 0) {
            throw new CreditosInsuficientesException("No tienes suficientes créditos para ofertar");
        }
    }
}
