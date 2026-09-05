# PLAN

## Precondiciones

Docker Desktop esta corriendo (necesario para Testcontainers).

PostgreSQL corriendo en jdbc:postgresql://localhost:5432/

La base prontaentregadevapp no existe, creala.

## PRE-Requerimientos

Todavia no hay proyecto: crea el proyecto de 0 con las pautas de arquitectura prestablecidas en la constitucion.

## Configuración

Perfil local para levantar la app. Las credenciales dejalas como constantes y luego mostrame un .env.example

user: postgres / password: root

Los tests NO usan este perfil: van contra Testcontainers, siempre.

## Technical Context

**Language/Version**: [Java 21, Spring Boot 4.x, usá la versión más reciente de Spring Boot 4.x, gradle]

**Storage**: [PostgreSQL]

**Testing**: [JUnit 5, Testcontainers, Spring Boot Test]

**Target Platform**: [Local development environment, Docker Desktop]

**Project Type**: [web-service]

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

[Gates determined based on constitution file]

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

Este es una estructura que deberías respetar pero puedes crear los directorios que creas convenientes, consultando primero
Los DTOs solo se usan en la capa de controllers.
services se no usa dtos, sino entidades del modelo.

```text
backend/
├── src/
│   ├── models/
│   ├── services/
│   ├── controllers/
│   │   └── dtos/
│   └── repository/
└── tests/

frontend/
├── src/
├── components/
├── pages/
├── services/
└── styles/
```

**Structure Decision**: [Document the selected structure and reference the real
directories captured above]

## Complexity Tracking

> **Fill ONLY if Constitution Check has violations that must be justified**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
