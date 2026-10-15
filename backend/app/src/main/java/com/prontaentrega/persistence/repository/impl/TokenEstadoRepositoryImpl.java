package com.prontaentrega.persistence.repository.impl;

import com.prontaentrega.models.TokenEstado;
import com.prontaentrega.persistence.dto.TokenEstadoDTOSQL;
import com.prontaentrega.persistence.repository.TokenEstadoRepository;
import com.prontaentrega.persistence.repository.dao.TokenEstadoDAOSQL;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class TokenEstadoRepositoryImpl implements TokenEstadoRepository {
    private final TokenEstadoDAOSQL daosql;

    public TokenEstadoRepositoryImpl(TokenEstadoDAOSQL daosql) {
        this.daosql = daosql;
    }

    @Override
    public TokenEstado save(TokenEstado tokenEstado) {
        TokenEstadoDTOSQL dto = TokenEstadoDTOSQL.desdeToken(tokenEstado);
        daosql.save(dto);
        tokenEstado.setId(dto.getId());
        return tokenEstado;
    }

    @Override
    public void deleteAll() {
        daosql.deleteAll();
    }

    @Override
    public Optional<TokenEstado> findByUsuarioId(UUID usuarioId) {
        return daosql.findTokenEstadoDTOSQLByUsuarioId(usuarioId)
                .map(TokenEstadoDTOSQL::aToken);
    }
}
