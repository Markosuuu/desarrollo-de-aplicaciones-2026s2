package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.AnuncioVenta;
import com.prontaentrega.models.EstadoAnuncioVenta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "AnuncioVenta")
@Table(name = "anuncios_venta")
@Getter
@Setter
@NoArgsConstructor
public class AnuncioVentaDTOSQL {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "vendedor_id", nullable = false)
    private UUID vendedorId;

    @Column(name = "jugador_id", nullable = false)
    private UUID jugadorId;

    @Column(name = "cantidad_tokens", nullable = false)
    private Integer cantidadTokens;

    @Column(name = "precio_por_token", nullable = false)
    private BigDecimal precioPorToken;

    @Column(name = "precio_total", nullable = false)
    private BigDecimal precioTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoAnuncioVenta estado;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    public static AnuncioVentaDTOSQL desdeModelo(AnuncioVenta anuncio) {
        AnuncioVentaDTOSQL dto = new AnuncioVentaDTOSQL();
        dto.id = anuncio.getId();
        dto.vendedorId = anuncio.getVendedorId();
        dto.jugadorId = anuncio.getJugadorId();
        dto.cantidadTokens = anuncio.getCantidadTokens();
        dto.precioPorToken = anuncio.getPrecioPorToken();
        dto.precioTotal = anuncio.getPrecioTotal();
        dto.estado = anuncio.getEstado();
        dto.creadoEn = anuncio.getCreadoEn();
        dto.actualizadoEn = anuncio.getActualizadoEn();
        return dto;
    }

    public AnuncioVenta aModelo() {
        AnuncioVenta anuncio = new AnuncioVenta();
        anuncio.setId(id);
        anuncio.setVendedorId(vendedorId);
        anuncio.setJugadorId(jugadorId);
        anuncio.setCantidadTokens(cantidadTokens);
        anuncio.setPrecioPorToken(precioPorToken);
        anuncio.setPrecioTotal(precioTotal);
        anuncio.setEstado(estado);
        anuncio.setCreadoEn(creadoEn);
        anuncio.setActualizadoEn(actualizadoEn);
        return anuncio;
    }
}
