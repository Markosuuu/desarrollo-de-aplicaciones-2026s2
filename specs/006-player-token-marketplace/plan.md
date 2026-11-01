# Implementation Plan: Mercado de tokens de jugadores

**Branch**: `006-player-token-marketplace` | **Date**: 2026-11-01 | **Spec**: `specs/006-player-token-marketplace/spec.md`

**Input**: Feature specification from `/specs/006-player-token-marketplace/spec.md`

## Summary

Esta funcionalidad agrega un marketplace para comprar y vender tokens de jugadores, con listado paginado, validación de disponibilidad de tokens y créditos, registro de historial y control de estados de venta. El diseño se apoya en la arquitectura actual del proyecto: backend Spring Boot con capas de modelo, servicio, persistencia y controladores; y frontend React con acceso a APIs por capa `api` y presentación en páginas/componente.

## Technical Context

**Language/Version**: Java 21 en backend; JavaScript (React + Vite) en frontend.

**Primary Dependencies**: Spring Boot, Spring Web, Spring Data JPA, Lombok, JUnit 5, React, Vite, fetch nativo del navegador.

**Storage**: PostgreSQL para persistencia del backend; repositorios existentes con patrón repository y entidades de dominio.

**Testing**: JUnit 5, pruebas de integración Spring Boot y validación funcional del flujo en frontend.

**Target Platform**: aplicación web con acceso desde navegador.

**Project Type**: web application.

**Performance Goals**: catálogo de hasta 32 anuncios por página, validación inmediata de compra/venta y respuesta de historial sin degradación visible para usuarios normales.

**Constraints**: todo el texto visible debe estar en español; no puede haber ventas con cantidad negativa o cero; no puede haber créditos negativos; no pueden editarse ni cancelarse ventas ya completadas; el precio unitario se define por sistema y no por el vendedor.

**Scale/Scope**: soporte para varios usuarios simultáneos con validaciones de negocio centralizadas en backend; historial de operaciones por usuario; límite de público en marketplace por páginas y filtros.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- Arquitectura en capas: APROBADO. La capacidad de compra/venta se implementará separando controlador, servicio, modelo y persistencia sin mezclar responsabilidades.
- Lógica de negocio solo en modelo: APROBADO. Las reglas de disponibilidad, saldo, no negativos y estados de venta deben vivir en el dominio y en servicios del backend, no en la UI.
- Cada validación en su nivel: APROBADO. Requests se validarán en DTO/controller, existencia y posibilidad de acción en servicio, invariantes del dominio en modelo.
- Tests no negociables: APROBADO, pero requiere evidencia explícita. Para esta feature deben incluirse pruebas unitarias del dominio y pruebas de integración para venta, compra y historial.
- Definición de terminado: APROBADO. La feature queda lista solo cuando el marketplace compila, se valida con datos reales y el historial refleja operaciones correctas.
- Documentación e idioma: APROBADO. La documentación, mensajes y UI deben mantenerse en español.

## Validation & Delivery Requirements

Antes de cerrar el feature, se debe comprobar que:

- `GET /api/marketplace/listings` devuelve anuncios activos con paginación correcta.
- `POST /api/orders/sell` rechaza cantidades inválidas o mayores a la disponibilidad real.
- `POST /api/orders/buy` valida créditos suficientes, no compra propia y no compra de anuncios no disponibles.
- `GET /api/users/{id}/transactions` lista compras y ventas con su tipo, estado, contraparte, monto y fecha.
- El backend mantiene consistencia entre saldo, inventario de tokens y estado de la publicación.

## Project Structure

### Documentation (this feature)

```text
specs/006-player-token-marketplace/
├── plan.md              # Este archivo
├── research.md          # Decisiones de diseño y alternativas
├── data-model.md        # Entidades y validaciones del dominio
├── quickstart.md        # Guía de validación funcional
├── contracts/           # Contratos de API del marketplace
├── checklists/
│   └── requirements.md
├── spec.md              # Especificación funcional del feature
└── tasks.md             # Generado en el paso de tareas (no en /speckit-plan)
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
│   └── utils/
└── package.json
```

**Structure Decision**: se reutiliza la arquitectura actual del proyecto con backend en `backend/app` y frontend en `frontend/src`. Las nuevas entidades y reglas del marketplace se integran en la capa de dominio y servicio del backend, mientras que el frontend consume las APIs desde `frontend/src/api` y presenta la información en páginas/componentes de la aplicación web.

## Complexity Tracking

No se registran violaciones a la constitución ni decisiones que requieran justificación adicional.
