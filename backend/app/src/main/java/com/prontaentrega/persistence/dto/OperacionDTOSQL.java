package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.enums.EstadoOperacion;
import com.prontaentrega.models.Operacion;
import com.prontaentrega.models.enums.TipoOperacion;
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

@Entity(name = "Operacion")
@Table(name = "operaciones")
@Getter
@Setter
@NoArgsConstructor
public class OperacionDTOSQL {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioDTOSQL usuario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "contraparte_id", nullable = false)
    private UsuarioDTOSQL contraparte;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "jugador_id", nullable = false)
    private JugadorDTOSQL jugador;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(name = "precio_por_token", nullable = false)
    private BigDecimal precioPorToken;

    @Column(nullable = false)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoOperacion tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoOperacion estado;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    public static OperacionDTOSQL desdeModelo(
            Operacion operacion,
            UsuarioDTOSQL usuario,
            UsuarioDTOSQL contraparte,
            JugadorDTOSQL jugador) {
        OperacionDTOSQL dto = new OperacionDTOSQL();
        dto.id = operacion.getId();
        dto.usuario = usuario;
        dto.contraparte = contraparte;
        dto.jugador = jugador;
        dto.cantidad = operacion.getCantidad();
        dto.precioPorToken = operacion.getPrecioPorToken();
        dto.total = operacion.getTotal();
        dto.tipo = operacion.getTipo();
        dto.estado = operacion.getEstado();
        dto.creadaEn = operacion.getCreadaEn();
        return dto;
    }

    public Operacion aModelo() {
        return Operacion.builder()
                .id(id)
                .usuario(UsuarioDTOSQL.aUsuario(usuario))
                .contraparte(UsuarioDTOSQL.aUsuario(contraparte))
                .jugador(jugador.aJugador())
                .cantidad(cantidad)
                .precioPorToken(precioPorToken)
                .total(total)
                .tipo(tipo)
                .estado(estado)
                .creadaEn(creadaEn)
                .build();
    }
}
