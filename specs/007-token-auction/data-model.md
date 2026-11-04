# Data Model: Subasta de tokens del mercado

## Entidades del dominio

### Subasta

**Propósito**: representa la oferta de venta de una cantidad de tokens de un jugador en un plazo específico.

**Campos clave**:
- `id`: identificador único de la subasta
- `propietario`: `Usuario` dueño de los tokens y del proceso de venta
- `jugador`: `Jugador` al que pertenecen los tokens ofrecidos
- `cantidadOfrecida`: cantidad de tokens a vender, con mínimo 1
- `vencimiento`: `LocalDateTime` con fecha y hora de cierre
- `ofertaActual`: `Oferta` o `null` si no hubo pujas válidas
- `interesados`: lista de ofertas recibidas en orden de llegada o valor
- `activa`: bandera que indica si la subasta sigue abierta
- `cotizacionActual`: valor vigente del token del jugador al momento de crearse la subasta, segun estrategia de cotizacion 
- `estado`: activo/cerrada, documento del ciclo de vida

**Reglas de validación**:
- la cantidad debe ser mayor o igual a 1
- la fecha de cierre debe ser posterior a la fecha actual
- el propietario no puede ofertar en su propia subasta
- la subasta se cierra al vencer o cuando el dueño la finaliza
- cuando no hay ganador válido, los tokens vuelven al inventario del propietario

### Oferta

**Propósito**: representa una puja concreta hecha por un usuario sobre una subasta.

**Campos clave**:
- `id`: identificador único de la oferta
- `subasta`: referencia a la subasta donde se hizo la oferta
- `usuario`: `Usuario` que realiza la puja
- `puja`: `BigDecimal` con el valor ofertado
- `momento`: `LocalDateTime` de creación de la oferta
- `esVigente`: indica si la oferta es la más alta y aún válida

**Reglas de validación**:
- la puja debe ser estrictamente superior a la oferta actual más alta
- no puede existir más de una oferta activa por usuario y subasta
- la oferta del propietario en su propia subasta queda invalidada
- si la oferta supera el valor y el usuario tiene crédito suficiente al cierre, puede ser la ganadora

### Usuario

**Propósito**: representa al participante del mercado y sus créditos e inventario.

**Campos clave**:
- `id`
- `nombre`, `email` o equivalente de identidad
- `creditosDisponibles`
- `inventario`: tokens asociados por jugador

**Relaciones**:
- puede ser propietario de subastas
- puede dejar ofertas sobre subastas ajenas
- puede recibir operaciones de compra/venta por cierre exitoso

### Jugador

**Propósito**: entidad del alto valor deportivo cuyo token se comercializa.

**Campos clave**:
- `id`
- `nombre`
- `estado deportivo o metadata relevante`

**Relaciones**:
- cada subasta se asocia a un jugador concreto
- los tokens del jugador pueden estar disponibles, congelados o transferidos tras liquidación

### Operacion

**Propósito**: registra la transacción final de la subasta para trazabilidad y historial.

**Campos clave**:
- `id`
- `usuarioOrigen`
- `usuarioDestino`
- `jugador`
- `cantidad`
- `monto`
- `tipo`: compra/venta
- `estado`: completada/cancelada
- `fecha`

**Reglas de validación**:
- al cierre exitoso, debe existir una operación para vendedor y otra para comprador
- no debe generarse operación cuando la subasta cierra sin ganador válido

## Relaciones principales

- `Usuario` 1..* `Subasta` (como propietario)
- `Subasta` 1..* `Oferta`
- `Oferta` *..1 `Usuario`
- `Subasta` *..1 `Jugador`
- `Operacion` *..1 `Usuario` (origen y destino)

## Estados relevantes

- `Subasta`: activa / cerrada
- `Oferta`: vigente / rechazada / superada / perdida
- `Operacion`: creada / completada / cancelada
- `Token`: disponible / congelado / transferido

## Consideraciones de persistencia

- Se recomienda mantener la relación de ofertas con una clave compuesta por `usuario_id + subasta_id` para garantizar idempotencia.
- La subasta debe llevar un `version` para soportar concurrencia optimista en la escritura de la oferta más alta.
- El historial de ofertas debe conservarse sin reescritura en orden cronológico para permitir trazabilidad y auditoría.
