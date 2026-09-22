# Scrapping de Jugadores

**Feature Branch**: `004-scrapping-jugadores`

**Created**: 2026-21-09

**Status**: Draft

### User Story 1 - Scrapping de datos (Priority: P1)

Se necesita actualizar la estadisticas de los jugadores en nuestra base de datos, y para eso, se utilizara scrapping de las siguientes fuentes de datos:
- WhoScored: se utilizará scraping para extraer datos detallados de rendimiento de jugadores y equipos (pases, tiros, intercepciones, calificaciones, entre otros).
- Football-Data.org: se utilizará la API oficial para obtener resultados de partidos, alineaciones y fixtures.

Estas paginas se utilizaran para obtener de cada jugador la siguiente data:
- nombre
- equipo al que pertenece
- liga a la que pertenece
- edad
- goles
- asistencias
- disparos
- pases
- dribbles
- faltas
- rating
- resultados e historial de partidos
- alineaciones
- fixtures

En caso de que la base de datos a actualizar se encuentre vacia, esto no deberia ser un impedimento y la base de datos debe poblarse igualmente.

Toda esta nueva información debera almacenarse en la entidad de modelo ya existente Jugador en el path `backend/app/src/main/java/com/prontaentrega/models/Jugador.java`. Los datos deben poderse luego recuperar sin necesidad de volver a consultar a estos proveedores externos.

En caso de errores o fallas del proveedor externo o de cualquier tipo, el sistema debera tolerarlas y continuar funcionando con datos locales.

Para realizar esto, se expondra un endpoint especifico que pueble la base de datos. Este endpoint, no tendra ninguna protección detras en terminos de rol. No sera necesario que para llamar este endpoint se tenga un rol de admin o algo similar. Eso se implementara en una feature futura.

**Independent Test**: Puede probarse ejecutando la actualización de estadísticas mediante el endpoint manual utilizando proveedores externos simulados, verificando que las estadísticas obtenidas se persistan correctamente, que los datos existentes permanezcan disponibles ante una falla de un proveedor externo, y que en caso de fallas se retorne un error descriptivo y claro. 