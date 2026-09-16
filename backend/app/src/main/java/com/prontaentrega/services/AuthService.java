package com.prontaentrega.services;

import com.prontaentrega.authentication.JwtService;
import com.prontaentrega.controllers.dtos.AuthResponse;
import com.prontaentrega.controllers.dtos.LoginRequest;
import com.prontaentrega.controllers.dtos.RegisterRequest;
import com.prontaentrega.controllers.dtos.UsuarioResponse;
import com.prontaentrega.models.TokenEstado;
import com.prontaentrega.models.Usuario;
import com.prontaentrega.repository.TokenEstadoRepository;
import com.prontaentrega.repository.UsuarioRepository;
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

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String nombre = normalizeName(request.nombre());
        String correo = normalizeEmail(request.correo());
        String password = request.password();

        validateNombre(nombre);
        validateCorreo(correo);
        validatePassword(password);

        if (usuarioRepository.existsByCorreoIgnoreCase(correo)) {
            throw new DuplicateUserException("La cuenta ya existe");
        }

        Usuario usuario = new Usuario(nombre, correo, passwordEncoder.encode(password));
        Usuario saved = usuarioRepository.save(usuario);
        String token = jwtService.generateToken(saved.getId(), saved.getCorreo());
        persistTokenState(saved.getId(), token);
        return new AuthResponse(UsuarioResponse.from(saved), token);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String correo = normalizeEmail(request.correo());
        Usuario usuario = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(request.password(), usuario.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }

        String token = jwtService.generateToken(usuario.getId(), usuario.getCorreo());
        persistTokenState(usuario.getId(), token);
        return new AuthResponse(UsuarioResponse.from(usuario), token);
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
