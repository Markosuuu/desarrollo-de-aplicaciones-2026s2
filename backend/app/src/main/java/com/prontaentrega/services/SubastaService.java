package com.prontaentrega.services;

import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.InventarioUsuario;
import com.prontaentrega.models.Oferta;
import com.prontaentrega.models.Operacion;
import com.prontaentrega.models.Subasta;
import com.prontaentrega.models.Usuario;
import com.prontaentrega.models.enums.EstadoOperacion;
import com.prontaentrega.models.enums.TipoOperacion;
import com.prontaentrega.models.exception.CantidadDeTokensException;
import com.prontaentrega.persistence.repository.InventarioUsuarioRepository;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.persistence.repository.OfertaRepository;
import com.prontaentrega.persistence.repository.OperacionRepository;
import com.prontaentrega.persistence.repository.SubastaRepository;
import com.prontaentrega.persistence.repository.UsuarioRepository;
import com.prontaentrega.services.exceptions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
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
        Usuario vendedor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));

        BolsaDeJugador bolsa = Optional.ofNullable(jugadorRepository.findById(jugadorId))
                .orElseThrow(() -> new JugadorNoEncontradoException("Jugador no encontrado con el id: " + jugadorId));

        validarCreacionSubasta(cantidad, precioInicial, vencimiento, usuarioId, jugadorId);

        InventarioUsuario inventario = inventarioUsuarioRepository.findByUsuarioIdAndJugadorId(usuarioId, jugadorId)
                .orElse(new InventarioUsuario(vendedor, bolsa, 0, 0));

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

        Subasta subasta = subastaRepository.findByIdForUpdate(subastaId)
                .orElseThrow(() -> new SubastaNoEncontradaException(subastaId));

        validarOferta(usuarioId, puja, subasta);

        BigDecimal ofertaMinima = subasta.obtenerOfertaMinima();

        if (puja.compareTo(ofertaMinima) <= 0) {
            throw new OfertaInvalidaException("La puja debe superar la oferta actual");
        }

        Usuario comprador = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException(usuarioId));

        if (comprador.getCreditosDisponibles() == null || comprador.getCreditosDisponibles().compareTo(puja) < 0) {
            throw new CreditosInsuficientesException("No tienes suficientes créditos para ofertar");
        }

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
        Subasta subasta = subastaRepository.findByIdForUpdate(subastaId)
                .orElseThrow(() -> new SubastaNoEncontradaException(subastaId));

        validarCerrarSubasta(usuarioId, subasta);

        return resolverCierre(subasta, motivo);
    }

    @Transactional
    public void resolverSubastasVencidas() {
        // TODO: Ver que onda esto
        LocalDateTime ahora = LocalDateTime.now(ZoneId.systemDefault());

        List<UUID> idsVencidas = subastaRepository.findIdsVencidas(ahora);

        for (UUID id : idsVencidas) {
            subastaRepository.findByIdForUpdate(id).ifPresent(subasta -> {
                if (Boolean.TRUE.equals(subasta.getActiva())
                        && !subasta.getVencimiento().isAfter(ahora)) {
                    resolverCierre(subasta, "POR_TIEMPO");
                }
            });
        }
    }

    private Subasta resolverCierre(Subasta subasta, String motivo) {
        List<Oferta> ofertas = ofertaRepository.findBySubastaIdOrderByPujaDesc(subasta.getId()).stream()
                .sorted(Comparator.comparing(Oferta::getPuja).reversed())
                .toList();

        Oferta ofertaGanadora = null;
        for (Oferta oferta : ofertas) {
            Usuario postor = usuarioRepository.findById(oferta.getUsuarioId())
                    .orElseThrow(() -> new UsuarioNoEncontradoException(oferta.getUsuarioId()));
            if (postor.getCreditosDisponibles() != null && postor.getCreditosDisponibles().compareTo(oferta.getPuja()) >= 0) {
                ofertaGanadora = oferta;
                break;
            }
        }

        if (ofertaGanadora == null) {
            devolverTokensAlPropietario(subasta);
        } else {
            ofertaGanadora.setEsSuperior(true);
            ofertaRepository.save(ofertaGanadora);
            liquidarVenta(subasta, ofertaGanadora);
        }

        subasta.setMotivoCierre(motivo);
        subasta.setActiva(false);
        subasta.setActualizadoEn(LocalDateTime.now(ZoneId.systemDefault()));
        subastaRepository.save(subasta);
        return subasta;
    }

    private void devolverTokensAlPropietario(Subasta subasta) {
        Usuario vendedor = usuarioRepository.findById(subasta.getPropietarioId())
                .orElseThrow(() -> new UsuarioNoEncontradoException(subasta.getPropietarioId()));

        UUID jugadorId = subasta.getJugadorId();
        BolsaDeJugador bolsa = subasta.getBolsaDeJugador();
        InventarioUsuario inventario = inventarioUsuarioRepository.findByUsuarioIdAndJugadorId(vendedor.getId(), jugadorId)
                .orElse(new InventarioUsuario(vendedor, bolsa, 0, 0));

        int actuales = inventario.getTokensDisponibles() == null ? 0 : inventario.getTokensDisponibles();
        int enVenta = inventario.getTokensEnVenta() == null ? 0 : inventario.getTokensEnVenta();
        inventario.setTokensDisponibles(actuales + subasta.getCantidadOfrecida());
        inventario.setTokensEnVenta(Math.max(0, enVenta - subasta.getCantidadOfrecida()));
        inventario.setActualizadoEn(LocalDateTime.now(ZoneId.systemDefault()));
        inventarioUsuarioRepository.save(inventario);
    }

    private void liquidarVenta(Subasta subasta, Oferta ofertaGanadora) {
        Usuario vendedor = usuarioRepository.findById(subasta.getPropietarioId())
                .orElseThrow(() -> new UsuarioNoEncontradoException(subasta.getPropietarioId()));

        Usuario ganador = usuarioRepository.findById(ofertaGanadora.getUsuarioId())
                .orElseThrow(() -> new UsuarioNoEncontradoException(ofertaGanadora.getUsuarioId()));

        BigDecimal totalPuja = ofertaGanadora.getPuja();
        BigDecimal precioPorToken = totalPuja.divide(
            BigDecimal.valueOf(subasta.getCantidadOfrecida()),
            2,
            java.math.RoundingMode.HALF_UP);

        ganador.restarCreditos(ofertaGanadora.getPuja());
        vendedor.sumarCreditos(ofertaGanadora.getPuja());
        usuarioRepository.save(ganador);
        usuarioRepository.save(vendedor);

        UUID jugadorId = subasta.getJugadorId();
        BolsaDeJugador bolsa = subasta.getBolsaDeJugador();

        InventarioUsuario inventarioGanador = inventarioUsuarioRepository.findByUsuarioIdAndJugadorId(ganador.getId(), jugadorId)
                .orElse(new InventarioUsuario(ganador, bolsa, 0, 0));
        InventarioUsuario inventarioVendedor = inventarioUsuarioRepository.findByUsuarioIdAndJugadorId(vendedor.getId(), jugadorId)
                .orElse(new InventarioUsuario(vendedor, bolsa, 0, 0));

        inventarioGanador.setTokensDisponibles((inventarioGanador.getTokensDisponibles() == null ? 0 : inventarioGanador.getTokensDisponibles()) + subasta.getCantidadOfrecida());
        inventarioVendedor.setTokensEnVenta(Math.max(0, (inventarioVendedor.getTokensEnVenta() == null ? 0 : inventarioVendedor.getTokensEnVenta()) - subasta.getCantidadOfrecida()));
        inventarioGanador.setActualizadoEn(LocalDateTime.now(ZoneId.systemDefault()));
        inventarioVendedor.setActualizadoEn(LocalDateTime.now(ZoneId.systemDefault()));
        inventarioUsuarioRepository.save(inventarioGanador);
        inventarioUsuarioRepository.save(inventarioVendedor);

        subasta.setOfertaActual(ofertaGanadora);

        Operacion operacionCompra = new Operacion(
                ganador,
                vendedor,
                bolsa.getEstadisticasDeportivas().getJugador(),
                subasta.getCantidadOfrecida(),
                precioPorToken,
                totalPuja,
                TipoOperacion.COMPRA,
                EstadoOperacion.COMPLETADA);

        Operacion operacionVenta = new Operacion(
                vendedor,
                ganador,
                bolsa.getEstadisticasDeportivas().getJugador(),
                subasta.getCantidadOfrecida(),
                precioPorToken,
                totalPuja,
                TipoOperacion.VENTA,
                EstadoOperacion.COMPLETADA);

        operacionRepository.save(operacionCompra);
        operacionRepository.save(operacionVenta);
    }

    private static void validarOferta(UUID usuarioId, BigDecimal puja, Subasta subasta) {
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
}
