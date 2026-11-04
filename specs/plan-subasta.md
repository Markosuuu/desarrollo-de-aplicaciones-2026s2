# Plan

## Feature de subasta de tokens del mercado.

### Controller
Se pide un controller llamado `SubastaController`, el cual admita los siguientes endpoints:

- `POST /subasta` con un body que admita: id de un Jugador, cantidad, precio inicial, fecha de vencimiento.
- `GET /subasta` lista todas las subastas activas de manera paginada
- `GET /subasta/:id` subasta activa específica
- `POST /subasta/ofertar` con un body que admita: oferta + idempotency key
- `UPDATE /subasta/:id` para cerrar la subasta
- `GET /subasta/:id/ofertas` lista las ofertas de una subasta específica

Adicionalmente, el controller recibe todas las operaciones del service y mapea los dtos request/response en su propia capa.

### Service

Un service denominado "SubastaService". El service debe llevar a cabo todos los pedidos necesarios del endpoint además de otros requerimientos necesarios.

### Persistencia

La capa de persistencia respeta el repository pattern, mapeando los objetos de modelo como DTOSQL, podés basarte en otros objetos de esa capa para ver como es.

### Modelo
En esta capa es necesaria la existencia de una clase `Subasta`, la cual contenga la siguiente estrucutra:
  - propietario: Usuario // propietario de los tokens del jugador y dueño de la subasta
  - vencimiento: LocalDateTime // fecha y hora de cuando vence la subasta
  - oferta: Oferta // Oferta que va ganando la subasta
  - interesados: List de Oferta  // La lista de toda los usuarios que dejaron una oferta.
  - activa: Boolean // Indica si la subasta está activa o no. 
  - cantidadOfrecida: Integer // Cantidad de tokens del jugador ofrecida
  - cotizacionActual: BigDecimal // cotización actual del token en ese momento
  - jugador: Jugador // Jugador asociado a la subasta

Luego una clase `Oferta` que va a tener:
- usuario: Usuario
- puja: BigDecimal