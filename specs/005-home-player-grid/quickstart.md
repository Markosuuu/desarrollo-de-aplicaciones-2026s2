# Guía rápida de validación: Home con catálogo de jugadores

## Requisitos

- Backend del proyecto levantado localmente.
- Frontend del proyecto levantado localmente.
- Base de datos con jugadores cargados o un conjunto de prueba con registros suficientes.

## 1. Levantar backend

```bash
cd backend
./gradlew bootRun
```

## 2. Levantar frontend

```bash
cd frontend
npm install
npm run dev
```

## 3. Validar el listado desde la API

```bash
curl "http://localhost:8080/api/players?nombre=&equipo=&liga=&page=1&perPage=20"
```

### Resultado esperado

- La respuesta devuelve una lista de jugadores.
- El campo `paginacion.total` refleja la cantidad total de registros.
- `paginacion.porPagina` debe coincidir con el número de registros mostrados por vista.

## 4. Validar la experiencia en el navegador

1. Abrir la vista de Home.
2. Confirmar que aparece una grilla con 4 columnas.
3. Validar que cada tarjeta incluye nombre, equipo, liga y posición.
4. Buscar por nombre, equipo o liga y comprobar que el listado se reduce correctamente.
5. Confirmar que, cuando existen más de 20 resultados, la paginación permite navegar entre páginas.
6. Probar un filtro sin coincidencias para confirmar el estado de vacío.

## Resultado esperado

- El catálogo se presenta con visual clara y legible.
- El filtro actualiza la vista sin recarga completa.
- La paginación respeta el conjunto filtrado.
- El usuario puede navegar los datos del catálogo completo sin perder el contexto visual.
