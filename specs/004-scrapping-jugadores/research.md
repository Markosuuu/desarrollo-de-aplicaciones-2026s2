# Research — Scrapping de jugadores y actualización de estadísticas

## Decision: reutilizar la entidad Jugador existente

- **Chosen**: ampliar la entidad `Jugador` con campos estadísticos necesarios para guardar nombre, equipo, liga, edad, goles, asistencias, disparos por partido, porcentaje de pases exitosos, dribbles exitosos, faltas cometidas y rating. `CatalogSnapshot` puede mantenerse como entidad técnica de auditoría si ya existe en el proyecto, pero no debe contener el catálogo ni reemplazar a `Jugador`.
- **Rationale**: la especificación exige que la consulta posterior dependa de `Jugador`; los metadatos de ejecución pueden ayudar al diagnóstico sin cambiar el modelo de catálogo.
- **Alternatives considered**: crear entidades totalmente separadas por fuente o por snapshot para guardar jugadores. Se descartan porque aumentan complejidad y no respetan la intención de persistir la información consultable en `Jugador`.

## Decision: tolerancia de fallos con respaldo local

- **Chosen**: si una fuente externa falla o devuelve datos inválidos, el sistema conserva la última información válida local y no reemplaza registros existentes por datos no confiables.
- **Rationale**: la feature exige que la base siga sirviendo datos locales y no quede vacía ni inconsistente.
- **Alternatives considered**: borrar la base o reemplazarla completamente al fallar. Se descarta porque rompe disponibilidad y no cumple la especificación.

## Decision: endpoint público de actualización sin protección por rol

- **Chosen**: exponer un endpoint específico que dispare la actualización sin exigir permisos de administración ni JWT.
- **Rationale**: la entrega explícita lo requiere y se adapta al alcance permitido para esta feature.
- **Alternatives considered**: limitar acceso por rol o autenticación. Se descarta porque la feature lo excluye.

## Decision: referencia concreta de WhoScored para parsing

- **Chosen**: tomar como referencia el endpoint `https://www.whoscored.com/statisticsfeed/1/getplayerstatistics?category=summary&subcategory=all&statsAccumulationType=0&isCurrent=true&playerId=&teamIds=&matchId=&stageId=&tournamentOptions=2,3,4,5,22&sortBy=Rating&sortAscending=&age=&ageComparisonType=&appearances=&appearancesComparisonType=&field=Overall&nationality=&positionOptions=&timeOfTheGameEnd=&timeOfTheGameStart=&isMinApp=false&page=1&includeZeroValues=&numberOfPlayersToPick=10&incPens=` y la respuesta con `playerTableStats`.
- **Rationale**: el ejemplo entregado por el usuario muestra el esquema principal para extraer `name`, `teamName`, `tournamentName`, `age`, `goal`, `assistTotal`, `shotsPerGame`, `passSuccess`, `dribbleWon`, `foulGiven`, `rating`, y campos complementarios. Esto reduce ambigüedad en el diseño del mapper.
- **Alternatives considered**: extraer datos desde páginas HTML o integrar un endpoint sin estructura clara. Se descarta porque el ejemplo provee una estructura estable y más segura para la implementación.

## Decision: pruebas reales sin mocks

- **Chosen**: todas las pruebas de esta feature deben validar comportamiento real del sistema y de la infraestructura disponible; no se aceptan mocks de servicios internos ni de repositorios para estos flujos críticos. Para fallos del proveedor, registros incompletos y duplicados, se acepta un servidor HTTP local real con respuestas controladas del formato WhoScored.
- **Rationale**: la constitución exige tests no negociables y el usuario lo exige explícitamente. El objetivo es verificar recuperación, actualización, persistencia y manejo de fallos de forma reproducible sin depender de disponibilidad externa impredecible.
- **Alternatives considered**: mockear la integración externa o el repositorio. Se descarta para repositorios y servicios internos por no cumplir la especificación ni la definición de terminado; para el proveedor remoto se prefiere HTTP local real porque permite probar casos negativos que WhoScored no puede garantizar bajo demanda.

## Decision: estrategia de carga inicial

- **Chosen**: la actualización debe funcionar estando la base vacía y debe crear los registros faltantes como parte del mismo proceso.
- **Rationale**: la feature explota un caso crítico y debe cubrir la primera carga sin intervención manual.
- **Alternatives considered**: requerir un pre-poblamiento manual o un script externo. Se descarta porque no cumple la necesidad de la entrega.
