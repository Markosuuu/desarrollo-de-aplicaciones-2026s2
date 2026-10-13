package com.prontaentrega.authentication;
import com.prontaentrega.services.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    /*@Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equals(request.getMethod())
                && "/api/players/update".equals(request.getServletPath()));
    }*/

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !("POST".equals(request.getMethod())
                && request.getRequestURI().equals("/api/players/update"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        System.out.println("=== API KEY FILTER: doFilterInternal ===");
        System.out.println("Método: " + request.getMethod());
        System.out.println("Ruta: " + request.getServletPath());

        String apiKey = request.getHeader("X-API-Key");

        System.out.println("ApiKeyAuthenticationFilter ejecutado");
        System.out.println("API Key recibida: " + (apiKey != null));
        System.out.println("API Key válida: " + apiKeyService.validar(apiKey));

        if (!apiKeyService.validar(apiKey)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(
                    "{\"error\":{\"code\":\"API_KEY_INVALIDA\","
                            + "\"message\":\"API Key ausente o invalida\"}}"
            );
            return;
        }

        var authentication = new UsernamePasswordAuthenticationToken(
                "API_KEY",
                null,
                List.of()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
