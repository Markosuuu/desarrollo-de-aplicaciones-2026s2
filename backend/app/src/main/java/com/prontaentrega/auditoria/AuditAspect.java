package com.prontaentrega.auditoria;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class AuditAspect {

    private static final Logger AUDIT = LoggerFactory.getLogger("AUDIT");

    @Around("@annotation(auditable)")
    public Object auditar(ProceedingJoinPoint pjp, Auditable auditable) throws Throwable {
        long inicio = System.currentTimeMillis();
        String resultado = "OK";
        try {
            return pjp.proceed();
        } catch (Throwable t) {
            resultado = "ERROR: " + t.getClass().getSimpleName();
            throw t;
        } finally {
            long duracion = System.currentTimeMillis() - inicio;
            String operacion = auditable.value().isBlank()
                    ? pjp.getSignature().toShortString()
                    : auditable.value();

            AUDIT.info("user={} operacion={} parametros={} tiempoMs={} resultado={}",
                    obtenerUsuario(),
                    operacion,
                    Arrays.toString(pjp.getArgs()),
                    duracion,
                    resultado);
        }
    }

    private String obtenerUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonimo";
    }
}
