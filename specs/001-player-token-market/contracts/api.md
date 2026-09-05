# contracts/api.md — API contracts (v1)

Auth
- API key issued at registration. Client stores and uses header `X-API-Key: <key>` for authenticated endpoints.

1) Register user
- POST /api/register
- Request JSON: { "name": "string", "email": "string", "password": "string" }
- Response 201 JSON: { "user": { "id": "uuid", "name": "string", "email": "string" }, "apikey": "string" }
- Errors: 400 (validation), 409 (email exists)

2) Get player catalog
- GET /api/players?league={league}&page={n}&per_page={m}
- Public endpoint (no auth required).
- Response 200 JSON:
  {
    "players": [
      { "id": "uuid", "name": "string", "team": "string", "league": "string", "rating": number }
    ],
    "pagination": { "page": n, "per_page": m, "total": x }
  }
- Filtering: `league` optional; server returns paginated results.

Notes
- Keep contracts minimal and stable; richer filtering/sorting can be added in planned iterations.
- Use consistent error envelope: { "error": { "code": "string", "message": "string" } }.
