package com.prontaentrega.controllers;

import static org.assertj.core.api.Assertions.assertThat;

import com.prontaentrega.controllers.exceptionHandler.QuoteApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Verifica códigos quote y que errores de infraestructura no expongan detalles SQL. */
class QuoteApiExceptionHandlerTest {
    private final QuoteApiExceptionHandler handler = new QuoteApiExceptionHandler();

    /** Devuelve conflicto recuperable para contención de locks sin filtrar el mensaje SQL. */
    @Test
    void mapeaConflictoDeLockA409Sanitizado() {
        ResponseEntity<?> response = handler.handleTransactionConflict(
                new PessimisticLockingFailureException("detalle interno de postgres"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().toString()).doesNotContain("postgres");
    }

    /** Devuelve error genérico 500 ante fallos de persistencia. */
    @Test
    void mapeaFalloPersistenciaA500SinDetallesInternos() {
        ResponseEntity<?> response = handler.handlePersistenceFailure(
                new DataIntegrityViolationException("constraint secreto de base"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().toString()).doesNotContain("secreto");
    }
}