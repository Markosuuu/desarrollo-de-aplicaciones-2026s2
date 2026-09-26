# Implementation Plan: Listado de jugadores en Home

**Branch**: `005-home-player-grid` | **Date**: 2026-09-26 | **Spec**: `specs/005-home-player-grid/spec.md`

**Input**: Feature specification from `/specs/005-home-player-grid/spec.md`

## Summary

Esta funcionalidad agrega al Home un catálogo paginado de jugadores con búsqueda por nombre, equipo o liga y tarjetas con nombre, equipo, liga y posición. La implementación reutiliza el endpoint existente de catálogo del backend (`GET /api/players`) y la estructura actual del frontend para mantener la separación por capas y evitar duplicar lógica de negocio.

## Technical Context

**Language/Version**: Java 21 para backend; JavaScript (React + Vite) para frontend.

**Primary Dependencies**: Spring Boot, Spring Data JPA, Spring Security, React Router, Vite, fetch nativo del navegador.

**Storage**: PostgreSQL/H2 a través de la capa de persistencia existente del backend.

**Testing**: JUnit 5 y pruebas Spring Boot del backend; validación manual del flujo en navegador para la vista del Home y la paginación.

**Target Platform**: aplicación web en navegador.

**Project Type**: web application.

**Performance Goals**: páginas de 20 jugadores como máximo, respuesta de búsqueda inmediata en la UI y navegación sin recarga completa.

**Constraints**: la interfaz debe mantenerse en español; la vista no debe romper la experiencia actual de Home; la paginación y el filtrado deben respetar el contrato de datos existente; si no hay resultados, debe haber un estado explícito de vacío.

**Scale/Scope**: catálogo de jugadores persistidos en base de datos con soporte para filtrado y navegación por páginas sin requerir cambios estructurales en la arquitectura.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Arquitectura en capas: APROBADO. La funcionalidad se integra con la capa de backend existente y con la organización del frontend en `api`, `pages` y `styles`.
- Lógica de negocio solo en modelo/servicio: APROBADO. La lógica de búsqueda y paginación ya existe en el servicio del backend, por lo que la UI solo consume y presenta resultados.
- Validación en los niveles correctos: APROBADO. La validación del filtro se realiza con el backend y la UI maneja estados vacíos y búsqueda del usuario sin duplicar reglas de dominio.
- Pruebas requeridas: APROBADO. El requisito se valida con pruebas de integración del backend y con verificación funcional del flujo de Home en el navegador.
- Definición de terminado: APROBADO. La entrega requiere que el catálogo se muestre, filtre y pagine correctamente, con datos reales desde la base.
- Documentación e idioma: APROBADO. Todo el texto visible y la documentación asociada deben mantenerse en español.

## Project Structure

### Documentation (this feature)

```text
specs/005-home-player-grid/
├── plan.md              # Este archivo
├── research.md          # Decisiones de diseño y alternativas
├── data-model.md        # Entidades y contratos de datos
├── quickstart.md        # Guía de validación funcional
├── contracts/           # Contratos públicos del feature
├── checklists/
│   └── requirements.md
└── spec.md              # Especificación funcional principal
```

### Source Code (repository root)

```text
backend/
├── app/
│   ├── src/main/java/com/prontaentrega/
│   │   ├── controllers/
│   │   ├── models/
│   │   ├── repository/
│   │   ├── services/
│   │   └── config/
│   └── src/test/java/com/prontaentrega/
└── gradlew

frontend/
├── src/
│   ├── api/
│   ├── components/
│   ├── context/
│   ├── pages/
│   ├── styles/
│   └── utils/
└── package.json
```

**Structure Decision**: se reutiliza la arquitectura ya existente con backend en `backend/app` y frontend en `frontend/src`. La vista de Home se integrará en `frontend/src/pages/HomePage.jsx`, los datos se consumen desde `frontend/src/api` y la visualización del catálogo se encapsula en el componente o bloque de tarjetas de la página sin crear nuevas capas de negocio fuera de la estructura actual.

## Complexity Tracking

No se registran violaciones a la constitución ni decisiones que requieran justificación adicional.
