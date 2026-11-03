# Investigación: mercado de tokens de jugadores

## Decisión

Se implementa el marketplace con la arquitectura existente del backend en capas y con una nueva entidad de historial de operaciones. El flujo se diseña alrededor de tres pilares: listado público de anuncios, venta de tokens con validación de disponibilidad, y compra con validación de créditos y transferencias de saldo/propiedad.

## Fundamentos

- La entidad `Usuario` ya existe y es el punto natural para almacenar `creditosDisponibles` sin introducir un agregado paralelo de dominio.
- La entidad `Jugador` y la estructura `BolsaDeJugador` ya existen, por lo que el modelo puede representar el inventario de tokens y la propiedad asociada al jugador sin forzar una nueva capa de almacenamiento.
- El dominio requiere distinguir entre `disponible`, `en venta` y `transferido`, por lo que el diseño usa un estado explícito en la operación de venta o en el token disponible, en lugar de mezclar validaciones en la UI.
- El historial debe ser auditable y consistente con la operación, por lo que se introduce `Operacion` como entidad de negocio y no como un registro ad hoc del controlador.
- Las reglas de negocio de “no puede vender más de lo disponible”, “precio fijo por sistema” y “compra no reversible” deben resolverse en servicio/modelo y no en la capa de presentación.

## Alternativas consideradas

- Crear una única entidad “AnuncioVenta” y guardar todo el flujo económico en ella: descartada porque el historial de operaciones exige separar la entidad de publicación del registro de movimiento final.
- Resolver la validación de crédito y estado de tokens en el frontend: descartada porque viola la constitución y permite inconsistencias si el backend no valida de nuevo.
- Usar una transacción sin control de estado por token: descartada porque no garantiza que dos compras concurrentes no generen doble venta.
- Dejar el precio como dato editable por el vendedor: descartada porque la especificación indica que el precio viene determinado por el sistema.

## Implicaciones de implementación

- El backend debe validar la disponibilidad del usuario y del token antes de modificar el estado de la publicación.
- El servicio de compra debe ejecutar validaciones de saldo, usuario vendedor y disponibilidad para evitar compras inválidas o dobles ventas.
- La operación de venta debe separar la parte de “cantidad reservada” de la “cantidad disponible” sin romper el inventario real del usuario.
- El historial debe registrarse en un único punto después de la validación, de modo que cada operación queda trazada en estado `PENDIENTE`, `COMPLETADA` o `CANCELADA`.
- La compra se debe implementar como una operación transaccional: creditos, tokens y registro de movimiento deben actualizarse sin dejar el sistema inconsistente si falla una parte.
