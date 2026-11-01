# Contrato: marketplace de tokens de jugadores

## Endpoint: GET /api/marketplace/listings

Obtiene el listado paginado de ventas activas para usuarios del marketplace.

### Query parameters

| Nombre | Tipo | Requerido | Descripción |
|--------|------|-----------|-------------|
| page | integer | No | Número de página. Valor mínimo: 1 |
| perPage | integer | No | Cantidad máxima de anuncios por página. Valor por defecto: 32 |
| jugador | string | No | Filtro parcial por nombre del jugador |
| vendedor | string | No | Filtro parcial por nombre del vendedor |

### Respuesta exitosa

```json
{
  "anuncios": [
    {
      "id": "uuid",
      "jugador": "Kylian Mbappé",
      "cantidadTokens": 5,
      "precioUnitario": 15.5,
      "precioTotal": 77.5,
      "vendedor": "usuarioA",
      "estado": "PENDIENTE"
    }
  ],
  "paginacion": {
    "pagina": 1,
    "porPagina": 32,
    "total": 96,
    "totalPaginas": 3
  }
}
```

## Endpoint: POST /api/orders/sell

Crea una nueva venta de tokens del usuario autenticado.

### Request body

```json
{
  "usuarioId": "uuid",
  "jugadorId": "uuid",
  "cantidadTokens": 5
}
```

### Respuesta exitosa

```json
{
  "id": "uuid",
  "estado": "PENDIENTE",
  "cantidadTokens": 5,
  "precioUnitario": 15.5,
  "precioTotal": 77.5
}
```

### Validaciones

- La cantidad debe ser un entero mayor a cero.
- La cantidad no puede superar la cantidad disponible del usuario.
- El precio unitario es determinado por el sistema.

## Endpoint: POST /api/orders/buy

Completa la compra de un anuncio activo de otro usuario.

### Request body

```json
{
  "compradorId": "uuid",
  "anuncioVentaId": "uuid"
}
```

### Respuesta exitosa

```json
{
  "id": "uuid",
  "tipo": "COMPRA",
  "estado": "COMPLETADA",
  "precioTotal": 77.5,
  "compradorId": "uuid",
  "vendedorId": "uuid"
}
```

### Validaciones

- No se permite comprar un anuncio propio.
- No se permite comprar anuncios no activos.
- El comprador debe tener créditos suficientes.
- Si la venta ya fue completada, la compra debe rechazarse.

## Endpoint: GET /api/users/{id}/transactions

Retorna el historial de compra/venta del usuario.

### Respuesta exitosa

```json
{
  "operaciones": [
    {
      "id": "uuid",
      "tipo": "VENTA",
      "estado": "COMPLETADA",
      "jugador": "Kylian Mbappé",
      "cantidadTokens": 3,
      "precioUnitario": 15.5,
      "precioTotal": 46.5,
      "usuarioRealizoOperacion": "usuarioA",
      "usuarioContraparte": "usuarioB",
      "fechaOperacion": "2026-11-01T20:00:00"
    }
  ]
}
```

## Consideraciones

- La API debe operar en español para los mensajes de negocio y errores de validación.
- El historial es de solo lectura para el usuario involucrado.
- Todas las operaciones de compra y venta deben producir una operación de registro persistente y auditable.
