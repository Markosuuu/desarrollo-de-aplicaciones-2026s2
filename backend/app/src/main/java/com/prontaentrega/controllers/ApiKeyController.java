package com.prontaentrega.controllers;

import com.prontaentrega.services.ApiKeyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;
    private final String masterSecret;

    public ApiKeyController(
            ApiKeyService apiKeyService,
            @Value("${api-key.master-secret}") String masterSecret) {
        this.apiKeyService = apiKeyService;
        this.masterSecret = masterSecret;
    }

    @PostMapping
    public ResponseEntity<?> generar(
            @RequestHeader("X-Master-Key") String masterKey,
            @RequestBody CreateApiKeyRequest request) {

        if (!MessageDigest.isEqual(
                masterSecret.getBytes(StandardCharsets.UTF_8),
                masterKey.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        String apiKey = apiKeyService.generar(request.nombre());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateApiKeyResponse(apiKey));
    }

    public record CreateApiKeyRequest(String nombre) {}

    public record CreateApiKeyResponse(String apiKey) {}
}
