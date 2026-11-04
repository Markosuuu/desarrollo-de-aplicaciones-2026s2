# Contrato de API: Subasta de tokens

## 1. Crear subasta

**Endpoint**: `POST /api/subastas`

**Request body**:
```json
{
  "jugadorId": "uuid",
  "cantidad": 3,
  "precioInicial": 150.00,
  "vencimiento": "2026-11-12T18:30:00"
}
```

**Validaciones**:
- `cantidad` debe ser mayor o igual a 1
- `precioInicial` debe ser positivo
- `vencimiento` debe ser fecha futura
- el usuario debe ser propietario de suficientes tokens del jugador

**Response**:
```json
{
  "id": "uuid",
  "estado": "ACTIVA",
  "cantidadOfrecida": 3,
  "ofertaActual": null,
  "vencimiento": "2026-11-12T18:30:00"
}
```

## 2. Listar subastas activas

**Endpoint**: `GET /api/subastas?page=1&size=10`

**Response**:
```json
{
  "items": [
    {
      "id": "uuid",
      "jugadorId": "uuid",
      "cantidadOfrecida": 3,
      "ofertaActual": 180.00,
      "vencimiento": "2026-11-12T18:30:00"
    }
  ],
  "page": 1,
  "size": 10
}
```

## 3. Obtener detalle de una subasta activa

**Endpoint**: `GET /api/subastas/{id}`

**Response**:
```json
{
  "id": "uuid",
  "propietarioId": "uuid",
  "jugadorId": "uuid",
  "cantidadOfrecida": 3,
  "ofertaActual": 180.00,
  "ofertasPrevias": [
    { "usuarioId": "uuid", "puja": 150.00 },
    { "usuarioId": "uuid", "puja": 180.00 }
  ],
  "estado": "ACTIVA",
  "vencimiento": "2026-11-12T18:30:00"
}
```

## 4. Realizar oferta

**Endpoint**: `POST /api/subastas/ofertar`

**Request body**:
```json
{
  "subastaId": "uuid",
  "puja": 200.00,
  "idempotencyKey": "unique-key"
}
```

**Validaciones**:
- `puja` debe ser mayor que la oferta actual más alta
- el usuario no puede ser propietario de la subasta
- la subasta no puede estar cerrada
- la misma clave de idempotencia no debe duplicar ofertas por usuario y subasta

**Response**:
```json
{
  "id": "uuid",
  "subastaId": "uuid",
  "usuarioId": "uuid",
  "puja": 200.00,
  "estado": "VIGENTE"
}
```

## 5. Cerrar subasta

**Endpoint**: `PATCH /api/subastas/{id}/cerrar`

**Request body**:
```json
{
  "motivo": "POR_TIEMPO"
}
```

**Validaciones**:
- solo el propietario puede cerrar la subasta
- una subasta cerrada no acepta nuevas ofertas
- la resolución debe evaluar la oferta válida más alta y recuperar tokens si no existe ganador

**Response**:
```json
{
  "id": "uuid",
  "estado": "CERRADA",
  "ganadorId": "uuid",
  "montoFinal": 200.00,
  "resultado": "VENTA_O_RETORNO"
}
```

## 6. Historial de ofertas

**Endpoint**: `GET /api/subastas/{id}/ofertas`

**Response**:
```json
[
  { "usuarioId": "uuid", "puja": 150.00, "timestamp": "2026-11-04T12:00:00" },
  { "usuarioId": "uuid", "puja": 200.00, "timestamp": "2026-11-04T12:05:00" }
]
```
