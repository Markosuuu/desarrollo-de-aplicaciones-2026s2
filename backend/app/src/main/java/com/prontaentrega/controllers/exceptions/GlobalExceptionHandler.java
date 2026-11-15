package com.prontaentrega.controllers.exceptions;

import com.prontaentrega.models.exception.CampoInvalidoException;
import com.prontaentrega.models.exception.CantidadDeTokensException;
import com.prontaentrega.models.exception.AnuncioNoPendienteException;
import com.prontaentrega.services.dto.RefreshCatalogResponse;
import com.prontaentrega.services.exceptions.jugadores.ProviderUnavailableException;
import com.prontaentrega.models.exception.CotizacionInvalidaException;
import com.prontaentrega.services.exceptions.auth.*;
import com.prontaentrega.services.exceptions.cotizacion.NoHayEstrategiaActivaConfiguradaException;
import com.prontaentrega.services.exceptions.jugadores.JugadorNoEncontradoException;
import com.prontaentrega.services.exceptions.mercado.*;
import com.prontaentrega.services.exceptions.subastas.OfertaInvalidaException;
import com.prontaentrega.services.exceptions.subastas.SubastaNoActivaException;
import com.prontaentrega.services.exceptions.subastas.SubastaNoEncontradaException;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String code = fieldError != null ? fieldError.getDefaultMessage() : "VALIDACION_INVALIDA";
        return error(HttpStatus.BAD_REQUEST, code, "Datos invalidos");
    }


    @ExceptionHandler(CampoInvalidoException.class)
    public ResponseEntity<ErrorResponse> handleCampoInvalido(CampoInvalidoException ex) {
        return error(HttpStatus.BAD_REQUEST, "DATOS_INVALIDOS",
                ex.getMessage());
    }

    @ExceptionHandler(CampoRequeridoException.class)
    public ResponseEntity<ErrorResponse> handleCampoInvalido(CampoRequeridoException ex) {
        return error(HttpStatus.BAD_REQUEST, "CAMPO_REQUERIDO",
                ex.getMessage());
    }

    @ExceptionHandler(ContraseniaEquivocadaException.class)
    public ResponseEntity<ErrorResponse> handleCampoInvalido(ContraseniaEquivocadaException ex) {
        return error(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS",
                ex.getMessage());
    }

    @ExceptionHandler(CorreoNoExistenteException.class)
    public ResponseEntity<ErrorResponse> handleCampoInvalido(CorreoNoExistenteException ex) {
        return error(HttpStatus.UNAUTHORIZED, "CORREO_NO_EXISTENTE",
                ex.getMessage());
    }

    /**
     * Maneja intentos de registrar usuarios duplicados.
     */
    @ExceptionHandler(UsuarioRepetidoException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(UsuarioRepetidoException ex) {
        return error(HttpStatus.CONFLICT, "CORREO_DUPLICADO", ex.getMessage());
    }

    /**
     * Maneja tokens JWT invalidos o vencidos.
     */
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwt(JwtException ex) {
        return error(HttpStatus.UNAUTHORIZED, "JWT_INVALIDO", "JWT invalido");
    }

    /** Maneja conflictos funcionales de estado/configuración de cotizaciones. */
    @ExceptionHandler(NoHayEstrategiaActivaConfiguradaException.class)
    public ResponseEntity<CotizacionErrorResponse> handleQuoteConflict(NoHayEstrategiaActivaConfiguradaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new CotizacionErrorResponse("RECOTIZACION_NO_DISPONIBLE", ex.getMessage()));
    }

    /** Maneja un resultado que incumple la invariante de cotización del mercado. */
    @ExceptionHandler(CotizacionInvalidaException.class)
    public ResponseEntity<CotizacionErrorResponse> handleInvalidQuote(CotizacionInvalidaException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new CotizacionErrorResponse("COTIZACION_INVALIDA", ex.getMessage()));
    }

    /**
     * Maneja fallas de WhoScored conservando el contrato del endpoint de actualizacion.
     */
    @ExceptionHandler(ProviderUnavailableException.class)
    public ResponseEntity<RefreshCatalogResponse> handleProviderUnavailable(ProviderUnavailableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(RefreshCatalogResponse.failure(ex.getMessage()));
    }

    @ExceptionHandler(CantidadDeTokensException.class)
    public ResponseEntity<ErrorResponse> handleValidation(CantidadDeTokensException ex) {
        return error(HttpStatus.BAD_REQUEST, "CANTIDAD_DE_TOKENS_INVALIDA", ex.getMessage());
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
        return error(HttpStatus.NOT_FOUND, "USUARIO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(JugadorNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleValidation(JugadorNoEncontradoException ex) {
        return error(HttpStatus.NOT_FOUND, "JUGADOR_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(AnuncioNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> handleValidation(AnuncioNoEncontradoException ex) {
        return error(HttpStatus.NOT_FOUND, "ANUNCIO_NO_ENCONTRADO", ex.getMessage());
    }

    @ExceptionHandler(SinAutorizacionExpcetion.class)
    public ResponseEntity<ErrorResponse> handleValidation(SinAutorizacionExpcetion ex) {
        return error(HttpStatus.FORBIDDEN, "SIN_AUTORIZACION", ex.getMessage());
    }

    @ExceptionHandler(AnuncioNoPendienteException.class)
    public ResponseEntity<ErrorResponse> handleValidation(AnuncioNoPendienteException ex) {
        return error(HttpStatus.CONFLICT, "ANUNCIO_NO_PENDIENTE", ex.getMessage());
    }

    @ExceptionHandler(VendedorComprandoTokensPropiosException.class)
    public ResponseEntity<ErrorResponse> handleValidation(VendedorComprandoTokensPropiosException ex) {
        return error(HttpStatus.BAD_REQUEST, "VENDEDOR_COMPRANDO_TOKENS_PROPIOS", ex.getMessage());
    }

    @ExceptionHandler(CreditosInsuficientesException.class)
    public ResponseEntity<ErrorResponse> handleValidation(CreditosInsuficientesException ex) {
        return error(HttpStatus.UNPROCESSABLE_ENTITY, "CREDITOS_INSUFICIENTES_PARA_COMPRAR", ex.getMessage());
    }

    @ExceptionHandler(SubastaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> handleSubastaNoEncontrada(SubastaNoEncontradaException ex) {
        return error(HttpStatus.NOT_FOUND, "SUBASTA_NO_ENCONTRADA", ex.getMessage());
    }

    @ExceptionHandler(SubastaNoActivaException.class)
    public ResponseEntity<ErrorResponse> handleSubastaNoActiva(SubastaNoActivaException ex) {
        return error(HttpStatus.CONFLICT, "SUBASTA_NO_ACTIVA", ex.getMessage());
    }

    @ExceptionHandler(OfertaInvalidaException.class)
    public ResponseEntity<ErrorResponse> handleOfertaInvalida(OfertaInvalidaException ex) {
        return error(HttpStatus.BAD_REQUEST, "OFERTA_INVALIDA", ex.getMessage());
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleAuctionConflict(RuntimeException ex) {
        return error(HttpStatus.CONFLICT, "SUBASTA_MODIFICADA_CONCURRENTEMENTE",
                "La subasta cambió mientras se procesaba la solicitud; vuelve a intentarlo");
    }



    private ResponseEntity<ErrorResponse> error(HttpStatus status, String code, String message) {
        ErrorResponse response = new ErrorResponse(
                new ErrorResponse.ErrorDetail(code, message)
        );

        return ResponseEntity.status(status).body(response);
    }
}
