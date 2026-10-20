package com.prontaentrega.services;
import com.prontaentrega.persistence.dto.ApiKey;
import com.prontaentrega.persistence.repository.dao.ApiKeyDAO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

@Service
public class ApiKeyService {

    private final ApiKeyDAO apiKeyDAO;
    private final SecureRandom secureRandom = new SecureRandom();

    public ApiKeyService(ApiKeyDAO apiKeyDAO) {
        this.apiKeyDAO = apiKeyDAO;
    }

    @Transactional
    public String generar(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        }

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String apiKey = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        String keyHash = calcularHash(apiKey);

        ApiKey entidad = new ApiKey(nombre.trim(), keyHash);
        apiKeyDAO.save(entidad);

        return apiKey;
    }

    @Transactional(readOnly = true)
    public boolean validar(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            return false;
        }

        String keyHash = calcularHash(apiKey);

        return apiKeyDAO
                .findByKeyHashAndActivaTrue(keyHash)
                .isPresent();
    }

    private String calcularHash(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valor.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible", e);
        }
    }
}
