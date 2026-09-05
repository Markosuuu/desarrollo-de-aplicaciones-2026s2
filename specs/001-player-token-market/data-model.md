# data-model.md — Entities (v1)

Scope note: v1 implements registration (RF-1) and player catalog (RF-2). Data model includes minimal fields required for these features and forward-compatible fields for tokens.

Entities

1) User
- id: UUID
- name: string
- email: string (unique)
- password_hash: string (stored securely)
- apikey: string (opaque token for API access)
- created_at: timestamp

2) Player
- id: UUID
- name: string
- team: string
- league: string
- who_scored_rating: decimal (current rating used as cotización in v1)
- total_tokens: integer (100) — present for forward compatibility
- tokens_available: integer — present for future flows
- updated_at: timestamp

Notes
- Catalog endpoints must support pagination and league filtering.
- Authentication stored as apikey for simple API access; password retained for UI/ops flows.
- Keep token-related fields nullable/unused for v1 but present to avoid later DB refactors.
