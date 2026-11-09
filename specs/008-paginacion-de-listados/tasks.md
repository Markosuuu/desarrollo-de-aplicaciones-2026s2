# Tasks: Paginación de listados del usuario y mercado

**Input**: Design documents from `/specs/008-paginacion-de-listados/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Prepare the backend and frontend for paginated list handling and shared UI states.

- [ ] T001 Create the feature implementation context in `backend/app/src/main/java/com/prontaentrega/` and `frontend/src/` per the existing project structure
- [ ] T002 [P] Audit existing paginated endpoints and reusable DTO patterns in `backend/app/src/main/java/com/prontaentrega/controllers/`, `services/`, and `persistence/repository/`
- [ ] T003 [P] Audit current frontend list rendering patterns in `frontend/src/pages/UserPage.jsx`, `MercadoPage.jsx`, `ListaSubastaPage.jsx`, and `frontend/src/api/`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Establish the shared pagination contract and reusable UI behavior before any story-specific implementation.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [ ] T004 Define paginated response contract and validate page/size behavior in `backend/app/src/main/java/com/prontaentrega/controllers/MercadoController.java`, `SubastaController.java`, and `specs/008-paginacion-de-listados/contracts/paginacion-listados-api.md`
- [ ] T005 [P] Implement backend pagination support primitives in `backend/app/src/main/java/com/prontaentrega/services/` and `backend/app/src/main/java/com/prontaentrega/persistence/repository/` using `Page` and `PageRequest`
- [ ] T006 [P] Create shared frontend pagination state utilities and empty/final-state rendering strategy in `frontend/src/components/` and `frontend/src/pages/`
- [ ] T007 Add validation for page bounds and empty responses across backend and frontend entry points in `backend/.../controllers/` and `frontend/src/api/`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel.

---

## Phase 3: User Story 1 - Inventario del usuario (Priority: P1) 🎯 MVP

**Goal**: Allow a user to navigate their inventory in pages of 10 items and display the correct empty-state message.

**Independent Test**: Load an inventory with more than 10 tokens and verify that only 10 appear per page, the page buttons respect first/last limits, and the empty state appears when there are no tokens.

### Implementation for User Story 1

- [ ] T010 [P] [US1] Update inventory data flow to accept `page` and `size` parameters in `backend/app/src/main/java/com/prontaentrega/controllers/MercadoController.java`
- [ ] T011 [US1] Implement paginated inventory retrieval in `backend/app/src/main/java/com/prontaentrega/services/MercadoService.java` and related persistence access for `InventarioUsuario`
- [ ] T012 [P] [US1] Add inventory pagination controls and empty-state handling in `frontend/src/pages/UserPage.jsx`
- [ ] T013 [US1] Add inventory-specific pager styling and disabled state rules in `frontend/src/styles/userPage.css`
- [ ] T014 [US1] Verify the exact message copy `Inventario vacío` and boundary behavior for first/last page in `frontend/src/pages/UserPage.jsx`

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently.

---

## Phase 4: User Story 2 - Historial del usuario (Priority: P1)

**Goal**: Allow a user to browse transaction history in pages of 10 items and show the proper empty state when there are no operations.

**Independent Test**: Load a user with more than 10 operations and validate that each page contains at most 10 entries, the navigation remains bounded, and the empty-state text appears for users with no operations.

### Implementation for User Story 2

- [ ] T020 [P] [US2] Update transaction retrieval to support request pagination in `backend/app/src/main/java/com/prontaentrega/controllers/MercadoController.java`
- [ ] T021 [US2] Implement paginated transaction loading in `backend/app/src/main/java/com/prontaentrega/services/MercadoService.java` and matching repository query logic
- [ ] T022 [P] [US2] Add pagination controls and empty state for the transaction list in `frontend/src/pages/UserPage.jsx`
- [ ] T023 [US2] Update transaction list styles and disabled-button logic in `frontend/src/styles/userPage.css`
- [ ] T024 [US2] Confirm the message `No hay operaciones registradas` displays when there are no records in `frontend/src/pages/UserPage.jsx`

**Checkpoint**: At this point, User Stories 1 and 2 should both work independently.

---

## Phase 5: User Story 3 - Mercado (Priority: P1)

**Goal**: Present market listings in pages of 20 items with centered navigation and explicit empty-state messaging.

**Independent Test**: Verify that the market shows up to 20 listings per page, navigation buttons disable at page limits, and the empty state message appears when there are no active listings.

### Implementation for User Story 3

- [ ] T030 [P] [US3] Update the marketplace endpoint to return a paginated response in `backend/app/src/main/java/com/prontaentrega/controllers/MercadoController.java`
- [ ] T031 [US3] Implement page-based fetching in `backend/app/src/main/java/com/prontaentrega/services/MercadoService.java` and repository access for `AnuncioVenta`
- [ ] T032 [P] [US3] Update `frontend/src/api/marketApi.js` to request page metadata and normalize the payload for the UI
- [ ] T033 [P] [US3] Add market pagination controls and empty-state rendering in `frontend/src/pages/MercadoPage.jsx`
- [ ] T034 [US3] Add market pager styling and center-aligned controls in `frontend/src/styles/mercadoPage.css`
- [ ] T035 [US3] Confirm the empty message `No existen anuncios publicados actualmente` appears when no listings exist in `frontend/src/pages/MercadoPage.jsx`

**Checkpoint**: Market browsing should work independently and remain bounded by the correct page limits.

---

## Phase 6: User Story 4 - Subastas (Priority: P1)

**Goal**: Support infinite scroll loading for active auctions and show the no-more-results and empty-state messages.

**Independent Test**: Scroll to the end of the auction list, verify that more items load, that `hasNext` is respected, and that the final and empty messages display as required.

### Implementation for User Story 4

- [ ] T040 [US4] Update the auction listing API contract for pagination/infinite scroll in `backend/app/src/main/java/com/prontaentrega/controllers/SubastaController.java` and `backend/app/src/main/java/com/prontaentrega/services/SubastaService.java`
- [ ] T041 [P] [US4] Update `frontend/src/api/subastaApi.js` to request page-based auction data and expose `hasNext`/`page` metadata
- [ ] T042 [US4] Add infinite-scroll observer and final-state logic in `frontend/src/pages/ListaSubastaPage.jsx`
- [ ] T043 [P] [US4] Add the no-more-items and empty-state messaging styling in `frontend/src/styles/` for the subasta list
- [ ] T044 [US4] Confirm the exact messages `Fin de las subastas activas` and `No existen subastas activas actualmente` are rendered in the correct scenarios in `frontend/src/pages/ListaSubastaPage.jsx`

**Checkpoint**: All page-based and infinite-scroll flows should now be independently usable.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Finalize quality, consistency, and validation after all stories are complete.

- [ ] T050 [P] Review the message copy, button states, and empty-state handling across `frontend/src/pages/` and `frontend/src/styles/`
- [ ] T051 [P] Validate the API contract and response metadata against `specs/008-paginacion-de-listados/contracts/paginacion-listados-api.md`
- [ ] T052 Run the relevant backend/frontend validation steps from `specs/008-paginacion-de-listados/quickstart.md` and fix any edge-case regressions
- [ ] T053 Check all user story tasks for dependency ordering, file path accuracy, and completion status before closing the feature

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user story work.
- **User Stories (Phase 3+)**: All depend on the Foundational phase completion.
- **Polish (Final Phase)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **User Story 1 (US1)**: Can start after Foundational - independent from the rest.
- **User Story 2 (US2)**: Can start after Foundational and reuses the same shared pagination primitives.
- **User Story 3 (US3)**: Can start after Foundational and is independent from US1/US2 once the shared contract is in place.
- **User Story 4 (US4)**: Can start after Foundational and uses the same paginated base model but follows a different UX pattern.

### Parallel Opportunities

- Setup tasks T002 and T003 can run in parallel.
- Foundational tasks T005, T006, and T007 can run in parallel.
- Within each user story, all applicable [P] tasks can progress in parallel once the foundational phase is complete.
- The final Polish tasks can proceed once all story phases are complete.

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup.
2. Complete Phase 2: Foundational.
3. Complete Phase 3: User Story 1.
4. Stop and validate the inventory pagination flow independently.
5. Expand to User Story 2, 3, and 4 only after the MVP is stable.

### Incremental Delivery

1. Setup + Foundational => foundation ready.
2. User Story 1 => validate inventory pagination and empty state.
3. User Story 2 => validate transaction history pagination.
4. User Story 3 => validate market page navigation.
5. User Story 4 => validate infinite-scroll auction flow.
6. Finish with polish, consistency checks, and quickstart validation.

---

## Notes

- [P] tasks = different files, no dependencies.
- [US#] label maps tasks to the corresponding user story for traceability.
- Each user story is independently completable and testable.
- All task descriptions include concrete file paths to keep execution unambiguous.
- This plan aligns with the feature specification and the current backend/frontend architecture already present in the repository.
