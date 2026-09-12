# Quickstart — Validación de autenticación y catálogo

## Precondiciones

- Java 21.
- Gradle disponible mediante wrapper del proyecto.
- Docker Desktop corriendo para Testcontainers.
- PostgreSQL local disponible en `jdbc:postgresql://localhost:5432/`.
- Base local `prontaentregadevapp` creada.
- Configuración local basada en `postgres` / `root`; las credenciales reales no deben versionarse. El proyecto debe incluir `.env.example`.

Los tests no usan el perfil local: levantan PostgreSQL en Testcontainers.

## Preparar y levantar

Desde la raíz:

```bash
cd backend
./gradlew test
./gradlew bootRun
```

La aplicación debe quedar disponible en `http://localhost:8080`.

## Validación 1: registro

```bash
curl -i -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"nombre":"Ana","correo":"ana@example.com","password":"clave123"}'
```

Esperado: `201 Created`, usuario sin contraseña y un JWT en `token`.

Repetir con el mismo correo debe devolver `409` sin crear un segundo usuario.

## Validación 2: inicio de sesión y reemplazo de JWT

```bash
curl -i -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"correo":"ana@example.com","password":"clave123"}'
```

Esperado: `200 OK` con un nuevo JWT. El JWT anterior debe quedar inválido inmediatamente para futuros endpoints protegidos.

Probar contraseña incorrecta debe devolver `401` con un mensaje que no revele si el correo existe.

## Validación 3: catálogo público

```bash
curl -i 'http://localhost:8080/api/players?liga=Premier%20League&page=1&per_page=20'
```

Esperado: `200 OK` sin header de autenticación, lista paginada y campos `nombre`, `equipo`, `liga`, `cotizacion` y `fecha_actualizacion`.

Probar combinaciones de `liga`, `equipo` y `nombre`, resultado vacío y página fuera de rango.

## Validación 4: actualización semanal y fallback

- Ejecutar el importador con un fixture válido de las cinco ligas.
- Verificar que jugadores y `fecha_actualizacion` cambian en el snapshot válido.
- Ejecutar una importación fallida.
- Verificar que la última versión válida sigue disponible y que su fecha no se presenta como una actualización nueva.
- Verificar que una instalación sin ningún snapshot válido informa `503 CATALOGO_NO_DISPONIBLE`.

## Evidencia requerida

- Tests unitarios de modelos para invariantes y validaciones, y tests del módulo de autenticación para firma, expiración y reemplazo de JWT.
- Tests de services para duplicados, credenciales y fallback de importación.
- Tests de repositorios con PostgreSQL real en Testcontainers.
- Tests E2E con MockMvc para los contratos de [api.md](contracts/api.md).
- Colección Postman con registro, login, catálogo y casos de error.
