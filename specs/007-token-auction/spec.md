# Feature Specification: Subasta de tokens del mercado

**Feature Branch**: `[007-token-auction]`

**Created**: 2026-11-04

**Status**: Draft

**Input**: User description: "Feature de subasta de tokens del mercado. Se necesita una funcionalidad de subasta de tokens de mercado, donde un usuario puede poner a la venta por subasta la cantidad de tokens que quieran de un jugador. De esa subasta otros usuarios pueden pujar para intentar comprar esos tokens al mejor precio. La subasta cierra cuando el dueño la da por finalizada, o si se llego a la fecha y hora de finalización que especifico el dueño (La hora solo admite horas y minutos). En caso de que una subasta no tenga un mayor postor al finalizarse por tiempo o sea cerrada por el dueño, los tokens que el dueño vendio vuelven a su inventario. Se espera que la funcionalidad cumpla el siguiente flujo: - Un usuario subasta cierta cantidad de tokens de un jugador, especificando la cantidad de tokens a vender y la fecha de finalización. - Otros usuarios encuentran/ven la subasta y pueden ver la oferta actual mas alta, ademas de ver las ofertas previas. - Los demas usuarios pueden dejar su oferta, siempre y cuando supere la oferta actual mas alta. - Al finalizar o cerrarse la subasta, los tokens que el usuario subasto se transfieren al postor mas alto, y el valor de la puja se suma al credito del dueño de la subasta. - Al finalizar la subasta, esta genera una operacion tanto para el dueño de la subasta como para el mayor postor, indicando que hubo una transaccion entre ambos de compra/venta. Reglas: - La cantidad minima de tokens a subastar debe ser 1. - El dueño no puede dejar una oferta en su propia subasta. - Cuando 2 usuarios o más pujan en la misma subasta al mismo tiempo, se espera un bloqueo optimista. - Cuando el mismo usuario puja en la misma subasta, no crea duplicados (clave idempotencia). - El postor más alto no se puede retirar. - Los tokens que se dediquen a la subasta, pasaran a estar en un estado 'ocupados', como ocurre con los anuncios de venta. - Los creditos que los usuarios oferten para una subasta no se descontarán. Solamente cuando la subasta cierre, se le descontaran al mayor postor los creditos que haya ofertado por la subasta. - No se puede pujar en una subasta que esta cerrada."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Publicar tokens en subasta (Priority: P1)

Un usuario dueño de tokens puede poner una cantidad determinada de tokens de un jugador a la venta mediante una subasta. Define la cantidad disponible y la fecha y hora de cierre, y la subasta queda visible para otros usuarios mientras sigue abierta.

**Why this priority**: Es el recorrido principal del caso de negocio, ya que sin una subasta abierta no puede desarrollarse la puja ni la venta final.

**Independent Test**: Se puede validar creando una subasta con una cantidad válida y comprobando que la oferta más alta inicial sea nula y que los tokens pasen a un estado reservado mientras la subasta está activa.

**Acceptance Scenarios**:

1. **Given** que un usuario tiene tokens disponibles de un jugador, **When** crea una subasta con una cantidad mayor o igual a 1 y una fecha de cierre en el futuro, **Then** la subasta queda activa y los tokens quedan reservados para esa venta.
2. **Given** que un usuario intenta crear una subasta con una cantidad menor a 1, **When** envía la solicitud, **Then** el sistema rechaza la creación y solicita una cantidad válida.
3. **Given** que el propietario de la subasta intenta cerrar la subasta manualmente, **When** confirma la finalización, **Then** el sistema determina el resultado final según la puja vigente o devuelve los tokens si no hubo postor válido.

---

### User Story 2 - Realizar pujas sobre una subasta activa (Priority: P1)

Otros usuarios pueden revisar la subasta, ver la oferta más alta actual y las pujas previas, y presentar una nueva oferta siempre que supere el valor actual más alto.

**Why this priority**: La esencia de la funcionalidad es que la oferta se cierre por competencia y precio, por lo que este flujo es central para el valor del producto.

**Independent Test**: Se puede probar con un usuario que revisa la subasta y luego realiza una puja superior a la oferta vigente, verificando que la nueva oferta se convierta en la más alta y que los registros históricos se mantengan.

**Acceptance Scenarios**:

1. **Given** que una subasta está abierta, **When** un usuario distinto del propietario realiza una oferta superior a la más alta actual, **Then** la puja queda registrada como la oferta vigente.
2. **Given** que una subasta está abierta y un usuario intenta ofertar el mismo valor o un valor menor que la oferta vigente, **When** intenta enviar la puja, **Then** el sistema la rechaza.
3. **Given** que el propietario de la subasta intenta hacer una puja en su propia subasta, **When** envía la oferta, **Then** el sistema la bloquea y no crea una puja válida.
4. **Given** que un usuario repite la misma puja en la misma subasta de forma consecutiva, **When** intenta enviarla otra vez, **Then** el sistema no genera duplicados y mantiene una sola inscripción por usuario y subasta activa.

---

### User Story 3 - Cierre automático y transferencia final (Priority: P1)

Cuando la subasta se cierra por tiempo o por decisión del propietario, el sistema resuelve la venta: si hubo un mayor postor, los tokens pasan a su inventario y el valor de la puja se acredita al propietario; si no hubo mejor oferta, los tokens vuelven al inventario del vendedor.

**Why this priority**: Es el momento en que el valor real de la subasta se materializa en la transferencia de tokens y créditos, por lo que define la transacción final del negocio.

**Independent Test**: Se puede verificar con dos usuarios, uno como vendedor y otro como postor, cerrando la subasta y comprobando que la operación resultante refleja venta o devolución de tokens según corresponda.

**Acceptance Scenarios**:

1. **Given** que una subasta tiene un mayor postor al cierre, **When** la subasta finaliza, **Then** los tokens pasan al ganador y el valor de la puja se suma al crédito del propietario.
2. **Given** que una subasta no tiene un mayor postor al cierre por tiempo o cancelación, **When** termina, **Then** los tokens vuelven al inventario del propietario y no se realiza ninguna venta.
3. **Given** que una subasta ya está cerrada, **When** un usuario intenta pujar, **Then** el sistema rechaza la operación y no modifica el estado de la subasta.
4. **Given** que dos usuarios intentan pujar de forma simultánea, **When** la subasta procesa la operación, **Then** se resuelve con control de concurrencia para evitar inconsistencias en la oferta máxima.

---

### Edge Cases

- Si la subasta expira exactamente en el mismo minuto de la fecha de cierre establecida, la subasta se da por finalizada y no se puede interactuar con ella.
- El postor no se puede retirar de una subasta en la cual ofertó, y si sus créditos no alcanzan, no puede ofertar. Al momento de cerrar la subasta, si el mayor postor ya no cuenta con suficientes créditos, el sistema pasa a la siguiente oferta más alta válida; si ninguna oferta cumple, la subasta se cierra y los tokens vuelven al dueño.
- Si el propietario cierra la subasta antes de que exista una oferta válida, la subasta se cierra normalmente y los tokens vuelven a su posesión.
- En caso de que la misma persona intente pujar varias veces desde distintos dispositivos, no ocurre nada; se cumplen las mismas reglas previamente definidas y no se crean duplicados ni efectos adicionales.
- Los tokens del dueño se deben congelar hasta que termine la subasta, y si el mayor postor afectó sus créditos posteriormente a ofertar en la subasta, se pasa a la siguiente oferta más alta válida; así hasta encontrar una oferta válida, y si no se encuentra ninguna, se cierra la subasta y los tokens vuelven al dueño.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST permitir a un usuario crear una subasta de tokens de un jugador con una cantidad mayor o igual a 1 y una fecha de cierre definida.
- **FR-002**: El sistema MUST impedir la creación de una subasta con cantidades no válidas o sin fecha de cierre establecida.
- **FR-003**: El sistema MUST marcar los tokens dedicados a la subasta como ocupados o reservados mientras la subasta esté activa.
- **FR-004**: El sistema MUST mostrar la oferta más alta actual y el historial de ofertas previas a cualquier usuario que consulte la subasta.
- **FR-005**: El sistema MUST permitir que cualquier usuario distinto del propietario haga una oferta siempre que supere la oferta más alta vigente.
- **FR-006**: El sistema MUST bloquear las ofertas del propietario en su propia subasta.
- **FR-007**: El sistema MUST rechazar una oferta que no supere la oferta máxima vigente o que se haga sobre una subasta cerrada.
- **FR-008**: El sistema MUST garantizar que el mismo usuario no cree ofertas duplicadas en la misma subasta mediante una clave de idempotencia.
- **FR-009**: El sistema MUST aplicar control de concurrencia optimista cuando dos o más usuarios intentan pujar al mismo tiempo sobre una misma subasta.
- **FR-010**: El sistema MUST cerrar la subasta cuando el propietario la finaliza de forma manual o cuando se alcanza la fecha y hora de finalización indicada.
- **FR-011**: El sistema MUST devolver los tokens al inventario del propietario cuando la subasta finaliza sin un mayor postor válido.
- **FR-012**: El sistema MUST transferir los tokens al mayor postor cuando existe una oferta válida al momento del cierre.
- **FR-013**: El sistema MUST sumar el valor de la puja al crédito del propietario y descontarlo del ganador en la liquidación final.
- **FR-014**: El sistema MUST generar una operación de transacción para el propietario y para el mayor postor al completar una venta exitosa.
- **FR-015**: El sistema MUST mantener la oferta más alta como registro inmutable de la subasta y no permitir su retiro cuando es la vigente.
- **FR-016**: El sistema MUST conservar el historial de ofertas para permitir la consulta de la evolución de la puja y la evidencia de la resolución final.

### Key Entities *(include if feature involves data)*

- **Subasta de tokens**: Representa la venta de una cantidad específica de tokens de un jugador, con propietario, cantidad, fecha de cierre y estado activo o cerrado.
- **Oferta**: Registra el valor ofertado por un usuario, la subasta asociada, el tiempo de la puja y si corresponde a la oferta actual más alta.
- **Usuario**: Representa a un participante del mercado con créditos y propiedad de tokens, capaz de poner en subasta, ofertar y recibir la liquidación final.
- **Token del jugador**: Es el activo comercial que se pone a la venta; puede estar disponible, reservado u ocupado mientras pertenece a una subasta activa.
- **Operación de transacción**: Registra la venta final entre el propietario y el mayor postor, con el valor y los activos involucrados.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 95% de las subastas creadas con datos válidos quedan activas y visibles para los usuarios dentro de los primeros 30 segundos de su publicación.
- **SC-002**: El 99% de las ofertas válidas se procesa sin errores de concurrencia y se reflejan en la oferta más alta correcta.
- **SC-003**: El 90% de los usuarios pueden completar el flujo completo de creación, puja y cierre de una subasta sin asistencia técnica.
- **SC-004**: El sistema resuelve correctamente el 100% de las subastas cerradas, garantizando que los tokens queden con el propietario correcto o con el mayor postor según corresponda.
- **SC-005**: Los usuarios pueden entender el estado de la subasta y la liquidación resultante sin requerir explicación técnica adicional.

## Assumptions

- La plataforma ya cuenta con usuarios, créditos y propiedad de tokens como base del mercado y se reutiliza ese estado para la subasta.
- Una subasta puede tener cero, una o varias ofertas, pero solo una oferta máxima vigente en un momento dado.
- La fecha y hora de cierre se interpreta en la zona horaria del sistema para evitar ambigüedades en la resolución por tiempo.
- Las operaciones de liquidación final se ejecutan en un único proceso de cierre para mantener la consistencia del inventario, créditos y transacciones.
- El comportamiento de las pujas duplicadas se resuelve utilizando la identidad del usuario y la subasta como clave de idempotencia.
