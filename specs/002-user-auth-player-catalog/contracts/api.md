# API Contract — Autenticacion y catalogo

Base path: `/api`

Todos los errores usan el envelope:

```json
{
  "error": {
    "code": "CODIGO_ESTABLE",
    "message": "Mensaje en español"
  }
}
```

## POST `/api/auth/register`

Crea un usuario y devuelve su JWT de autenticación.

Request:

```json
{
  "nombre": "Ana",
  "correo": "ana@example.com",
  "password": "clave123"
}
```

Rules:

- `nombre`, `correo` y `password` son obligatorios.
- La contraseña debe tener al menos 8 caracteres, una letra y un número.
- El correo se compara sin distinguir mayúsculas/minúsculas.

Success `201 Created`:

```json
{
  "usuario": {
    "id": "uuid",
    "nombre": "Ana",
    "correo": "ana@example.com"
  },
  "token": "jwt-token"
}
```

Errors: `400 VALIDACION_INVALIDA`, `409 CORREO_DUPLICADO`.

## POST `/api/auth/login`

Autentica un usuario y reemplaza su JWT anterior.

Request:

```json
{
  "correo": "ana@example.com",
  "password": "clave123"
}
```

Success `200 OK`:

```json
{
  "usuario": {
    "id": "uuid",
    "nombre": "Ana",
    "correo": "ana@example.com"
  },
  "token": "new-jwt-token"
}
```

Errors: `400 VALIDACION_INVALIDA`, `401 CREDENCIALES_INVALIDAS`. El mensaje de error no distingue correo inexistente de contraseña incorrecta.

## GET `/api/players`

Consulta pública del catálogo; no requiere JWT.

Query parameters:

- `liga` opcional: una de las cinco ligas habilitadas.
- `equipo` opcional: coincidencia por nombre de equipo.
- `nombre` opcional: coincidencia por nombre de jugador.
- `page` opcional: página basada en 1; valor por defecto `1`.
- `per_page` opcional: tamaño acotado por el servidor; valor por defecto definido durante implementación.

Success `200 OK`:

```json
{
  "jugadores": [
    {
      "id": "uuid",
      "nombre": "Ana Player",
      "equipo": "Equipo FC",
      "liga": "Premier League",
      "cotizacion": 1.0,
      "fecha_actualizacion": "2026-09-12T00:00:00Z"
    }
  ],
  "paginacion": {
    "pagina": 1,
    "por_pagina": 20,
    "total": 1,
    "total_paginas": 1
  }
}
```

Rules:

- Solo devuelve jugadores de las cinco ligas permitidas.
- Los filtros se combinan con AND.
- Una página sin resultados devuelve `200` con lista vacía.
- La respuesta incluye la última fecha válida de actualización.
- Si la importación semanal falló, el endpoint conserva el último snapshot válido y no lo presenta como recién actualizado.

Errors: `400 FILTRO_INVALIDO`, `503 CATALOGO_NO_DISPONIBLE` únicamente si no existe ningún snapshot válido.

## Protected endpoint convention

Las futuras operaciones protegidas deben enviar el JWT en el header estándar:

```http
Authorization: Bearer jwt-token
```

Un JWT ausente, inválido, expirado o reemplazado debe devolver `401 JWT_INVALIDO`. Esta validación pertenece al módulo de autenticación y ocurre antes de invocar el servicio de negocio. El catálogo público es la excepción explícita de esta feature.
