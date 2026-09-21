# Plan de implementación: flujo de autenticación y sesión para el sitio dinámico

**Branch**: `003-auth-flow` | **Fecha**: 2026-09-20 | **Especificación**: `specs/003-auth-flow/spec.md`

**Entrada**: Especificación de la funcionalidad desde `/specs/003-auth-flow/spec.md`

## Resumen

Esta funcionalidad agrega un flujo completo de autenticación para el sitio dinámico: registro de usuario, inicio de sesión, acceso protegido a home, cierre de sesión, reglas de redirección de sesión y un cambio de tema claro/oscuro. La implementación encaja con el backend actual en Spring Boot, que ya ofrece autenticación JWT, hash de contraseñas con BCrypt y reglas de seguridad, mientras mantiene la experiencia del frontend alineada con la constitución del proyecto: textos visibles en español, validaciones claras y responsabilidades separadas por capas.

## Contexto técnico

**Lenguaje/Versión**: Java 21

**Dependencias principales**: Spring Boot 3.3.3, Spring Web, Spring Data JPA, Spring Validation, Spring Security, JWT (jjwt), PostgreSQL, H2, Yup para validación de formularios, Toastify para notificaciones de error

**Almacenamiento**: PostgreSQL para persistencia en ejecución; H2 para ejecución local y pruebas

**Pruebas**: Spring Boot test starter, Spring Security test, JUnit 5, cobertura de controladores e integración

**Plataforma objetivo**: aplicación web basada en navegador servida por el backend de Spring Boot con una capa frontend bajo `frontend/`

**Tipo de proyecto**: aplicación web

**Objetivos de rendimiento**: el inicio de sesión, el registro y la verificación de rutas protegidas deben completarse dentro de un tiempo de respuesta típico del usuario; el flujo de autenticación debe mantenerse ágil para volúmenes pequeños y validaciones locales de baja latencia

**Restricciones**: los mensajes visibles y las validaciones deben estar en español; la autenticación debe aplicar reglas de redirección para usuarios autenticados y no autenticados; no existe recuperación de contraseña en este alcance; la preferencia de tema es solo visual y no se persiste en base de datos, debe gestionarse con ThemeContext y/o clases de Tailwind; los errores de formulario se mostrarán con Toastify y la validación de strings/correos con Yup

**Escala/alcance**: MVP para una base pequeña de usuarios, autenticación básica y una sola vista protegida; no existe un modelo de permisos por roles en esta iteración

## Revisión de la constitución

*GATE: debe pasar antes de la investigación de la fase 0. También se vuelve a verificar después del diseño de la fase 1.*

- Arquitectura en capas: APROBADO. El backend actual ya está organizado en capas de controlador, servicio, modelo, repositorio, configuración y seguridad. La funcionalidad debe conservar esta separación y mantener las reglas de negocio en la capa de servicio/modelo.
- Lógica de negocio solo en modelo/servicio: APROBADO. El registro y la validación de credenciales ya están centralizados en `AuthService`; la lógica solo de la interfaz no debe duplicar reglas de negocio.
- Validación en los niveles correctos: APROBADO. Se debe usar validación de requests en DTOs y controladores, validación de negocio en la capa de servicio y control de rutas/sesión en la configuración de seguridad y guardas del frontend.
- Pruebas requeridas: APROBADO. Esta funcionalidad necesita pruebas de controlador y validación de requests, cubriendo tanto el flujo feliz como casos borde (correo inválido, contraseña faltante, redirección de usuario autenticado y cierre de sesión).
- Definición de terminado: APROBADO. La funcionalidad debe incluir pruebas de validación, ejecución local correcta y un flujo claro de login/registro/home/logout.
- Documentación e idioma: APROBADO. Los textos de la interfaz, los mensajes de validación y la documentación deben permanecer en español; los identificadores técnicos pueden mantenerse en inglés solo cuando lo exija el código base.

## Estructura del proyecto

### Documentación (esta funcionalidad)

```text
specs/003-auth-flow/
├── plan.md              # Este archivo (salida del comando /speckit-plan)
├── research.md          # Salida de la fase 0 (/speckit-plan)
├── data-model.md        # Salida de la fase 1 (/speckit-plan)
├── quickstart.md        # Salida de la fase 1 (/speckit-plan)
├── contracts/           # Salida de la fase 1 (/speckit-plan)
├── checklists/
│   └── requirements.md
└── tasks.md             # Salida de la fase 2 (/speckit-tasks; NO la crea /speckit-plan)
```

### Código fuente (raíz del repositorio)

```text
backend/
├── app/
│   ├── src/main/java/com/prontaentrega/
│   │   ├── authentication/
│   │   ├── config/
│   │   ├── controllers/
│   │   ├── models/
│   │   ├── repository/
│   │   └── services/
│   └── src/test/java/com/prontaentrega/
├── settings.gradle
└── gradlew

frontend/
├── src/
│   ├── api/
│   ├── components/
│   ├── pages/
│   ├── styles/
│   └── utils/
└── tests/
```

**Decisión de estructura**: usar la aplicación Java/Spring Boot existente como fuente de verdad del backend para la lógica de autenticación, persistencia y validación JWT. El área de frontend se organizará en `api`, `components`, `pages` y `styles`, siguiendo la constitución del proyecto, con el flujo de autenticación implementado en la capa del navegador pero sin duplicar reglas de negocio.

## Seguimiento de complejidad

No se identificaron violaciones a la constitución. Este plan se mantiene dentro de la arquitectura por capas y de las prácticas de validación requeridas.

## Fase 0: resumen de investigación

- El proyecto ya contiene autenticación JWT, codificación de contraseñas con BCrypt y una base de rutas protegidas bajo `backend/app`.
- La funcionalidad debe extender el contrato existente de `AuthController` / `AuthService` y no introducir una arquitectura de autenticación separada.
- Un modelo de usuarios persistidos es el valor predeterminado correcto para esta funcionalidad porque la especificación requiere login, registro y flujo seguro de redirección.
- El frontend debe validar formularios con Yup y mostrar errores por Toastify; el cambio de tema debe gestionarse como estado visual sin crear una entidad persistente, usando ThemeContext y/o clases de Tailwind.

## Fase 1: artefactos de diseño

Los siguientes artefactos se crearán en este directorio de funcionalidad:

- `research.md`: decisiones, fundamentos y alternativas
- `data-model.md`: entidades y reglas de validación
- `contracts/auth-api.md`: contrato público de autenticación
- `quickstart.md`: pasos de validación local
