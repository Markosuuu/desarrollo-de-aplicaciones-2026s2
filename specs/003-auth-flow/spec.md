# Feature Specification: Authentication and session flow for the dynamic site

**Feature Branch**: `003-auth-flow`

**Created**: 2026-09-20

**Status**: Draft

**Input**: User description: "Necesito un sitio dinámico para mi backend. Páginas requeridas: Login, Registro, Home, Logout. El usuario debe poder iniciar sesión con correo y contraseña, con validaciones por campos faltantes o valores incorrectos. El registro debe pedir nombre, correo y contraseña, con validaciones similares. Tras iniciar sesión o registrarse, ambas acciones llevan a una vista home con un mensaje placeholder 'home'. Si ya hay sesión iniciada, el usuario no puede volver a login o registro y debe ser redirigido al home. El home debe tener un botón de logout que cierre la sesión y redirija a login. El diseño será simple, fondo blanco y texto negro, con posibilidad de cambiar al modo oscuro con fondo negro y texto blanco."

## Clarifications

### Session 2026-09-20

- Q: Should the login and registration flow use real persisted user accounts with server-side validation, or is a mock in-memory account system acceptable for this feature? → A: Real persisted user accounts and server-side validation

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Iniciar sesión con credenciales válidas (Priority: P1)

Un usuario registrado puede ingresar su correo y contraseña desde la página de login y acceder al home en un solo flujo claro. La validación inmediata evita errores comunes y da confianza antes de enviar la información.

**Why this priority**: Es la acción central del sitio y la base para que el resto del flujo sea útil. Sin inicio de sesión, no existe acceso a la experiencia protegida ni a la navegación principal.

**Independent Test**: Se puede probar completamente con un usuario que ingresa credenciales correctas y confirma que queda en home sin poder volver al login.

**Acceptance Scenarios**:

1. **Given** que el usuario se encuentra en la página de login, **When** ingresa un correo y contraseña válidos y envía el formulario, **Then** se inicia la sesión y se le redirige al home.
2. **Given** que el usuario se encuentra en la página de login, **When** deja campos vacíos o envia credenciales incorrectas, **Then** el sistema muestra mensajes de error y no permite continuar.

---

### User Story 2 - Registrarse y llegar al home (Priority: P1)

Una persona nueva puede crear una cuenta con su nombre, correo y contraseña y, al completar el registro, queda dentro del área protegida con acceso inmediato al home.

**Why this priority**: La creación de cuenta es el segundo paso crítico para la adopción del sistema y permite que nuevos usuarios puedan entrar al sitio sin intervención manual.

**Independent Test**: Se puede validar con un usuario nuevo que completa el formulario con datos válidos y confirma que el flujo termina en home.

**Acceptance Scenarios**:

1. **Given** que el usuario se encuentra en la página de registro, **When** completa nombre, correo y contraseña con datos válidos y envía el formulario, **Then** se crea la cuenta y se redirige al home.
2. **Given** que el usuario intenta registrar un formulario incompleto o con datos inválidos, **When** envía la información, **Then** se muestran errores específicos y la operación no se completa.

---

### User Story 3 - Mantener la sesión protegida y salir del sistema (Priority: P2)

Un usuario autenticado no puede volver a las páginas de acceso y debe poder cerrar la sesión desde el home para volver al login. El sitio también debe permitir alternar entre el esquema claro y oscuro sin perder la experiencia básica.

**Why this priority**: Esto protege el flujo principal, mantiene el acceso seguro y mejora la usabilidad bajo diferentes preferencias visuales.

**Independent Test**: Se puede comprobar abriendo el sitio con sesión activa y confirmando que login/registro redirigen al home, y luego usando el botón de logout para retornar al login.

**Acceptance Scenarios**:

1. **Given** que el usuario ya inició sesión, **When** intenta acceder a la ruta de login o registro, **Then** el sistema lo redirige automáticamente al home.
2. **Given** que el usuario está en el home, **When** hace clic en logout, **Then** se cierra la sesión y vuelve a la pantalla de login.
3. **Given** que el usuario está navegando en la aplicación, **When** cambia el tema a modo oscuro, **Then** el sitio muestra fondo negro y texto blanco sin romper la lectura ni la navegación.

---

### Edge Cases

- Si un usuario intenta iniciar sesión con un correo malformado o sin contraseña, el sistema muestra un mensaje de error indicando el problema y no permite continuar.
- Si un usuario ya autenticado intenta volver a las páginas de acceso, el sistema lo redirige automáticamente al home.
- Si un usuario cierra sesión, solo puede acceder a las páginas de login y registro; cualquier intento de volver a una vista protegida requiere autenticación.
- Si el usuario cambia entre el tema claro y oscuro, no ocurre ningún error ni interrupción en la navegación; simplemente se actualiza el tema del sitio.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST permitir que un usuario inicie sesión con correo y contraseña en la página de login usando credenciales persistidas del sistema.
- **FR-002**: El sistema MUST validar que los campos de login no estén vacíos y que las credenciales sean correctas antes de permitir el acceso.
- **FR-003**: El sistema MUST mostrar mensajes de error claros cuando faltan campos, el correo es inválido o la contraseña es incorrecta.
- **FR-004**: El sistema MUST permitir el registro con nombre, correo y contraseña en la sección de registro y guardar la cuenta como usuario persistente.
- **FR-005**: El sistema MUST validar los campos de registro y mostrar errores si faltan datos, el formato es incorrecto o la información no es válida.
- **FR-006**: El sistema MUST redirigir automáticamente a un usuario autenticado desde la página de login o registro al home.
- **FR-007**: El sistema MUST llevar al usuario a una vista de home tras un login o registro exitosos.
- **FR-008**: El home MUST incluir un botón de logout que cierre la sesión del usuario y lo redirija a la pantalla de login.
- **FR-009**: El sistema MUST mantener una sesión activa para el usuario que ha ingresado correctamente y proteger el acceso a páginas públicas cuando ya está autenticado.
- **FR-010**: El sitio MUST ofrecer una apariencia básica en blanco y negro con texto oscuro sobre fondo claro, y una opción de cambio a tema oscuro con fondo negro y texto blanco.
- **FR-011**: El sistema MUST mantener la navegación y el contenido visibles en ambos temas sin que la funcionalidad de autenticación se vea afectada.

### Key Entities *(include if feature involves data)*

- **Usuario**: representa a la persona que accede al sitio; incluye nombre, correo y contraseña, además de su estado de autenticación.
- **Sesión**: representa el acceso activo del usuario dentro de la aplicación y determina si puede ver login, registro o home.
- **Tema**: representa la preferencia visual del usuario entre modo claro y modo oscuro.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Un usuario puede completar la secuencia principal de login o registro y llegar al home en menos de 2 minutos en la mayoría de los casos.
- **SC-002**: El 100% de los usuarios autenticados se redirige automáticamente a home si intenta volver a login o registro.
- **SC-003**: Al menos el 90% de los intentos con datos inválidos muestran errores claros y evitan avanzar con información incompleta.
- **SC-004**: Un usuario puede cerrar sesión desde el home y volver al login sin perder la capacidad de iniciar sesión nuevamente.
- **SC-005**: La experiencia visual cumple con la preferencia de color claro/oscuro y mantiene la legibilidad y la navegación sin fricciones.

## Assumptions

- El sistema usa una base de usuarios persistente para la autenticación básica con correo y contraseña, con validación real de credenciales.
- La sesión del usuario se considera válida durante la navegación activa del sitio y no incluye recuperación de contraseña en esta entrega.
- El modo oscuro es una preferencia visual del usuario dentro del sitio, no una configuración de cuenta con persistencia compleja.
- La funcionalidad de backend y frontend se integran como un flujo de sitio dinámico con validaciones del lado de la interfaz y del lado del servicio.
