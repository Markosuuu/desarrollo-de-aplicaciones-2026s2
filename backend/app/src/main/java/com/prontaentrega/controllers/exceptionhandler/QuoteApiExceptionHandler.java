package com.prontaentrega.controllers.exceptionhandler;

import com.prontaentrega.controllers.QuoteController;
import com.prontaentrega.controllers.dtos.QuoteErrorResponse;
import com.prontaentrega.services.exceptions.QuoteOperationConflictException;
import com.prontaentrega.services.exceptions.QuoteRequestValidationException;
import com.prontaentrega.services.exceptions.QuoteStrategyNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Traduce errores de quote al contrato plano sin modificar respuestas de otros controllers. */
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = QuoteController.class)
public class QuoteApiExceptionHandler {
    /** Devuelve 400 cuando el body no es JSON válido o tiene tipos incompatibles. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<QuoteErrorResponse> handleBodyUnreadable(HttpMessageNotReadableException exception) {
        return error(HttpStatus.BAD_REQUEST, "BODY_INVALIDO", "El cuerpo de la solicitud no es válido");
    }

    /** Devuelve 400 para campos ausentes, desconocidos o reglas de formato del request. */
    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class,
            QuoteRequestValidationException.class})
    public ResponseEntity<QuoteErrorResponse> handleRequestValidation(Exception exception) {
        String message = exception instanceof QuoteRequestValidationException
                ? exception.getMessage() : "Los datos enviados no cumplen el contrato de cotizaciones";
        return error(HttpStatus.BAD_REQUEST, "CONFIGURACION_INVALIDA", message);
    }

    /** Devuelve 404 para un identificador de estrategia que no está registrado. */
    @ExceptionHandler(QuoteStrategyNotFoundException.class)
    public ResponseEntity<QuoteErrorResponse> handleStrategyNotFound(QuoteStrategyNotFoundException exception) {
        return error(HttpStatus.NOT_FOUND, "ESTRATEGIA_NO_ENCONTRADA", exception.getMessage());
    }

    /** Devuelve 409 para estado incompleto, conflicto o resultado de cotización inválido. */
    @ExceptionHandler(QuoteOperationConflictException.class)
    public ResponseEntity<QuoteErrorResponse> handleConflict(QuoteOperationConflictException exception) {
        return error(HttpStatus.CONFLICT, "CONFLICTO_COTIZACION", exception.getMessage());
    }

    /** Devuelve 409 para fallos recuperables de bloqueo o serialización de PostgreSQL. */
    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<QuoteErrorResponse> handleTransactionConflict(Exception exception) {
        return error(HttpStatus.CONFLICT, "CONFLICTO_TRANSACCIONAL",
                "La operación de cotizaciones entró en conflicto; puede reintentarse");
    }

    /** No expone SQL, credenciales ni detalles internos de persistencia. */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<QuoteErrorResponse> handlePersistenceFailure(DataAccessException exception) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_PERSISTENCIA",
                "No se pudo completar la operación de cotizaciones");
    }

    /** Construye una respuesta quote plana sin propagar excepciones ni datos internos. */
    private ResponseEntity<QuoteErrorResponse> error(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status).body(new QuoteErrorResponse(code, message));
    }
}