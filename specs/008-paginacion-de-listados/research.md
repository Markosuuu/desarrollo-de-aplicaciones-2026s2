# Research: paginación de listados del usuario y mercado

## Decision 1: usar `Page` de Spring Data para todos los listados paginados

**Decision**: Los endpoints de inventario, historial, mercado y subastas deben devolver `Page<T>` desde servicio y persistencia, y el controller debe convertir su contenido a una respuesta paginada con metadatos útiles para el frontend.

**Rationale**: El proyecto ya usa `Page` y `Pageable` en repositorios de mercado y subastas (`MercadoService`, `SubastaService`, `JugadorRepository`, etc.). Esto reduce duplicación de lógica, conserva la arquitectura en capas y mantiene el comportamiento consistente con el resto del backend.

**Alternatives considered**:
- Cargar listas completas y paginar en frontend: descartado porque el backend ya resuelve la paginación y evita sobrecarga innecesaria.
- Crear paginación manual con listas y cálculos en controlador: descartado porque duplica la lógica de negocio y rompe la separación de capas.

## Decision 2: definir un contrato de página reusable para el frontend

**Decision**: Todos los listados paginados tendrán un envelope con `content`, `page`, `size`, `totalElements`, `totalPages`, `hasNext` y `hasPrevious`.

**Rationale**: El frontend necesita saber si hay más páginas, cuándo bloquear botones y cuándo mostrar mensajes vacíos o de fin de contenido. El envelope normaliza la UX en todas las vistas.

**Alternatives considered**:
- Devolver solo `List<T>` y manejar el cálculo en UI: descartado porque requiere lógica repetida y puede desincronizar el estado.
- Devolver solo `totalPages` sin la lista: descartado porque el frontend necesita el contenido real.

## Decision 3: separar la navegación por páginas del scroll infinito de subastas

**Decision**: Inventario, historial y mercado usarán paginación con botones; el listado de subastas usará carga incremental con observador de scroll y un mensaje final cuando no haya más resultados.

**Rationale**: La especificación define un comportamiento distinto para subastas: carga incremental al llegar al final del scroll, mientras que mercado e inventario requieren navegación discreta por páginas.

**Alternatives considered**:
- Usar botones para todas las vistas: descartado porque contradice el requisito del listado de subastas.
- Usar scroll infinito en todas las listas: descartado porque la experiencia del mercado y el inventario requiere mejor control por página.

## Decision 4: centralizar mensajes de estado y estados vacíos en el frontend

**Decision**: El frontend debe manejar mensajes como "Inventario vacío", "No hay operaciones registradas", "No existen anuncios publicados actualmente" y "Fin de las subastas activas" a partir del estado de la respuesta y de la ausencia de elementos.

**Rationale**: Los requisitos de UX especifican textos de estado distintos según la vista y la disponibilidad de datos; mantenerlos en UI reduce errores de consistencia entre pantallas.

**Alternatives considered**:
- Poner mensajes en backend: descartado porque los textos son de presentación y no de negocio.
- Reutilizar una sola frase para todas las vistas: descartado porque la especificación exige mensajes distintos y verificables.

## Risks and mitigations

- **Riesgo**: la UI mantiene páginas desfasadas si el backend cambia el tamaño de la lista en tiempo real.
  - **Mitigación**: recalcular `page` y `totalPages` con cada respuesta; evitar páginas fuera de rango y bloquear controles en extremos.
- **Riesgo**: scroll infinito dispara cargas duplicadas.
  - **Mitigación**: bloquear la carga mientras `loading` sea `true`, y considerar `hasNext`/`totalElements` para prevenir peticiones extra.
- **Riesgo**: la API actual devuelve listas simples en varias rutas.
  - **Mitigación**: extender los controladores sin romper compatibilidad; adaptar los clientes frontend a los nuevos envelopes paginados.
