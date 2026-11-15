package com.prontaentrega.models.auth;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.regex.Pattern;

import com.prontaentrega.models.exception.CampoFaltanteException;
import com.prontaentrega.models.exception.CampoInvalidoException;
import com.prontaentrega.models.exception.ValorNegativoException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class Usuario {
    private UUID id;
    private final String nombre;
    private final String correo;
    private final String passwordHash;
    private final LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private BigDecimal creditosDisponibles;

    public Usuario(String nombre, String correo, String passwordHash) {
        validarUsuario(nombre, correo, passwordHash);

        this.nombre = nombre;
        this.correo = correo;
        this.passwordHash = passwordHash;
        this.creadoEn = LocalDateTime.now(ZoneId.systemDefault());
        this.actualizadoEn = this.creadoEn;
        this.creditosDisponibles = BigDecimal.valueOf(100);
    }

    private static void validarUsuario(String nombre, String correo, String passwordHash) {
        if (!StringUtils.hasText(nombre)) {
            throw new CampoFaltanteException("El nombre debe ser válido");
        }

        if (!StringUtils.hasText(correo) || !EMAIL_PATTERN.matcher(correo).matches()) {
            throw new CampoFaltanteException("El correo debe ser válido");
        }

        if (!StringUtils.hasText(passwordHash)) {
            throw new CampoFaltanteException("Contraseña requerida");
        }

        if (passwordHash.length() < 8 || passwordHash.length() > 72
                || passwordHash.chars().noneMatch(Character::isLetter)
                || passwordHash.chars().noneMatch(Character::isDigit)) {
            throw new CampoInvalidoException("Contraseña inválida");
        }
    }

    public void restarCreditos(BigDecimal precioTotal) {
        if (precioTotal.compareTo(this.creditosDisponibles) > 0) {
            throw new ValorNegativoException("Los créditos no pueden ser negativos");
        }

        this.creditosDisponibles = this.creditosDisponibles.subtract(precioTotal);
        this.actualizadoEn = LocalDateTime.now(ZoneId.systemDefault());
    }

    public void sumarCreditos(BigDecimal precioTotal) {
        this.creditosDisponibles = this.creditosDisponibles.add(precioTotal);
        this.actualizadoEn = LocalDateTime.now(ZoneId.systemDefault());
    }

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]{1,64}@(?:[A-Za-z0-9-]{1,63}\\.){1,10}[A-Za-z]{2,63}$");

}
