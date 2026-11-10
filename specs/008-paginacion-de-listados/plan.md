# Implementation Plan: Paginación de listados del usuario y mercado

**Branch**: `[008-paginacion-de-listados]` | **Date**: 2026-11-08 | **Spec**: `spec.md`

**Input**: Feature specification from `/specs/008-paginacion-de-listados/spec.md`

## Summary

Esta feature agrega paginación y estados vacíos a los listados clave de la aplicación: inventario del usuario, historial de operaciones, mercado y subastas. La implementación se apoya en la arquitectura existente de Spring Boot + React, reutiliza `Page` y `Pageable` del backend y encapsula la navegación con componentes compatibles en frontend para evitar lógica duplicada.

## Technical Context

**Language/Version**: Java 21 en backend; JavaScript (React + Vite) en frontend.

**Primary Dependencies**: Spring Boot, Spring Web, Spring Data JPA, React, Vite, JUnit 5 y fetch nativo del navegador.

**Storage**: PostgreSQL a través de repositorios JPA/Spring Data; los listados se obtienen mediante `PageRequest` y `Page`.

**Testing**: JUnit 5, pruebas de integración de Spring Boot y tests de frontend con React Testing Library/Vitest.

**Target Platform**: aplicación web.

**Project Type**: web application.

**Performance Goals**: responder listados paginados en bloques pequeños (10 para inventario/historial/subastas, 20 para mercado) sin degradación visible para usuarios normales.

**Constraints**: todo el texto visible debe estar en español; los controles de navegación deben bloquearse en los extremos; la carga incremental de subastas no puede repetir llamadas cuando no hay más contenido; el backend debe validar rangos de página y tamaño de bloque.

**Scale/Scope**: varias vistas de consulta, usuarios concurrentes, listados con más de 10/20 elementos y mensajes de estado para listas vacías o sin más contenido.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Arquitectura en capas: APROBADO. La feature se implementará respetando controller, service, persistence y frontend modularizado por `api`, `pages`, `components` y `styles`.
- Lógica de negocio solo en modelo: APROBADO. Los invariantes de rango de página, estados vacíos y límites de navegación se gestionarán en la capa de servicio y dominio, sin duplicar reglas en la UI.
- Cada validación en su nivel: APROBADO. Se validará la entrada del cliente en request DTO, el rango en servicios y la consistencia del dominio en los modelos.
- Tests no negociables: APROBADO, con requisito de mantener la cobertura y no eliminar tests existentes. Se deben ajustar pruebas para reflejar el cambio de listas a páginas.
- Definición de terminado: APROBADO. La feature solo será entregable cuando backend y frontend compilen, los tests pasen y los listados con estados vacíos y fin de contenido se validen en UI.
- Documentación e idioma: APROBADO. Los textos de usuario y la documentación se mantendrán en español.

## Project Structure

### Documentation (this feature)

```text
specs/008-paginacion-de-listados/
├── plan.md              # Este archivo
├── research.md          # Decisiones de diseño y alternativas
├── data-model.md        # Entidades y reglas del dominio
├── quickstart.md        # Guía de validación funcional
├── contracts/
│   └── paginacion-listados-api.md
├── checklists/
│   └── requirements.md
├── spec.md              # Especificación funcional
└── tasks.md             # Generado en la fase de tareas (no en /speckit-plan)
```

### Source Code (repository root)

```text
backend/
├── app/
│   ├── src/main/java/com/prontaentrega/
│   │   ├── controllers/
│   │   ├── models/
│   │   ├── persistence/
│   │   ├── services/
│   │   └── config/
│   └── src/test/java/com/prontaentrega/
└── gradlew

frontend/
├── src/
│   ├── api/
│   ├── components/
│   ├── pages/
│   ├── styles/
│   └── tests/
└── package.json
```

**Structure Decision**: se reutiliza la arquitectura actual del proyecto con backend en `backend/app` y frontend en `frontend/src`. El cambio de paginación se integra en controller/service/persistence del backend y en `api`/`pages`/`components` del frontend para centralizar la navegación y los estados vacíos.

## Complexity Tracking

No se registran violaciones a la constitución ni decisiones que requieran justificación adicional.
