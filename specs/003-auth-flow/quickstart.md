# Guía rápida: validación del flujo de autenticación

## Requisitos previos

- Java 21
- Gradle o el wrapper `gradlew` provisto
- PostgreSQL ejecutándose localmente, o usar el perfil de pruebas de la app con H2 para validación

## Ejecutar pruebas del backend

```bash
cd backend
./gradlew test
```

## Ejecutar la aplicación localmente

```bash
cd backend
./gradlew bootRun
```

## Validar el registro

1. Hacer un POST a `/api/auth/register` con JSON:
   ```json
   {
     "nombre": "Ana",
     "correo": "ana@ejemplo.com",
     "password": "clave123"
   }
   ```
2. Confirmar una respuesta `201 Created` y un token JWT.
3. Confirmar que la respuesta incluye la información básica del usuario.

## Validar el login

1. Hacer un POST a `/api/auth/login` con:
   ```json
   {
     "correo": "ana@ejemplo.com",
     "password": "clave123"
   }
   ```
2. Confirmar una respuesta `200 OK` y un token válido.
3. Confirmar que credenciales inválidas devuelven un error.

## Validar acceso protegido y redirecciones

1. Abrir la página de login sin sesión activa.
2. Confirmar que las páginas de login y registro están disponibles.
3. Iniciar sesión correctamente.
4. Confirmar que la aplicación redirige automáticamente a la página de home.
5. Confirmar que al volver a login/register mientras está autenticado, se redirige a home.
6. Usar la acción de logout y confirmar que la aplicación vuelve a login.

## Validar el cambio de tema

1. Abrir la página de home.
2. Cambiar entre modo claro y oscuro.
3. Confirmar que el esquema de colores del interfaz cambia sin romper la autenticación ni la navegación.
