# Quickstart: validación de paginación y estados de UI

## Requisitos previos

- Java 21 instalado.
- Gradle o wrapper de proyecto disponible en `backend/gradlew`.
- Node.js y dependencias del frontend instaladas en `frontend`.
- Backend levantado en `http://localhost:8080` con datos de prueba.

## Validación del backend

### 1. Inventario paginado
1. Ejecutá el backend con los datos del usuario con inventario de más de 10 tokens.
2. Llamá a:
   - `GET /api/users/{id}/inventario?page=1&size=10`
3. Verificá que:
   - devuelve hasta 10 elementos,
   - incluye `totalPages`, `totalElements` y `hasNext`,
   - `page` es 1 en la primera carga,
   - si no hay elementos devuelve un contenido vacío y el frontend puede mostrar "Inventario vacío".

### 2. Historial paginado
1. Ejecutá el backend con un usuario que tenga más de 10 operaciones.
2. Llamá a:
   - `GET /api/users/{id}/transactions?page=1&size=10`
3. Verificá que:
   - devuelve 10 elementos máximo,
   - la segunda página refleja los registros siguientes,
   - un usuario sin operaciones devuelve un payload vacío y permite mostrar "No hay operaciones registradas".

### 3. Mercado paginado
1. Ingresa a un entorno con más de 20 anuncios activos.
2. Llamá a:
   - `GET /api/marketplace/listings?page=1&size=20`
3. Verificá que:
   - el primer bloque contiene 20 elementos como máximo,
   - la paginación calcula correctamente el total de páginas,
   - si no hay anuncios activos, el payload está vacío y la vista muestra "No existen anuncios publicados actualmente".

### 4. Listado de subastas con scroll infinito
1. Generá un conjunto de subastas activas.
2. Llamá a:
   - `GET /api/subastas?page=1&size=10`
3. Verificá que:
   - la primera respuesta contiene una página inicial,
   - cuando se solicita la siguiente página se agregan nuevos elementos,
   - si no quedan elementos, el frontend puede mostrar "Fin de las subastas activas".

## Validación del frontend

1. Abrí la vista del usuario y revisá que la tabla de inventario se pagine con botones de navegación bloqueados en los extremos.
2. Abrí el historial y verificá que los botones de paginación se deshabilitan en la primera y última página.
3. Abrí el mercado y revisá que la grilla se muestra en bloques de 20 y que el estado vacío aparece cuando no hay anuncios.
4. Abrí el listado de subastas y desplazá el scroll hasta el final; comprobá que se cargan más elementos y que el mensaje final aparece cuando no hay más subastas.

## Comandos de verificación

```bash
cd backend
./gradlew test

cd ../frontend
npm test -- --run
```

## Resultado esperado

- Todos los listados deben ofrecer una navegación clara y consistente.
- Los mensajes de estado deben aparecer exactamente en los escenarios previstos por la especificación.
- La UI debe bloquear la navegación fuera de rango y la carga extra cuando ya no hay más contenido disponible.
