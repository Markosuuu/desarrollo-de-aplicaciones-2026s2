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

## Contexto tecnico

**Lenguaje de programación/Version**: [Java 21, Spring Boot 4.x, usá la versión más reciente de Spring Boot 4.x, gradle]

**Almacenamiento**: [PostgreSQL]

**Testing**: [JUnit 5, Testcontainers, Spring Boot Test]

**Plataforma objetivo**: [Ambiente de desarrollo local, Docker Desktop]

**TIpo de proyecto**: [web-service]


## Estructura de proyecto

### Documentación (de esta feature)

```text
specs/[###-feature]/
├── plan.md              # Este archivo (/speckit-plan output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - no creado por /speckit-plan)
```

### Codigo fuente (repository root)

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


## Traqueo de complejidad

> **Completar solo si la validacion de la constitution tiene violaciones que deben ser justificadas**

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [e.g., 4th project] | [current need] | [why 3 projects insufficient] |
| [e.g., Repository pattern] | [specific problem] | [why direct DB access insufficient] |
