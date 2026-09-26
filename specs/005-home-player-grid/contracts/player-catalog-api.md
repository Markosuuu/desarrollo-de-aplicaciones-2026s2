# Contrato: catálogo de jugadores

## Endpoint

### GET /api/players

Obtiene jugadores persistidos con soporte de filtrado y paginación.

### Query parameters

| Nombre | Tipo | Requerido | Descripción |
|--------|------|-----------|-------------|
| nombre | string | No | Filtro parcial por nombre del jugador |
| equipo | string | No | Filtro parcial por nombre del equipo |
| liga | string | No | Filtro parcial por nombre de la liga |
| page | integer | No | Número de página actual. El valor mínimo es 1 |
| perPage | integer | No | Cantidad máxima de resultados por página |

### Respuesta exitosa

```json
{
  "jugadores": [
    {
      "id": "uuid",
      "nombre": "Lionel Messi",
      "equipo": "Inter Miami",
      "liga": "MLS",
      "posicion": "Delantero"
    }
  ],
  "paginacion": {
    "pagina": 1,
    "porPagina": 20,
    "total": 145,
    "totalPaginas": 8
  }
}
```

### Consideraciones

- El backend realiza el filtrado por coincidencia parcial de texto.
- La paginación y el filtrado deben aplicarse de manera combinada.
- Si no hay resultados, la respuesta devuelve una colección vacía con el total correspondiente a cero.
