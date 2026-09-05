# 001-player-token-market — Mercado tokenizado de jugadores (Pronta Entrega)

Resumen

Crear un mercado de tokens representativos de jugadores de fútbol donde los usuarios puedan comprar y vender tokens basados en una cotización que varía en el tiempo. Versión inicial soporta cinco ligas principales y operaciones contra un superusuario que posee la emisión inicial.

Actores

- Usuario registrado: puede operar (comprar/vender), ver portfolio e historial.
- Superusuario: titular inicial de todos los tokens; contraparte primaria para compras.
- Sistema de datos: fuentes externas (WhoScored scraping y Football-Data.org API).

Objetivos

- Permitir operaciones básicas de mercado (compra/venta) y seguimiento de posiciones.
- Exponer APIs para consumo por un frontend que muestre catálogo, cotizaciones y portfolio.

Alcance (versión 1)

Incluye:
- Registro de usuarios y emisión de una apikey por usuario.
- Catálogo de jugadores de: Premier League, Bundesliga, LaLiga, Serie A, Ligue 1.
- Compra y venta de tokens contra el superusuario hasta agotar disponibilidad.
- Visualización de portfolio con cantidad, precio promedio, valor actual y P/L.
- Registro de todas las operaciones.

Excluye (no en v1):
- Mercado secundario P2P entre usuarios (solo ventas al sistema/superusuario).
- Integraciones de pago reales (simular saldo interno).
- Gestión avanzada de fraudes o compliance.

Requerimientos funcionales (versión 1 — alcance mínimo)

La versión 1 implementará únicamente los siguientes requerimientos funcionales (alcance mínimo para entrega rápida), tal como solicitó el product owner:

1. Registro y autenticación
   - RF-1: Un usuario puede registrarse y recibir una apikey para autenticar peticiones.
   - Criterio de aceptación: Tras registro, la API retorna apikey y el usuario puede llamar endpoints autenticados.

2. Catálogo de jugadores
   - RF-2: Consultar listado de jugadores con liga, nombre, equipo y cotización vigente.
   - Criterio de aceptación: El endpoint devuelve jugadores filtrables por liga y paginados.

Otros comportamientos del mercado (compra/venta de tokens, portfolio en tiempo real, etc.) quedan fuera del alcance de la v1 y se planificarán en iteraciones posteriores. Se conservarán las entidades y modelos necesarios para habilitar estas funciones en el futuro, pero no se expondrán operaciones de compra/venta en esta entrega inicial.

Fuentes de datos

- WhoScored (scraping) para métricas de rendimiento.
- Football-Data.org API para resultados, alineaciones y fixtures.

## Estrategia de valuación (decisión provisional)

Decisión: En la versión 1 se utilizará la estrategia "Rating simple": la cotización del token se derivará directamente de la calificación pública de WhoScored para el jugador en el periodo correspondiente. Esta decisión es provisional; el product owner indicó que la estrategia se definirá en una etapa futura, por lo que aquí se registra como una asunción para permitir avanzar con la entrega rápida.

Implicaciones:
- Desarrollo más rápido y menor complejidad de integración (ETL mínimo).
- Precisión limitada frente a un indicador compuesto; revisar en próximas iteraciones.

## Mercado secundario (decisión)

Decisión: No se permitirá mercado P2P en v1. Todas las compras y ventas se realizarán contra el superusuario (el sistema) hasta nueva indicación del product owner. Esta elección fue proporcionada por el product owner y reduce la complejidad operativa en la primera versión.

Implicaciones:
- Menor complejidad en matching y órdenes; liquidez controlada por el superusuario.
- Posibilidad de re-evaluar en futuras versiones para habilitar mercado secundario.

Entidades clave

- Usuario: id, nombre, apikey, saldo (simulado)
- Jugador: id, nombre, equipo, liga, total_tokens (100), tokens_disponibles, cotizacion_actual
- Posición: user_id, player_id, tokens_posed, precio_promedio_compra
- Operación: id, user_id, player_id, tipo (compra/venta), cantidad, precio_unitario, timestamp

Aceptación y criterios de éxito

- Los usuarios nuevos obtienen apikey al registrarse y pueden realizar operaciones autenticadas.
- 95% de las consultas al catálogo retornan en <2s (medible en pruebas de integración).
- Portfolio refleja correctamente P/L tras cambios de cotización en escenarios de prueba deterministas.
- Todas las operaciones quedan registradas y son reproducibles en el historial del usuario.

Asunciones

- Saldo del usuario es un campo interno simulado (no integración de pagos en v1).
- Cada jugador tiene 100 tokens emitidos; superusuario posee inicialmente los 100 de cada jugador.
- La cotización inicial del token en t0 = 1 crédito.
- Actualizaciones de cotización son periódicas y atómicas (se aplican unívocamente por periodo).

Riesgos y dependencias

- Dependencia de disponibilidad y legalidad del scraping de WhoScored.
- Calidad de datos externos (latencia, cambios en API, estructura HTML).

Pruebas y escenarios de usuario

Escenario principal: Registro → Compra → Cotización sube → Verificar P/L
1. Usuario se registra y recibe apikey.
2. Usuario adquiere 10 tokens de un jugador con cotización 1 (costo 10).
3. Cotización simula subida a 1.2; consulta portfolio muestra valor 12 y ganancia 2.

Escenario de error: Compra sin saldo suficiente
1. Usuario intenta comprar más de lo que su saldo permite.
2. API rechaza la operación con error claro y no registra movimiento.

Notas finales

- Mantener el documento centrado en valor de negocio y comportamientos observables.
- Las preguntas marcadas como [NEEDS CLARIFICATION] requieren respuesta antes de planificar implementación detallada.

---
Generated by speckit-specify