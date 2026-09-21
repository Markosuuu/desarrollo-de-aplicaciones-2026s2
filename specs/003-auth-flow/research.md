# Investigación: flujo de autenticación y sesión

## Decisión

Usar el flujo existente de autenticación con JWT en Spring Boot, junto con usuarios persistidos y hash de contraseñas con BCrypt. Esto coincide con la implementación actual del backend ya presente en `backend/app`.

## Fundamentos

- El repositorio ya incluye `AuthController`, `AuthService`, `JwtService` y `SecurityConfig`, por lo que la funcionalidad puede extender un patrón seguro ya existente en lugar de crear una autenticación paralela.
- La especificación de la funcionalidad requiere registro, inicio de sesión, protección de sesión, cierre de sesión y reglas de redirección, lo que encaja bien con un modelo JWT sin estado.
- Persistir usuarios aporta un ciclo de vida real de cuentas y permite aplicar reglas de validación ya presentes en el código, como la unicidad del correo y la fortaleza de la contraseña.

## Alternativas consideradas

- Cuentas en memoria: descartada porque no cumple con el requisito de que el registro e inicio de sesión usen credenciales reales y persistidas.
- Cookies de sesión en servidor: se evaluó, pero no se eligió porque el código ya usa JWT y autenticación sin estado, lo que reduce almacenamiento de sesión en servidor y encaja con la configuración actual de seguridad.
- Sin control de autenticación en páginas públicas: descartada porque no cumpliría el requisito de redirigir a usuarios autenticados fuera de login/register y proteger el acceso a home.

## Implicaciones de implementación

- Las peticiones de registro e inicio de sesión deben seguir pasando por `/api/auth/register` y `/api/auth/login`.
- Los mensajes de validación deben mantenerse en español y ser consistentes con el contrato del dominio.
- Los formularios deben usar Yup para validar strings, emails y otros inputs del cliente; los errores deben mostrarse como notificaciones Toastify y no como mensajes inline exclusivamente.
- Las redirecciones para usuarios autenticados deben aplicarse en el nivel del router o guard de rutas del frontend, mientras que la seguridad del backend protege los recursos protegidos.
- El cambio de tema es solo de interfaz y no debe alterar el estado de sesión ni las reglas de autenticación; debe gestionarse con un ThemeContext y/o clases de Tailwind, sin crear una entidad persistente en la base de datos.
