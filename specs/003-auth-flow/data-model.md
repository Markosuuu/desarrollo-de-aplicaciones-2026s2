# Modelo de datos: flujo de autenticación y sesión

## Entidad: Usuario

Representa a una persona con una cuenta en la aplicación.

| Campo | Tipo | Notas |
|-------|------|-------|
| id | UUID | Identificador principal |
| nombre | string | Obligatorio, se recorta antes de persistir |
| correo | string | Obligatorio, único y normalizado en minúsculas |
| passwordHash | string | Se almacena como hash BCrypt |
| fechaCreacion | datetime | Fecha de registro |

Reglas de validación:
- nombre no puede quedar vacío.
- correo debe tener un formato válido de email.
- la contraseña debe tener al menos 8 caracteres y contener al menos una letra y un número.
- se rechazan correos duplicados.

## Entidad: Sesion / TokenEstado

Representa el estado activo del token de un usuario luego de autenticarse correctamente.

| Campo | Tipo | Notas |
|-------|------|-------|
| usuarioId | UUID | Vincula el estado del token con un usuario |
| jtiVigente | UUID | Identificador actual del JWT |
| versionToken | integer | Contador de versión del token |
| emitidaEn | datetime | Fecha de emisión del token |
| expiraEn | datetime | Fecha de expiración |

Reglas de validación:
- el estado del token se crea solo después de un login o registro exitoso.
- el estado del token se actualiza cuando el usuario vuelve a autenticarse.
- los estados inactivos o expirados deben rechazarse antes del acceso a rutas protegidas.

## Preferencia visual (no persistida)

La selección de tema claro/oscuro es un estado de interfaz, no una entidad del dominio ni un dato a persistir en la base de datos.

Reglas de validación:
- alternar entre modos no debe afectar el estado de autenticación.
- el comportamiento de home/sesión continúa igual sin importar el modo visual.
- la preferencia puede gestionarse desde un `ThemeContext` y/o clases de Tailwind.

## Relaciones

- Un `Usuario` puede tener un único registro activo de `TokenEstado`.
- `TokenEstado` referencia a `Usuario` por `usuarioId`.
- La preferencia visual no se modela como entidad de negocio ni se persiste en la base de datos del MVP.
