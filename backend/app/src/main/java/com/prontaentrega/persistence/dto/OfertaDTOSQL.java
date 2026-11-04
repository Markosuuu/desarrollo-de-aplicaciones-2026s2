package com.prontaentrega.persistence.dto;

import com.prontaentrega.models.Oferta;
import com.prontaentrega.models.Subasta;
import com.prontaentrega.models.enums.EstadoOferta;
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
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "Oferta")
@Table(name = "ofertas_subasta", uniqueConstraints = @UniqueConstraint(
        name = "uk_oferta_usuario_idempotency_key",
        columnNames = {"usuario_id", "idempotency_key"}))
@Getter
@Setter
@NoArgsConstructor
public class OfertaDTOSQL {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "subasta_id", nullable = false)
    private SubastaDTOSQL subasta;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioDTOSQL usuario;

    @Column(nullable = false)
    private BigDecimal puja;

    @Column(nullable = false)
    private Boolean esSuperior;

    @Column(name = "idempotency_key", length = 255)
    private String idempotencyKey;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    public static OfertaDTOSQL desdeModelo(Oferta oferta, SubastaDTOSQL subasta, UsuarioDTOSQL usuario) {
        OfertaDTOSQL dto = new OfertaDTOSQL();
        dto.id = oferta.getId();
        dto.subasta = subasta;
        dto.usuario = usuario;
        dto.puja = oferta.getPuja();
        dto.esSuperior = oferta.getEsSuperior();
        dto.idempotencyKey = oferta.getIdempotencyKey();
        dto.creadoEn = oferta.getCreadoEn();
        return dto;
    }

    public Oferta aModelo() {
        return aModelo(subasta.aModelo());
    }

    public Oferta aModelo(Subasta subastaModelo) {
        Oferta oferta = new Oferta(
                subastaModelo,
                UsuarioDTOSQL.aUsuario(usuario),
                puja,
                creadoEn,
                idempotencyKey);
        oferta.setId(id);
        oferta.setEsSuperior(esSuperior);
        return oferta;
    }
}
