# Tasks: Subasta de tokens del mercado

**Input**: Design documents from `/specs/007-token-auction/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description with file path`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare the existing Spring Boot project for the auction feature and confirm integration points with the marketplace domain.

- [x] T001 Review existing marketplace patterns and confirm package structure in `backend/app/src/main/java/com/prontaentrega/`
- [x] T002 [P] Add auction DTOs and request validation contracts in `backend/app/src/main/java/com/prontaentrega/controllers/dtos/` and `backend/app/src/main/java/com/prontaentrega/controllers/`
- [x] T003 [P] Add repository stubs and persistence contracts for auctions and offers in `backend/app/src/main/java/com/prontaentrega/persistence/repository/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Create the core auction domain and persistence base that all stories depend on.

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel.

- [x] T004 Create `Subasta` domain model in `backend/app/src/main/java/com/prontaentrega/models/Subasta.java`
- [x] T005 Create `Oferta` domain model in `backend/app/src/main/java/com/prontaentrega/models/Oferta.java`
- [x] T006 [P] Add validation and status logic for auction lifecycle in `backend/app/src/main/java/com/prontaentrega/models/`
- [x] T007 [P] Add persistence mapping and repository support for `Subasta` and `Oferta` in `backend/app/src/main/java/com/prontaentrega/persistence/`
- [x] T008 Create auction service exceptions and domain error handling in `backend/app/src/main/java/com/prontaentrega/services/exceptions/`
- [x] T009 Create `SubastaService` skeleton and transaction boundaries in `backend/app/src/main/java/com/prontaentrega/services/SubastaService.java`

---

## Phase 3: User Story 1 - Publicar tokens en subasta (Priority: P1) 🎯 MVP

**Goal**: Allow a token owner to create an active auction with a valid quantity and closing time.

**Independent Test**: Create a valid auction and verify it is active, visible, and freezes the seller's tokens.

### Implementation for User Story 1

- [x] T010 [P] [US1] Add controller endpoint contract for auction creation in `backend/app/src/main/java/com/prontaentrega/controllers/SubastaController.java`
- [x] T011 [P] [US1] Implement create-auction flow in `backend/app/src/main/java/com/prontaentrega/services/SubastaService.java`
- [x] T012 [US1] Validate minimum quantity, future deadline, and owner token availability in `backend/app/src/main/java/com/prontaentrega/models/Subasta.java`
- [x] T013 [US1] Persist auction state and lock token availability in `backend/app/src/main/java/com/prontaentrega/persistence/repository/` and `backend/app/src/main/java/com/prontaentrega/services/SubastaService.java`
- [x] T014 [US1] Add `GET /api/subastas` listing support and pagination in `backend/app/src/main/java/com/prontaentrega/controllers/SubastaController.java`
- [x] T015 [US1] Add `GET /api/subastas/{id}` detail retrieval in `backend/app/src/main/java/com/prontaentrega/controllers/SubastaController.java`

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently.

---

## Phase 4: User Story 2 - Realizar pujas sobre una subasta activa (Priority: P1)

**Goal**: Let other users bid on active auctions only if they exceed the current highest offer and do not violate auction rules.

**Independent Test**: Place a valid bid, reject non-valid bids, and verify that the highest offer remains correct and idempotent.

### Implementation for User Story 2

- [x] T016 [P] [US2] Add bid request DTOs and validation rules in `backend/app/src/main/java/com/prontaentrega/controllers/dtos/`
- [x] T017 [P] [US2] Implement `POST /api/subastas/ofertar` in `backend/app/src/main/java/com/prontaentrega/controllers/SubastaController.java`
- [x] T018 [US2] Implement bidding rules in `backend/app/src/main/java/com/prontaentrega/services/SubastaService.java` (owner cannot bid, open auction only, higher offer required)
- [x] T019 [US2] Enforce idempotence by user + auction in `backend/app/src/main/java/com/prontaentrega/models/Oferta.java` and `backend/app/src/main/java/com/prontaentrega/services/SubastaService.java`
- [x] T020 [US2] Add optimistic concurrency handling for simultaneous bids in `backend/app/src/main/java/com/prontaentrega/models/Subasta.java` and service transaction logic
- [x] T021 [US2] Add `GET /api/subastas/{id}/ofertas` history endpoint in `backend/app/src/main/java/com/prontaentrega/controllers/SubastaController.java`

**Checkpoint**: At this point, User Stories 1 and 2 should both work independently.

---

## Phase 5: User Story 3 - Cierre automático y transferencia final (Priority: P1)

**Goal**: Resolve the auction at timeout or owner closure, transfer tokens to the valid winner, and register the transaction for both parties.

**Independent Test**: Close an auction with and without a valid highest bidder and verify tokens, credits, and operations are mutually consistent.

### Implementation for User Story 3

- [x] T022 [P] [US3] Add close-auction request/response contracts in `backend/app/src/main/java/com/prontaentrega/controllers/dtos/`
- [x] T023 [P] [US3] Implement `PATCH /api/subastas/{id}/cerrar` in `backend/app/src/main/java/com/prontaentrega/controllers/SubastaController.java`
- [x] T024 [US3] Implement auction close logic in `backend/app/src/main/java/com/prontaentrega/services/SubastaService.java` for time expiration and owner-driven close
- [x] T025 [US3] Resolve winner selection including fallback to the next valid highest offer and rejection of insufficient-credit bids in `backend/app/src/main/java/com/prontaentrega/services/SubastaService.java`
- [x] T026 [US3] Update token inventory and credit balances for seller and winner in `backend/app/src/main/java/com/prontaentrega/models/` and `backend/app/src/main/java/com/prontaentrega/services/SubastaService.java`
- [x] T027 [US3] Generate final transaction records for both parties in `backend/app/src/main/java/com/prontaentrega/services/SubastaService.java` and `backend/app/src/main/java/com/prontaentrega/persistence/`

**Checkpoint**: All user stories should now be independently functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final validation, cleanup, and consistency checks for the full auction feature.

- [x] T028 [P] Add integration test coverage for edge cases in `backend/app/src/test/java/com/prontaentrega/services/SubastaServiceTest.java`
- [x] T029 [P] Add controller tests for closed auctions, invalid offers, and final settlement in `backend/app/src/test/java/com/prontaentrega/controllers/SubastaControllerTest.java`
- [x] T030 Documentation pass over `specs/007-token-auction/quickstart.md`, `research.md`, and `contracts/subasta-api.md` for final consistency
- [x] T031 Run the auction quickstart validation and verify the complete flow in `backend/app` and `frontend/src` if the UI is connected

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - blocks all stories
- **User Stories (Phase 3+)**: All depend on Foundational completion
  - Story 1 can start after Phase 2 and is the MVP
  - Story 2 depends on the auction base and can begin in parallel with Story 1 once foundation is complete
  - Story 3 depends on the same base and the valid winner logic from Story 2
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational - no dependency on other stories
- **User Story 2 (P1)**: Can start after Foundational - depends on the active auction flow from US1
- **User Story 3 (P1)**: Can start after US1 and US2 are complete enough to close and resolve auctions

### Parallel Opportunities

- `T002` and `T003` can run together in Phase 1
- `T006` and `T007` can run together in Phase 2
- `T010` and `T011` can run together in US1
- `T016` and `T017` can run together in US2
- `T022` and `T023` can run together in US3
- `T028` and `T029` can run together in the polish phase

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Setup
2. Complete Foundational
3. Complete User Story 1
4. Validate the auction creation and listing flow independently
5. Stop and confirm the MVP is correct before progressing

### Incremental Delivery

1. Setup + Foundational → auction base ready
2. Add User Story 1 → create and list active auctions
3. Add User Story 2 → accepted and rejected bids with idempotence and concurrency guardrails
4. Add User Story 3 → close, settle, and register final transactions
5. Final polish and validation pass

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundation is done:
   - Developer A: User Story 1
   - Developer B: User Story 2
   - Developer C: User Story 3
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps tasks to a specific user story for traceability
- Each user story is independently testable and should be validated separately
- Keep each task scoped to a single file cluster or service responsibility
- Avoid tasks that cross story boundaries unless they are shared infrastructure items
