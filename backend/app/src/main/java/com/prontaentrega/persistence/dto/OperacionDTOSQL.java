package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.enums.EstadoOperacion;
import com.prontaentrega.models.Operacion;
import com.prontaentrega.models.enums.TipoOperacion;
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

@Entity(name = "Operacion")
@Table(name = "operaciones")
@Getter
@Setter
@NoArgsConstructor
public class OperacionDTOSQL {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "contraparte_id", nullable = false)
    private UUID contraparteId;

    @Column(name = "usuario_nombre")
    private String usuarioNombre;

    @Column(name = "contraparte_nombre")
    private String contraparteNombre;

    @Column(name = "jugador_id", nullable = false)
    private UUID jugadorId;

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

    @Column(name = "nombre_jugador", nullable = false)
    private String nombreJugador;

    @Column(name = "creada_en", nullable = false)
    private LocalDateTime creadaEn;

    public static OperacionDTOSQL desdeModelo(Operacion operacion) {
        OperacionDTOSQL dto = new OperacionDTOSQL();
        dto.id = operacion.getId();
        dto.usuarioId = operacion.getUsuarioId();
        dto.contraparteId = operacion.getContraparteId();
        dto.usuarioNombre = operacion.getUsuarioNombre();
        dto.contraparteNombre = operacion.getContraparteNombre();
        dto.jugadorId = operacion.getJugadorId();
        dto.cantidad = operacion.getCantidad();
        dto.precioPorToken = operacion.getPrecioPorToken();
        dto.total = operacion.getTotal();
        dto.tipo = operacion.getTipo();
        dto.estado = operacion.getEstado();
        dto.nombreJugador = operacion.getNombreJugador();
        dto.creadaEn = operacion.getCreadaEn();
        return dto;
    }

    public Operacion aModelo() {
        Operacion operacion = new Operacion();
        operacion.setId(id);
        operacion.setUsuarioId(usuarioId);
        operacion.setContraparteId(contraparteId);
        operacion.setUsuarioNombre(usuarioNombre);
        operacion.setContraparteNombre(contraparteNombre);
        operacion.setJugadorId(jugadorId);
        operacion.setCantidad(cantidad);
        operacion.setPrecioPorToken(precioPorToken);
        operacion.setTotal(total);
        operacion.setTipo(tipo);
        operacion.setEstado(estado);
        operacion.setNombreJugador(nombreJugador);
        operacion.setCreadaEn(creadaEn);
        return operacion;
    }
}
