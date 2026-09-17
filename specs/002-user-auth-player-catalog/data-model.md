# Data Model — Autenticacion de usuarios y catalogo

## Usuario

Representa a una persona registrada que puede autenticarse.

| Campo | Tipo conceptual | Reglas |
|---|---|---|
| id | UUID | Identificador unico generado por el sistema |
| nombre | texto | Obligatorio; se recorta y no puede quedar vacio |
| correo | texto | Obligatorio, formato valido, unico sin distinguir mayusculas/minusculas |
| password_hash | texto | Obligatorio; nunca se persiste la contraseña en claro |
| creado_en | fecha-hora | Obligatoria |
| actualizado_en | fecha-hora | Obligatoria |

### Reglas de dominio

- Un correo duplicado no puede crear un segundo usuario.
- Usuario no conoce ni administra credenciales de transporte o autenticación.

## JWT de autenticación

Credencial técnica de infraestructura asociada a un usuario. No es un objeto de dominio y no participa en reglas de negocio de Usuario o Jugador.

| Campo | Tipo conceptual | Reglas |
|---|---|---|
| usuario_id | UUID | Relación uno a uno con Usuario |
| jti_vigente | UUID | Identificador del JWT actualmente válido |
| version_token | entero | Alternativa para invalidar tokens anteriores |
| emitida_en | fecha-hora | Obligatoria |
| expira_en | fecha-hora | Obligatoria; el JWT también contiene `exp` |

La emisión, firma, validación e invalidación de JWT pertenecen al módulo `authentication` y a su servicio de credenciales. El JWT debe incluir como mínimo `sub`, `jti`, `iat` y `exp`; la persistencia conserva el `jti` o la versión vigente, no el token completo. Los modelos de negocio no deben depender de esta entidad. La respuesta de registro/login entrega el JWT al cliente sin exponer secretos de firma.

## Jugador

Representa un jugador visible en el catálogo público.

| Campo | Tipo conceptual | Reglas |
|---|---|---|
| id | UUID | Identificador unico |
| nombre | texto | Obligatorio |
| equipo | texto | Obligatorio |
| liga | enum/texto controlado | Solo Premier League, Bundesliga, La Liga, Serie A o Ligue 1 |
| cotizacion_actual | decimal | Inicialmente 1 credito; no negativa |
| fecha_actualizacion | fecha-hora | Indica la antigüedad del snapshot |
| fuente | texto | Identifica el origen de datos |
| tokens_restantes | int | Describe la cantiadad de tokens restantes |

La identidad funcional de un jugador debe distinguir nombres repetidos mediante la combinación de equipo y liga; el id persistente mantiene la identidad entre actualizaciones cuando la fuente lo permite.

## Snapshot de catalogo

Representa el estado válido más reciente de los datos importados.

| Campo | Tipo conceptual | Reglas |
|---|---|---|
| id | UUID | Identificador unico |
| iniciado_en | fecha-hora | Obligatoria |
| finalizado_en | fecha-hora nullable | Se completa si el proceso termina |
| estado | valido/fallido | Solo el último snapshot válido se expone como vigente |
| error | texto nullable | Mensaje técnico no expuesto directamente al usuario |

## Consulta de catalogo

No es una entidad persistente. Representa filtros `liga`, `equipo`, `nombre`, `page` y `per_page`, con respuesta paginada y total de resultados.

## Relaciones

- Módulo de autenticación asocia Usuario 1:1 con el estado JWT vigente; la relación no forma parte del modelo de dominio.
- Jugador pertenece a una Liga controlada.
- Snapshot de catalogo actualiza muchos Jugadores.
- No se modelan todavía compras, ventas, posiciones ni portfolio.
