package com.prontaentrega.services;

import com.prontaentrega.controllers.dtos.AuthResponse;
import com.prontaentrega.controllers.dtos.LoginRequest;
import com.prontaentrega.controllers.dtos.RegisterRequest;
import com.prontaentrega.models.TokenEstado;
import com.prontaentrega.models.Usuario;
import com.prontaentrega.repository.TokenEstadoRepository;
import com.prontaentrega.repository.UsuarioRepository;
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
        RegisterRequest request = new RegisterRequest("Ana", "ana@example.com", "clave123");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.token());
        assertNotNull(response.usuario());
        assertEquals("Ana", response.usuario().nombre());
        assertEquals("ana@example.com", response.usuario().correo());
    }

    /** Se verifica que al registrar un usuario se persista correctamente en la base de datos.*/
    @Test
    void registerShouldPersistUserAndTokenState() {
        RegisterRequest request = new RegisterRequest("Ana", "ana@example.com", "clave123");

        authService.register(request);

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
        RegisterRequest request = new RegisterRequest("  Ana  ", " ANA@EXAMPLE.COM ", "clave123");

        authService.register(request);

        Usuario usuario = usuarioRepository
                .findByCorreoIgnoreCase("ana@example.com")
                .orElseThrow();

        assertEquals("Ana", usuario.getNombre());
        assertEquals("ana@example.com", usuario.getCorreo());
    }

    /** Se verifica que no se permita registrar un usuario con un correo ya existente. */
    @Test
    void registerShouldRejectDuplicateEmail() {
        RegisterRequest firstRequest = new RegisterRequest("Ana", "ana@example.com", "clave123"
        );

        authService.register(firstRequest);

        RegisterRequest secondRequest = new RegisterRequest("Maria", "ana@example.com", "clave456"
        );

        DuplicateUserException exception = assertThrows(
                DuplicateUserException.class,
                () -> authService.register(secondRequest)
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
        RegisterRequest request = new RegisterRequest("   ", "ana@example.com", "clave123");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.register(request)
        );

        assertEquals("VALIDACION_INVALIDA", exception.getCode());
        assertEquals("Nombre requerido", exception.getMessage());
    }

    /** Se verifica que no se permita registrar un usuario con un correo inválido. */
    @Test
    void registerShouldRejectInvalidEmail() {
        RegisterRequest request = new RegisterRequest("Ana", "ana@", "clave123");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.register(request)
        );

        assertEquals("VALIDACION_INVALIDA", exception.getCode());
        assertEquals("Correo invalido", exception.getMessage());
    }

    /** Se verifica que no se permita registrar un usuario con una contraseña menor a ocho caracteres. */
    @Test
    void registerShouldRejectPasswordShorterThanEightCharacters() {
        RegisterRequest request = new RegisterRequest("Ana", "ana@example.com", "abc1234");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.register(request)
        );

        assertEquals("VALIDACION_INVALIDA", exception.getCode());
        assertEquals("Password invalido", exception.getMessage());
    }

    /** Se verifica que no se permita registrar un usuario con una contraseña sin letras. */
    @Test
    void registerShouldRejectPasswordWithoutLetters() {
        RegisterRequest request = new RegisterRequest("Ana", "ana@example.com", "12345678");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.register(request)
        );

        assertEquals("VALIDACION_INVALIDA", exception.getCode());
        assertEquals("Password invalido", exception.getMessage());
    }

    /** Se verifica que no se permita registrar un usuario con una contraseña sin números. */
    @Test
    void registerShouldRejectPasswordWithoutNumbers() {
        RegisterRequest request = new RegisterRequest("Ana", "ana@example.com", "abcdefgh");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.register(request)
        );

        assertEquals("VALIDACION_INVALIDA", exception.getCode());
        assertEquals("Password invalido", exception.getMessage());
    }

    /** Se verifica que no se permita registrar un usuario con el nombre nulo. */
    @Test
    void registerShouldHandleNullName() {
        RegisterRequest request = new RegisterRequest(null, "ana@example.com", "clave123");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.register(request)
        );

        assertEquals("VALIDACION_INVALIDA", exception.getCode());
        assertEquals("Nombre requerido", exception.getMessage());
    }
    /** Se verifica que no se permita registrar un usuario con el correo nulo. */
    @Test
    void registerShouldHandleNullEmail() {
        RegisterRequest request = new RegisterRequest("Ana", null, "clave123");

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.register(request)
        );

        assertEquals("VALIDACION_INVALIDA", exception.getCode());
        assertEquals("Correo invalido", exception.getMessage());
    }

    /** Se verifica que no se permita registrar un usuario con una contraseña nula. */
    @Test
    void registerShouldHandleNullPassword() {
        RegisterRequest request = new RegisterRequest("Ana", "ana@example.com", null);

        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> authService.register(request)
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
        RegisterRequest registerRequest = new RegisterRequest("Ana", "ana@example.com", "clave123");

        authService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest("ana@example.com", "clave123");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.token());
        assertNotNull(response.usuario());
        assertEquals("Ana", response.usuario().nombre());
        assertEquals("ana@example.com", response.usuario().correo());
    }

    /** Se verifica que no se permita iniciar sesión con un correo que no existe. */
    @Test
    void loginShouldRejectUnknownEmail() {
        LoginRequest loginRequest = new LoginRequest("unknown@example.com", "clave123");

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    /** Se verifica que no se permita iniciar sesión con una contraseña incorrecta. */
    @Test
    void loginShouldRejectInvalidPassword() {
        RegisterRequest registerRequest = new RegisterRequest("Ana", "ana@example.com", "clave123");

        authService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest("ana@example.com", "clave456");

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals("Invalid credentials", exception.getMessage());
    }

    /** Se verifica que al iniciar sesión se normalice correctamente el correo. */
    @Test
    void loginShouldNormalizeEmail() {
        RegisterRequest registerRequest = new RegisterRequest("Ana", "ana@example.com", "clave123");

        authService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest(" ANA@EXAMPLE.COM ", "clave123");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertNotNull(response.token());
        assertEquals("ana@example.com", response.usuario().correo());
    }

    /** Se verifica que al iniciar sesión nuevamente se actualice la versión del estado del token. */
    @Test
    void loginShouldUpdateExistingTokenState() {
        RegisterRequest registerRequest = new RegisterRequest("Ana", "ana@example.com", "clave123");

        authService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest("ana@example.com", "clave123");

        authService.login(loginRequest);

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

        LoginRequest loginRequest = new LoginRequest("ana@example.com", "clave123");

        authService.login(loginRequest);

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
