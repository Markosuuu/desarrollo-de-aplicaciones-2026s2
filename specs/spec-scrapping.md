# Feature Specification: Scraping de jugadores y actualización de estadísticas

**Feature Branch**: `004-scrapping-jugadores`

**Created**: 2026-09-22

**Status**: Draft

**Input**: User description: "Actualizar las estadísticas de jugadores con datos obtenidos desde fuentes externas de scraping, persistir los datos en la entidad Jugador, soportar una base inicialmente vacía, mantener la información local disponible cuando falle un proveedor y exponer un endpoint de actualización sin restricciones de rol en esta entrega."

## Clarifications

### Session 2026-09-22

- Q: ¿La base de datos debe conservar la información existente si una fuente externa falla? → A: Sí, el sistema debe seguir operando con los datos locales y no debe perder la última información válida.
- Q: ¿El endpoint de actualización debe quedar abierto para cualquier persona sin rol administrativo? → A: Sí, en esta entrega no se requiere autorización por rol.
- Q: ¿La base puede estar vacía al iniciar la actualización? → A: Sí, el proceso debe completar la carga inicial sin depender de datos previos.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Actualizar estadísticas de jugadores (Priority: P1)

Como responsable del contenido deportivo, quiero actualizar las estadísticas de los jugadores desde fuentes externas para mantener la base de datos con información actualizada y consistente.

**Why this priority**: La actualización del catálogo es la funcionalidad central del cambio y es la base sobre la cual se pueden consultar y reutilizar los datos sin depender de proveedores externos en cada acceso.

**Independent Test**: Se puede probar ejecutando la actualización de estadísticas con una fuente externa disponible, verificando que los registros se persisten en la base de datos y que los datos se pueden recuperar luego sin consultar la fuente externa otra vez.

**Acceptance Scenarios**:

1. **Given** que existe una base de datos con jugadores registrados, **When** se dispara la actualización de estadísticas, **Then** el sistema obtiene información relevante de las fuentes externas y actualiza los campos de cada jugador correspondientes.
2. **Given** que los datos de una fuente externa están completos, **When** se procesa la actualización, **Then** la información actualizada queda guardada en la entidad Jugador y puede recuperarse sin volver a consultar la fuente externa.
3. **Given** que el usuario solicita la actualización, **When** la operación finaliza, **Then** la base de datos conserva los registros existentes y añade o modifica los datos necesarios para reflejar el inventario actual.

---

### User Story 2 - Poblar la base desde cero (Priority: P1)

Como usuario del sistema, quiero poder iniciar un proceso de actualización aunque la base de datos esté vacía para que el catálogo quede disponible sin requerir una carga manual previa.

**Why this priority**: La primera carga es el punto de entrada de valor para el producto; sin ella, la funcionalidad completa no puede operar ni ofrecer datos útiles.

**Independent Test**: Se puede probar con la base de datos vacía, ejecutando la actualización y verificando que se crean los registros necesarios y que el sistema no falla por la ausencia de datos previos.

**Acceptance Scenarios**:

1. **Given** que la base de datos está vacía, **When** se ejecuta la actualización, **Then** el sistema crea las entidades de jugadores con la información disponible en las fuentes externas.
2. **Given** que la base no tiene jugadores registrados, **When** la actualización termina, **Then** la base queda poblada con datos útiles para consultar luego sin depender del proveedor externo.
3. **Given** que el proceso se ejecuta por primera vez, **When** una fuente externa entrega datos válidos, **Then** la operación completa el poblamiento sin errores de consistencia ni registros incompletos.

---

### User Story 3 - Continuar funcionando ante fallas de proveedor (Priority: P1)

Como responsable de la operación, quiero que el sistema siga funcionando con datos locales cuando una fuente externa falla para evitar pérdida de información o interrupción del servicio.

**Why this priority**: La confiabilidad del catálogo es esencial; un problema temporal del proveedor no debe dejar la base vacía ni inutilizable.

**Independent Test**: Se puede probar provocando una falla del proveedor externo o una respuesta inválida, verificando que la base de datos sigue disponible, conserva su última información válida y reporta un error claro.

**Acceptance Scenarios**:

1. **Given** que una de las fuentes externas no responde o devuelve datos inválidos, **When** se intenta actualizar la base, **Then** el sistema informa el problema y no destruye la información ya guardada.
2. **Given** que la actualización no puede completarse, **When** la operación termina con error, **Then** el sistema sigue utilizando la última información válida disponible localmente.
3. **Given** que el proveedor externo falla repetidamente, **When** se intenta ejecutar la actualización, **Then** el sistema devuelve un mensaje descriptivo y claro sin dejar la base en un estado inconsistente.

---

### Edge Cases

- Si una fuente externa devuelve datos incompletos, la actualización debe descartar o marcar la información no confiable sin vaciar el conjunto existente.
- Si una actualización parcial falla luego de haber empezado, la base debe conservar la última versión válida y no quedar en un estado intermedio ambiguo.
- Si una fuente externa devuelve jugadores duplicados, el sistema debe evitar registros inconsistentes y mantener una única versión por jugador.
- Si la base estaba vacía y falla una fuente, el proceso debe informar claramente la falla y no dejar la base sin datos confiables.
- Si una actualización se ejecuta varias veces, el resultado debe ser idempotente en términos de mantener la información más reciente y no duplicar registros.
- Si una actualización no produce información útil, la operación debe responder con un error comprensible y sin comprometer los datos actuales.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST permitir actualizar las estadísticas de los jugadores a partir de fuentes externas de información sin requerir que la base ya esté poblada.
- **FR-002**: El sistema MUST extraer y persistir para cada jugador al menos nombre, equipo, liga, edad, goles, asistencias, disparos, pases, dribbles, faltas y rating.
- **FR-003**: La información actualizada MUST almacenarse en la entidad existente Jugador sin requerir una migración de dominio para esta entrega.
- **FR-004**: El sistema MUST permitir poblar la base de datos desde cero cuando la colección de jugadores no tiene registros previos.
- **FR-005**: Si una fuente externa falla o devuelve datos inválidos, el sistema MUST seguir funcionando con la última información local válida y MUST evitar destruir datos ya almacenados.
- **FR-006**: El sistema MUST exponer un endpoint específico para disparar la actualización de estadísticas sin exigir permisos de administrador o roles adicionales en esta entrega.
- **FR-007**: El sistema MUST devolver mensajes claros y descriptivos cuando una actualización no pueda completarse por errores del proveedor o de la fuente externa.
- **FR-008**: El sistema MUST preservar la disponibilidad del catálogo existente durante procesos fallidos y MUST evitar reemplazar información válida por datos no confiables.
- **FR-009**: El sistema MUST impedir la duplicación de registros al actualizar jugadores ya existentes y MUST mantener una única versión consistente por entidad.
- **FR-010**: El sistema MUST permitir recuperar los datos de los jugadores sin volver a consultar a los proveedores externos tras una actualización exitosa.
- **FR-011**: El sistema MUST incluir la información actual relevante del jugador en la base de datos para que la consulta posterior sea independiente de los proveedores externos.
- **FR-012**: Esta funcionalidad MUST limitarse a la actualización y persistencia del catálogo de jugadores; no incluye interfaz de usuario, programación de ejecución automática ni procesos de validación financiera adicionales.

### Key Entities *(include if feature involves data)*

- **Jugador**: Persona deportiva cuya información estadística se actualiza y persiste; incluye nombre, equipo, liga, edad y métricas relevantes como goles, asistencias, disparos, pases, dribbles, faltas y rating.
- **Fuente externa de estadísticas**: Proveedor que entrega información del rendimiento del jugador; puede ser disponible o no y debe tratarse como una dependencia no confiable del sistema.
- **Actualización de catálogo**: Proceso que consulta fuentes externas, normaliza la información y guarda los cambios en la base local para mantener el catálogo consistente.
- **Datos locales**: La última versión válida almacenada en la base de datos; se utiliza como respaldo cuando una fuente externa falla.
- **Liga**: Competencia a la que pertenece el jugador y que forma parte de la información relevante para la consulta posterior.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 100% de las ejecuciones de actualización con base vacía termina con al menos un conjunto útil de jugadores persistidos y disponibles para consulta.
- **SC-002**: El 100% de los jugadores actualizados incluye los campos obligatorios de nombre, equipo, liga, edad, goles, asistencias, disparos, pases, dribbles, faltas y rating.
- **SC-003**: El 100% de las ejecuciones fallidas de proveedor mantiene la última información local válida y no deja la base de datos vacía ni inutilizable.
- **SC-004**: Al menos el 95% de las actualizaciones exitosas permiten recuperar la información del jugador localmente sin requerir consultar de nuevo a la fuente externa.
- **SC-005**: El 100% de los errores de proveedor se reportan con mensajes claros y comprensibles para el usuario o el operador que ejecuta la operación.
- **SC-006**: La funcionalidad documentada permite que un operador complete una actualización de referencia en menos de 10 minutos en un entorno con datos de prueba representativos.

## Assumptions

- La actualización de estadísticas depende de proveedores externos que pueden fallar o devolver datos incompletos; la última versión local válida debe mantenerse como respaldo.
- El sistema opera en capa de backend y persiste la información en la base de datos existente sin cambiar el modelo de dominio principal para esta entrega.
- El endpoint de actualización se usa para poblar la base inicial y refrescarla posteriormente; no está protegido por roles ni autenticación en esta versión.
- La información de cada jugador debe quedar persistida localmente para que las consultas futuras no dependan de la disponibilidad de proveedores externos.
- La funcionalidad está enfocada en la actualización y la persisitencia del catálogo; la automatización periódica, la interfaz visual y la lógica de negocio adicional quedan fuera del alcance de esta entrega.
