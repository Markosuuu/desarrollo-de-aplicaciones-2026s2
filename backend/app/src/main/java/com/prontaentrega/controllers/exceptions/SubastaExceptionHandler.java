package com.prontaentrega.controllers.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = com.prontaentrega.controllers.SubastaController.class)
public class SubastaExceptionHandler {
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateOffer(DataIntegrityViolationException ex) {
        ErrorResponse response = new ErrorResponse(
                new ErrorResponse.ErrorDetail("OFERTA_DUPLICADA", "Ya existe una oferta de este usuario para la subasta"));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
}
