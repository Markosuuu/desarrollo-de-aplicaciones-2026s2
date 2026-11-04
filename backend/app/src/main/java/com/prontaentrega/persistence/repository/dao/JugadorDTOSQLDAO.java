package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.JugadorDTOSQL;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JugadorDTOSQLDAO extends JpaRepository<JugadorDTOSQL, UUID> {}
