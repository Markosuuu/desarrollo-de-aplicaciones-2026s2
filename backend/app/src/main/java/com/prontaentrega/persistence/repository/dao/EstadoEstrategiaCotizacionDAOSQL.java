package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.EstadoEstrategiaCotizacionDTOSQL;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Acceso relacional al estado global de estrategias de cotización. */
public interface EstadoEstrategiaCotizacionDAOSQL
        extends JpaRepository<EstadoEstrategiaCotizacionDTOSQL, String> {
    /** Busca y bloquea la fila global para serializar cambios administrativos. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select estado from EstadoEstrategiaCotizacionDTOSQL estado where estado.id = :id")
    Optional<EstadoEstrategiaCotizacionDTOSQL> buscarBloqueando(@Param("id") String id);
}