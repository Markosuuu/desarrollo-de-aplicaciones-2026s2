package com.prontaentrega.models;

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
    private Boolean esSuperior;
    private LocalDateTime creadoEn;

    public Oferta(Subasta subasta, Usuario usuario, BigDecimal puja) {
        this.subasta = subasta;
        this.usuario = usuario;
        this.puja = puja;
        this.esSuperior = true;
        this.creadoEn = LocalDateTime.now(ZoneId.systemDefault());
    }

    public UUID getSubastaId() {
        return subasta.getId();
    }

    public UUID getUsuarioId() {
        return usuario.getId();
    }

    public boolean esLaMasAltaDeSubasta() {
        return subasta.getOfertaActual().getUsuarioId() == usuario.getId();
    }
}
