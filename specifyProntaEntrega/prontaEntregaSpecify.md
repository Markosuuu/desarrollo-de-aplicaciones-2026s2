# 1. Descripción general

El sistema representa un mercado de jugadores basado en criterios de valuación, donde:
* Cada jugador tiene una cotización que varía en el tiempo
* Los usuarios pueden operar comprando y vendiendo tokens de jugadores
* El valor de la inversión cambia según la cotización actual del jugador
* El sistema deberá calcular una cotización periódica basada en criterios aún no definidos

##### Ejemplo:
Un jugador tiene una cotización de 100.
Un usuario compra 10 tokens, invirtiendo 1000.
Si la cotización sube a 120, su posición pasa a valer 1200, obteniendo una ganancia de 200.
Si la cotización baja a 90, su posición pasa a valer 900, generando una pérdida de 100.

## Ligas que se incluirán
Información de jugadores de fútbol de 5 ligas principales: 
  * Premier League (Inglaterra) 
  * Bundesliga (Alemania) 
  * La Liga (España) 
  * Serie A (Italia)
  * Ligue 1 (Francia)

## Fuente de datos 
Se utilizarán las siguientes fuentes de datos:
* WhoScored: se utilizará scraping para extraer datos detallados de rendimiento de jugadores y equipos (pases, tiros, intercepciones, calificaciones, entre otros).
* Football-Data.org: se utilizará la API oficial para obtener resultados de partidos, alineaciones y fixtures.

## Inicio y transferencia de tokens

Existirá un único superusuario que será el dueño inicial de todos los tokens de los jugadores. 
Cada jugador tendrá un total de 100 tokens emitidos. 
En el momento cero, cada token tendrá un valor inicial de 1 crédito.
A partir de ese momento, la cotización de los tokens irá cambiando según la estrategia de valuación configurada en el sistema y la evolución del valor del jugador.

## Operaciones de compra y venta

En una operación de compra, el sistema deberá:
* validar que exista disponibilidad de tokens, 
* utilizar la cotización vigente del jugador, 
* actualizar la posición del usuario y
* registrar la operación. 
 
En una operación de venta, el sistema deberá:
* validar que el usuario posea la cantidad de tokens a vender, 
* actualizar su saldo, 
* ajustar su posición y 
* registrar la operación. 

Las operaciones de compra de los usuarios deberán realizarse inicialmente contra el superusuario, quien concentra la tenencia inicial de todos los tokens.
El sistema deberá permitir el registro y creación de nuevos usuarios para operar en el mercado.

## Portfolio del usuario
El sistema deberá permitir visualizar la posición del usuario, debe incluir:
* cantidad de tokens por jugador
* precio promedio de compra
* valor actual
* ganancia o pérdida
* historial de operaciones

## Interfaz

El sistema deberá contar con una interfaz de usuario (Frontend) que consuma las APIs expuestas por el backend.
Esta interfaz deberá permitir:
* Visualización del catálogo de jugadores con filtros.
* Visualización de detalles y evolución de cotizaciones de un jugador.
* Visualización del ranking de jugadores.
* Operar en el mercado (compra y venta de tokens) autenticándose como usuario.
* Gestión y visualización del portfolio personal.
