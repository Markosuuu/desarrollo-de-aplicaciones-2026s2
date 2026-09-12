# Implementation Plan: Autenticacion de usuarios y catalogo de jugadores

**Branch**: `002-user-auth-player-catalog` | **Date**: 2026-09-12 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-user-auth-player-catalog/spec.md`

## Summary

La primera entrega permitirá registrar usuarios, iniciar sesión y consultar un catálogo público de jugadores de cinco ligas. El backend expondrá contratos HTTP estables, almacenará usuarios, estado JWT y jugadores en PostgreSQL, y mantendrá un solo JWT vigente por usuario. La actualización semanal del catálogo se aislará detrás de un servicio de importación para usar fixtures deterministas en pruebas y conservar la última versión válida ante fallos externos.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 4.x más reciente disponible, Spring Web, Spring Validation, Spring Data JPA, PostgreSQL driver, Gradle

**Storage**: PostgreSQL para ejecución local y Testcontainers PostgreSQL para pruebas

**Testing**: JUnit 5, Spring Boot Test, MockMvc y Testcontainers

**Target Platform**: Servicio web en ambiente local Linux/Docker Desktop; frontend web local

**Project Type**: web-service con frontend web

**Constraints**: Catálogo público; endpoints protegidos mediante `Authorization: Bearer`; un solo JWT vigente por usuario; invalidación inmediata del JWT anterior; mensajes visibles en español; secretos nunca se almacenan en texto plano; datos externos pueden fallar sin invalidar la última versión válida

**Scale/Scope**: Hasta 10.000 jugadores, cinco ligas, tres flujos principales y una actualización semanal; compras, ventas, portfolio, ranking y cotizaciones dinámicas quedan fuera

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] Arquitectura en capas: controllers solo delegan en services; services coordinan modelos y persistencia.
- [x] Lógica de negocio en modelos: invariantes de Usuario y Jugador viven en objetos de dominio; JWT queda fuera del dominio y se trata como credencial de infraestructura.
- [x] Validación por nivel: DTOs validan forma y sanitización; services validan existencia; modelos validan invariantes.
- [x] Tests no negociables: unitarios de dominio, integración/repositorios con Testcontainers y E2E con MockMvc, incluyendo éxito, borde y excepciones.
- [x] Definición de terminado: backend/frontend compilables, aplicación levantable y colección Postman documentada en tareas de implementación.
- [x] Documentación: cada clase y método tendrá documentación de responsabilidad y funcionamiento.
- [x] Idioma: mensajes y documentación en español; identificadores de código sin acentos ni ñ.

Resultado pre-Phase 0: PASS. No se identifican violaciones que requieran Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/002-user-auth-player-catalog/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
backend/
├── src/
│   ├── main/java/.../models/
│   ├── main/java/.../authentication/
│   ├── main/java/.../services/
│   ├── main/java/.../controllers/dtos/
│   ├── main/java/.../repository/
│   └── test/java/.../
│       ├── models/
│       ├── services/
│       ├── repository/
│       └── controllers/
├── build.gradle
├── settings.gradle
└── .env.example

frontend/
├── src/
│   ├── api/
│   ├── styles/
│   ├── pages/
│   └── components/
└── tests/
```

**Structure Decision**: Se adopta un repositorio con `backend/` Gradle y `frontend/` separado. El backend respeta la separación `models`, `services`, `controllers/dtos` y `repository` indicada por la documentación de la entrega; los tests se separan por responsabilidad. El frontend usa `api`, `styles`, `pages` y `components` como exige la constitución.

## Complexity Tracking

No aplica: el diseño pasa la Constitution Check sin violaciones justificables.
