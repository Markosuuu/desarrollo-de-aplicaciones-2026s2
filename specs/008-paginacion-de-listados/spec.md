# Feature Specification: Paginación de listados del usuario y mercado

**Feature Branch**: `[008-paginacion-de-listados]`

**Created**: 2026-11-08

**Status**: Draft

**Input**: User description: "Se pide que las siguientes secciones contemplen un sistema de paginación para visualizar los datos de manera más ordenada y no scrollear infinitamente entre varios elementos.\n\n### Inventario del usuario\nUna sección de botones de paginación centrados al contenedor que permita moverse de derecha a izquierda entre el listado de tokens de un jugador. El listado debe mostrar como máximo 10 tokens y luego paginar. Si el usuario no tiene tokens en su inventario, se muestra el mensaje por defecto "Inventario vacío".\n\n### Historial del usuario\nUna sección de botones de paginación centrados al contenedor que permita moverse de derecha a izquierda entre el historial de operaciones de un jugador. El listado debe mostrar como máximo 10 operaciones y luego paginar. Si el usuario no operaciones realizadas, aparecerá un mensaje de "No hay operaciones registradas".\n\n### Mercado\nEn el mercado, se pagina por cada 20 elementos de la grilla que se muestren, después de eso hay una sección de botones centrados en el medio que permita moverse de derecha a izquierda entre los anuncios de venta del mercado. Si no existen ofertas en el mercado, debe aparecer un mensaje que diga "No existen anuncios publicados actualmente".\n\n### Subastas\nEn el listado de subastas se pagina mediante un scroll infinito, por cada que se llegue a la parte inferior del listado, se pagina y se trae un nuevo contenido. Una vez que no existan más subastas, aparecerá un mensaje que diga "Fin de las subastas activas". De no existir ninguna subasta para el listado, debe aparecer un mensaje que diga "No existen subastas activas actualmente".\n\n## Limites\n- Si el usuario está en la primera página, el botón para moverse a la izquierda aparecerá bloqueado, mientras que si el usuario está en la última página, se bloqueará el de la derecha."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Revisar el inventario del jugador (Priority: P1)

Un jugador necesita ver sus tokens ordenados en páginas para identificar rápidamente su colección sin recorrer una lista excesivamente larga.

**Why this priority**: Es la vista principal del usuario cuando consulta su inventario y define la base de la navegación por páginas de su patrimonio digital.

**Independent Test**: Se puede validar accediendo a un inventario con más de 10 tokens y comprobando que solo se muestran 10 por página, con la navegación disponible y los mensajes de vacío cuando corresponde.

**Acceptance Scenarios**:

1. **Given** que un usuario tiene más de 10 tokens en su inventario, **When** abre la sección, **Then** se muestran solo 10 elementos por página y aparece la navegación de paginación.
2. **Given** que un usuario tiene exactamente 10 o menos tokens, **When** abre la sección, **Then** no se requiere paginación adicional y se visualiza el inventario completo.
3. **Given** que un usuario no tiene tokens, **When** entra a la sección, **Then** se muestra el mensaje "Inventario vacío".
4. **Given** que el usuario se encuentra en la primera página del inventario, **When** intenta avanzar hacia la izquierda, **Then** el botón queda bloqueado y no se mueve a una página anterior.

---

### User Story 2 - Consultar el historial de operaciones (Priority: P1)

Un jugador necesita revisar su historial de movimientos en bloques manejables para analizar operaciones pasadas sin sobrecargar la vista.

**Why this priority**: El historial es una vista de consulta frecuente y debe permitir navegación clara sin pérdida de contexto ni scroll infinito.

**Independent Test**: Se puede verificar creando varios movimientos y comprobando que cada página muestra como máximo 10 operaciones, además de los mensajes de estado cuando no existen registros.

**Acceptance Scenarios**:

1. **Given** que un usuario tiene más de 10 operaciones registradas, **When** abre el historial, **Then** el sistema muestra hasta 10 resultados por página y habilita la paginación.
2. **Given** que un usuario no tiene operaciones realizadas, **When** abre el historial, **Then** se muestra el mensaje "No hay operaciones registradas".
3. **Given** que el usuario está en la última página del historial, **When** intenta avanzar hacia la derecha, **Then** el botón queda bloqueado y no se mueve más allá del límite.

---

### User Story 3 - Explorar el mercado por páginas (Priority: P1)

Un usuario del mercado necesita navegar por anuncios de venta en bloques definidos para comparar ofertas sin perder tiempo en listas muy largas.

**Why this priority**: El mercado es una vista de discovery y comparación, por lo que la paginación ordenada es fundamental para la experiencia de compra y venta.

**Independent Test**: Se puede probar cargando más de 20 anuncios y comprobando que la grilla se divide en bloques de 20, con botones para avanzar y retroceder según la página actual.

**Acceptance Scenarios**:

1. **Given** que existen más de 20 anuncios en el mercado, **When** la grilla se carga por primera vez, **Then** se muestran 20 elementos y la paginación aparece centrada en la vista.
2. **Given** que no existen anuncios publicados, **When** un usuario accede al mercado, **Then** se muestra el mensaje "No existen anuncios publicados actualmente".
3. **Given** que el usuario está en la primera página del mercado, **When** intenta navegar hacia la izquierda, **Then** el botón queda bloqueado.
4. **Given** que el usuario está en la última página del mercado, **When** intenta avanzar hacia la derecha, **Then** el botón queda bloqueado.

---

### User Story 4 - Consultar subastas activas con carga incremental (Priority: P1)

Un usuario que revisa subastas necesita un flujo de navegación fluido donde el contenido se va cargando conforme llega al final de la lista sin necesidad de nuevas pantallas.

**Why this priority**: El listado de subastas es un flujo de exploración continua y la carga incremental reduce la fricción de navegación en colecciones extensas.

**Independent Test**: Se puede validar desplazando el listado hacia el final y comprobando que se cargan más subastas, o que se muestra el mensaje final cuando ya no hay más registros disponibles.

**Acceptance Scenarios**:

1. **Given** que existen subastas activas disponibles, **When** el usuario llega al final del listado, **Then** se carga un nuevo bloque de contenido y la vista continúa sin reiniciar la búsqueda.
2. **Given** que ya no existen más subastas activas disponibles, **When** el usuario llega al final del listado, **Then** aparece el mensaje "Fin de las subastas activas".
3. **Given** que no hay subastas activas para mostrar, **When** se abre el listado, **Then** aparece el mensaje "No existen subastas activas actualmente".

---

### Edge Cases

- Si la cantidad de elementos en una lista es exactamente igual a un múltiplo del tamaño de página, la vista debe mostrar la última página sin errores ni elementos duplicados.
- Si el usuario intenta navegar más allá del primer o último bloque de una lista, el sistema debe mantener el límite de página actual sin romper la interfaz.
- Si una lista cambia dinámicamente mientras el usuario está navegando, la vista debe conservar la consistencia del estado actual y evitar mostrar páginas vacías o fuera de rango.
- Si la lista de subastas no tiene contenido inicial, la vista debe mostrar el estado vacío y no debe disparar carga adicional repetida.
- Si la lista de subastas ya está agotada, el mensaje final debe permanecer visible hasta que cambie el conjunto de datos disponible.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST permitir que el inventario del usuario se muestre en páginas con un máximo de 10 tokens por página.
- **FR-002**: El sistema MUST mostrar el mensaje "Inventario vacío" cuando un usuario no tiene tokens en su inventario.
- **FR-003**: El sistema MUST ofrecer una sección de paginación centrada para navegar entre páginas del inventario de derecha a izquierda.
- **FR-004**: El sistema MUST bloquear el botón de navegación izquierda cuando el usuario está en la primera página del inventario.
- **FR-005**: El sistema MUST bloquear el botón de navegación derecha cuando el usuario está en la última página del inventario.
- **FR-006**: El sistema MUST permitir que el historial de operaciones del usuario se muestre en páginas con un máximo de 10 operaciones por página.
- **FR-007**: El sistema MUST mostrar el mensaje "No hay operaciones registradas" cuando el usuario no tiene operaciones en su historial.
- **FR-008**: El sistema MUST ofrecer una sección de paginación centrada para navegar entre páginas del historial de operaciones de derecha a izquierda.
- **FR-009**: El sistema MUST bloquear el botón de navegación izquierda cuando el usuario está en la primera página del historial.
- **FR-010**: El sistema MUST bloquear el botón de navegación derecha cuando el usuario está en la última página del historial.
- **FR-011**: El sistema MUST paginar el mercado en bloques de 20 elementos por grilla y mostrar una sección de paginación centrada entre los anuncios disponibles.
- **FR-012**: El sistema MUST mostrar el mensaje "No existen anuncios publicados actualmente" cuando no hay ofertas disponibles en el mercado.
- **FR-013**: El sistema MUST bloquear el botón de navegación izquierda cuando el usuario está en la primera página del mercado.
- **FR-014**: El sistema MUST bloquear el botón de navegación derecha cuando el usuario está en la última página del mercado.
- **FR-015**: El sistema MUST cargar más subastas al llegar al final del listado mediante scroll infinito sin requerir una nueva pantalla.
- **FR-016**: El sistema MUST mostrar el mensaje "Fin de las subastas activas" cuando ya no existen más subastas disponibles para cargar.
- **FR-017**: El sistema MUST mostrar el mensaje "No existen subastas activas actualmente" cuando el listado de subastas no tiene contenido disponible.
- **FR-018**: El sistema MUST evitar que la navegación por páginas permita ingresar a un rango inexistente fuera del primer o último bloque disponible en cada listado.

### Key Entities *(include if feature involves data)*

- **Inventario del usuario**: Representa la colección de tokens que posee un jugador, ordenada y separada en páginas para facilitar su revisión.
- **Historial de operaciones**: Registra los movimientos realizados por un jugador y se presenta en bloques paginados para consulta ordenada.
- **Anuncio del mercado**: Corresponde a una oferta de venta visible en el mercado y se agrupa en páginas de 20 elementos.
- **Subasta activa**: Representa una oferta pública de compra o venta pendiente, que se carga en bloques progresivos con scroll infinito.
- **Paginación**: Define el control de navegación para cada listado, incluyendo el estado actual, los límites de página y los mensajes vacíos o finales.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: El 95% de los usuarios pueden navegar por el inventario, historial, mercado y subastas sin necesidad de desplazamiento infinito excesivo ni pérdida de contexto.
- **SC-002**: En cada vista paginada, al menos el 99% de los elementos visibles corresponden al bloque de datos correcto sin duplicados, omisiones ni páginas fuera de rango.
- **SC-003**: El 90% de los usuarios completan el recorrido de visualización de cada listado en menos de 10 segundos tras abrir la vista inicial, sin requerir ayuda adicional.
- **SC-004**: Los mensajes de estado vacío y final de carga son visibles en el 100% de los casos en los que no existen registros o ya no hay más contenido disponible.
- **SC-005**: Los usuarios perciben que la navegación por páginas es clara y estable, con más del 85% de satisfacción en evaluaciones de usabilidad centradas en la organización de contenido.

## Assumptions

- Los listados de inventario, historial, mercado y subastas ya existen como fuentes de datos ordenadas y son accesibles para su paginación y presentación.
- Cada vista puede mostrar un estado vacío o final de contenido sin requerir cambios en la fuente de datos base.
- La navegación por páginas se basa en el orden de presentación del sistema para evitar inconsistencias entre bloques de contenido.
- Las páginas del mercado e inventario son de lectura, sin acciones de edición en la misma vista, y los botones de navegación deben reflejar el estado actual del listado.
- La carga incremental de subastas se activa únicamente cuando el usuario alcanza el extremo del listado disponible y no se repite indefinidamente cuando ya no existen más registros.
