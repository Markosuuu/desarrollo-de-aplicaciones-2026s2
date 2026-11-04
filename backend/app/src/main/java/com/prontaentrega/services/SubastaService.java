package com.prontaentrega.services;

import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.InventarioUsuario;
import com.prontaentrega.models.Oferta;
import com.prontaentrega.models.Operacion;
import com.prontaentrega.models.Subasta;
import com.prontaentrega.models.Usuario;
import com.prontaentrega.models.enums.EstadoOperacion;
import com.prontaentrega.models.enums.EstadoSubasta;
import com.prontaentrega.models.enums.TipoOperacion;
import com.prontaentrega.persistence.repository.InventarioUsuarioRepository;
import com.prontaentrega.persistence.repository.JugadorRepository;
import com.prontaentrega.persistence.repository.OfertaRepository;
import com.prontaentrega.persistence.repository.OperacionRepository;
import com.prontaentrega.persistence.repository.SubastaRepository;
import com.prontaentrega.persistence.repository.UsuarioRepository;
import com.prontaentrega.services.exceptions.CantidadDeTokensException;
import com.prontaentrega.services.exceptions.CreditosInsuficientesException;
import com.prontaentrega.services.exceptions.OfertaInvalidaException;
import com.prontaentrega.services.exceptions.SinAutorizacionExpcetion;
import com.prontaentrega.services.exceptions.SubastaNoActivaException;
import com.prontaentrega.services.exceptions.SubastaNoEncontradaException;
import com.prontaentrega.services.exceptions.UsuarioNoEncontradoException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public Page<Subasta> listarSubastas(int page, int size) {
        int normalizedPage = Math.max(page, 1) - 1;
        int normalizedSize = Math.max(size, 1);
        Pageable pageable = PageRequest.of(normalizedPage, normalizedSize);
        return subastaRepository.findByEstado(EstadoSubasta.ACTIVA, pageable);
    }

    public Subasta obtenerSubasta(UUID subastaId) {
        return subastaRepository.findById(subastaId)
                .orElseThrow(() -> new SubastaNoEncontradaException("Subasta no encontrada: " + subastaId));
    }

    public List<Oferta> obtenerOfertas(UUID subastaId) {
        return ofertaRepository.findBySubastaIdOrderByPujaDesc(subastaId);
    }

    @Transactional
    public Subasta crearSubasta(
            UUID usuarioId,
            UUID jugadorId,
            Integer cantidad,
            BigDecimal precioInicial,
            LocalDateTime vencimiento) {
        Usuario vendedor = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con el id: " + usuarioId));

        BolsaDeJugador bolsa = Optional.ofNullable(jugadorRepository.findById(jugadorId))
                .orElseThrow(() -> new RuntimeException("Jugador no encontrado con el id: " + jugadorId));

        if (cantidad == null || cantidad < 1) {
            throw new CantidadDeTokensException("La cantidad de tokens debe ser mayor o igual a 1");
        }

        if (precioInicial == null || precioInicial.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OfertaInvalidaException("El precio inicial debe ser mayor a cero");
        }

        if (vencimiento == null || !vencimiento.isAfter(LocalDateTime.now())) {
            throw new OfertaInvalidaException("La fecha de cierre debe ser futura");
        }
        if (vencimiento.getSecond() != 0 || vencimiento.getNano() != 0) {
            throw new OfertaInvalidaException("La fecha de cierre solo admite precisión de minutos");
        }

        InventarioUsuario inventario = inventarioUsuarioRepository.findByUsuarioIdAndJugadorId(usuarioId, jugadorId)
                .orElse(new InventarioUsuario(vendedor, bolsa, 0, 0));

        int disponibles = inventario.getTokensDisponibles() == null ? 0 : inventario.getTokensDisponibles();
        if (disponibles < cantidad) {
            throw new CantidadDeTokensException("No posees suficientes tokens para subastar");
        }

        inventario.setTokensDisponibles(disponibles - cantidad);
        inventario.setTokensEnVenta((inventario.getTokensEnVenta() == null ? 0 : inventario.getTokensEnVenta()) + cantidad);
        inventario.setActualizadoEn(LocalDateTime.now());
        inventarioUsuarioRepository.save(inventario);

        Subasta subasta = new Subasta(vendedor, bolsa, cantidad, precioInicial, vencimiento);
        return subastaRepository.save(subasta);
    }

    @Transactional
    public Oferta ofertar(UUID usuarioId, UUID subastaId, BigDecimal puja, String idempotencyKey) {
        Subasta subasta = subastaRepository.findByIdForUpdate(subastaId)
                .orElseThrow(() -> new SubastaNoEncontradaException("Subasta no encontrada: " + subastaId));

        if (subasta.getEstado() != EstadoSubasta.ACTIVA) {
            throw new SubastaNoActivaException("La subasta ya no está activa");
        }

        if (!subasta.getVencimiento().isAfter(LocalDateTime.now())) {
            throw new SubastaNoActivaException("La subasta ya venció");
        }

        if (subasta.getPropietario().getId().equals(usuarioId)) {
            throw new OfertaInvalidaException("El propietario no puede ofertar en su propia subasta");
        }

        if (puja == null || puja.compareTo(BigDecimal.ZERO) <= 0) {
            throw new OfertaInvalidaException("La puja debe ser mayor a cero");
        }

        Optional<Oferta> ofertaExistente = ofertaRepository.findByUsuarioIdAndSubastaId(usuarioId, subastaId);
        if (ofertaExistente.isPresent()) {
            return ofertaExistente.get();
        }

        BigDecimal ofertaMinima = subasta.getOfertaActual() == null
            ? subasta.getPrecioInicial()
            : subasta.getOfertaActual();
        if (puja.compareTo(ofertaMinima) <= 0) {
            throw new OfertaInvalidaException("La puja debe superar la oferta actual");
        }

        Usuario comprador = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con el id: " + usuarioId));

        if (comprador.getCreditosDisponibles() == null || comprador.getCreditosDisponibles().compareTo(puja) < 0) {
            throw new CreditosInsuficientesException("No tienes suficientes créditos para ofertar");
        }

        Oferta oferta = new Oferta(subasta, comprador, puja);
        ofertaRepository.findBySubastaId(subastaId).stream()
                .filter(anterior -> anterior.getEstado() == com.prontaentrega.models.enums.EstadoOferta.VIGENTE)
                .forEach(anterior -> {
                    anterior.setEstado(com.prontaentrega.models.enums.EstadoOferta.SUPERADA);
                    ofertaRepository.save(anterior);
                });
        ofertaRepository.save(oferta);

        subasta.setOfertaActual(puja);
        subasta.setOfertanteActual(comprador);
        subasta.setActualizadoEn(LocalDateTime.now());
        subastaRepository.save(subasta);

        return oferta;
    }

    @Transactional
    public Subasta cerrarSubasta(UUID usuarioId, UUID subastaId) {
        return cerrarSubasta(usuarioId, subastaId, "POR_PROPIETARIO");
    }

    @Transactional
    public Subasta cerrarSubasta(UUID usuarioId, UUID subastaId, String motivo) {
        Subasta subasta = subastaRepository.findByIdForUpdate(subastaId)
                .orElseThrow(() -> new SubastaNoEncontradaException("Subasta no encontrada: " + subastaId));

        if (!subasta.getPropietario().getId().equals(usuarioId)) {
            throw new SinAutorizacionExpcetion("No puedes cerrar una subasta ajena");
        }

        if (subasta.getEstado() != EstadoSubasta.ACTIVA) {
            throw new SubastaNoActivaException("La subasta ya está cerrada");
        }

        return resolverCierre(subasta, motivo);
    }

    @Transactional
    public void resolverSubastasVencidas() {
        for (Subasta vencida : subastaRepository.findVencidas(LocalDateTime.now())) {
            subastaRepository.findByIdForUpdate(vencida.getId()).ifPresent(subasta -> {
                if (subasta.getEstado() == EstadoSubasta.ACTIVA
                        && !subasta.getVencimiento().isAfter(LocalDateTime.now())) {
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
                    .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con el id: " + oferta.getUsuarioId()));
            if (postor.getCreditosDisponibles() != null && postor.getCreditosDisponibles().compareTo(oferta.getPuja()) >= 0) {
                ofertaGanadora = oferta;
                break;
            }
        }

        if (ofertaGanadora == null) {
            devolverTokensAlPropietario(subasta);
        } else {
            ofertaGanadora.setEstado(com.prontaentrega.models.enums.EstadoOferta.GANADORA);
            ofertaRepository.save(ofertaGanadora);
            liquidarVenta(subasta, ofertaGanadora);
        }

        subasta.setMotivoCierre(motivo);
        subasta.setEstado(EstadoSubasta.CERRADA);
        subasta.setActualizadoEn(LocalDateTime.now());
        subastaRepository.save(subasta);
        return subasta;
    }

    private void devolverTokensAlPropietario(Subasta subasta) {
        Usuario vendedor = usuarioRepository.findById(subasta.getPropietarioId())
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con el id: " + subasta.getPropietarioId()));

        UUID jugadorId = subasta.getJugadorId();
        BolsaDeJugador bolsa = subasta.getBolsaDeJugador();
        InventarioUsuario inventario = inventarioUsuarioRepository.findByUsuarioIdAndJugadorId(vendedor.getId(), jugadorId)
                .orElse(new InventarioUsuario(vendedor, bolsa, 0, 0));

        int actuales = inventario.getTokensDisponibles() == null ? 0 : inventario.getTokensDisponibles();
        int enVenta = inventario.getTokensEnVenta() == null ? 0 : inventario.getTokensEnVenta();
        inventario.setTokensDisponibles(actuales + subasta.getCantidadOfrecida());
        inventario.setTokensEnVenta(Math.max(0, enVenta - subasta.getCantidadOfrecida()));
        inventario.setActualizadoEn(LocalDateTime.now());
        inventarioUsuarioRepository.save(inventario);
    }

    private void liquidarVenta(Subasta subasta, Oferta ofertaGanadora) {
        Usuario vendedor = usuarioRepository.findById(subasta.getPropietarioId())
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con el id: " + subasta.getPropietarioId()));

        Usuario ganador = usuarioRepository.findById(ofertaGanadora.getUsuarioId())
                .orElseThrow(() -> new UsuarioNoEncontradoException("Usuario no encontrado con el id: " + ofertaGanadora.getUsuarioId()));

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
        inventarioGanador.setActualizadoEn(LocalDateTime.now());
        inventarioVendedor.setActualizadoEn(LocalDateTime.now());
        inventarioUsuarioRepository.save(inventarioGanador);
        inventarioUsuarioRepository.save(inventarioVendedor);

        subasta.setOfertaActual(ofertaGanadora.getPuja());
        subasta.setOfertanteActual(ganador);

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
        // Lo hace aca no dije nadaaaaa
    }
}
