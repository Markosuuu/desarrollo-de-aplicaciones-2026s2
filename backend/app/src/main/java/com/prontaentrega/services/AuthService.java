package com.prontaentrega.services;

import com.prontaentrega.authentication.JwtService;
import com.prontaentrega.models.TokenEstado;
import com.prontaentrega.models.Usuario;
import com.prontaentrega.repository.TokenEstadoRepository;
import com.prontaentrega.repository.UsuarioRepository;
import com.prontaentrega.services.dto.AuthResult;
import com.prontaentrega.services.exceptions.DuplicateUserException;
import com.prontaentrega.services.exceptions.ValidationException;
import io.jsonwebtoken.Claims;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;
    private final TokenEstadoRepository tokenEstadoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       TokenEstadoRepository tokenEstadoRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.tokenEstadoRepository = tokenEstadoRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    /**
     * Registra un nuevo usuario en el sistema.
     * Valida el nombre, correo y contraseña proporcionados.
     * Si el correo ya está registrado, lanza una excepción de usuario duplicado.
     * Si la validación es exitosa, guarda el usuario en la base de datos, genera un token JWT y persiste el estado del token.
     *
     * @param nombre Nombre del usuario a registrar.
     * @param correo Correo electrónico del usuario a registrar.
     * @param password Contraseña del usuario a registrar.
     * @return Un objeto AuthResult que contiene los detalles del usuario registrado y el token JWT generado.
     * @throws ValidationException Si el nombre, correo o contraseña no cumplen con los criterios de validación.
     * @throws DuplicateUserException Si ya existe un usuario registrado con el mismo correo.
     */
    @Transactional
    public AuthResult register(String nombre, String correo, String password) {
        String n = normalizeName(nombre);
        String c = normalizeEmail(correo);
        String p = password;

        validateNombre(n);
        validateCorreo(c);
        validatePassword(p);

        if (usuarioRepository.existsByCorreoIgnoreCase(c)) {
            throw new DuplicateUserException("Ya existe una cuenta con ese correo registrado");
        }

        Usuario usuario = new Usuario(n, c, passwordEncoder.encode(p));
        Usuario saved = usuarioRepository.save(usuario);
        String token = jwtService.generateToken(saved.getId(), saved.getCorreo());
        persistTokenState(saved.getId(), token);
        return new AuthResult(saved, token);
    }

    /**
     * Inicia sesión para un usuario existente.
     * Valida el correo y la contraseña proporcionados.
     * Si las credenciales son válidas, genera un token JWT y persiste el estado del token.
     * Si las credenciales son inválidas, lanza una excepción de credenciales incorrectas.
     *
     * @param correo Correo electrónico del usuario que intenta iniciar sesión.
     * @param password Contraseña del usuario que intenta iniciar sesión.
     * @return Un objeto AuthResult que contiene los detalles del usuario autenticado y el token JWT generado.
     * @throws BadCredentialsException Si las credenciales proporcionadas son inválidas.
     */
    @Transactional
    public AuthResult login(String correo, String password) {
        String c = normalizeEmail(correo);
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(c)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(usuario.getId(), usuario.getCorreo());
        persistTokenState(usuario.getId(), token);
        return new AuthResult(usuario, token);
    }

    private void persistTokenState(UUID usuarioId, String token) {
        Claims claims = jwtService.parseToken(token);
        TokenEstado tokenEstado = tokenEstadoRepository.findByUsuarioId(usuarioId)
                .orElse(new TokenEstado());
        tokenEstado.setUsuarioId(usuarioId);
        tokenEstado.setJtiVigente(UUID.fromString(claims.getId()));
        tokenEstado.setVersionToken((tokenEstado.getVersionToken() == null ? 0 : tokenEstado.getVersionToken()) + 1);
        tokenEstado.setEmitidaEn(LocalDateTime.now());
        tokenEstado.setExpiraEn(LocalDateTime.now().plusMinutes(60));
        tokenEstadoRepository.save(tokenEstado);
    }

    private void validateNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ValidationException("VALIDACION_INVALIDA", "Nombre requerido");
        }
    }

    private void validateCorreo(String correo) {
        if (correo == null || !correo.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ValidationException("VALIDACION_INVALIDA", "Correo invalido");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8 || !password.matches(".*[A-Za-z].*") || !password.matches(".*\\d.*")) {
            throw new ValidationException("VALIDACION_INVALIDA", "Password invalido");
        }
    }

    private String normalizeName(String nombre) {
        return nombre == null ? null : nombre.trim();
    }

    private String normalizeEmail(String correo) {
        return correo == null ? null : correo.trim().toLowerCase();
    }
}
