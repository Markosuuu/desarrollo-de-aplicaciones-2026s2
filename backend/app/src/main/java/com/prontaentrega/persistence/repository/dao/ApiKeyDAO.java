package com.prontaentrega.persistence.repository.dao;

import com.prontaentrega.persistence.dto.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiKeyDAO extends JpaRepository<ApiKey, UUID> {

    Optional<ApiKey> findByKeyHashAndActivaTrue(String keyHash);

    boolean existsByKeyHash(String keyHash);
}
