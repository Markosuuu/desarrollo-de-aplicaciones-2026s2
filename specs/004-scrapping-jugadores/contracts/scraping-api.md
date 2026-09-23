# Contract — Endpoint de actualización de estadísticas

## Endpoint

`POST /api/players/update`

## Purpose

Disparar la carga inicial o la actualización del catálogo de jugadores utilizando fuentes externas de estadísticas.

## Behavior

- Si la operación tiene éxito, persiste la información actualizada y devuelve un estado de éxito con un mensaje claro.
- Si la base está vacía, debe poblarse con los datos obtenidos.
- Si una fuente externa falla, debe responder con un error descriptivo y conservar la última información válida local.
- Si la fuente devuelve registros válidos e incompletos, debe persistir los válidos, omitir los incompletos y reportar cuántos registros fueron omitidos.
- Si la fuente no produce ningún registro útil, debe responder con error y no modificar el catálogo local.
- La operación se ejecuta sin permisos de administrador ni roles adicionales en esta entrega.

## Status codes

- `200 OK`: actualización completada con al menos un registro válido persistido.
- `502 Bad Gateway`: WhoScored no respondió, respondió con error, entregó un payload inválido o no produjo registros útiles.
- `500 Internal Server Error`: error interno no esperado durante la actualización.

## Response schema

- `success`: booleano que indica si la actualización persistió al menos un registro válido.
- `message`: texto claro en español.
- `created`: cantidad de jugadores creados.
- `updated`: cantidad de jugadores actualizados.
- `skipped`: cantidad de registros omitidos por estar incompletos, duplicados no resolubles o inválidos.
- `source`: fuente usada para la actualización; en esta entrega debe ser `WhoScored`.

## Response examples

### Success

```json
{
  "success": true,
  "message": "Actualizacion completada correctamente",
  "created": 8,
  "updated": 2,
  "skipped": 1,
  "source": "WhoScored"
}
```

### Failure with preserved local data

```json
{
  "success": false,
  "message": "No se pudo completar la actualizacion. Se conserva la informacion local vigente.",
  "created": 0,
  "updated": 0,
  "skipped": 0,
  "source": "WhoScored"
}
```

## Notes

La respuesta debe ser clara, comprensible y consistente con la política del proyecto de mantener mensajes en español. La estructura del payload de WhoScored y el ejemplo `playerTableStats` deben usarse como base de parsing y validación de integridad.
