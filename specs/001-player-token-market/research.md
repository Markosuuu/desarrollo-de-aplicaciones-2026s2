# research.md — Decisions and rationale

Decision: Valuation strategy (v1)
- Chosen: "Rating simple" — derive token cotización from WhoScored public rating (one-to-one mapping) as a provisional approach.
- Rationale: Enables a fast v1 with minimal ETL and fewer data quality risks; product owner deferred final formula to a later phase.
- Alternatives considered: composite indicator from multiple WhoScored metrics; admin-configurable rules. These increase complexity and testing effort.

Decision: Market model (v1)
- Chosen: No P2P in v1 — all trades (if later implemented) will be against the system/superuser.
- Rationale: Reduces matching/liquidity complexity for the first delivery; aligns with product owner direction.

Implications
- V1 focuses on user onboarding and a browsable, paginated player catalog with stable ratings.
- Future work: valuation formula, token issuance lifecycle, buy/sell flows, portfolio and history.
