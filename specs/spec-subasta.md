# Specify:
Feature de subasta de tokens del mercado.

Se necesita una funcionalidad de subasta de tokens de mercado, donde un usuario puede poner a la venta por subasta la cantidad de tokens que quieran de un jugador. De esa subasta otros usuarios pueden pujar para intentar comprar esos tokens al mejor precio. La subasta cierra cuando el dueño la da por finalizada, o si se llego a la fecha y hora de finalización que especifico el dueño (La hora solo admite horas y minutos). En caso de que una subasta no tenga un mayor postor al finalizarse por tiempo o sea cerrada por el dueño, los tokens que el dueño vendio vuelven a su inventario. 

Se espera que la funcionalidad cumpla el siguiente flujo:
- Un usuario subasta cierta cantidad de tokens de un jugador, especificando la cantidad de tokens a vender y la fecha de finalización.
- Otros usuarios encuentran/ven la subasta y pueden ver la oferta actual mas alta, ademas de ver las ofertas previas.
- Los demas usuarios pueden dejar su oferta, siempre y cuando supere la oferta actual mas alta.
- Al finalizar o cerrarse la subasta, los tokens que el usuario subasto se transfieren al postor mas alto, y el valor de la puja se suma al credito del dueño de la subasta.
- Al finalizar la subasta, esta genera una operacion tanto para el dueño de la subasta como para el mayor postor, indicando que hubo una transaccion entre ambos de compra/venta.

## Reglas
- La cantidad minima de tokens a subastar debe ser 1.
- El dueño no puede dejar una oferta en su propia subasta.
- Cuando 2 usuarios o más pujan en la misma subasta al mismo tiempo, se espera un bloqueo optimista.
- Cuando el mismo usuario puja en la misma subasta, no crea duplicados (clave idempotencia).
- El postor más alto no se puede retirar.
- Los tokens que se dediquen a la subasta, pasaran a estar en un estado "ocupados", como ocurre con los anuncios de venta.
- Los creditos que los usuarios oferten para una subasta no se descontarán. Solamente cuando la subasta cierre, se le descontaran al mayor postor los creditos que haya ofertado por la subasta.
- No se puede pujar en una subasta que esta cerrada.