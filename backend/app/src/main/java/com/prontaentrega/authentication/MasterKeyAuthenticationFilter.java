package com.prontaentrega.authentication;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Autentica con X-Master-Key únicamente las escrituras administrativas habilitadas. */
@Component
public class MasterKeyAuthenticationFilter extends OncePerRequestFilter {
    private final byte[] configuredMasterKey;

    /** Toma la clave configurada desde el entorno, sin persistirla ni exponerla. */
    public MasterKeyAuthenticationFilter(@Value("${api-key.master-secret}") String masterSecret) {
        this.configuredMasterKey = masterSecret.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    /** Omite todas las rutas excepto los endpoints que requieran validacion de api key maestra */
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !requiereClaveMaestra(request.getMethod(), request.getRequestURI());
    }

    /** Indica si el método/ruta pertenece a una operación protegida por master key. */
    private boolean requiereClaveMaestra(String method, String path) {
        if ("POST".equals(method) && (path.equals("/quotes/recalculate")
                || path.equals("/api/quotes/recalculate") || path.equals("/api/keys"))) {
            return true;
        }
        if (!"PUT".equals(method)) {
            return false;
        }
        return path.equals("/quotes/strategy") || path.equals("/api/quotes/strategy")
                || path.matches("^/(api/)?quotes/strategies/[^/]+/config$");
    }

    @Override
    /** Valida la cabecera maestra y establece la autenticación para la petición autorizada. */
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String suppliedKey = request.getHeader("X-Master-Key");
        if (suppliedKey == null || !MessageDigest.isEqual(configuredMasterKey,
                suppliedKey.getBytes(StandardCharsets.UTF_8))) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write(
                    "{\"code\":\"CLAVE_MAESTRA_INVALIDA\","
                            + "\"message\":\"Clave maestra ausente o inválida\"}");
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("MASTER_KEY", null, List.of()));
        filterChain.doFilter(request, response);
    }
}