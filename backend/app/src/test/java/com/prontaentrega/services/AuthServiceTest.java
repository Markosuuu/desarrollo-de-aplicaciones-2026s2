package com.prontaentrega.services;

import com.prontaentrega.controllers.dtos.AuthResponse;
import com.prontaentrega.controllers.dtos.LoginRequest;
import com.prontaentrega.controllers.dtos.RegisterRequest;
import com.prontaentrega.models.TokenEstado;
import com.prontaentrega.models.Usuario;
import com.prontaentrega.repository.TokenEstadoRepository;
import com.prontaentrega.repository.UsuarioRepository;
import com.prontaentrega.services.dto.AuthResult;
import com.prontaentrega.services.exceptions.DuplicateUserException;
import com.prontaentrega.services.exceptions.ValidationException;
import com.prontaentrega.utils.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
public class AuthServiceTest extends AbstractIntegrationTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenEstadoRepository tokenEstadoRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        tokenEstadoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }


    /** -------------------------------------------------------------------------------**/
    /** ------------------------------ TESTS DE REGISTER ------------------------------**/
    /** -------------------------------------------------------------------------------**/

    /** Se verifica que al registrar un usuario se devuelva correctamente su información y un token.*/
    @Test
    void registerShouldReturnUserAndToken() {
        AuthResult response = authService.register("Ana", "ana@example.com", "clave123");

        assertNotNull(response);
        assertNotNull(response.token());
        assertNotNull(response.usuario());
        assertEquals("Ana", response.usuario().getNombre());
        assertEquals("ana@example.com", response.usuario().getCorreo());
    }

    /** Se verifica que al registrar un usuario se persista correctamente en la base de datos.*/
    @Test
    void registerShouldPersistUserAndTokenState() {
        authService.register("Ana", "ana@example.com", "clave123");

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase("ana@example.com")
                .orElseThrow();

        assertEquals("Ana", usuario.getNombre());
        assertEquals("ana@example.com", usuario.getCorreo());
        assertNotEquals("clave123", usuario.getPasswordHash());
        TokenEstado tokenEstado = tokenEstadoRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow();
        assertEquals(usuario.getId(), tokenEstado.getUsuarioId());
        assertNotNull(tokenEstado.getJtiVigente());
        assertEquals(1, tokenEstado.getVersionToken());
        assertNotNull(tokenEstado.getEmitidaEn());
        assertNotNull(tokenEstado.getExpiraEn());
    }

    /** Se verifica que al registrar un usuario se normalicen correctamente el nombre y el correo. */
    @Test
    void registerShouldNormalizeNameAndEmail() {
        authService.register("Ana", "ana@example.com", "clave123");

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase("ana@example.com")
                .orElseThrow();

        assertEquals("Ana", usuario.getNombre());
        assertEquals("ana@example.com", usuario.getCorreo());
    }

    /** Se verifica que no se permita registrar un usuario con un correo ya existente. */
    @Test
    void registerShouldRejectDuplicateEmail() {
        authService.register("Ana", "ana@example.com", "clave123");

        DuplicateUserException exception = assertThrows(
                DuplicateUserException.class,
                () -> authService.register("Maria", "ana@example.com", "clave456")
        );

        assertEquals(
                "Ya existe una cuenta con ese correo registrado",
                exception.getMessage()
        );
        // Aca estoy testeando por mensaje y CREO recordar algo de epers de que esto no estaba muy bien pero ahora
        // me entraron dudas, si queres borralo o si no, dejemoslo. Tengo pesima memoria y eso que era ayudante en
        // epers te pido perdon marcsos. Los test abajo de este tambien lo hacen :(

        assertEquals(1, usuarioRepository.count());
    }

    /** Se verifica que no se permita registrar un usuario sin un nombre válido. */
    @Test
    void registerShouldRejectBlankName() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.register("   ", "ana@example.com", "clave123")
        );

        assertEquals("VALIDACION_INVALIDA", exception.getCode());
        assertEquals("Nombre requerido", exception.getMessage());
    }

    /** Se verifica que no se permita registrar un usuario con un correo inválido. */
    @Test
    void registerShouldRejectInvalidEmail() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.register("Ana", "ana@", "clave123")
        );

        assertEquals("VALIDACION_INVALIDA", exception.getCode());
        assertEquals("Correo invalido", exception.getMessage());
    }

    /** Se verifica que no se permita registrar un usuario con una contraseña menor a ocho caracteres. */
    @Test
    void registerShouldRejectPasswordShorterThanEightCharacters() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.register("Ana", "ana@example.com", "abc1234")
        );

        assertEquals("VALIDACION_INVALIDA", exception.getCode());
        assertEquals("Password invalido", exception.getMessage());
    }

    /** Se verifica que no se permita registrar un usuario con una contraseña sin letras. */
    @Test
    void registerShouldRejectPasswordWithoutLetters() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.register("Ana", "ana@example.com", "12345678")
        );

        assertEquals("VALIDACION_INVALIDA", exception.getCode());
        assertEquals("Password invalido", exception.getMessage());
    }

    /** Se verifica que no se permita registrar un usuario con una contraseña sin números. */
    @Test
    void registerShouldRejectPasswordWithoutNumbers() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.register("Ana", "ana@example.com", "abcdefgh")
        );

        assertEquals("VALIDACION_INVALIDA", exception.getCode());
        assertEquals("Password invalido", exception.getMessage());
    }

    /** Se verifica que no se permita registrar un usuario con el nombre nulo. */
    @Test
    void registerShouldHandleNullName() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.register(null, "ana@example.com", "clave123")
        );

        assertEquals("VALIDACION_INVALIDA", exception.getCode());
        assertEquals("Nombre requerido", exception.getMessage());
    }
    /** Se verifica que no se permita registrar un usuario con el correo nulo. */
    @Test
    void registerShouldHandleNullEmail() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.register("Ana", null, "clave123")
        );

        assertEquals("VALIDACION_INVALIDA", exception.getCode());
        assertEquals("Correo invalido", exception.getMessage());
    }

    /** Se verifica que no se permita registrar un usuario con una contraseña nula. */
    @Test
    void registerShouldHandleNullPassword() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.register("Ana", "ana@example.com", null)
        );

        assertEquals("VALIDACION_INVALIDA", exception.getCode());
        assertEquals("Password invalido", exception.getMessage());
    }


    /** ----------------------------------------------------------------------------**/
    /** ------------------------------ TESTS DE LOGIN ------------------------------**/
    /** ----------------------------------------------------------------------------**/

    /** Se verifica que un usuario pueda iniciar sesión con credenciales válidas y obtener un token. */
    @Test
    void loginShouldReturnTokenForValidCredentials() {
        authService.register("Ana", "ana@example.com", "clave123");

        AuthResult response = authService.login("ana@example.com", "clave123");

        assertNotNull(response);
        assertNotNull(response.token());
        assertNotNull(response.usuario());
        assertEquals("Ana", response.usuario().getNombre());
        assertEquals("ana@example.com", response.usuario().getCorreo());
    }

    /** Se verifica que no se permita iniciar sesión con un correo que no existe. */
    @Test
    void loginShouldRejectUnknownEmail() {
        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login("unknown@example.com", "clave123")
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    /** Se verifica que no se permita iniciar sesión con una contraseña incorrecta. */
    @Test
    void loginShouldRejectInvalidPassword() {
        authService.register("Ana", "ana@example.com", "clave123");

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login("ana@example.com", "clave456")
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    /** Se verifica que al iniciar sesión se normalice correctamente el correo. */
    @Test
    void loginShouldNormalizeEmail() {
        authService.register("Ana", "ana@example.com", "clave123");

        LoginRequest loginRequest = new LoginRequest(" ANA@EXAMPLE.COM ", "clave123");

        AuthResult response = authService.login(" ANA@EXAMPLE.COM ", "clave123");

        assertNotNull(response);
        assertNotNull(response.token());
        assertEquals("ana@example.com", response.usuario().getCorreo());
    }

    /** Se verifica que al iniciar sesión nuevamente se actualice la versión del estado del token. */
    @Test
    void loginShouldUpdateExistingTokenState() {
        authService.register("Ana", "ana@example.com", "clave123");

        authService.login("ana@example.com", "clave123");

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase("ana@example.com")
                .orElseThrow();

        TokenEstado tokenEstado = tokenEstadoRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow();

        assertEquals(2, tokenEstado.getVersionToken());
    }

    /** Se verifica que al iniciar sesión se cree el estado del token cuando no existe. */
    @Test
    void loginShouldCreateTokenStateWhenItDoesNotExist() {
        Usuario usuario = usuarioRepository.save(
                new Usuario("Ana", "ana@example.com",
                passwordEncoder.encode("clave123"))
        );

        authService.login("ana@example.com", "clave123");

        TokenEstado tokenEstado = tokenEstadoRepository
                .findByUsuarioId(usuario.getId())
                .orElseThrow();

        assertEquals(usuario.getId(), tokenEstado.getUsuarioId());
        assertEquals(1, tokenEstado.getVersionToken());
        assertNotNull(tokenEstado.getJtiVigente());
        assertNotNull(tokenEstado.getEmitidaEn());
        assertNotNull(tokenEstado.getExpiraEn());
    }
}
