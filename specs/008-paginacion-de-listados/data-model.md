# Data Model: paginación de listados del usuario y mercado

## Overview

La feature no introduce un nuevo modelo de dominio principal; en cambio, redefine la representación de los listados existentes para que el sistema exponga contenido paginado y un estado claro de finalización o vacío. Los modelos relevantes siguen estando en la capa `models` del backend.

## Entidades y atributos

### Inventario del usuario

**Entidad**: `InventarioUsuario`

**Campos clave**:
- `id`: identificador del registro de inventario.
- `usuario`: usuario propietario del inventario.
- `jugador`: jugador asociado al token.
- `tokensDisponibles`: cantidad disponible para vender o consumir.
- `tokensEnVenta`: cantidad actualmente reservada para anuncios activos.
- `estado`: estado del token o de la asignación del usuario al jugador.

**Reglas de validación**:
- La cantidad disponible no puede ser negativa.
- Los tokens reservados no deben contarse como disponibles.
- El inventario vacío debe producir una respuesta de contenido vacío con el mensaje de UI correspondiente.

### Historial de operaciones

**Entidad**: `Operacion`

**Campos clave**:
- `id`: identificador de la operación.
- `tipo`: compra, venta, transferencia, cierre de subasta o similar.
- `estado`: estado actual de la operación.
- `creadaEn`: fecha de generación de la transacción.
- `cantidad`: número de tokens involucrados.
- `precioPorToken`: valor unitario asociado.
- `total`: valor total de la operación.
- `contraparteNombre`: nombre del usuario relacionado.
- `nombreJugador`: jugador involucrado.

**Reglas de validación**:
- La cantidad debe ser mayor que cero.
- El valor total debe ser consistente con la cantidad y el precio.
- El historial paginado debe conservar el orden cronológico inverso según corresponda al sistema.

### Anuncio del mercado

**Entidad**: `AnuncioVenta`

**Campos clave**:
- `id`: identificador del anuncio.
- `vendedor`: usuario que publica la venta.
- `jugador`: jugador asociado al anuncio.
- `cantidadTokens`: cantidad disponible para venta.
- `precioPorToken`: precio unitario.
- `precioTotal`: resultado del cálculo del anuncio.
- `estado`: estado del anuncio activo/cancelado/completado.

**Reglas de validación**:
- `cantidadTokens` debe ser mayor que cero.
- El anuncio solo debe ser visible si su estado está activo.
- El listado paginado debe respetar el tamaño de grilla definido por la especificación (20).

### Subasta activa

**Entidad**: `Subasta`

**Campos clave**:
- `id`: identificador de la subasta.
- `usuario`: propietario que publica tokens.
- `jugador`: jugador asociado a la oferta.
- `cantidad`: tokens destinados a la subasta.
- `precioInicial`: base de la oferta.
- `vencimiento`: fecha y hora de cierre.
- `activa`: estado del ciclo de vida.
- `ofertas`: historial de pujas relacionadas.

**Reglas de validación**:
- El cierre se produce por tiempo o por acción manual del propietario.
- La subasta debe ocultarse del listado de resultados activos una vez cerrada.
- Cuando no hay más elementos, la UI debe mostrar el estado final del scroll.

### Paginación

**Entidad**: `PageResponse<T>` (no persistente)

**Campos clave**:
- `content`: lista de elementos de la página actual.
- `page`: número de página actual (base 1).
- `size`: cantidad de elementos por página.
- `totalElements`: total general del conjunto.
- `totalPages`: total de páginas calculado.
- `hasNext`: indicador de más páginas disponibles.
- `hasPrevious`: indicador de página previa disponible.

**Reglas de validación**:
- `page` no puede ser menor que 1.
- `size` debe ser un valor positivo y validado por el servicio.
- El frontend debe mantener el estado actual y bloquear navegación fuera de rango.

## Relaciones

- Un `Usuario` tiene muchos `InventarioUsuario` y muchas `Operacion`.
- Un `AnuncioVenta` pertenece a un `Usuario` y a un `Jugador`.
- Una `Subasta` pertenece a un `Usuario` y a un `Jugador` y puede tener muchas `Oferta`.
- Los listados paginados no cambian la relación del dominio; sólo agregan una capa de presentación para el cliente.

## State transitions

### Inventario
- `disponible` -> `reservado` cuando se publica un anuncio.
- `reservado` -> `disponible` cuando la venta se cancela o no hay venta finalizada.

### Mercado
- `activo` -> `cancelado` -> `inactivo` cuando el vendedor retira el anuncio.
- `activo` -> `completado` al finalizar una compra válida.

### Subasta
- `activa` -> `cerrada` cuando se alcanza la fecha o el propietario la cierra.
- `cerrada` -> `resuelta` al liquidar la oferta final válida.
