package com.prontaentrega.controllers.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.prontaentrega.services.exceptions.IdempotencyKeyException;

@RestControllerAdvice(assignableTypes = com.prontaentrega.controllers.SubastaController.class)
public class SubastaExceptionHandler {
    @ExceptionHandler(IdempotencyKeyException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyKeyConflict(IdempotencyKeyException ex) {
        ErrorResponse response = new ErrorResponse(
                new ErrorResponse.ErrorDetail("IDEMPOTENCY_KEY_CONFLICT", ex.getMessage()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateOffer(DataIntegrityViolationException ex) {
        ErrorResponse response = new ErrorResponse(
                new ErrorResponse.ErrorDetail("OFERTA_DUPLICADA", "Ya existe una solicitud con esta clave de idempotencia"));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
