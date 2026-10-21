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
    public ApiKeyController(
            ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    public ResponseEntity<?> generar(@RequestBody CreateApiKeyRequest request) {
        String apiKey = apiKeyService.generar(request.nombre());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateApiKeyResponse(apiKey));
    }

    public record CreateApiKeyRequest(String nombre) {}

    public record CreateApiKeyResponse(String apiKey) {}
}
