package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.InventarioUsuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioDTOSQL usuario;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "bolsa_de_jugador_id", nullable = false)
    private BolsaDeJugadorDTOSQL bolsaDeJugador;

    @Column(name = "tokens_disponibles", nullable = false)
    private Integer tokensDisponibles;

    @Column(name = "tokens_en_venta", nullable = false)
    private Integer tokensEnVenta;

    @Column(name = "actualizado_en", nullable = false)
    private LocalDateTime actualizadoEn;

    // TODO: Revisar si hace falta pasar 3 params
    public static InventarioUsuarioDTOSQL desdeModelo(
            InventarioUsuario token,
            UsuarioDTOSQL usuario,
            BolsaDeJugadorDTOSQL bolsaDeJugador) {
        InventarioUsuarioDTOSQL dto = new InventarioUsuarioDTOSQL();
        dto.id = token.getId();
        dto.usuario = usuario;
        dto.bolsaDeJugador = bolsaDeJugador;
        dto.tokensDisponibles = token.getTokensDisponibles();
        dto.tokensEnVenta = token.getTokensEnVenta();
        dto.actualizadoEn = token.getActualizadoEn();
        return dto;
    }

    public InventarioUsuario aModelo() {
        InventarioUsuario token = new InventarioUsuario();
        token.setId(id);
        token.setUsuario(UsuarioDTOSQL.aUsuario(usuario));
        token.setBolsaDeJugador(bolsaDeJugador.aModelo());
        token.setTokensDisponibles(tokensDisponibles);
        token.setTokensEnVenta(tokensEnVenta);
        token.setActualizadoEn(actualizadoEn);
        return token;
    }
}
