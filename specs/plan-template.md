# Implementation Plan: Scrapping de Jugadores con un scheduler para actualizar semanalmente la base de datos

**Branch**: `feat/implementacion-scrapping` | **Date**: 2026-21-09 | **Spec**: `specs/004-scrapping-jugadores/spec.md` 

## Summary
Esta funcionalidad busca agregar un flujo puro de backend para poder recopilar informacion, mediante el uso de scrapping, de las siguientes fuentes:
- WhoScored
- Football-Data.org

Estas paginas se utilizaran para obtener de cada jugador la siguiente información:
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

Toda esta funcionalidad no contempla frontend ni contempla el manejo de un sistema de snapshots para persistir información antigua.

## Technical Context

**Lenguaje/Versión**: Java 21

**Dependencias principales**: Spring Boot 3.3.3, Spring Web, Spring Data JPA, Spring Validation, Spring Security, JWT (jjwt), PostgreSQL, Yup para validación de formularios, Toastify para notificaciones de error

**Almacenamiento**: PostgreSQL para persistencia en ejecución; PostgreSQL para ejecución local y pruebas

**Pruebas**: Spring Boot test starter, Spring Security test, JUnit 5, cobertura de controladores e integración

**Plataforma objetivo**: aplicación web basada en navegador servida por el backend de Spring Boot con una capa frontend bajo `frontend/`

**Tipo de proyecto**: aplicación web

## Estructura

Esta funcionalidad debe construirse en capas respetando la arquitectura en capas convencional que el proyecto de BackEnd ya respeta, siguiendo las normas de la constitución. Se espera que Controller solo conozca a Service, que Service conozca a Modelo y Repository, que Repository solo conozca a Modelo y que Modelo no conozca a ninguna otra capa.

La entidad de modelo Jugador.java ya existe, pero le faltan muchos de los campos (goles, asistencias, etc). Campos como partidos, equipos y demas, que logicamente se persisitirian en la base como una nueva entidad para poder relacionar jugadores entre, en esta primera version NO se hara de esa manera para no complejizar mas esta funcionalidad. La unica excepcion son las ligas, que son Premier League (Inglaterra), Bundesliga (Alemania), La Liga (España), Serie A (Italia) y Ligue 1 (Francia). Estas podes manejarlas como un enumerativo si lo ves pertinente.

Para la implementacion de toda esta funcionalidad, se proponen las siguientes clases principales a implementar:

### StatsUpdateController
Un controller especicon un endpoint dedicado a actualizar la base de datos de jugadores.

### StatsUpdateService
Un service, cuyo unico objetivo es encargarse de operar con WhoScoredScrapper, FootballDataScrapper, y PlayerRepository , para recopilar toda la informacion necesaria y persistirla/actualizarla en la base de datos.

### WhoScoredScrapper
Esta clase sera la encargada de recopilar toda la informacion necesaria de WhoScored.

### FootballDataScrapper
Esta clase sera la encargada de recopilar toda la informacion necesaria de Football-Data.org.

## Manejo de errores y fallas
El flujo de esta funcionalidad tiene que ser tal que, en caso de fallos u errores de las paginas WhoScored y football-data, la base de datos no quede vacia.
En caso de errores al obtener datos de las fuentes externas, la base de datos debera conservarse y no verse alterada ni modificada bajo ningun punto de vista.

## Scrapping de WhoScored
Para realizar el scrapping de WhoScored, podes acceder al siguiente endpoint de tipo xhr (de ser necesario, podes cambiar el tamaño de pagina o cualquier otro campo que veas necesario para que la recopilación de datos sea mas rapida, pero ANTES consultalo conmigo):
https://www.whoscored.com/statisticsfeed/1/getplayerstatistics?category=summary&subcategory=all&statsAccumulationType=0&isCurrent=true&playerId=&teamIds=&matchId=&stageId=&tournamentOptions=2,3,4,5,22&sortBy=Rating&sortAscending=&age=&ageComparisonType=&appearances=&appearancesComparisonType=&field=Overall&nationality=&positionOptions=&timeOfTheGameEnd=&timeOfTheGameStart=&isMinApp=false&page=1&includeZeroValues=&numberOfPlayersToPick=10&incPens=

Aca te dejo una respuesta de ejemplo, con un tamaño de pagina de 1, para que veas toda la estructura y los campos del jugador:
```json
 {  
    "playerTableStats" :  [
        {
            "height":176,
            "weight":64,
            "age":29,
            "isManOfTheMatch":false,
            "isActive":true,"playedPositions":"-AMC-AML-AMR-FW-",
            "playedPositionsShort":"AM(CLR),FW",
            "teamRegionName":"Spain",
            "regionCode":"br",
            "tournamentShortName":"SLL",
            "apps":7,
            "subOn":0,
            "manOfTheMatch":2,
            "goal":12,
            "assistTotal":3,
            "shotsPerGame":3.5714285714285716,
            "aerialWonPerGame":0.14285714285714285,
            "name":"Raphinha",
            "firstName":"Raphael",
            "lastName":"Dias Belloli",
            "playerId":300447,
            "positionText":"Forward",
            "teamId":65,
            "teamName":"Barcelona",
            "seasonId":11213,
            "seasonName":"2026/2027",
            "isOpta":true,
            "tournamentId":4,
            "tournamentRegionId":206,
            "tournamentRegionCode":"es",
            "tournamentRegionName":"Spain",
            "tournamentName":"LaLiga",
            "rating":8.9485714285714284,
            "minsPlayed":555,
            "yellowCard":0.0,
            "redCard":0.0,
            "passSuccess":84.210526315789465,"ranking":1
        }
    ],  
    "paging" : {   
        "currentPage" :  1, 
        "totalPages" :  2105, 
        "resultsPerPage" :  1, 
        "totalResults" :  2105, 
        "firstRecordIndex" :  1, 
        "lastRecordIndex" :  1
    },  
    "statColumns" :  
        ["apps",
        "subOn",
        "minsPlayed",
        "goal",
        "assistTotal",
        "yellowCard",
        "redCard",
        "shotsPerGame",
        "passSuccess",
        "aerialWonPerGame",
        "manOfTheMatch"
        ] 
    } 
```

## Scrapping de FootballData
Para el scrapping de football-data.org se cuenta con la siguiente documentación provista por la pagina https://www.football-data.org/documentation/quickstart