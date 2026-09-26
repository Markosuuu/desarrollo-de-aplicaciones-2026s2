# Investigación: catálogo de jugadores en Home

## Decisión

Se reutiliza el endpoint existente de consulta del catálogo y la estructura actual de frontend para implementar la vista de Home. La página consultará `GET /api/players` con parámetros de búsqueda y paginación, y renderizará una grilla de tarjetas con nombre, equipo, liga y posición.

## Fundamentos

- El backend ya expone `PlayerController.getPlayers(...)` con filtros por `liga`, `equipo`, `nombre`, `page` y `perPage`, lo que cubre la necesidad del feature sin agregar una nueva API.
- El servicio `PlayerCatalogService.search(...)` ya devuelve un `CatalogResponse` paginado con `jugadores` y `paginacion`, por lo que la UI puede consumir directamente los resultados y evitar lógica duplicada.
- La home actual ya tiene un layout base para la vista autenticada, así que el cambio se concentra en la página principal y en el estilo de las tarjetas y el buscador.
- El repositorio ya persiste la entidad `Jugador`, lo que permite mostrar registros reales en vez de datos locales simulados.

## Alternativas consideradas

- Crear una nueva ruta de backend exclusiva para Home: descartada porque el endpoint existente ya resuelve búsqueda, filtros y paginación sin introducir más superficie de API.
- Filtrar únicamente en frontend sobre una lista completa recibida en una sola respuesta: descartada porque limita la escalabilidad del catálogo y no reutiliza el comportamiento ya implementado.
- Renderizar solo listados sin soporte de paginación: descartada porque la especificación exige navegar entre más de 20 registros.

## Implicaciones de implementación

- La UI debe mantener un estado de búsqueda y de página actual para sincronizar los parámetros enviados al backend.
- La grilla debe respetar un máximo de 20 jugadores por página y mostrar 4 columnas visibles en desktop.
- Cuando el backend devuelve cero resultados, el componente debe mostrar un mensaje claro de “sin resultados”.
- El cambio debe quedar en la página de Home y no afectar el flujo de autenticación ni la estructura de navegación actual.
