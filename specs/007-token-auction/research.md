# Research: Subasta de tokens del mercado

## Decisión 1: Control de concurrencia en pujas

**Decision**: Se utilizará bloqueo optimista sobre la entidad `Subasta` para controlar concurrentes y evitar que dos pujas simultáneas generen un estado inconsistente.

**Rationale**: El requerimiento exige explicitamente concurrencia optimista cuando dos o más usuarios pujan al mismo tiempo. El proyecto ya usa transacciones en servicio y repositorios JPA, por lo que el patrón más coherente con la arquitectura existente es proteger la escritura de la oferta más alta y la actualización del estado de la subasta bajo una transacción con versionado del registro.

**Alternatives considered**: bloqueo pesimista con `SELECT FOR UPDATE`; descartado por ser más restrictivo y menos alineado con la expectativa de bloqueo optimista declarada. También se evaluó no proteger la operación, pero se descarta por inconsistencia de puja.

## Decisión 2: Idempotencia por usuario y subasta

**Decision**: La idempotencia de la puja se resolverá en la capa de servicio con una clave compuesta por `usuarioId + subastaId`, utilizando la misma semántica que una oferta no duplicada para el mismo participante.

**Rationale**: La especificación exige que el mismo usuario no cree duplicados en la misma subasta. La clave compuesta evita reintentos repetidos desde distintos clientes o llamadas accidentales sin romper la lógica de historial de ofertas.

**Alternatives considered**: usar una clave de idempotencia con timestamp; descartado porque produce registros múltiples y complica la deduplicación. También se consideró dejarlo a frontend, pero eso no garantiza la integridad del backend.

## Decisión 3: Estado de tokens durante la subasta

**Decision**: Los tokens asignados a una subasta quedan en estado congelado o reservado mientras la subasta esté activa; no están disponibles para otra venta ni para compra directa.

**Rationale**: La especificación indica que los tokens dedicados a la subasta deben pasar a un estado “ocupado”, igual que los anuncios de venta. Este estado es necesario para impedir doble asignación durante la vigencia del proceso.

**Alternatives considered**: no congelar tokens y dejar la validación solo en la lógica de subasta; descartado porque rompe la consistencia del inventario y permite ventas simultáneas sobre el mismo activo.

## Decisión 4: Resolución de cierre y ganador válido

**Decision**: El cierre calcula el ganador de mayor oferta válida, verificando además que el usuario tenga créditos suficientes para la liquidación. Si el mayor postor no cumple, se recorre a la siguiente oferta activa válida; si ninguna cumple, la subasta finaliza sin venta y los tokens regresan al dueño.

**Rationale**: La especificación define explícitamente la necesidad de “saltar” a la siguiente oferta más alta válida y la devolución de tokens cuando no hay un ganador viable. Esto protege el modelo financiero del sistema y evita ventas con balance insuficiente.

**Alternatives considered**: cerrar siempre con la oferta máxima aunque el postor no tenga créditos; descartado porque rompe la obligación de liquidación consistente y genera deudas inválidas.

## Decisión 5: Operaciones de transacción

**Decision**: Se generarán dos operaciones de tipo transacción al cierre exitoso: una para el vendedor y otra para el comprador, con el saldo del valor final y estado completado.

**Rationale**: La especificación obliga a registrar la operación tanto para el dueño como para el mayor postor, indicando la compra/venta entre ambos. Esto también respalda el historial de la cuenta y la trazabilidad del flujo.

**Alternatives considered**: generar una sola operación total; descartado porque la experiencia de historial y auditoría del negocio requiere distinguir el rol de cada usuario.
