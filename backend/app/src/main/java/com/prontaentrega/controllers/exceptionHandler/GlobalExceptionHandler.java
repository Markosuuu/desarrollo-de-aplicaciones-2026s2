package com.prontaentrega.controllers;

import com.prontaentrega.services.exceptions.CatalogUnavailableException;
import com.prontaentrega.services.exceptions.DuplicateUserException;
import com.prontaentrega.services.dto.RefreshCatalogResponse;
import com.prontaentrega.services.exceptions.ProviderUnavailableException;
import com.prontaentrega.services.exceptions.ValidationException;
import io.jsonwebtoken.JwtException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja errores de validacion declarados en DTOs.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String code = fieldError != null ? fieldError.getDefaultMessage() : "VALIDACION_INVALIDA";
        return error(HttpStatus.BAD_REQUEST, code, "Datos invalidos");
    }

    /**
     * Maneja errores de validacion generados por servicios.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(ValidationException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage());
    }

    /**
     * Maneja intentos de registrar usuarios duplicados.
     */
    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateUserException ex) {
        return error(HttpStatus.CONFLICT, "CORREO_DUPLICADO", ex.getMessage());
    }

    /**
     * Maneja credenciales invalidas de autenticacion.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", "Credenciales invalidas");
    }

    /**
     * Maneja tokens JWT invalidos o vencidos.
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwt(JwtException ex) {
        return error(HttpStatus.UNAUTHORIZED, "JWT_INVALIDO", "JWT invalido");
    }

    /**
     * Maneja la ausencia de catalogo local disponible.
     */
    @ExceptionHandler(CatalogUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleCatalogUnavailable(CatalogUnavailableException ex) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "CATALOGO_NO_DISPONIBLE", ex.getMessage());
    }

    /**
     * Maneja fallas de WhoScored conservando el contrato del endpoint de actualizacion.
     */
    @ExceptionHandler(ProviderUnavailableException.class)
    public ResponseEntity<RefreshCatalogResponse> handleProviderUnavailable(ProviderUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(RefreshCatalogResponse.failure(ex.getMessage()));
    }

    private ResponseEntity<Map<String, Object>> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(Map.of(
                "error", Map.of(
                        "code", code,
                        "message", message
                )
        ));
    }
}
