# Tasks: Autenticacion de usuarios y catalogo de jugadores

**Entrada**: Documentos de diseño desde `/specs/002-user-auth-player-catalog/`

**Prerequisitos**: plan.md (obligatorio), spec.md (obligatorio para historias de usuario), data-model.md, research.md, quickstart.md, contracts/

## Fase 1: Configuracion (infraestructura compartida)

**Proposito**: Inicializar el proyecto y la estructura compartida antes del desarrollo funcional.

- [ ] T001 Crear la estructura del proyecto backend en backend/src/main/java/com/prontaentrega/, backend/src/test/java/com/prontaentrega/ y backend/src/main/resources/
- [ ] T002 Inicializar el proyecto Java 21 + Spring Boot 4.x + Gradle en backend/build.gradle y backend/settings.gradle
- [ ] T003 [P] Configurar la muestra de entorno y la configuracion de PostgreSQL/Testcontainers en backend/.env.example y backend/src/main/resources/application.yml

---

## Fase 2: Base (pre requisitos bloqueantes)

**Proposito**: Preparar la infraestructura base que todas las historias de usuario requieren antes de avanzar.

**Punto de control**: La base ya esta lista y el desarrollo de historias puede comenzar en paralelo.

- [ ] T004 Configurar el esquema de base de datos y scripts de migracion para usuarios, token_state, jugadores y catalog_snapshot en backend/src/main/resources/db/migration/
- [ ] T005 [P] Implementar validacion compartida y envelope de errores en backend/src/main/java/com/prontaentrega/controllers/GlobalExceptionHandler.java y backend/src/main/java/com/prontaentrega/controllers/dtos/
- [ ] T006 [P] Implementar utilidades de firma, parseo y validacion JWT en backend/src/main/java/com/prontaentrega/authentication/JwtService.java
- [ ] T007 [P] Configurar Spring Security y el filtro JWT en backend/src/main/java/com/prontaentrega/config/SecurityConfig.java y backend/src/main/java/com/prontaentrega/authentication/JwtAuthenticationFilter.java
- [ ] T008 Crear patrones de repositorio y entidad base para Usuario, Jugador y CatalogSnapshot en backend/src/main/java/com/prontaentrega/models/ y backend/src/main/java/com/prontaentrega/repository/

---

## Fase 3: Historia de usuario 1 - Registrar una cuenta de usuario (Prioridad: P1) 🎯 MVP

**Objetivo**: Permitir que un visitante cree una cuenta, valide sus entradas y reciba un JWT asociado.

**Prueba independiente**: Una cuenta nueva se registra con datos validos, devuelve 201 y emite un JWT usable; un correo duplicado devuelve 409.

### Pruebas para la historia de usuario 1

> NOTA: Escribir estas pruebas primero y asegurarse de que fallen antes de la implementacion.

- [ ] T010 [P] [US1] Prueba de contrato para POST /api/auth/register en backend/src/test/java/com/prontaentrega/controllers/AuthControllerTest.java
- [ ] T011 [P] [US1] Prueba de integracion para registro duplicado y password invalida en backend/src/test/java/com/prontaentrega/services/AuthServiceIT.java

### Implementacion para la historia de usuario 1

- [ ] T012 [P] [US1] Crear la entidad Usuario y sus reglas de validacion en backend/src/main/java/com/prontaentrega/models/Usuario.java
- [ ] T013 [P] [US1] Crear el repositorio de usuarios y la consulta por correo unico en backend/src/main/java/com/prontaentrega/repository/UsuarioRepository.java
- [ ] T014 [US1] Implementar el servicio de registro y manejo de duplicados en backend/src/main/java/com/prontaentrega/services/AuthService.java
- [ ] T015 [US1] Implementar el endpoint de registro y DTOs de request en backend/src/main/java/com/prontaentrega/controllers/AuthController.java y backend/src/main/java/com/prontaentrega/controllers/dtos/RegisterRequest.java
- [ ] T016 [US1] Agregar emision de JWT y seguimiento del token activo en backend/src/main/java/com/prontaentrega/authentication/JwtService.java y backend/src/main/java/com/prontaentrega/authentication/TokenStateService.java
- [ ] T017 [US1] Agregar validacion y mapeo de error de usuario duplicado en backend/src/main/java/com/prontaentrega/controllers/GlobalExceptionHandler.java

**Punto de control**: En este punto, la historia de usuario 1 debe estar completamente funcional y probada de forma independiente.

---

## Fase 4: Historia de usuario 2 - Iniciar sesion para operar (Prioridad: P1)

**Objetivo**: Autenticar usuarios registrados, reemplazar el JWT vigente y bloquear credenciales invalidas o antiguas.

**Prueba independiente**: Se valida el login correcto, el login con contraseña incorrecta y la invalidacion del JWT previo.

### Pruebas para la historia de usuario 2

- [ ] T020 [P] [US2] Prueba de contrato para POST /api/auth/login en backend/src/test/java/com/prontaentrega/controllers/AuthControllerTest.java
- [ ] T021 [P] [US2] Prueba de integracion para login, credenciales invalidas y reemplazo de JWT en backend/src/test/java/com/prontaentrega/services/AuthServiceIT.java

### Implementacion para la historia de usuario 2

- [ ] T022 [P] [US2] Crear el modelo de estado del token activo en backend/src/main/java/com/prontaentrega/authentication/TokenEstado.java
- [ ] T023 [US2] Implementar el flujo de login y validacion de credenciales en backend/src/main/java/com/prontaentrega/services/AuthService.java
- [ ] T024 [US2] Agregar el filtro JWT y validaciones de autorizacion para endpoints protegidos en backend/src/main/java/com/prontaentrega/authentication/JwtAuthenticationFilter.java
- [ ] T025 [US2] Garantizar un unico JWT activo e invalidar inmediatamente el token anterior en backend/src/main/java/com/prontaentrega/authentication/TokenRevocationService.java
- [ ] T026 [US2] Conectar errores de login y JWT a 401 CREDENCIALES_INVALIDAS y 401 JWT_INVALIDO en backend/src/main/java/com/prontaentrega/controllers/GlobalExceptionHandler.java

**Punto de control**: En este punto, las historias de usuario 1 y 2 deben funcionar de forma independiente.

---

## Fase 5: Historia de usuario 3 - Consultar el catalogo de jugadores (Prioridad: P1)

**Objetivo**: Exponer un catalogo publico de jugadores con filtros, paginacion y respaldo ante importaciones fallidas.

**Prueba independiente**: Se consulta el endpoint publico con filtros por liga, equipo y nombre y se verifica la respuesta paginada y el fallback del ultimo snapshot valido.

### Pruebas para la historia de usuario 3

- [ ] T030 [P] [US3] Prueba de contrato para GET /api/players en backend/src/test/java/com/prontaentrega/controllers/PlayerControllerTest.java
- [ ] T031 [P] [US3] Prueba de integracion para filtros, paginacion y comportamiento de fallback en backend/src/test/java/com/prontaentrega/services/PlayerCatalogServiceIT.java

### Implementacion para la historia de usuario 3

- [ ] T032 [P] [US3] Crear los modelos de jugador y snapshot del catalogo en backend/src/main/java/com/prontaentrega/models/Jugador.java y backend/src/main/java/com/prontaentrega/models/CatalogSnapshot.java
- [ ] T033 [P] [US3] Crear el repositorio de jugadores y consultas de paginacion en backend/src/main/java/com/prontaentrega/repository/JugadorRepository.java
- [ ] T034 [US3] Implementar el worker de importacion semanal y el fallback al ultimo snapshot valido en backend/src/main/java/com/prontaentrega/services/PlayerCatalogService.java y backend/src/main/java/com/prontaentrega/services/ExternalPlayerImporter.java
- [ ] T035 [US3] Implementar el endpoint publico de jugadores y los DTOs de respuesta en backend/src/main/java/com/prontaentrega/controllers/PlayerController.java y backend/src/main/java/com/prontaentrega/controllers/dtos/PlayerResponse.java
- [ ] T036 [US3] Agregar manejo de disponibilidad del catalogo y 503 CATALOGO_NO_DISPONIBLE en backend/src/main/java/com/prontaentrega/controllers/GlobalExceptionHandler.java

**Punto de control**: Las tres historias de usuario ya deben ser funcionales y verificables de forma independiente.

---

## Fase 6: Pulido y preocupaciones transversales

**Proposito**: Mejorar la calidad, la validacion integral y la documentacion final.

- [ ] T090 [P] Ampliar las pruebas unitarias de dominio para invariantes de Usuario y Jugador en backend/src/test/java/com/prontaentrega/models/
- [ ] T091 [P] Agregar la coleccion de Postman y la documentacion en español para flujos de autenticacion y catalogo en docs/postman/ProntaEntrega.postman_collection.json y README.md
- [ ] T092 Ejecutar la suite de validacion del backend y los comandos de quickstart: `cd backend && ./gradlew test` y `./gradlew bootRun`

---

## Dependencias y orden de ejecucion

### Dependencias por fase

- **Configuracion (Fase 1)**: No tiene dependencias; puede comenzar de inmediato.
- **Base (Fase 2)**: Depende de la configuracion y bloquea todas las historias de usuario.
- **Historias de usuario (Fase 3+)**: Todas dependen de la Fase 2; pueden progresar en orden de prioridad o en paralelo si hay capacidad.
- **Pulido (Fase final)**: Depende de que todas las historias deseadas esten completas.

### Dependencias de historias de usuario

- **Historia de usuario 1 (P1)**: Comienza luego de la base; no depende de otras historias.
- **Historia de usuario 2 (P1)**: Comienza luego de la base; depende de la misma infraestructura de autenticacion que US1.
- **Historia de usuario 3 (P1)**: Comienza luego de la base; puede ejecutarse en paralelo con US1/US2 siempre que la infraestructura compartida ya este lista.

### Dentro de cada historia de usuario

- Las pruebas deben escribirse antes de la implementacion.
- Los modelos antes que los servicios.
- Los servicios antes que los endpoints.
- La funcionalidad principal antes que la integracion final y la limpieza.

### Oportunidades de paralelismo

- Las tareas de configuracion T001-T003 pueden ejecutarse en paralelo.
- Las tareas de base T004-T008 pueden ejecutarse en paralelo cuando tocan archivos distintos.
- Todas las pruebas de contrato T010, T020 y T030 pueden ejecutarse en paralelo.
- Las tareas de creacion de entidades T012, T013, T022, T032 y T033 pueden ejecutarse en paralelo cuando los directorios son independientes.
- Diferentes historias de usuario pueden desarrollarse en paralelo por distintos miembros del equipo una vez completada la Fase 2.

---

## Ejemplo paralelo: ejecucion de historias

```bash
# Luego de completar la base, lanzar pruebas relacionadas juntas
Task: "T010 [US1] Prueba de contrato para POST /api/auth/register en backend/src/test/java/com/prontaentrega/controllers/AuthControllerTest.java"
Task: "T020 [US2] Prueba de contrato para POST /api/auth/login en backend/src/test/java/com/prontaentrega/controllers/AuthControllerTest.java"
Task: "T030 [US3] Prueba de contrato para GET /api/players en backend/src/test/java/com/prontaentrega/controllers/PlayerControllerTest.java"

# Trabajo paralelo de modelos en archivos independientes
Task: "T012 [US1] Crear la entidad Usuario y sus reglas de validacion en backend/src/main/java/com/prontaentrega/models/Usuario.java"
Task: "T032 [US3] Crear los modelos de jugador y snapshot del catalogo en backend/src/main/java/com/prontaentrega/models/Jugador.java y backend/src/main/java/com/prontaentrega/models/CatalogSnapshot.java"
```

---

## Estrategia de implementacion

### MVP primero (solo historia de usuario 1)

1. Completar la Fase 1: Configuracion
2. Completar la Fase 2: Base
3. Completar la Fase 3: Historia de usuario 1
4. Validar el flujo de registro y la emision de JWT
5. Luego avanzar a la historia de usuario 2 y 3 en el mismo sprint

### Entrega incremental

- La historia 1 entrega la base de identidad y autenticacion.
- La historia 2 agrega login y invalidacion de token, protegiendo futuros endpoints.
- La historia 3 agrega acceso publico al catalogo con paginacion y comportamiento resiliente de importacion.
- El pulido final se enfoca en cobertura, documentacion y verificacion del quickstart.
