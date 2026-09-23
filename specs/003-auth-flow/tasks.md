# Tasks: flujo de autenticación y sesión para el sitio dinámico

**Input**: Design documents from `/specs/003-auth-flow/`

**Prerequisitos**: plan.md, spec.md, research.md, data-model.md, contracts/

## Phase 1: Setup (infraestructura compartida)

**Purpose**: inicialización del proyecto y estructura base para backend y frontend

- [x] T001 Create backend/frontend directory layout for auth flow in backend/app/src/main/java/com/prontaentrega/ and frontend/src/{api,components,pages,styles,context}
- [x] T002 Configure frontend dependencies for auth flow, Toastify notifications, and theme styling in frontend/package.json
- [x] T003 [P] Create global app shell and route entry points in frontend/src/main.jsx and frontend/src/App.jsx
- [x] T004 [P] Create shared theme and auth context scaffold in frontend/src/context/ThemeContext.jsx and frontend/src/context/AuthContext.jsx

---

## Phase 2: Foundational (pre requisitos bloqueantes)

**Purpose**: preparar la base de seguridad, persistencia y validación que deben existir antes de cualquier historia de usuario

**⚠️ CRÍTICO**: ninguna historia de usuario puede empezar hasta que esta fase esté completa

- [x] T005 Confirm and stabilize backend auth contract and security config in backend/app/src/main/java/com/prontaentrega/config/SecurityConfig.java and backend/app/src/main/java/com/prontaentrega/controllers/AuthController.java
- [x] T006 [P] Review and stabilize request validation contracts for login/register in backend/app/src/main/java/com/prontaentrega/controllers/dtos/LoginRequest.java and backend/app/src/main/java/com/prontaentrega/controllers/dtos/RegisterRequest.java
- [x] T007 [P] Verify persistence and repository layer for usuarios and token state in backend/app/src/main/java/com/prontaentrega/models/Usuario.java, backend/app/src/main/java/com/prontaentrega/models/TokenEstado.java, backend/app/src/main/java/com/prontaentrega/repository/UsuarioRepository.java, and backend/app/src/main/java/com/prontaentrega/repository/TokenEstadoRepository.java
- [x] T008 Implement shared auth service validation and session issuance flow in backend/app/src/main/java/com/prontaentrega/services/AuthService.java
- [x] T009 Configure frontend API utilities and error handling wrappers in frontend/src/api/authApi.js and frontend/src/api/client.js

**Checkpoint**: la base de autenticación y seguridad está lista para empezar historias de usuario en paralelo

---

## Phase 3: User Story 1 - iniciar sesión con credenciales válidas (Priority: P1) 🎯 MVP

**Goal**: permitir que un usuario autenticado ingrese con correo y contraseña y acceda al home

**Independent Test**: un usuario puede abrir login, enviar credenciales válidas y quedar redirigido al home sin acceso a login/register

### Tests for User Story 1

- [x] T010 [P] [US1] Add backend login contract test in backend/app/src/test/java/com/prontaentrega/controllers/AuthControllerTest.java
- [x] T011 [P] [US1] Add backend invalid-login and invalid-email tests in backend/app/src/test/java/com/prontaentrega/controllers/AuthControllerTest.java
- [x] T012 [P] [US1] Add frontend Yup validation test for login schema in frontend/src/tests/loginSchema.test.js

### Implementation for User Story 1

- [x] T013 [P] [US1] Create login form page and submit flow in frontend/src/pages/LoginPage.jsx
- [x] T014 [P] [US1] Create Yup schema and validation for login fields in frontend/src/validation/loginSchema.js
- [x] T015 [US1] Integrate Toastify error notifications for invalid login attempts in frontend/src/components/ToastNotifications.jsx
- [x] T016 [US1] Implement login submission and token storage logic in frontend/src/api/authApi.js and frontend/src/context/AuthContext.jsx
- [x] T017 [US1] Add route guard for authenticated users in frontend/src/components/ProtectedRoute.jsx
- [x] T018 [US1] Add redirect behavior from login to home after successful authentication in frontend/src/App.jsx

**Checkpoint**: User Story 1 queda funcional y testeable de manera independiente

---

## Phase 4: User Story 2 - registro y acceso al home (Priority: P1)

**Goal**: permitir crear una nueva cuenta y completar el registro exitoso para entrar al home

**Independent Test**: un usuario nuevo completa nombre, correo y contraseña válidos, se registra y queda redirigido al home.

### Tests for User Story 2

- [x] T019 [P] [US2] Add backend registration validation test in backend/app/src/test/java/com/prontaentrega/controllers/AuthControllerTest.java
- [x] T020 [P] [US2] Add duplicate-user and invalid-register tests in backend/app/src/test/java/com/prontaentrega/controllers/AuthControllerTest.java
- [x] T021 [P] [US2] Add frontend Yup validation test for register schema in frontend/src/tests/registerSchema.test.js

### Implementation for User Story 2

- [x] T022 [P] [US2] Create register form page in frontend/src/pages/RegisterPage.jsx
- [x] T023 [P] [US2] Create Yup schema and validation for registration fields in frontend/src/validation/registerSchema.js
- [x] T024 [US2] Integrate registration submission and API call flow in frontend/src/api/authApi.js and frontend/src/context/AuthContext.jsx
- [x] T025 [US2] Add Toastify notifications for invalid registration data and duplicate user errors in frontend/src/components/ToastNotifications.jsx
- [x] T026 [US2] Add redirect from register to home after successful registration in frontend/src/App.jsx
- [x] T027 [US2] Ensure unauthenticated users can access register and authenticated users are redirected away from it in frontend/src/components/ProtectedRoute.jsx

**Checkpoint**: las historias 1 y 2 funcionan de forma independiente y pueden desarrollarse en paralelo

---

## Phase 5: User Story 3 - home, logout y tema (Priority: P2)

**Goal**: entregar la vista protegida, la salida de sesión y el tema visual

**Independent Test**: un usuario autenticado puede ver el home, cambiar el tema claro/oscuro y hacer logout para volver a login.

### Tests for User Story 3

- [x] T028 [P] [US3] Add redirect and logout navigation test in frontend/src/tests/auth-flow.test.js
- [x] T029 [P] [US3] Add theme toggle test in frontend/src/tests/theme-toggle.test.js

### Implementation for User Story 3

- [x] T030 [P] [US3] Create protected home page shell in frontend/src/pages/HomePage.jsx
- [x] T031 [P] [US3] Create dark/light theme toggle with ThemeContext and Tailwind classes in frontend/src/context/ThemeContext.jsx and frontend/src/styles/theme.css
- [x] T032 [US3] Add logout action and session clear flow in frontend/src/context/AuthContext.jsx and frontend/src/pages/HomePage.jsx
- [x] T033 [US3] Enforce authenticated redirect rules for protected routes and login/register access in frontend/src/App.jsx and frontend/src/components/ProtectedRoute.jsx
- [x] T034 [US3] Validate final navigation behavior: login/register unavailable while session is active, and logout returns to login in frontend/src/App.jsx

**Checkpoint**: la experiencia completa queda funcional y lista para validación end-to-end

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: ajustes finales y validación de calidad para entrega

- [x] T035 [P] Run backend login/register tests and frontend auth flow tests from specs/003-auth-flow/quickstart.md
- [x] T036 [P] Review Spanish strings and Toastify messaging across frontend and backend for consistency
- [x] T037 Ensure backend and frontend compile cleanly together in backend/app and frontend/
- [x] T038 Final cleanup of dead code, duplicated validation, and route inconsistencies across frontend/src/ and backend/app/src/main/java/com/prontaentrega/

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: no dependencies
- **Foundational (Phase 2)**: depends on Setup completion and blocks all user stories
- **User Story 1 (Phase 3)**: depends on foundational completion
- **User Story 2 (Phase 4)**: depends on foundational completion; can be implemented in parallel with US1
- **User Story 3 (Phase 5)**: depends on foundational completion; can be implemented in parallel with US1 and US2
- **Polish (Phase 6)**: depends on all user stories being complete

### Parallel Opportunities

- T002 and T003 can run in parallel during setup
- T006 and T007 can run in parallel during foundational work
- US1, US2, and US3 can be implemented in parallel once foundational setup is done
- Theme and auth context scaffolding can be developed independently from route validation work

## Implementation Strategy

### MVP first

1. Complete Phase 1 and Phase 2
2. Deliver User Story 1 (login)
3. Validate independently with a valid credential flow
4. Add User Story 2 (register)
5. Add User Story 3 (home/logout/theme)

### Incremental delivery

- Each user story should be independently testable and deployable
- Do not add cross-story dependencies that block independent validation
- Keep auth logic centralized in backend service and frontend route guards

### Suggested MVP scope

The recommended MVP is User Story 1 only if time is limited: login, token storage, protected route guard, and accessibility to home from valid credentials.
