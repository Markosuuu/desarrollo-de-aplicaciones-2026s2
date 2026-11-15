package com.prontaentrega.models;

import com.prontaentrega.models.auth.Usuario;
import com.prontaentrega.models.exception.CampoFaltanteException;
import com.prontaentrega.models.exception.CampoInvalidoException;
import com.prontaentrega.models.exception.ValorNegativoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Tag("modelo")
public class UsuarioTest {

    private Usuario user;

    @BeforeEach
    void setUp() {
        user = new Usuario("Pepe", "pepe@gmail.com", "12341234abc");
    }

    @Test
    void noCreaUsuarioConNombreNulo() {
        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> new Usuario(null, "pepe@gmail.com", "12341234abc"));
        assertEquals("El nombre debe ser válido", ex.getMessage());
    }

    @Test
    void noCreaUsuarioConCorreoNulo() {
        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> new Usuario("pepe", null, "12341234abc"));
        assertEquals("El correo debe ser válido", ex.getMessage());
    }

    @Test
    void noCreaUsuarioConCorreoQueNoEsUnCorreo() {
        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> new Usuario("pepe", "pepegmailcom", "12341234abc"));
        assertEquals("El correo debe ser válido", ex.getMessage());
    }

    @Test
    void noCreaUsuarioConContraseñaNula() {
        CampoFaltanteException ex = assertThrows(CampoFaltanteException.class,
                () -> new Usuario("pepe", "pepe@gmail.com", null));
        assertEquals("Contraseña requerida", ex.getMessage());
    }

    @Test
    void noCreaUsuarioConContraseñaSinLetras() {
        CampoInvalidoException ex = assertThrows(CampoInvalidoException.class,
                () -> new Usuario("Pepe", "pepe@gmail.com", "12341234"));
        assertEquals("Contraseña inválida", ex.getMessage());
    }

    @Test
    void noCreaUsuarioConContraseñaSinNumeros() {
        CampoInvalidoException ex = assertThrows(CampoInvalidoException.class,
                () -> new Usuario("Pepe", "pepe@gmail.com", "abcdefghij"));
        assertEquals("Contraseña inválida", ex.getMessage());
    }

    @Test
    void noCreaUsuarioConContraseñaConMenosDe8Caracteres() {
        CampoInvalidoException ex = assertThrows(CampoInvalidoException.class,
                () -> new Usuario("Pepe", "pepe@gmail.com", "123"));
        assertEquals("Contraseña inválida", ex.getMessage());
    }

    @Test
    void noCreaUsuarioConContraseñaConMasDe72Caracteres() {
        CampoInvalidoException ex = assertThrows(CampoInvalidoException.class,
                () -> new Usuario("Pepe", "pepe@gmail.com", "abc1002000abc1002000abc1002000abc1002000abc1002000abc1002000abc1002000xyz"));
        assertEquals("Contraseña inválida", ex.getMessage());
    }

    @Test
    void restarCreditosAUnUsuarioLeDejaMenosCreditos(){
        user.restarCreditos(BigDecimal.valueOf(50));

        assertEquals(BigDecimal.valueOf(50), user.getCreditosDisponibles());
    }

    @Test
    void restarCreditosAUnUsuarioLoActualiza(){
        LocalDateTime fecha = user.getActualizadoEn();
        user.restarCreditos(BigDecimal.valueOf(50));

        assertTrue(user.getActualizadoEn().isAfter(fecha));
    }

    @Test
    void restarCreditosAUnUsuarioNoPuedeSerNegativo() {
        ValorNegativoException ex = assertThrows(ValorNegativoException.class,
                () -> user.restarCreditos(BigDecimal.valueOf(101)));

        assertEquals("Los créditos no pueden ser negativos",
                ex.getMessage());
    }

    @Test
    void sumarCreditosAUnUsuarioLeDejaMasCreditos(){
        user.sumarCreditos(BigDecimal.valueOf(50));

        assertEquals(BigDecimal.valueOf(150), user.getCreditosDisponibles());
    }

    @Test
    void sumarCreditosAUnUsuarioLoActualiza(){
        LocalDateTime fecha = user.getActualizadoEn();
        user.sumarCreditos(BigDecimal.valueOf(50));

        assertTrue(user.getActualizadoEn().isAfter(fecha));
    }
}
