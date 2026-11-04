package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.AnuncioVenta;
import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.Usuario;
import com.prontaentrega.models.enums.EstadoAnuncioVenta;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "vendedor_id", nullable = false)
    private UsuarioDTOSQL vendedor;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "bolsa_de_jugador_id", nullable = false)
    private BolsaDeJugadorDTOSQL bolsaDeJugador;

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

    public static AnuncioVentaDTOSQL desdeModelo(
            AnuncioVenta anuncio,
            UsuarioDTOSQL vendedor,
            BolsaDeJugadorDTOSQL bolsaDeJugador) {
        AnuncioVentaDTOSQL dto = new AnuncioVentaDTOSQL();
        dto.id = anuncio.getId();
        dto.vendedor = vendedor;
        dto.bolsaDeJugador = bolsaDeJugador;
        dto.cantidadTokens = anuncio.getCantidadTokens();
        dto.precioPorToken = anuncio.getPrecioPorToken();
        dto.precioTotal = anuncio.getPrecioTotal();
        dto.estado = anuncio.getEstado();
        dto.creadoEn = anuncio.getCreadoEn();
        dto.actualizadoEn = anuncio.getActualizadoEn();
        return dto;
    }

    public AnuncioVenta aModelo() {
        return AnuncioVenta.builder()
                .id(id)
                .vendedor(UsuarioDTOSQL.aUsuario(vendedor))
                .bolsaDeJugador(bolsaDeJugador.aModelo())
                .cantidadTokens(cantidadTokens)
                .precioPorToken(precioPorToken)
                .precioTotal(precioTotal)
                .estado(estado)
                .creadoEn(creadoEn)
                .actualizadoEn(actualizadoEn)
                .build();
    }
}
