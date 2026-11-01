# Feature Specification: Mercado de tokens de jugadores

**Feature Branch**: `006-player-token-marketplace`

**Created**: 2026-11-01

**Status**: Draft

**Input**: User description: "Se preciza incluir en el proyecto los siguientes requerimientos: 1. Los usuarios pueden ver un listado de jugadores a la venta por otros usuarios 2. Los usuarios pueden vender los tokens de jugadores que posean y obtener ganancia por ello. 3. Los usuarios pueden comprar tokens de jugadores que estén a la venta por otros usuarios siempre y cuando tengan los créditos necesarios. Para eso se necesita lo siguiente ..."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Explorar el mercado de jugadores en venta (Priority: P1)

Como usuario registrado, quiero ver un listado de jugadores que otros usuarios están vendiendo para comparar opciones de compra y elegir el token que mejor se adapte a mi presupuesto.

**Why this priority**: Es la entrada principal al feature porque permite descubrir el catálogo disponible antes de iniciar una compra o publicación.

**Independent Test**: Se puede probar abriendo la vista del mercado con varios anuncios cargados y verificando que la grilla muestre cada publicación con la información requerida y una acción de compra disponible.

**Acceptance Scenarios**:

1. **Given** que existen anuncios activos de venta de tokens de jugadores, **When** el usuario abre la vista del marketplace, **Then** se visualiza una grilla de 32 anuncios (4x8) paginada con la información de cada publicación.
2. **Given** que hay más de 32 anuncios, **When** el usuario navega entre páginas, **Then** el sistema muestra el siguiente conjunto de anuncios sin duplicar ni perder publicaciones.
3. **Given** que un anuncio pertenece a un usuario vendedor, **When** el usuario revisa la tarjeta, **Then** puede identificar el nombre del jugador, la cantidad de tokens en venta, el precio por token, el precio total, el nombre del vendedor y el botón de compra.

---

### User Story 2 - Publicar tokens en venta (Priority: P1)

Como usuario que posee tokens de un jugador, quiero poner una parte de ellos a la venta para generar ingresos sin poner en riesgo los tokens que necesito conservar.

**Why this priority**: La venta es el eje principal del negocio del marketplace y permite que el sistema genere movimiento de tokens entre usuarios.

**Independent Test**: Se puede probar desde la cuenta de un usuario con 10 tokens disponibles, vender 5 y confirmar que esos 5 quedan reservados para venta y que los restantes siguen disponibles para otras operaciones.

**Acceptance Scenarios**:

1. **Given** que un usuario tiene una cantidad de tokens disponible, **When** define una cantidad menor o igual a la disponible y confirma la venta, **Then** la operación queda registrada como venta pendiente y la cantidad indicada queda bloqueada para futuras ventas del mismo usuario hasta que se cancele o se complete.
2. **Given** que un usuario intenta vender una cantidad mayor a la disponible, **When** confirma la acción, **Then** el sistema rechaza la operación y muestra un error de validación.
3. **Given** que un usuario desea retirar una publicación que aún no fue comprada, **When** cancela la venta, **Then** el sistema libera esa cantidad para ser vendida nuevamente o usada en otra operación.
4. **Given** que un usuario editó una venta pendiente, **When** modifica la cantidad a un valor válido y no comprado, **Then** la publicación se actualiza con la nueva cantidad disponible para venta.

---

### User Story 3 - Comprar tokens de otro usuario (Priority: P1)

Como usuario con créditos suficientes, quiero comprar tokens de jugadores en venta para transferir la propiedad a mi cuenta y completar la operación de forma segura.

**Why this priority**: Es la segunda pieza central del marketplace y mueve el flujo económico entre usuarios.

**Independent Test**: Se puede probar con un comprador y un vendedor distintos, con suficientes créditos, y verificar que el precio total se descuenta de los créditos del comprador y se suma a los créditos del vendedor, además de transferir los tokens.

**Acceptance Scenarios**:

1. **Given** que un usuario tiene suficientes créditos para cubrir el total de la compra, **When** confirma la compra de una publicación activa de otro usuario, **Then** la operación pasa a estado COMPLETADA, se descuentan los créditos del comprador y se acreditan al vendedor.
2. **Given** que un usuario intenta comprar una publicación propia, **When** intenta confirmar la compra, **Then** el sistema rechaza la operación.
3. **Given** que no hay disponibilidad de tokens en venta, **When** el usuario intenta comprar, **Then** el sistema bloquea la compra porque la publicación ya no está disponible.
4. **Given** que el comprador no tiene créditos suficientes, **When** intenta confirmar la compra, **Then** el sistema rechaza la operación sin modificar el estado ni los saldos.

---

### User Story 4 - Consultar historial de operaciones (Priority: P2)

Como usuario, quiero ver el historial de mis movimientos de compra y venta para conocer qué operaciones realicé, en qué estado quedaron y con qué usuarios interactué.

**Why this priority**: El historial es fundamental para la trazabilidad, la transparencia y la resolución de dudas sobre movimientos previos, aunque no constituye la funcionalidad principal del flujo de compra/venta.

**Independent Test**: Se puede probar solicitando el historial de un usuario con compras y ventas registradas y verificando que cada operación muestre sus datos requeridos y el estado correspondiente.

**Acceptance Scenarios**:

1. **Given** que un usuario tiene operaciones registradas, **When** consulta su historial, **Then** puede ver cada movimiento con tipo, estado, nombre del jugador, cantidad, precio por token, precio total, usuario que realizó la operación y usuario con el que se realizó la operación.
2. **Given** que una operación fue cancelada antes de completarse, **When** el usuario revisa el historial, **Then** el estado aparece como CANCELADA y la operación no se considera completada.
3. **Given** que una operación fue completada, **When** el usuario revisa el historial, **Then** el estado aparece como COMPLETADA y el movimiento queda registrado como finalizado.

### Edge Cases

- Si un usuario intenta vender más tokens de los que tiene disponibles, el sistema MUST rechazar la operación y mostrar un error de validación sin modificar el saldo ni la publicación.
- Si un usuario intenta poner una venta con cantidad cero o negativa, el sistema MUST rechazar la operación y no crear ni actualizar la publicación.
- Si dos usuarios intentan comprar la misma publicación en paralelo, la primera operación que logre completarse MUST quedar efectiva y la otra MUST rechazarse como compra no disponible.
- Si un usuario tiene exactamente 0 créditos, el sistema MUST impedir cualquier compra y no debe permitir aplicar una operación que exceda su saldo disponible.
- Si un usuario intenta cancelar una venta que ya fue completada, el sistema MUST rechazar la acción porque esa operación ya no es cancelable.
- Si un usuario intenta editar una venta que ya fue comprada por otra persona, el sistema MUST rechazar la modificación porque la venta ya no es editable.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST mostrar un listado de anuncios de jugadores a la venta por otros usuarios en una grilla paginada de 32 anuncios por página (4 columnas x 8 filas).
- **FR-002**: Cada anuncio MUST incluir el nombre del jugador, la cantidad de tokens disponibles para venta, el precio por token, el precio total de la venta, el nombre del usuario vendedor y un botón para comprar los tokens.
- **FR-003**: El sistema MUST permitir a un usuario ver el listado completo de anuncios disponibles, navegando entre páginas cuando exista más de un conjunto de resultados.
- **FR-004**: El sistema MUST impedir que un usuario venda más tokens de los que tiene disponibles en su saldo actual.
- **FR-005**: El sistema MUST impedir que un usuario venda una cantidad negativa o igual a cero de tokens.
- **FR-006**: El sistema MUST determinar el precio por token del jugador de forma automática, sin permitir que el usuario lo configure manualmente.
- **FR-007**: Cuando un usuario publica tokens en venta, esos tokens MUST quedar marcados como "en venta" y separados del saldo disponible para nuevas ventas hasta que se cancele o complete la operación.
- **FR-008**: Un usuario MUST poder cancelar una venta pendiente siempre y cuando la operación no haya sido comprada por otro usuario.
- **FR-009**: Un usuario MUST poder editar la cantidad de tokens en venta siempre y cuando la operación no haya sido comprada por otro usuario.
- **FR-010**: Al editar una venta pendiente, el sistema MUST validar que la nueva cantidad no supere la cantidad disponible del usuario y que sea mayor a cero.
- **FR-011**: El sistema MUST registrar cada compra y venta con su tipo de operación (COMPRA o VENTA), estado (PENDIENTE, COMPLETADA o CANCELADA), nombre del jugador, cantidad, precio por token, precio total, usuario que realizó la operación, fecha y hora, y usuario con el que se realizó la operación.
- **FR-012**: Un usuario MUST poder comprar tokens de jugadores que estén en venta por otros usuarios siempre y cuando tenga el saldo de créditos necesario.
- **FR-013**: Cada usuario MUST iniciar con 100 créditos virtuales disponibles para la compra de tokens.
- **FR-014**: El sistema MUST impedir que los créditos de un usuario queden en valor negativo.
- **FR-015**: El sistema MUST impedir que un usuario compre tokens de un jugador que esté a la venta por sí mismo.
- **FR-016**: El sistema MUST impedir que un usuario compre tokens de jugadores que no estén en venta o que ya no estén disponibles.
- **FR-017**: El sistema MUST impedir que un usuario compre tokens si el total de la operación supera sus créditos disponibles.
- **FR-018**: Cuando una compra se confirma, el sistema MUST descontar el precio total del comprador y acreditar el mismo monto al vendedor.
- **FR-019**: Cuando una compra se confirma, el sistema MUST transferir los tokens del jugador al comprador y descontarlos del vendedor.
- **FR-020**: Una operación de compra MUST pasar a estado "COMPLETADA" y no puede deshacerse una vez finalizada.
- **FR-021**: El sistema MUST registrar la operación de compra o venta con el usuario implicado y la contraparte correspondiente en el historial de movimientos del usuario.
- **FR-022**: El sistema MUST distinguir entre operaciones pendientes, completadas y canceladas en el historial para que el usuario pueda seguir el estado de cada movimiento.
- **FR-023**: El sistema MUST garantizar que la cantidad de tokens en venta sea consistente con la cantidad de tokens disponibles del usuario, evitando solapamientos entre ventas activas y saldo disponible.
- **FR-024**: Si varios usuarios intentan comprar la misma publicación al mismo tiempo, la primera operación exitosa MUST prevalecer y las demás MUST rechazarse como no disponibles.
- **FR-025**: El sistema MUST impedir cancelar o editar una venta que ya haya sido completada por otra persona, ya que esa operación queda inmodificable.

### Key Entities *(include if feature involves data)*

- **Usuario**: Representa a cada participante del marketplace; posee identidad y saldo de créditos asociado a la cuenta.
- **Jugador**: Representa a la entidad deportiva cuyos tokens pueden ser comprados y vendidos dentro del sistema.
- **TokenJugador**: Representa la fracción de propiedad asociada a un jugador, con un estado que indica si está disponible, en venta o transferido.
- **AnuncioVenta**: Representa una publicación de venta de tokens por parte de un usuario; contiene jugador, cantidad de tokens, precio por token, total, vendedor y estado de la venta.
- **Operacion**: Representa el movimiento histórico de compra o venta entre dos usuarios; incluye tipo, estado, detalles del jugador, cantidad, precio, total, usuarios implicados y fecha de creación.
- **SaldoCredito**: Representa el valor virtual disponible para cada usuario para realizar compras; no puede ser negativo.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un usuario puede ver y navegar el catálogo de anuncios en menos de 30 segundos desde la apertura de la vista, sin requerir asistencia.
- **SC-002**: El 95% de las ventas y compras realizadas con datos válidos se procesan sin errores de validación ni inconsistencias de saldo.
- **SC-003**: El 100% de las compras completadas reflejan correctamente la reducción de créditos del comprador, el aumento de créditos del vendedor y la transferencia de tokens asociados.
- **SC-004**: El sistema evita que un usuario llegue a un saldo de créditos negativo y que se publiquen ventas con cantidades inválidas en el 100% de los casos de prueba de validación.
- **SC-005**: El historial del usuario muestra todas las operaciones realizadas con su tipo, estado, contraparte, monto y fecha, con una tasa de completitud del 100% para los movimientos registrados.
- **SC-006**: La experiencia del usuario permite completar el flujo principal de compra y venta con una tasa de éxito superior al 90% en primera interacción.

## Assumptions

- El sistema ya cuenta con usuarios registrados y con un registro base de jugadores y sus tokens disponibles.
- El precio por token es fijo por sistema y no es configurable por cada vendedor.
- Los usuarios no pueden publicar ventas de cantidades superiores a las que realmente poseen de forma disponible.
- Las transacciones de compra y venta deben validarse antes de ejecutar cambios para evitar inconsistencias de saldo y propiedad.
- El historial de operaciones es un registro auditable de todos los movimientos de compra y venta realizados por cada usuario.
- El feature cubre la operación de compra/venta de tokens, pero no incluye subastas ni negociación libre de precios fuera del mecanismo definido por el sistema.
