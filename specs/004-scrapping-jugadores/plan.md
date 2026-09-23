# Implementation Plan: Scrapping de jugadores y actualización de estadísticas

**Branch**: `004-scrapping-jugadores` | **Date**: 2026-09-22 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/004-scrapping-jugadores/spec.md`

## Summary

Esta funcionalidad agrega la actualización del catálogo de jugadores mediante scraping de fuentes externas, preserva la última información local válida ante fallos y permite poblar la base desde cero sin requerir datos previos. Se implementará en la capa de backend siguiendo la arquitectura existente: controller → service → repository/model, con la entidad `Jugador` como almacenamiento persistente consultable para nombre, equipo, liga, edad y métricas estadísticas clave. Se permite usar `CatalogSnapshot` como registro técnico de auditoría de cada intento de actualización, sin separar ni duplicar el catálogo de jugadores. El proceso se expondrá en un endpoint específico sin protección por rol y se validará con pruebas reales, sin mockear servicios ni repositorios internos, usando respuestas HTTP reales o fixtures HTTP locales con formato WhoScored.

## Technical Context

**Language/Version**: Java 21

**Primary Dependencies**: Spring Boot 3.3.3, Spring Web, Spring Data JPA, Spring Validation, PostgreSQL, JUnit 5, MockMvc, Testcontainers

**Storage**: PostgreSQL para persistencia local y de ejecución; Testcontainers PostgreSQL para pruebas de integración reales

**Testing**: JUnit 5, Spring Boot Test, MockMvc, Testcontainers; pruebas de integración y controlador sin mockear servicios internos ni repositorios. Para cubrir fallos de WhoScored se puede usar un servidor HTTP local real con respuestas controladas del formato esperado.

**Target Platform**: Servicio web backend ejecutado localmente bajo Linux/Docker para la aplicación ProntaEntrega

**Project Type**: web-service

**Performance Goals**: actualizar un lote representativo de jugadores en un tiempo aceptable para la operación manual del administrador; recuperar los registros ya persistidos sin depender de la fuente externa en la consulta normal

**Constraints**: no deben usarse mocks de servicios internos ni repositorios en las pruebas de esta feature; si fallan proveedores externos, se conserva la última información válida; la base vacía debe poder poblarse; los mensajes de error deben ser claros y estar en español; la lógica de negocio no debe mezclarse con la capa HTTP

**Scale/Scope**: catálogo de jugadores del mercado, con foco en carga inicial y actualización de métricas; sin frontend ni automatización periódica en esta entrega

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] Arquitectura en capas: controller delega en service; service coordina repositorio y modelo; modelo conserva invariantes del dominio.
- [x] Lógica de negocio solo en modelo/servicio: la actualización, validación de integridad y manejo de fallos se centralizan en service y repositorio, sin duplicar reglas en el controller.
- [x] Validación por nivel: DTOs, servicio y persistencia conservan sus responsabilidades; los errores de proveedor se elevan con mensajes claros.
- [x] Tests no negociables: se exige cubrir flujo feliz, casos borde y errores con pruebas reales; no se aceptan mocks de servicios internos ni repositorios para esta funcionalidad.
- [x] Definición de terminado: esta feature requiere backend compilando, pruebas reales y colección de Postman/documentación de endpoint en tareas futuras.
- [x] Documentación e idioma: los textos y mensajes de error deben mantenerse en español.

Resultado pre-Phase 0: PASS. No se identifican violaciones justificables para Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/004-scrapping-jugadores/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
├── checklists/
│   └── requirements.md
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
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
│   │   └── authentication/
│   └── src/test/java/com/prontaentrega/
│       ├── controllers/
│       ├── integration/
│       ├── repository/
│       └── services/
├── build.gradle
├── settings.gradle
└── gradlew
```

**Structure Decision**: Se usa la estructura actual del backend Java/Spring Boot ya presente en `backend/app`, respetando `models`, `repository`, `services` y `controllers`. Los tests se agrupan por nivel y deben ejecutarse con infraestructura real; no se agregan mocks como capa de verificación para este caso de uso.

## Complexity Tracking

No aplica: la solución encaja en la arquitectura vigente y cumple los requisitos de la constitución sin excepciones.

## Fase 0: decisiones de investigación

- El conjunto de datos de `Jugador` ya existe y se reutiliza para almacenamiento del catálogo, ampliando los campos estadísticos necesarios sin introducir una nueva entidad de dominio para consultar jugadores. `CatalogSnapshot` puede usarse como entidad técnica de auditoría de ejecución si el proyecto ya cuenta con ella.
- La carga inicial y la actualización deben ser idempotentes: si el jugador ya existe, se actualiza su registro; si no, se crea.
- El flujo de falla tiene prioridad: si una fuente externa no responde, se debe mantener la última información local válida y devolver un error descriptivo, pero no dejar la base vacía ni inconsistente.
- El endpoint de actualización debe ser público y no protegido por rol en esta entrega, como lo indica la especificación y la funcionalidad actual del proyecto.
- La estructura del payload de WhoScored debe servir como referencia de mapeo: `playerTableStats` contiene la colección principal, con campos como `name`, `teamName`, `tournamentName`, `age`, `goal`, `assistTotal`, `shotsPerGame`, `passSuccess`, `dribbleWon`, `foulGiven`, `rating`, entre otros. Ese ejemplo debe integrarse en la estrategia de extracción y normalización.
- Los tests deben ejecutarse sin mockear servicios internos ni repositorios. Cuando se prueben fallos o respuestas parciales del proveedor, se usará un servidor HTTP local real o fixture HTTP equivalente para validar el comportamiento de integración de forma reproducible.

## Fase 1: artefactos de diseño

Los siguientes artefactos se crearán en este directorio de funcionalidad:

- `research.md`: decisiones, criterios de selección, referencia del endpoint WhoScored y reglas de tolerancia a fallos
- `data-model.md`: entidad `Jugador`, campos adicionales y validaciones de dominio
- `contracts/scraping-api.md`: contrato del endpoint de actualización y comportamiento de respuesta
- `quickstart.md`: pasos para ejecutar la actualización manual con el endpoint y verificar resultados reales

**Nota crítica de pruebas**: todas las pruebas de esta feature deben ejecutarse sin mocks para servicios internos ni repositorios; cualquier validación basada en mocks de esas dependencias no cumple la especificación del proyecto y debe considerarse inválida para esta entrega. Para WhoScored se aceptan respuestas HTTP reales controladas por un servidor local de prueba cuando el caso requiera simular fallos, datos incompletos o datos duplicados.
