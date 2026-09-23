# Data Model — Scrapping de jugadores y actualización de estadísticas

## Entity: Jugador

**Objetivo**: conservar la información del jugador en la base de datos local para que la consulta posterior no dependa de proveedores externos.

**Campos principales**:
- nombre: texto obligatorio
- equipo: texto obligatorio
- liga: texto obligatorio
- edad: entero obligatorio
- goles: entero obligatorio
- asistencias: entero obligatorio
- disparosPorPartido: decimal obligatorio
- porcentajePasesExitosos: decimal obligatorio
- dribblesExitosos: decimal obligatorio
- faltasCometidas: decimal obligatorio
- rating: decimal obligatorio
- fechaActualizacion: fecha/hora de última actualización local
- fuente: texto que identifica la fuente de origen

**Reglas de negocio**:
- un jugador debe identificarse unívocamente para evitar duplicados
- si un jugador ya existe, la actualización debe reemplazar los campos estadísticos con los datos más recientes
- si la fuente externa falla, no se debe borrar la última versión válida
- la información local debe seguir siendo consultable aunque el proveedor externo se encuentre caído

## Entity: CatalogSnapshot

**Objetivo**: registrar el estado de cada actualización del catálogo y permitir diagnóstico de fallas. Es una entidad técnica de auditoría; no contiene datos de jugadores ni reemplaza a `Jugador` como fuente local consultable.

**Campos principales**:
- iniciadoEn: fecha de inicio del proceso
- finalizadoEn: fecha de fin del proceso
- estado: valido o fallido
- error: detalle textual en caso de falla

**Reglas de negocio**:
- cada intento de actualización debe dejar evidencia del estado final
- en caso de error, la base debe seguir conservando la última información válida
- la operación debe ser auditable y comunicable en lenguaje claro para responsables del sistema

## Source mapping notes

La extracción desde WhoScored debe mapear el payload ejemplo a la entidad `Jugador` conforme a estos atributos clave:
- `playerTableStats[*].name` → `nombre`
- `playerTableStats[*].teamName` → `equipo`
- `playerTableStats[*].tournamentName` → `liga`
- `playerTableStats[*].age` → `edad`
- `playerTableStats[*].goal` → `goles`
- `playerTableStats[*].assistTotal` → `asistencias`
- `playerTableStats[*].shotsPerGame` → `disparosPorPartido`
- `playerTableStats[*].passSuccess` → `porcentajePasesExitosos`
- `playerTableStats[*].dribbleWon` → `dribblesExitosos`
- `playerTableStats[*].foulGiven` → `faltasCometidas`
- `playerTableStats[*].rating` → `rating`

Los campos faltantes o no confiables deben manejarse sin provocar pérdida de la información local ya validada.
