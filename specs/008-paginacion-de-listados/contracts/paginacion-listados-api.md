# Contrato de paginación para listados de usuario y mercado

## General

Todos los endpoints listados a continuación deben aceptar `page` y `size` como query params; `page` es base 1 y `size` debe ser un entero positivo. El backend debe responder con un envelope paginado para permitir que el frontend controle estados de vacío, final de lista y límites de navegación.

## Envelope de respuesta paginada

```json
{
  "content": [
    {}
  ],
  "page": 1,
  "size": 10,
  "totalElements": 42,
  "totalPages": 5,
  "hasNext": true,
  "hasPrevious": false
}
```

## Endpoints

### 1. Inventario del usuario

**GET** `/api/users/{id}/inventario?page=1&size=10`

**Respuesta exitosa**: `PageResponse<InventarioUsuarioResponse>`

**Comportamiento**:
- Si el usuario no posee tokens, devuelve `content: []` y el frontend puede mostrar "Inventario vacío".
- Si la página solicitada está fuera de rango, la API debe devolver la última página válida o una página vacía con `hasPrevious`/`hasNext` coherentes.

### 2. Historial de operaciones

**GET** `/api/users/{id}/transactions?page=1&size=10`

**Respuesta exitosa**: `PageResponse<OperacionResponse>`

**Comportamiento**:
- Si el usuario no tiene operaciones, devuelve `content: []` y el frontend puede mostrar "No hay operaciones registradas".
- La respuesta debe mantener un orden estable para la navegación por páginas.

### 3. Mercado

**GET** `/api/marketplace/listings?page=1&size=20`

**Respuesta exitosa**: `PageResponse<AnuncioVentaResponse>`

**Comportamiento**:
- Si no existen anuncios activos, devuelve `content: []` y el frontend puede mostrar "No existen anuncios publicados actualmente".
- La grilla debe paginar en múltiplos de 20.

### 4. Subastas activas

**GET** `/api/subastas?page=1&size=10`

**Respuesta exitosa**: `PageResponse<SubastaResponse>`

**Comportamiento**:
- El frontend usará la paginación para cargar bloques adicionales por scroll.
- Cuando `hasNext` sea `false`, se debe mostrar "Fin de las subastas activas".
- Si no hay subastas, la respuesta debe estar vacía y el frontend mostrar "No existen subastas activas actualmente".

## Reglas de integración

- El frontend debe enviar `Authorization` cuando el endpoint requiera autenticación.
- El backend debe validar `page >= 1` y `size > 0` antes de construir `PageRequest`.
- Todas las respuestas deben estar en español o ser convertidas a textos de usuario en el frontend sin perder la semántica del endpoint.
