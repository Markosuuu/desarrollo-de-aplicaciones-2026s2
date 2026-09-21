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

Para la implementacion de esta Funcionalidad, se proponen las siguientes clases principales a implementar:

### StatsUpdateController
Un controller especicon un endpoint dedicado a actualizar la base de datos de jugadores.

### StatsUpdateService
Un service, cuyo unico objetivo es encargarse de operar con WhoScoredScrapper, FootballDataScrapper, y PlayerRepository , para recopilar toda la informacion necesaria y persistirla/actualizarla en la base de datos.

### WhoScoredScrapper
Esta clase sera la encargada de recopilar toda la informacion necesaria de WhoScored.

### FootballDataScrapper
Esta clase sera la encargada de recopilar toda la informacion necesaria de Football-Data.org.

### StatsUpdateScheduler
Esta clase sera la encargada de realizar la actualización semanal de la base de datos, cada domingo a la 1:00 AM. Debera conocer unicamente a StatsUpdateService y usarla para actualizar la base.

## Manejo de errores y fallas
El flujo de esta funcionalidad tiene que ser tal que, en caso de fallos u errores de las paginas WhoScored y football-data, la base de datos no quede vacia.
En caso de errores al obtener datos de las fuentes externas, la base de datos debera conservarse y no verse alterada ni modificada bajo ningun punto de vista.