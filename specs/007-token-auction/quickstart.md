# Quickstart: validación funcional de la subasta

## Requisitos previos

- Backend del proyecto corriendo en entorno local
- Base de datos configurada para la aplicación
- Usuario autenticado con créditos disponibles y tokens del jugador asignados
- Cliente HTTP o Postman para invocar endpoints

## Escenarios de validación

### 1. Crear una subasta válida

1. Autenticar como usuario propietario con tokens disponibles del jugador.
2. Ejecutar `POST /api/subastas` con un payload válido:
   - `jugadorId`: id existente
   - `cantidad`: 1 o más
   - `precioInicial`: valor positivo
   - `vencimiento`: fecha futura
3. Esperado: respuesta `201` y la subasta queda activa.

### 2. Consultar subastas activas

1. Ejecutar `GET /api/subastas?page=1&size=10`.
2. Esperado: lista paginada con subastas activas y sin las cerradas.

### 3. Realizar una oferta válida

1. Autenticar como otro usuario distinto del propietario.
2. Ejecutar `POST /api/subastas/ofertar` con `subastaId`, `puja` superior a la vigente y `idempotencyKey`.
3. Esperado: respuesta exitosa y la nueva oferta pasa a ser la más alta.

### 4. Rechazar una oferta inválida

1. Intentar ofertar con un valor igual o inferior a la oferta vigente.
2. Intentar ofertar desde el propietario de la subasta.
3. Intentar ofertar en una subasta ya cerrada.
4. Esperado: todas las llamadas deben devolver error de validación con estado apropiado.

### 5. Cierre por tiempo o manual

1. Cerrar la subasta manualmente o dejar que expire el vencimiento.
2. Esperado: estado de la subasta pasa a cerrada; si hubiese ganador válido, se transferirá el paquete de tokens y se sumará el valor al crédito del vendedor.

### 6. Resolución con oferta inválida por créditos

1. Crear una subasta con varias ofertas en orden descendente.
2. Reducir los créditos del mayor postor antes de cerrar la subasta.
3. Cerrar la subasta.
4. Esperado: el sistema salta a la siguiente oferta válida; si ninguna cumple, devuelve los tokens al propietario y no genera una venta.

### 7. Ver historial de ofertas y operaciones

1. Ejecutar `GET /api/subastas/{id}/ofertas`.
2. Ejecutar `GET /api/usuarios/{id}/operaciones` o el endpoint equivalente del proyecto.
3. Esperado: el historial refleja el valor de cada oferta y la operación final de compra/venta de cada parte.

## Criterios de éxito

- las subastas se crean solo con datos válidos
- la oferta más alta es consistente y no se duplican pujas para un mismo usuario/subasta
- la liquidación final hace transferencias correctas y mantiene consistencia del inventario
- todas las reglas de cierre, crédito y congelamiento de tokens se cumplen en los casos de borde
