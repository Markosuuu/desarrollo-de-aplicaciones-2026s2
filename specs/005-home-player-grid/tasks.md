# Tasks: Listado de jugadores en Home

**Input**: Design documents from `/specs/005-home-player-grid/`

**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `contracts/player-catalog-api.md`, `quickstart.md`

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup

**Purpose**: Review the existing Home structure and catalog contract before implementing the UI features.

- [ ] T001 Review the current Home page, styles, and player catalog API contract in `frontend/src/pages/HomePage.jsx`, `frontend/src/index.css`, `backend/app/src/main/java/com/prontaentrega/controllers/PlayerController.java`, and `backend/app/src/main/java/com/prontaentrega/services/PlayerCatalogService.java`
- [ ] T002 [P] Confirm the player payload and pagination response shape in `backend/app/src/main/java/com/prontaentrega/controllers/dtos/PlayerResponse.java` and `backend/app/src/main/java/com/prontaentrega/services/dto/CatalogResponse.java`

---

## Phase 2: Foundational

**Purpose**: Prepare the shared frontend plumbing to fetch, filter and render the catalog within the existing app structure.

- [ ] T003 Create the player catalog fetch client in `frontend/src/api/playerApi.js`
- [ ] T004 Define the shared card, empty-state, search and pagination styles in `frontend/src/index.css`
- [ ] T005 Add the initial Home state model for page, query and record loading in `frontend/src/pages/HomePage.jsx`

**Checkpoint**: The app can request paginated player data from the backend and hold it in a consistent UI state.

---

## Phase 3: User Story 1 - Mostrar jugadores en Home (Priority: P1) 🎯 MVP

**Goal**: Present the existing catalog on the Home page with clear player cards and a four-column grid.

**Independent Test**: Open the Home page with data in the database and verify that players appear as cards with the required fields and a readable grid layout.

### Implementation for User Story 1

- [ ] T006 [P] [US1] Fetch the first page of players from the API and render each card in `frontend/src/pages/HomePage.jsx`
- [ ] T007 [US1] Map backend fields to the Home display: nombre, equipo, liga and posicion in `frontend/src/pages/HomePage.jsx`
- [ ] T008 [US1] Add the four-column grid layout and card styling in `frontend/src/index.css`

**Checkpoint**: User Story 1 is functional and testable independently.

---

## Phase 4: User Story 2 - Buscar jugadores por nombre, equipo o liga (Priority: P1)

**Goal**: Allow the user to filter the player list by name, team or league directly from Home.

**Independent Test**: Type a player name, team or league into the search field and verify that only matching cards remain visible in the list.

### Implementation for User Story 2

- [ ] T009 [P] [US2] Add the search input and query state handling in `frontend/src/pages/HomePage.jsx`
- [ ] T010 [US2] Pass the active search term to the catalog API and reset the page to the first result set in `frontend/src/api/playerApi.js` and `frontend/src/pages/HomePage.jsx`
- [ ] T011 [US2] Show the empty-results message when no players match the filter in `frontend/src/pages/HomePage.jsx` and `frontend/src/index.css`

**Checkpoint**: The search flow works independently and keeps the rendered list aligned with the query.

---

## Phase 5: User Story 3 - Navegar resultados con paginación (Priority: P2)

**Goal**: Handle catalogs larger than 20 players without making the page unreadable, while keeping filters and page state consistent.

**Independent Test**: With more than 20 records available, change pages and confirm the list updates correctly while preserving the current search filter.

### Implementation for User Story 3

- [ ] T012 [P] [US3] Add the pagination controls and page navigation state in `frontend/src/pages/HomePage.jsx`
- [ ] T013 [US3] Ensure page changes request the correct backend page and preserve active filters in `frontend/src/api/playerApi.js` and `frontend/src/pages/HomePage.jsx`
- [ ] T014 [US3] Disable pagination controls at the ends of the result set and keep the total page count in sync with the API response in `frontend/src/pages/HomePage.jsx`

**Checkpoint**: The Home page can explore the full catalog while staying readable and consistent.

---

## Phase 6: Validation & Acceptance

**Purpose**: Prove the feature works across empty, filtered and paginated states before closing the story.

- [ ] T015 [P] [US2] Add integration coverage for empty query results in `backend/app/src/test/java/com/prontaentrega/integration/`
- [ ] T016 [P] [US3] Add integration coverage for pagination boundaries when requesting pages beyond the last available result in `backend/app/src/test/java/com/prontaentrega/integration/`
- [ ] T017 [P] [US2] Add request/response examples for filtered `GET /api/players` calls to `postman/`
- [ ] T018 [P] Review the implemented Home experience against the product requirements in `specs/005-home-player-grid/spec.md`
- [ ] T019 Run the quickstart validation for list display, search and pagination in `specs/005-home-player-grid/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies.
- **Foundational (Phase 2)**: Depends on Setup and blocks all user stories.
- **User Stories (Phases 3–5)**: Depend on Foundational completion.
- **Polish (Phase 6)**: Depends on all story work being complete.

### User Story Dependencies

- **US1**: Depends only on the data contract and shared fetch setup; it delivers the base Home view.
- **US2**: Depends on US1's rendering contract and adds filtering behavior on top of the same list.
- **US3**: Depends on US1 and US2 to preserve accurate content across page changes and filtered searches.

### Parallel Opportunities

- T001 and T002 can run in parallel during Setup.
- T003, T004 and T005 can be implemented in parallel once Setup is complete.
- T006 and T008 can be developed in parallel within US1 once the state model exists.
- T009 and T011 can be parallelized within US2.
- T012 and T014 can be developed in parallel inside US3.
- T015 and T016 can be validated in parallel during the final acceptance phase.

---

## Implementation Strategy

### MVP First

1. Complete Setup and Foundational work.
2. Finish User Story 1 to get a visible catalog in Home.
3. Validate the base card grid before adding filters.
4. Add User Story 2 for search and User Story 3 for pagination.
5. Run the quickstart validation and polish the final UX.

### Incremental Delivery

1. Display players on Home with the correct fields.
2. Add filtering by name, team and league.
3. Add pagination while preserving search state.
4. Validate the entire flow end-to-end against the spec and quickstart.
