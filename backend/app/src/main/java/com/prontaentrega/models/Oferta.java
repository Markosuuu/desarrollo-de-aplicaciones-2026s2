package com.prontaentrega.models;

import com.prontaentrega.models.enums.EstadoOferta;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Oferta {
    private UUID id;
    private Subasta subasta;
    private Usuario usuario;
    private BigDecimal puja;
    private EstadoOferta estado; // TODO: Quizas cambiar a boolean? esLaMaxima/esSuperior
    private LocalDateTime creadoEn;

    public Oferta(Subasta subasta, Usuario usuario, BigDecimal puja) {
        this.subasta = subasta;
        this.usuario = usuario;
        this.puja = puja;
        this.estado = EstadoOferta.VIGENTE;
        this.creadoEn = LocalDateTime.now(ZoneId.systemDefault());
    }

    public UUID getSubastaId() {
        return subasta.getId();
    }

    public UUID getUsuarioId() {
        return usuario.getId();
    }
}
