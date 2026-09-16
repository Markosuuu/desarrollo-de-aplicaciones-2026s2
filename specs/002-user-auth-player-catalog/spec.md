# Feature Specification: Autenticacion de usuarios y catalogo de jugadores

**Feature Branch**: `002-user-auth-player-catalog`

**Created**: 2026-09-12

**Status**: Draft

**Input**: User description: Primera entrega del mercado de jugadores: registro de usuarios con JWT, inicio de sesion con JWT y consulta del catalogo de jugadores de cinco ligas principales.

## Clarifications

### Session 2026-09-12

- Q: ¿El catálogo de jugadores debe poder consultarse sin autenticación o debe requerir un JWT? → A: El catálogo es público y puede consultarse sin JWT.
- Q: ¿Qué datos deben ser obligatorios para registrarse? → A: Correo, contraseña y nombre.
- Q: ¿Al iniciar sesión, un nuevo JWT debe invalidar el JWT anterior? → A: Sí, cada usuario conserva un solo JWT vigente y el anterior se invalida.
- Q: ¿Qué política mínima debe cumplir la contraseña al registrarse? → A: Mínimo 8 caracteres, incluyendo al menos una letra y un número.
- Q: ¿Con qué frecuencia debe actualizarse la información del catálogo de jugadores? → A: Actualización semanal.
- Q: ¿Qué mecanismo debe utilizarse para la credencial de autenticación? → A: JWT firmado; cada usuario conserva un solo JWT vigente y el anterior se invalida.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Registrar una cuenta de usuario (Priority: P1)

Como visitante, quiero registrarme con mis credenciales para obtener un JWT y poder utilizar las funciones protegidas del mercado.

**Why this priority**: El registro habilita la identidad necesaria para cualquier operacion futura y es el punto de entrada de nuevos usuarios.

**Independent Test**: Se puede probar creando una cuenta con datos validos, verificando que recibe un JWT y usando ese JWT para acceder a una accion protegida de prueba.

**Acceptance Scenarios**:

1. **Given** que no existe una cuenta con el correo informado, **When** el visitante envia datos validos de registro, **Then** se crea una cuenta y se entrega un JWT asociado a ella.
2. **Given** que ya existe una cuenta con el correo informado, **When** el visitante intenta registrarse nuevamente, **Then** el sistema rechaza la solicitud, informa que la cuenta ya existe y no crea una segunda cuenta.
3. **Given** que faltan datos obligatorios o alguno no cumple las reglas de validacion, **When** el visitante envia el registro, **Then** el sistema rechaza la solicitud e indica los datos que deben corregirse.

### User Story 2 - Iniciar sesion para operar (Priority: P1)

Como usuario registrado, quiero iniciar sesion para obtener un JWT valido y autenticar mis consultas y operaciones permitidas.

**Why this priority**: El inicio de sesion permite recuperar el acceso de forma controlada y es necesario para utilizar el sistema despues del registro.

**Independent Test**: Se puede probar con una cuenta existente, credenciales correctas e incorrectas, verificando la entrega o rechazo del JWT correspondiente.

**Acceptance Scenarios**:

1. **Given** que existe una cuenta activa, **When** el usuario envia credenciales correctas, **Then** el sistema entrega un JWT valido para esa cuenta.
2. **Given** que existe una cuenta activa, **When** el usuario envia una contraseña incorrecta, **Then** el sistema rechaza el inicio de sesion sin entregar un JWT.
3. **Given** que el JWT recibido es presentado en una consulta protegida, **When** el sistema valida la credencial, **Then** permite la consulta asociada al usuario autenticado.
4. **Given** que el usuario inicia sesion nuevamente, **When** recibe un nuevo JWT, **Then** el JWT anterior deja de permitir el acceso a consultas protegidas.

### User Story 3 - Consultar el catalogo de jugadores (Priority: P1)

Como usuario, quiero consultar el catalogo de jugadores para conocer los jugadores disponibles, su liga, equipo y cotizacion vigente.

**Why this priority**: El catalogo es la primera funcionalidad de valor del mercado y permite descubrir los activos disponibles antes de futuras operaciones.

**Independent Test**: Se puede probar consultando el catalogo y aplicando filtros por liga, equipo o nombre, verificando que los resultados cumplen los criterios solicitados.

**Acceptance Scenarios**:

1. **Given** que existen jugadores registrados en el catalogo, **When** el usuario realiza una consulta valida, **Then** recibe jugadores con nombre, equipo, liga y cotizacion vigente.
2. **Given** que una persona no posee un JWT, **When** consulta el catalogo, **Then** puede recibir el listado y sus filtros sin autenticarse.
3. **Given** que el usuario selecciona una de las cinco ligas habilitadas, **When** aplica el filtro de liga, **Then** solo recibe jugadores pertenecientes a esa liga.
4. **Given** que el usuario informa un nombre o equipo, **When** aplica el filtro correspondiente, **Then** recibe solo coincidencias relevantes o una respuesta vacia si no existen resultados.
5. **Given** que el catalogo contiene mas resultados que el limite de una consulta, **When** el usuario solicita una pagina, **Then** recibe un subconjunto acotado y puede consultar las paginas disponibles sin duplicaciones.
6. **Given** que el catalogo no puede entregar datos actualizados, **When** el usuario realiza una consulta, **Then** recibe un mensaje comprensible y no se muestran cotizaciones presentadas como vigentes sin respaldo.

### Edge Cases

- Un registro con correo vacio, formato invalido, contraseña vacia, contraseña de menos de 8 caracteres o sin una letra y un número, nombre vacio o datos con espacios innecesarios debe rechazarse sin crear una cuenta incompleta.
- Dos registros simultaneos con el mismo correo deben dejar una sola cuenta valida y comunicar el conflicto al segundo intento.
- Las credenciales de inicio de sesion no deben revelar si el correo existe cuando la autenticacion falla.
- Un JWT ausente, invalido, expirado, revocado o reemplazado debe impedir el acceso a acciones protegidas.
- Al emitir un nuevo JWT durante el inicio de sesion, el JWT anterior debe invalidarse inmediatamente.
- La ausencia de JWT no debe impedir la consulta publica del catalogo.
- La información del catalogo debe actualizarse semanalmente; si una actualización falla, debe conservarse la última información válida e indicarse su fecha, sin presentarla como actualizada.
- Un filtro con texto vacio debe comportarse como ausencia de filtro; un filtro sin coincidencias debe devolver una lista vacia sin error.
- Una pagina fuera del rango disponible debe devolver una respuesta valida y claramente indicar que no hay resultados.
- El catalogo debe distinguir jugadores con el mismo nombre mediante su equipo y liga.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST permitir que una persona cree una cuenta proporcionando correo, contraseña y nombre.
- **FR-002**: El sistema MUST validar el formato del correo, exigir una contraseña de al menos 8 caracteres con una letra y un número, validar el nombre antes de crear la cuenta y MUST rechazar correos duplicados.
- **FR-003**: El sistema MUST generar y entregar un JWT firmado asociado de forma unica a cada cuenta creada correctamente y MUST mantener un solo JWT vigente por cuenta.
- **FR-004**: El sistema MUST permitir que una cuenta existente inicie sesion con sus credenciales validas y MUST invalidar el JWT vigente anterior al emitir uno nuevo.
- **FR-005**: El sistema MUST rechazar credenciales invalidas sin entregar un JWT y sin revelar informacion que permita confirmar la existencia de la cuenta.
- **FR-006**: El sistema MUST aceptar un JWT valido como credencial para las consultas protegidas incluidas en esta feature, sin exigirlo para el catalogo publico.
- **FR-007**: El sistema MUST permitir consultar sin autenticacion un catalogo publico de jugadores de Premier League, Bundesliga, La Liga, Serie A y Ligue 1.
- **FR-008**: Cada jugador del catalogo MUST incluir nombre, equipo, liga y cotizacion vigente; la cotizacion inicial MUST ser de 1 credito cuando no exista una actualizacion posterior, y la información MUST actualizarse semanalmente.
- **FR-009**: El sistema MUST permitir filtrar el catalogo por liga, equipo y nombre, combinando filtros cuando el usuario los informe.
- **FR-010**: El sistema MUST entregar el catalogo en paginas acotadas e informar la cantidad o disponibilidad necesaria para recorrer los resultados.
- **FR-011**: El sistema MUST comunicar errores de validacion, autenticacion y consulta en español, sin exponer credenciales ni datos sensibles.
- **FR-012**: Esta primera entrega MUST excluir la compra y venta de tokens, el portfolio, el historial de operaciones, el ranking de jugadores y la gestion de cotizaciones; esas capacidades quedan fuera de esta feature.

### Key Entities *(include if feature involves data)*

- **Usuario**: Persona registrada que puede autenticarse y consultar funcionalidades protegidas; posee nombre, correo y credenciales protegidas.
- **JWT**: Credencial técnica firmada, gestionada por el módulo de autenticación, que identifica a un usuario en consultas protegidas; cada usuario posee un solo JWT vigente y una nueva emisión invalida el anterior.
- **Jugador**: Jugador de futbol incluido en el catalogo; posee nombre, equipo, liga y cotizacion vigente.
- **Liga**: Competencia de origen del jugador. En esta entrega se limita a las cinco ligas definidas en el alcance.
- **Consulta de catalogo**: Solicitud que combina filtros y paginacion para obtener jugadores disponibles.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Al menos el 95% de los usuarios de prueba completa el registro y obtiene un JWT valido en menos de 2 minutos.
- **SC-002**: Al menos el 95% de los intentos con credenciales validas obtiene un JWT utilizable, y el 100% de los intentos con credenciales invalidas es rechazado sin entregar credenciales.
- **SC-003**: Al menos el 95% de las consultas validas del catalogo muestra resultados en menos de 2 segundos para un catalogo de hasta 10.000 jugadores.
- **SC-004**: El 100% de los resultados del catalogo pertenece a una de las cinco ligas habilitadas y contiene nombre, equipo, liga y cotizacion vigente.
- **SC-005**: Al menos el 95% de los usuarios de prueba encuentra un jugador aplicando un filtro de liga, equipo o nombre en el primer intento.
- **SC-006**: El 100% de las pruebas de JWT ausente, invalido, expirado, revocado o reemplazado bloquea la consulta protegida sin exponer informacion sensible.

## Assumptions

- El usuario se registra e inicia sesion mediante correo y contraseña; el nombre es obligatorio al registrarse, la contraseña debe tener al menos 8 caracteres con una letra y un número, y no se incluyen proveedores externos de identidad en esta entrega.
- El JWT se entrega al registrarse y al iniciar sesion, permanece utilizable hasta expirar, ser revocado o reemplazado, y cada usuario puede tener un solo JWT vigente.
- Los nombres, equipos, ligas y cotizaciones provienen de fuentes de datos externas autorizadas por el proyecto; la calidad y disponibilidad de esas fuentes son dependencias de la consulta.
- La cotizacion inicial de cada jugador es 1 credito y la estrategia de actualizacion de cotizaciones se definira en una feature posterior.
- La información del catalogo se actualiza semanalmente; si la fuente externa no está disponible, se conserva la última versión válida y se informa su fecha de actualización.
- La paginacion usa un tamaño predeterminado razonable y permite solicitar paginas sucesivas; el detalle del tamaño se definira durante la planificacion.
- El soporte visual de portfolio y la operacion de compra o venta no forman parte de esta primera entrega, aunque el catalogo sera consumido por el frontend del proyecto.
- Los mensajes visibles para usuarios se presentan en español.
