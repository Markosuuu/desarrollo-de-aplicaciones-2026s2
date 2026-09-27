# Modelo de datos: catálogo de jugadores en Home

## Entidad: Jugador

Representa cada registro del catálogo disponible para la vista de Home.

| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | Identificador interno del registro |
| nombre | string | Nombre del jugador visible en la tarjeta |
| equipo | string | Nombre del equipo al que pertenece |
| liga | string | Liga o competencia del jugador |
| posicion | string | Posición deportiva principal |

Reglas de validación:
- `nombre`, `equipo` y `liga` deben existir y no quedar vacíos.
- `posicion` puede ser nula si el registro no la incluye, pero en la vista se mostrará como dato opcional.
- Los registros se presentan a partir del listado persistido en la base de datos.

## Entidad: CatalogResponse

Representa la respuesta paginada devuelta desde el backend.

| Campo | Tipo | Notas |
|-------|------|-------|
| jugadores | Lista<Jugador> | Resultado visible para la página actual |
| paginacion | Paginacion | Metadatos de página, total y cantidad de páginas |

### Paginacion

| Campo | Tipo | Notas |
|-------|------|-------|
| pagina | integer | Número de página actual |
| porPagina | integer | Cantidad máxima de jugadores por página |
| total | long | Total de registros coincidentes |
| totalPaginas | integer | Cantidad de páginas disponibles |

## Relaciones

- La vista de Home consume una colección de `jugadores` obtenida por medio del servicio del backend.
- La lista está paginada; cada página contiene hasta 20 jugadores y el total de páginas se calcula a partir del total general.
- El filtrado por nombre, equipo o liga reduce el conjunto de resultados sin alterar la estructura del modelo de dominio.

## Reglas de presentación

- La grilla de la página mostrará una tarjeta por jugador con el nombre, equipo, liga y posición.
- La paginación solo debe mostrarse cuando el total de páginas es mayor a 1.
- El filtro no debe producir resultados vacíos sin un mensaje visible. 
