package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.InventarioUsuario;
import com.prontaentrega.models.BolsaDeJugador;
import com.prontaentrega.models.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "InventarioUsuario")
@Table(name = "inventarios_usuarios")
@Getter
@Setter
@NoArgsConstructor
public class InventarioUsuarioDTOSQL {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(name = "jugador_id", nullable = false)
    private UUID jugadorId;

    @Column(name = "tokens_disponibles", nullable = false)
    private Integer tokensDisponibles;

    @Column(name = "tokens_en_venta", nullable = false)
    private Integer tokensEnVenta;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    public static InventarioUsuarioDTOSQL desdeModelo(InventarioUsuario token) {
        InventarioUsuarioDTOSQL dto = new InventarioUsuarioDTOSQL();
        dto.id = token.getId();
        dto.usuarioId = token.getUsuario().getId();
        dto.jugadorId = token.getBolsaDeJugador()
                .getEstadisticasDeportivas()
                .getJugador()
                .getId();
        dto.tokensDisponibles = token.getTokensDisponibles();
        dto.tokensEnVenta = token.getTokensEnVenta();
        dto.actualizadoEn = token.getActualizadoEn();
        return dto;
    }

    public InventarioUsuario aModelo(Usuario usuario, BolsaDeJugador bolsaDeJugador) {
        InventarioUsuario token = new InventarioUsuario();
        token.setId(id);
        token.setUsuario(usuario);
        token.setBolsaDeJugador(bolsaDeJugador);
        token.setTokensDisponibles(tokensDisponibles);
        token.setTokensEnVenta(tokensEnVenta);
        token.setActualizadoEn(actualizadoEn);
        return token;
    }
}
