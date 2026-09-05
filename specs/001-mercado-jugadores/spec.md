# Feature Specification: Mercado de jugadores

**Feature Branch**: [001-mercado-jugadores]

**Created**: 2026-09-05

**Status**: Draft

**Input**: User description: "El sistema representa un mercado de jugadores basado en criterios de valuación, donde:
* Cada jugador tiene una cotización que varía en el tiempo
* Los usuarios pueden operar comprando y vendiendo tokens de jugadores
* El valor de la inversión cambia según la cotización actual del jugador
* El sistema deberá calcular una cotización periódica basada en criterios aún no definidos"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Registrar usuarios y operar con tokens de jugadores (Priority: P1)

Un usuario nuevo puede registrarse en el mercado, recibir acceso para operar y comprar tokens de jugadores en la cotización vigente. El sistema debe permitir ejecutar operaciones de compra y venta dentro del mismo mercado sin que la posición del usuario quede inconsistentes ante cambios de precio. El valor del portafolio debe reflejar la cotización actual del jugador y los resultados reales de la inversión.

**Why this priority**: Es el valor central del producto: la compra y venta de tokens y el cálculo de ganancias o pérdidas son la razón de existencia del mercado. Sin esta capacidad, la plataforma no cumple su objetivo principal.

**Independent Test**: Puede comprobarse con un registro de usuario, la compra de tokens a la cotización actual y la verificación de que la posición y el saldo son actualizados correctamente según el movimiento del precio.

**Acceptance Scenarios**:

1. **Given** un usuario registrado y disponible para operar, **When** compra tokens de un jugador a la cotización vigente, **Then** el sistema valida la disponibilidad, actualiza la posición del usuario, registra la operación y descuenta el importe correspondiente del saldo.
2. **Given** un usuario que posee tokens de un jugador, **When** vende una cantidad válida, **Then** el sistema valida la titularidad, actualiza el saldo y la posición del usuario, y registra la operación con el precio vigente al momento de la operación.
3. **Given** un usuario intenta vender más tokens de los que posee o comprar sin disponibilidad suficiente, **When** realiza la operación, **Then** el sistema rechaza la transacción y informa claramente la causa de la validación.

---

### User Story 2 - Consultar el catálogo, el detalle del jugador y el ranking (Priority: P2)

Un usuario del mercado puede explorar el conjunto de jugadores disponibles, filtrar la vista por liga, y consultar la evolución de su cotización y desempeño. Además, puede comparar jugadores en un ranking para apoyar decisiones de compra y venta antes de operar.

**Why this priority**: Permite que la decisión de inversión se base en información útil y comparable, aumentando la confianza del usuario y la utilidad de la plataforma como mercado de valor.

**Independent Test**: Puede validarse con la búsqueda y comparación de jugadores, junto con la visualización de detalle y la evolución de cotización durante un periodo de referencia.

**Acceptance Scenarios**:

1. **Given** un usuario autenticado, **When** accede al catálogo de jugadores, **Then** puede ver la lista con la información base de cada jugador y aplicar filtros por liga o criterio de búsqueda.
2. **Given** un usuario selecciona un jugador del catálogo, **When** abre su detalle, **Then** visualiza su cotización actual, evolución reciente y la información de valoración asociada.
3. **Given** varios jugadores con diferentes niveles de valoración, **When** accede al ranking, **Then** el sistema ordena y presenta la comparación de manera clara.

---

### User Story 3 - Revisar el portafolio y el historial de operaciones (Priority: P3)

El usuario puede revisar su cartera personal para ver cuánto tiene invertido en cada jugador, el precio promedio de compra, el valor actual de la posición, la ganancia o pérdida acumulada y el historial de movimientos realizados. Esto le permite entender el resultado de sus decisiones y planificar nuevas operaciones.

**Why this priority**: La transparencia del estado del portafolio es esencial para la confianza del usuario y para que el mercado pueda operar con información completa sobre la evolución de la inversión.

**Independent Test**: Puede probarse con una secuencia de compras y ventas en la que la cartera refleje cambios de precio y el historial muestre cada operación registrada con fecha, cantidad y precio.

**Acceptance Scenarios**:

1. **Given** un usuario con al menos una operación registrada, **When** consulta su portafolio, **Then** se muestra la cantidad de tokens por jugador, el costo promedio, el valor actual y la ganancia o pérdida.
2. **Given** una posición con diferentes compras y ventas, **When** se actualiza la cotización del jugador, **Then** el valor del portafolio se recalcula automáticamente en función del precio vigente.
3. **Given** un usuario revisa el historial, **When** consulta sus operaciones, **Then** cada movimiento está identificado con la fecha, cantidad, precio y tipo de transacción.

### Edge Cases

- ¿Qué ocurre cuando una operación intenta vender más tokens de los que tiene el usuario?
- ¿Cómo responde el sistema si la cotización de un jugador cambia mientras una operación está en proceso?
- ¿Qué sucede cuando un jugador no tiene disponibilidad de tokens para una compra en ese momento?
- ¿Cómo se comporta el sistema si un usuario intenta comprar con saldo insuficiente?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST mantener un catálogo de jugadores con información base y su cotización vigente para cada liga admitida.
- **FR-002**: El sistema MUST admitir al menos cinco ligas de fútbol en la operación del mercado: Premier League, Bundesliga, La Liga, Serie A y Ligue 1.
- **FR-003**: El sistema MUST permitir el registro de nuevos usuarios con acceso para operar en el mercado.
- **FR-004**: El sistema MUST asignar a un único superusuario la tenencia inicial de todos los tokens emitidos para cada jugador.
- **FR-005**: El sistema MUST emitir un total de 100 tokens por jugador al inicio del sistema y establecer su valor inicial en 1 crédito por token.
- **FR-006**: El sistema MUST calcular y actualizar periódicamente la cotización de cada jugador sobre la base de una estrategia de valuación definida por el negocio.
- **FR-007**: El sistema MUST validar la disponibilidad de tokens antes de ejecutar una compra y rechazar la operación si no existe suficiente stock.
- **FR-008**: El sistema MUST utilizar la cotización vigente del jugador al momento de la compra para calcular el importe total de la operación.
- **FR-009**: El sistema MUST registrar cada operación de compra y venta con la información mínima necesaria para auditarlas y mostrar historial posterior.
- **FR-010**: El sistema MUST actualizar la posición del usuario tras una compra o venta, reflejando la cantidad acumulada de tokens por jugador y el saldo disponible.
- **FR-011**: El sistema MUST permitir la venta de tokens solo si el usuario cuenta con la cantidad solicitada y debe ajustar el saldo y la posición del usuario en consecuencia.
- **FR-012**: El sistema MUST permitir a los usuarios visualizar el portafolio personal con cantidad de tokens, precio promedio de compra, valor actual, ganancia o pérdida y historial de operaciones.
- **FR-013**: El sistema MUST ofrecer una interfaz de usuario con acceso al catálogo de jugadores, detalle de jugadores, ranking y operación del mercado.
- **FR-014**: El sistema MUST permitir a los usuarios autenticados operar en el mercado y consultar su portafolio de manera segura y consistente.
- **FR-015**: El sistema MUST mantener la información de cotizaciones y operaciones en un estado coherente para que el valor de la inversión refleje el cambio de precio actual del jugador.

### Key Entities *(include if feature involves data)*

- **Jugador**: Representa a un futbolista disponible en el mercado, con su identificación, liga, cotización vigente y evolución asociada.
- **Usuario**: Representa a un participante del mercado con credenciales de acceso, saldo disponible y participación en operaciones.
- **Token**: Representa una fracción de propiedad del jugador en el mercado y existe en una cantidad total limitada por jugador.
- **Operación**: Registra una compra o venta de tokens, con usuario, jugador, cantidad, precio unitario, saldo resultante y fecha.
- **Posición**: Representa la cantidad de tokens que posee un usuario por jugador, el costo promedio de adquisición y el valor actual asociado.
- **Portafolio**: Consolidado de las posiciones de un usuario, con el valor total, plusvalía o minusvalía y el historial de operaciones.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Los usuarios pueden registrarse, comprar y vender tokens de jugadores en menos de 3 minutos en una operación estándar del mercado.
- **SC-002**: El sistema actualiza la cotización y el valor del portafolio de cada usuario en tiempo real durante la sesión de mercado, sin dejar posiciones inconsistentes.
- **SC-003**: Al menos el 95% de las operaciones de compra y venta concluyen con un registro correcto y un saldo actualizado conforme al resultado final.
- **SC-004**: Los usuarios pueden consultar su cartera y comprender claramente la evolución de su inversión con una visión del valor actual y la ganancia o pérdida asociada.
- **SC-005**: El mercado ofrece una experiencia navegable para explorar jugadores, comparar ranking y tomar decisiones de inversión con información disponible para cada jugador.

## Assumptions

- La estrategia de valuación se define como un criterio periódico y configurable, con la lógica final ajustada durante el diseño del negocio y la modelización del dominio.
- El mercado opera con una única fuente de verdad para la cotización vigente de cada jugador y para la validación de inventario de tokens.
- La operación inicial de tokens solo está disponible para el superusuario y es la base de la actividad comercial del sistema.
- La plataforma asume que los usuarios cuentan con saldo suficiente para operar, y que la validación de fondos y la presentación del saldo forman parte del flujo de negocio del producto.
- La versión inicial del mercado cubre el catálogo, la operatoria, la cartera y la trazabilidad de transacciones, sin ampliar el alcance a otras clases de activos o mercados secundarios.
