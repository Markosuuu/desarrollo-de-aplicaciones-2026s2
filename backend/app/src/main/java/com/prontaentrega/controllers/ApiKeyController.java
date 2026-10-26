package com.prontaentrega.controllers;

import com.prontaentrega.services.ApiKeyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Controller encargado de la creación de API KEYS.
 * Que son requeridas únicamente para la recotización manual del mercado.
 */
@RestController
@RequestMapping("/api/keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(
            ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    public ResponseEntity<ApiKeyResponse> generar(@RequestBody ApiKeyRequest request) {
        String apiKey = apiKeyService.generar(request.nombre());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiKeyResponse(apiKey));
    }

    public record ApiKeyRequest(String nombre) {}

    public record ApiKeyResponse(String apiKey) {}
}
