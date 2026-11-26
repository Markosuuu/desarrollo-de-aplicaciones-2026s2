package com.prontaentrega.controllers;

import com.prontaentrega.services.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import com.prontaentrega.controllers.exceptions.ErrorResponse;
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

    @Operation(
            summary = "Generar api-key",
            description = "Genera una api-key asociada a un nombre."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Se retorna la API key generada."),
            @ApiResponse(responseCode = "400", description = "El nombre de la API key no fue informado o está vacío.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<ApiKeyResponse> generar(@RequestBody ApiKeyRequest request) {
        String apiKey = apiKeyService.generar(request.nombre());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiKeyResponse(apiKey));
    }

    public record ApiKeyRequest(String nombre) {}

    public record ApiKeyResponse(String apiKey) {}
}
