# Guía rápida de validación: marketplace de tokens de jugadores

## Requisitos

- Backend levantado localmente.
- Frontend levantado localmente.
- Base de datos con usuarios, jugadores y al menos una venta activa de prueba.

## 1. Levantar backend

```bash
cd backend
./gradlew bootRun
```

## 2. Levantar frontend

```bash
cd frontend
npm install
npm run dev
```

## 3. Validar listado de anuncios

```bash
curl "http://localhost:8080/api/marketplace/listings?page=1&perPage=32"
```

### Resultado esperado

- Responde con una grilla paginada de anuncios.
- Cada anuncio incluye nombre del jugador, tokens disponibles, precio por token, precio total, vendedor y botón de compra.
- Cuando hay más de 32 anuncios, la paginación debe mover el conjunto de resultados sin duplicar entradas.

## 4. Validar venta de tokens

```bash
curl -X POST http://localhost:8080/api/orders/sell \
  -H "Content-Type: application/json" \
  -d '{
    "usuarioId": "<usuario-id>",
    "jugadorId": "<jugador-id>",
    "cantidadTokens": 5
  }'
```

### Resultado esperado

- La operación se acepta solo si el usuario tiene al menos 5 tokens disponibles.
- La publicación queda en estado `PENDIENTE` y los tokens quedan reservados para esa venta.
- Si la cantidad es cero, negativa o mayor a la disponible, la operación falla con validación.

## 5. Validar compra de tokens

```bash
curl -X POST http://localhost:8080/api/orders/buy \
  -H "Content-Type: application/json" \
  -d '{
    "compradorId": "<comprador-id>",
    "anuncioVentaId": "<anuncio-id>"
  }'
```

### Resultado esperado

- La compra solo puede completarse si el comprador tiene créditos suficientes.
- Se descuentan los créditos del comprador y se acreditan al vendedor.
- Los tokens se transfieren al comprador y el anuncio pasa a estado `COMPLETADA`.
- El sistema rechaza compras propias, compras sin stock disponible o compras con saldo insuficiente.

## 6. Validar historial de operaciones

```bash
curl "http://localhost:8080/api/users/<usuario-id>/transactions"
```

### Resultado esperado

- El usuario recibe un historial con tipo, estado, nombre del jugador, cantidad, precio por token, total, contraparte y fecha.
- Las operaciones completadas, pendientes y canceladas se identifican con claridad.

## Resultado esperado

- El flujo principal de compra y venta queda funcional con validación de negocio.
- El marketplace mantiene consistencia entre créditos, stock y estado de venta.
- El historial registra todas las transacciones para trazabilidad del usuario.
