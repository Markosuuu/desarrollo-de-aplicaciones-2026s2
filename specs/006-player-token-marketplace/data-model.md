# Modelo de datos: mercado de tokens de jugadores

## Entidad: Usuario

Representa al participante del marketplace y su saldo de créditos disponible para comprar tokens.

| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | Identificador del usuario |
| nombre | string | Nombre visible en el marketplace |
| correo | string | Identificador de acceso del usuario |
| passwordHash | string | Hash de la contraseña |
| creditosDisponibles | BigDecimal | Saldo virtual disponible para comprar tokens; no puede ser negativo |
| creadoEn | LocalDateTime | Fecha de creación |
| actualizadoEn | LocalDateTime | Fecha de última actualización |

Reglas de validación:
- `creditosDisponibles` debe inicializarse en `100` para nuevos usuarios.
- El valor no puede caer por debajo de `0`.
- El usuario no puede vender una cantidad superior a la que realmente tiene disponible.

## Entidad: Jugador

Representa a cada jugador del catálogo y a la entidad cuyo token puede ser transferido entre usuarios.

| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | Identificador del jugador |
| whoscoredId | integer | Identificador externo del jugador |
| nombre | string | Nombre del jugador |
| equipo | string | Equipo al que pertenece |
| liga | Liga | Liga o competición |
| posicion | Posicion | Posición principal del jugador |
| edad | integer | Edad opcional |
| altura | integer | Altura opcional |
| peso | integer | Peso opcional |

Reglas de validación:
- `nombre`, `equipo` y `liga` son obligatorios.
- `edad`, `altura` y `peso` no pueden ser negativos.

## Entidad: BolsaDeJugador

Representa la disponibilidad real de tokens de un jugador asociados a un usuario. En este proyecto se reutiliza la entidad existente como referente de inventario del usuario.

| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | Identificador del stock |
| estadisticasDeportivas | EstadisticasDeportivas | Datos del jugador asociado |
| cotizacionActual | BigDecimal | Precio actual del token del jugador |
| fechaActualizacion | LocalDateTime | Fecha de última actualización |
| fuente | string | Fuente de datos |
| tokensRestantes | integer | Cantidad disponible del usuario |

Reglas de validación:
- `tokensRestantes` debe ser mayor o igual a cero.
- La cantidad disponible no puede solaparse con cantidades ya reservadas en ventas activas.
- La compra debe reducir el inventario del vendedor y aumentarlo del comprador.

## Entidad: AnuncioVenta

Representa la publicación que un usuario pone en venta para ofrecer parte de sus tokens.

| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | Identificador del anuncio |
| jugadorId | UUID | Jugador asociado |
| vendedorId | UUID | Usuario que publica la venta |
| cantidadTokens | integer | Tokens ofrecidos en la venta |
| precioUnitario | BigDecimal | Precio fijo determinado por el sistema |
| precioTotal | BigDecimal | Cálculo de `cantidadTokens * precioUnitario` |
| estado | enum | `PENDIENTE`, `COMPLETADA`, `CANCELADA` |
| fechaCreacion | LocalDateTime | Fecha de publicación |
| fechaActualizacion | LocalDateTime | Última modificación |

Reglas de validación:
- `cantidadTokens` debe ser mayor a cero.
- La cantidad no puede superar la disponibilidad real del vendedor.
- El vendedor puede cancelar o editar solo mientras la venta siga `PENDIENTE`.
- Un anuncio en `COMPLETADA` queda inmodificable.

## Entidad: Operacion

Representa el historial de movimientos de compra y venta del usuario.

| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | Identificador del movimiento |
| tipo | enum | `COMPRA` o `VENTA` |
| estado | enum | `PENDIENTE`, `COMPLETADA` o `CANCELADA` |
| jugadorId | UUID | Jugador involucrado |
| cantidadTokens | integer | Cuántos tokens fueron comprados o vendidos |
| precioUnitario | BigDecimal | Precio por token |
| precioTotal | BigDecimal | Monto de la operación |
| usuarioSolicitanteId | UUID | Usuario que realizó la operación |
| usuarioContraparteId | UUID | Usuario con quien se realizó la operación |
| fechaOperacion | LocalDateTime | Momento en que se generó el movimiento |

Reglas de validación:
- Una compra concluye en `COMPLETADA` únicamente si se valida el saldo y transferencia de tokens.
- Una venta puede quedar `CANCELADA` solo si no fue completada.
- El historial debe conservar ambos usuarios implicados para trazabilidad y auditoría.

## Relaciones

- Un `Usuario` puede tener varios `AnuncioVenta` activos y múltiples `Operacion` asociadas.
- Un `Jugador` puede aparecer en varios anuncios y operaciones distintas.
- Un `AnuncioVenta` se relaciona con una sola operación de compra cuando llega a completarse.
- El historial no reemplaza la entidad de venta; sirve como registro permanente del evento económico.

## Reglas de negocio aplicadas al modelo

- El precio de cada token no es editable por el vendedor; lo define el sistema.
- La compra debe descontar `precioTotal` del comprador y acreditarlo al vendedor.
- La cantidad publicada en venta debe quedar separada del saldo disponible real del usuario.
- La operación de compra debe ser inmodificable después de completarse.
- La compra simultánea de la misma venta debe resolverse con una única operación exitosa y rechazar las demás.
