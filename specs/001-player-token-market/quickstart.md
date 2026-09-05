# quickstart.md — Validation guide

Purpose: runnable steps to validate RF-1 and RF-2 end-to-end using a local dev server and seeded data.

Prerequisites
- Node/Python/your backend runtime available (exact tech chosen during implementation)
- Repo checked out

Steps
1. Start backend (example): `npm run dev` or `python -m app` — ensure server listens on http://localhost:3000
2. Seed players (script or fixture): load a small set of players with leagues and WhoScored ratings.

Validation 1 — Registration
- Request:
  curl -X POST http://localhost:3000/api/register -H "Content-Type: application/json" -d '{"name":"Alice","email":"alice@example.com","password":"secret"}'
- Expect: 201 and JSON containing `apikey` and user object.

Validation 2 — Catalog
- Request:
  curl http://localhost:3000/api/players?league=Premier%20League&page=1&per_page=20
- Expect: 200 and paginated list of players with `rating` field.

Notes
- If external data APIs are unavailable in CI, use local fixtures for deterministic tests.
