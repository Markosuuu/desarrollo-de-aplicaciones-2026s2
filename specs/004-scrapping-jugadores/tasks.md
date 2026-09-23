# Tasks: Scraping de jugadores y actualización de estadísticas

**Input**: Design documents from `/specs/004-scrapping-jugadores/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/scraping-api.md`, `quickstart.md`

**Tests**: Required by the feature plan and constitution. Feature tests must exercise real infrastructure and must not mock internal services or repositories. Provider failure, incomplete rows and duplicate rows can be exercised through a real local HTTP test server that returns controlled WhoScored-shaped responses.

## Format: `[ID] [P?] [Story] Description`

## Phase 1: Setup

**Purpose**: Prepare the backend integration points for the catalog refresh.

- [x] T001 Review the existing player entity, repository, catalog service, controller and security configuration in `backend/app/src/main/java/com/prontaentrega/`
- [x] T002 [P] Confirm PostgreSQL configuration without Testcontainers for real integration tests in `backend/app/build.gradle` and `backend/app/src/test/`

---

## Phase 2: Foundational

**Purpose**: Establish the external data contract and persistence safety needed by all stories.

- [x] T003 Define WhoScored response DTOs and mapping of `playerTableStats` fields in `backend/app/src/main/java/com/prontaentrega/services/dto/`
- [x] T004 Add provider client configuration and HTTP retrieval with timeouts and Spanish provider errors in `backend/app/src/main/java/com/prontaentrega/services/`
- [x] T005 Review/update the existing `Jugador` uniqueness and upsert support for stable player identity in `backend/app/src/main/java/com/prontaentrega/models/Jugador.java` and `backend/app/src/main/java/com/prontaentrega/repository/JugadorRepository.java`
- [x] T006 Define validation for complete usable player records; incomplete records must be skipped without overwriting valid local data in `backend/app/src/main/java/com/prontaentrega/services/`
- [x] T007 Implement transaction boundaries for catalog persistence so a failed refresh cannot leave an ambiguous partial state in `backend/app/src/main/java/com/prontaentrega/services/`
- [x] T007a Finalize the update endpoint status codes and response body from `specs/004-scrapping-jugadores/contracts/scraping-api.md` before controller tests are written

**Checkpoint**: Provider parsing, validation and safe persistence foundations are ready for the user stories.

---

## Phase 3: User Story 1 - Actualizar estadísticas de jugadores (Priority: P1)

**Goal**: Refresh existing player records from WhoScored and make the latest values available locally.

**Independent Test**: Run the refresh against a valid WhoScored response and PostgreSQL; verify updated fields persist and are readable without another provider request.

### Tests for User Story 1

- [x] T008 [P] [US1] Add real PostgreSQL integration coverage for updating an existing player and verifying persisted fields in `backend/app/src/test/java/com/prontaentrega/integration/`
- [x] T009 [P] [US1] Add provider contract integration coverage for parsing required statistics from a real WhoScored response or a controlled local HTTP response with the same `playerTableStats` shape in `backend/app/src/test/java/com/prontaentrega/integration/`

### Implementation for User Story 1

- [x] T010 [US1] Implement mapping of name, team, league, age, goals, assists, shots per game, pass success percentage, successful dribbles, fouls committed and rating to `Jugador` in `backend/app/src/main/java/com/prontaentrega/services/`
- [x] T011 [US1] Implement idempotent update-or-insert behavior for existing players in `backend/app/src/main/java/com/prontaentrega/services/PlayerCatalogService.java` and `backend/app/src/main/java/com/prontaentrega/repository/JugadorRepository.java`
- [x] T012 [US1] Ensure normal catalog reads return persisted statistics without calling WhoScored in `backend/app/src/main/java/com/prontaentrega/services/PlayerCatalogService.java` and `backend/app/src/main/java/com/prontaentrega/controllers/PlayerController.java`

**Checkpoint**: Existing player records refresh and remain available from local storage.

---

## Phase 4: User Story 2 - Poblar la base desde cero (Priority: P1)

**Goal**: Populate an empty player catalog from the provider.

**Independent Test**: With an empty PostgreSQL player table, run refresh and verify useful, complete player records are created.

### Tests for User Story 2

- [x] T013 [P] [US2] Add real PostgreSQL integration coverage for initial population from an empty catalog in `backend/app/src/test/java/com/prontaentrega/integration/`
- [x] T014 [P] [US2] Verify repeated initial refreshes do not create duplicate player records in `backend/app/src/test/java/com/prontaentrega/integration/`

### Implementation for User Story 2

- [x] T015 [US2] Extend the refresh service to persist valid new players when no prior records exist in `backend/app/src/main/java/com/prontaentrega/services/PlayerCatalogService.java`
- [x] T016 [US2] Ensure persistence enforces a single consistent record per player during initial and repeated loads in `backend/app/src/main/java/com/prontaentrega/repository/JugadorRepository.java`

**Checkpoint**: An empty database can be populated and repeated loads remain idempotent.

---

## Phase 5: User Story 3 - Continuar funcionando ante fallas de proveedor (Priority: P1)

**Goal**: Preserve the last valid local catalog when WhoScored fails or returns invalid/incomplete data.

**Independent Test**: Start with persisted players, exercise provider failure and mixed valid/incomplete records, then verify valid local data remains and an understandable error/result is reported.

### Tests for User Story 3

- [x] T017 [P] [US3] Add real PostgreSQL integration coverage proving provider failure preserves the last valid catalog, using a real local HTTP failure response when the remote provider cannot produce the case on demand, in `backend/app/src/test/java/com/prontaentrega/integration/`
- [x] T018 [P] [US3] Add integration coverage that valid records are saved and incomplete records skipped without overwriting existing valid records, using controlled WhoScored-shaped HTTP responses, in `backend/app/src/test/java/com/prontaentrega/integration/`
- [x] T019 [P] [US3] Add integration coverage that an empty catalog remains safe and reports a clear failure when the provider fails or yields no useful data, using controlled HTTP responses, in `backend/app/src/test/java/com/prontaentrega/integration/`

### Implementation for User Story 3

- [x] T020 [US3] Handle provider errors and empty/unusable responses while retaining local catalog data in `backend/app/src/main/java/com/prontaentrega/services/PlayerCatalogService.java`
- [x] T021 [US3] Persist valid rows from mixed provider results while skipping incomplete rows and preserving prior valid player values in `backend/app/src/main/java/com/prontaentrega/services/PlayerCatalogService.java`
- [x] T022 [US3] Return clear Spanish error information for failed refresh attempts through `backend/app/src/main/java/com/prontaentrega/services/exceptions/` and `backend/app/src/main/java/com/prontaentrega/controllers/GlobalExceptionHandler.java`

**Checkpoint**: Provider failure or malformed rows do not destroy or corrupt the local catalog.

---

## Phase 6: Endpoint, access and delivery documentation

**Purpose**: Expose and document the refresh operation consistently with the API contract.

- [x] T023 Add `POST /api/players/update` to `backend/app/src/main/java/com/prontaentrega/controllers/PlayerController.java`, delegating refresh to the service
- [x] T024 Configure the update endpoint to require no role or authentication while preserving existing access rules for other endpoints in `backend/app/src/main/java/com/prontaentrega/config/SecurityConfig.java`
- [x] T025 [P] Add MockMvc coverage for successful and failed update responses, public access and the finalized response body in `backend/app/src/test/java/com/prontaentrega/controllers/PlayerControllerTest.java`
- [x] T026 [P] Add the refresh endpoint request and response examples to `postman/`
- [x] T027 Document local execution and real-provider verification steps in `specs/004-scrapping-jugadores/quickstart.md`

---

## Phase 7: Polish and cross-cutting concerns

- [x] T028 [P] Review `specs/004-scrapping-jugadores/contracts/scraping-api.md` against the implemented status codes and response behavior
- [x] T029 Review class and method documentation for new provider, mapper and refresh behavior in `backend/app/src/main/java/com/prontaentrega/`
- [x] T030 Verify the feature against all acceptance scenarios, edge cases and success criteria in `specs/004-scrapping-jugadores/spec.md`

---

## Dependencies & Execution Order

### Phase dependencies

- **Setup (Phase 1)**: No dependencies.
- **Foundational (Phase 2)**: Depends on Setup and blocks all user stories.
- **User Stories (Phases 3–5)**: Depend on Foundational; prioritize P1 stories and preserve their individual checkpoints.
- **Endpoint and delivery (Phase 6)**: Depends on refresh service behavior from Phases 3–5.
- **Polish (Phase 7)**: Depends on all implementation and endpoint work.

### User story dependencies

- **US1**: Depends on provider mapping and safe persistence; establishes refresh of existing players.
- **US2**: Depends on the same refresh path and adds creation when the catalog is empty.
- **US3**: Depends on provider retrieval and persistence; covers failure handling and partial valid results across both update and initial-load states.

### Parallel opportunities

- T008 and T009 can be authored independently; T010–T012 follow the shared model and mapping foundations.
- T013 and T014 can be authored independently; US2 persistence builds on US1's upsert path.
- T017–T019 can be authored independently; failure handling and partial-record behavior are implemented after the shared persistence path.
- T025–T027 and T028–T029 can be handled in parallel once endpoint behavior is settled by T007a.

## Implementation Strategy

1. Complete Setup and Foundational work.
2. Implement and verify US1 refresh of existing records.
3. Implement and verify US2 initial population and idempotence.
4. Implement and verify US3 failure preservation and valid-row filtering.
5. Expose the endpoint, document it in Postman and quickstart, then review the acceptance criteria.
