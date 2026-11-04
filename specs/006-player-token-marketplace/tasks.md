# Tasks: Mercado de tokens de jugadores

**Input**: Design documents from `/specs/006-player-token-marketplace/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Las pruebas se incluyen por requerimiento del feature y por la necesidad de validar compra, venta, historial y edge cases.

**Organization**: Tasks grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initial project structure and alignment with existing backend/frontend patterns

- [ ] T001 Create baseline feature structure and confirm repository layout under `backend/app` and `frontend/src`
- [ ] T002 [P] Review repository and model conventions in `backend/app/src/main/java/com/prontaentrega/models`, `persistence/repository`, and `services`
- [ ] T003 [P] Define the marketplace domain contract baseline in `backend/app/src/main/java/com/prontaentrega/models/Usuario.java`, `backend/app/src/main/java/com/prontaentrega/models/Operacion.java`, and `backend/app/src/main/java/com/prontaentrega/models/BolsaDeJugador.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared validation, persistence and API infrastructure that MUST be complete before any story implementation

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T004 Implement marketplace domain exceptions in `backend/app/src/main/java/com/prontaentrega/services/exceptions/`
- [ ] T005 [P] Add credit and token validation rules to `backend/app/src/main/java/com/prontaentrega/models/Usuario.java` and `backend/app/src/main/java/com/prontaentrega/models/BolsaDeJugador.java`
- [ ] T006 [P] Add repository contracts for operation and marketplace persistence in `backend/app/src/main/java/com/prontaentrega/persistence/repository/`
- [ ] T007 Implement `MarketplaceService` core validation and state logic in `backend/app/src/main/java/com/prontaentrega/services/MarketplaceService.java`
- [ ] T008 Implement `MarketplaceController` endpoints in `backend/app/src/main/java/com/prontaentrega/controllers/MarketplaceController.java`
- [ ] T009 [P] Add foundational integration tests for invalid marketplace states in `backend/app/src/test/java/com/prontaentrega/services/MarketplaceServiceTest.java`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Explorar el mercado de jugadores en venta (Priority: P1) 🎯 MVP

**Goal**: Expose a paginated marketplace listing to users so they can browse available sales.

**Independent Test**: Given a populated marketplace, a user can open the listing page and see a 4x8 paginated grid with all required fields for each listing.

### Tests for User Story 1

- [ ] T010 [P] [US1] Contract test for `GET /api/marketplace/listings` in `backend/app/src/test/java/com/prontaentrega/controllers/MarketplaceControllerIntegrationTest.java`
- [ ] T011 [P] [US1] Integration test for marketplace grid rendering in `frontend/src/__tests__/MarketplacePage.test.js`

### Implementation for User Story 1

- [ ] T012 [P] [US1] Create listing DTOs and response models in `backend/app/src/main/java/com/prontaentrega/services/dto/MarketplaceListingResponse.java`
- [ ] T013 [US1] Implement active listings query logic in `backend/app/src/main/java/com/prontaentrega/services/MarketplaceService.java`
- [ ] T014 [US1] Expose `GET /api/marketplace/listings` in `backend/app/src/main/java/com/prontaentrega/controllers/MarketplaceController.java`
- [ ] T015 [P] [US1] Create frontend marketplace API client in `frontend/src/api/marketplaceApi.js`
- [ ] T016 [US1] Build paginated market page and card grid in `frontend/src/pages/MarketplacePage.jsx`
- [ ] T017 [US1] Add empty-state and pagination styling in `frontend/src/styles/marketplace.css`

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Publicar tokens en venta (Priority: P1)

**Goal**: Allow a user to publish a valid number of tokens for sale without exceeding their available inventory.

**Independent Test**: Given a user with available tokens, they can publish a valid amount and the sale remains pending; invalid quantities are rejected.

### Tests for User Story 2

- [ ] T018 [P] [US2] Contract test for `POST /api/orders/sell` in `backend/app/src/test/java/com/prontaentrega/controllers/MarketplaceControllerIntegrationTest.java`
- [ ] T019 [P] [US2] Service test for invalid sell quantities in `backend/app/src/test/java/com/prontaentrega/services/MarketplaceServiceTest.java`

### Implementation for User Story 2

- [ ] T020 [P] [US2] Create sell request DTO in `backend/app/src/main/java/com/prontaentrega/services/dto/VenderTokensRequest.java`
- [ ] T021 [US2] Implement sell validation, stock reservation and pending state logic in `backend/app/src/main/java/com/prontaentrega/services/MarketplaceService.java`
- [ ] T022 [US2] Implement `POST /api/orders/sell` in `backend/app/src/main/java/com/prontaentrega/controllers/MarketplaceController.java`
- [ ] T023 [P] [US2] Add frontend sell form component in `frontend/src/components/SellTokensForm.jsx`
- [ ] T024 [US2] Add validation UX and error messaging in `frontend/src/pages/MarketplacePage.jsx`

**Checkpoint**: At this point, User Stories 1 and 2 should both work independently

---

## Phase 5: User Story 3 - Comprar tokens de otro usuario (Priority: P1)

**Goal**: Allow secure purchase of active listings with checks for credits, self-buy restrictions and transfer completion.

**Independent Test**: Given a buyer with sufficient credits and a valid active sale from another user, the purchase completes and updates credits, ownership and state.

### Tests for User Story 3

- [ ] T025 [P] [US3] Contract test for `POST /api/orders/buy` in `backend/app/src/test/java/com/prontaentrega/controllers/MarketplaceControllerIntegrationTest.java`
- [ ] T026 [P] [US3] Service test for insufficient credits, self-buy and concurrent completion in `backend/app/src/test/java/com/prontaentrega/services/MarketplaceServiceTest.java`

### Implementation for User Story 3

- [ ] T027 [P] [US3] Create buy request DTO in `backend/app/src/main/java/com/prontaentrega/services/dto/ComprarTokensRequest.java`
- [ ] T028 [US3] Implement purchase validation, credit deduction, token transfer and completion state in `backend/app/src/main/java/com/prontaentrega/services/MarketplaceService.java`
- [ ] T029 [US3] Implement `POST /api/orders/buy` in `backend/app/src/main/java/com/prontaentrega/controllers/MarketplaceController.java`
- [ ] T030 [P] [US3] Add purchase action handler in `frontend/src/api/marketplaceApi.js`
- [ ] T031 [US3] Wire buy button action and confirmation flow in `frontend/src/pages/MarketplacePage.jsx`

**Checkpoint**: Purchase flow should be independently testable and complete for the seller/buyer transaction

---

## Phase 6: User Story 4 - Consultar historial de operaciones (Priority: P2)

**Goal**: Let each user review the complete list of their own buy/sell movements and their states.

**Independent Test**: Given a user with multiple operations, the history endpoint returns the JSON contract with type, state, amount, counterpart and timestamp fields.

### Tests for User Story 4

- [ ] T032 [P] [US4] Contract test for `GET /api/users/{id}/transactions` in `backend/app/src/test/java/com/prontaentrega/controllers/MarketplaceControllerIntegrationTest.java`
- [ ] T033 [P] [US4] History service test for completed, pending and cancelled operations in `backend/app/src/test/java/com/prontaentrega/services/MarketplaceServiceTest.java`

### Implementation for User Story 4

- [ ] T034 [P] [US4] Create transaction DTOs in `backend/app/src/main/java/com/prontaentrega/services/dto/OperacionResponse.java`
- [ ] T035 [US4] Implement historical query/filter logic in `backend/app/src/main/java/com/prontaentrega/services/MarketplaceService.java`
- [ ] T036 [US4] Expose `GET /api/users/{id}/transactions` in `backend/app/src/main/java/com/prontaentrega/controllers/MarketplaceController.java`
- [ ] T037 [P] [US4] Add frontend history fetch client in `frontend/src/api/userTransactionsApi.js`
- [ ] T038 [US4] Build transaction history view in `frontend/src/pages/UserTransactionsPage.jsx`

**Checkpoint**: User Story 4 should work independently and add auditability to the marketplace

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final validation, documentation and regression checks across all stories

- [ ] T039 [P] Validate API contract and edge-case behaviour against `specs/006-player-token-marketplace/contracts/marketplace-api.md`
- [ ] T040 [P] Update `specs/006-player-token-marketplace/quickstart.md` with final marketplace validation scenarios
- [ ] T041 Run backend regression suite covering marketplace, validation and transaction consistency
- [ ] T042 Run frontend smoke tests for listing, sell, buy and transaction history flows
- [ ] T043 Security review of credit, stock and concurrency protections across all marketplace operations

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational completion
  - User stories can be implemented in parallel if the team has capacity
  - Recommended order: US1 → US2 → US3 → US4
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: No dependency on other stories; can ship as the MVP entry point
- **User Story 2 (P2)**: Depends on foundational validation and may reuse US1 UI shell
- **User Story 3 (P3)**: Depends on US1 and US2 domain/state migration; should be implemented after valid sell flow
- **User Story 4 (P2)**: Depends on operation records created by US2/US3 and can be implemented after those states are stable

### Parallel Opportunities

- All tasks marked [P] can be executed in parallel within a phase when files are independent
- Test creation for each story can run in parallel with unrelated service and UI tasks
- Frontend API, component and CSS tasks can proceed independently once backend contract is stable
- Different user stories can be developed in parallel after the foundational layer is complete

---

## Parallel Example: Story 1 + Story 2

```bash
# Launch backend and frontend work for Story 1 together
Task: "Create listing DTOs in backend/app/src/main/java/com/prontaentrega/services/dto/MarketplaceListingResponse.java"
Task: "Create frontend marketplace API client in frontend/src/api/marketplaceApi.js"

# Launch story 2 validation tasks together
Task: "Create sell request DTO in backend/app/src/main/java/com/prontaentrega/services/dto/VenderTokensRequest.java"
Task: "Add frontend sell form component in frontend/src/components/SellTokensForm.jsx"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Verify listing page and pagination independently
5. Ship the marketplace shell as the first deliverable

### Incremental Delivery

1. Setup + Foundational → shared domain validation ready
2. User Story 1 → market listing becomes visible and browsable
3. User Story 2 → sellers can expose inventory
4. User Story 3 → buyers can complete purchases safely
5. User Story 4 → dashboard/history becomes auditable
6. Polish and regression validation across all stories

### Recommended Team Split

- Developer A: User Story 1 (backend + frontend listing UI)
- Developer B: User Story 2 (sell flow + validations)
- Developer C: User Story 3 + User Story 4 (purchase and history flow)
- Shared validation: cross-cutting and regression checks
