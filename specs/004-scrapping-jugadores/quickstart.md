# Quickstart — Scrapping de jugadores y actualización de estadísticas

## Objetivo

Validar la funcionalidad de carga inicial y actualización del catálogo de jugadores desde una fuente externa real sin depender de mocks.

## Requisitos

- backend de la aplicación ejecutándose en entorno local
- PostgreSQL disponible para la aplicación
- acceso a WhoScored o a un servidor HTTP local de prueba que devuelva payloads reales del formato `playerTableStats`

## Pasos

1. Levantar la aplicación backend con la configuración local adecuada.
2. Confirmar que la base de datos está vacía o contiene un estado previo conocido.
3. Ejecutar el endpoint específico de actualización de estadísticas:

   ```bash
   curl -X POST http://localhost:8080/api/players/update
   ```

4. Verificar que la respuesta del endpoint indica éxito o error claro según el estado de la fuente externa.
5. Consultar la base local mediante el flujo de lectura correspondiente y validar que los jugadores tienen los campos esperados:

   ```bash
   curl "http://localhost:8080/api/players?page=1&perPage=20"
   ```

6. Ejecutar nuevamente la actualización y confirmar que no se duplican jugadores.
7. Simular una falla de proveedor con un servidor HTTP local que responda error o payload inválido, y confirmar que la última información local válida sigue disponible y la operación no destruye la base.

## Verificación clave

- Un caso feliz debe poblar la base desde cero si está vacía.
- Un caso con error de proveedor debe conservar la última información válida.
- La respuesta del endpoint debe ser clara y comprensible en español.
- Las pruebas deben ejecutarse sin mocks de servicios internos ni de repositorio para respetar la especificación.
- Los casos de fallo del proveedor pueden usar un servidor HTTP local real con respuestas controladas para validar errores, registros incompletos y respuestas vacías de forma reproducible.

## Endpoint de referencia

Se debe tener en cuenta la estructura del endpoint de WhoScored y el ejemplo `playerTableStats` entregado en la especificación para mapear correctamente los campos de la respuesta a la entidad local `Jugador`.
