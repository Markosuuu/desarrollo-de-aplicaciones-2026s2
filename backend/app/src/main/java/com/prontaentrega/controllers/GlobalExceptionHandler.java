package com.prontaentrega.controllers;

import com.prontaentrega.services.exceptions.CatalogUnavailableException;
import com.prontaentrega.services.exceptions.DuplicateUserException;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String code = fieldError != null ? fieldError.getDefaultMessage() : "VALIDACION_INVALIDA";
        return error(HttpStatus.BAD_REQUEST, code, "Datos invalidos");
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(ValidationException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateUserException ex) {
        return error(HttpStatus.CONFLICT, "CORREO_DUPLICADO", ex.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS", "Credenciales invalidas");
    }

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<Map<String, Object>> handleJwt(JwtException ex) {
        return error(HttpStatus.UNAUTHORIZED, "JWT_INVALIDO", "JWT invalido");
    }

    @ExceptionHandler(CatalogUnavailableException.class)
    public ResponseEntity<Map<String, Object>> handleCatalogUnavailable(CatalogUnavailableException ex) {
        return error(HttpStatus.SERVICE_UNAVAILABLE, "CATALOGO_NO_DISPONIBLE", ex.getMessage());
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
