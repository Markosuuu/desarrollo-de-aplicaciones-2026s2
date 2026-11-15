package com.prontaentrega.services;

import com.prontaentrega.authentication.JwtService;
import com.prontaentrega.models.auth.TokenEstado;
import com.prontaentrega.models.auth.Usuario;
import com.prontaentrega.persistence.repository.TokenEstadoRepository;
import com.prontaentrega.persistence.repository.UsuarioRepository;
import com.prontaentrega.services.dto.AuthResult;
import com.prontaentrega.services.exceptions.*;
import com.prontaentrega.services.exceptions.auth.*;
import io.jsonwebtoken.Claims;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
     * @throws UsuarioRepetidoException Si ya existe un usuario registrado con el mismo correo.
     */
    @Transactional
    public AuthResult register(String nombre, String correo, String password) {
        String nombreUser = normalizeName(nombre);
        String correoUser = normalizeEmail(correo);

        validarNombre(nombreUser);
        validarCorreo(correoUser);
        validarPassword(password);

        Usuario usuario = new Usuario(nombreUser, correoUser, passwordEncoder.encode(password));
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
        String correoUser = normalizeEmail(correo);
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correoUser)
                .orElseThrow(() -> new CorreoNoExistenteException("No existe el correo: " + correo));

        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            throw new ContraseniaEquivocadaException("La contraseña no coincide");
        }

        String token = jwtService.generateToken(usuario.getId(), usuario.getCorreo());
        persistTokenState(usuario.getId(), token);
        return new AuthResult(usuario, token);
    }

    private void persistTokenState(UUID usuarioId, String token) {
        Claims claims = jwtService.parseToken(token);
        TokenEstado tokenEstado = tokenEstadoRepository.findByUsuarioId(usuarioId)
                .orElse(new TokenEstado());

        tokenEstado.actualizarToken(usuarioId, claims);
        tokenEstadoRepository.save(tokenEstado);
    }


    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]{1,64}@(?:[A-Za-z0-9-]{1,63}\\.){1,10}[A-Za-z]{2,63}$");

    private void validarNombre(String nombre) {
        if (!StringUtils.hasText(nombre)) {
            throw new CampoRequeridoException("Nombre requerido");
        }
    }

    private void validarCorreo(String correo) {
        if (!StringUtils.hasText(correo)) {
            throw new CampoRequeridoException("Correo requerido");
        }

        if (correo.length() > 254 || !EMAIL_PATTERN.matcher(correo).matches()) {
            throw new CampoInvalidoException("Correo inválido");
        }

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new UsuarioRepetidoException("Ya existe una cuenta con ese correo registrado");
        }
    }

    private void validarPassword(String password) {
        if (!StringUtils.hasText(password)) {
            throw new CampoRequeridoException("Contraseña requerida");
        }

        if (password.length() < 8 || password.length() > 72
                || password.chars().noneMatch(Character::isLetter)
                || password.chars().noneMatch(Character::isDigit)) {
            throw new CampoInvalidoException("Contraseña inválida");
        }
    }

    private String normalizeName(String nombre) {
        return nombre == null ? null : nombre.trim();
    }

    private String normalizeEmail(String correo) {
        return correo == null ? null : correo.trim().toLowerCase();
    }
}
