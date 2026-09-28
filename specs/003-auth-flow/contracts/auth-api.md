# Contrato de API de autenticación

## Endpoints de autenticación

### POST /api/auth/register

Crea una nueva cuenta de usuario y devuelve un token JWT.

Cuerpo de la solicitud:
```json
{
  "nombre": "Ana",
  "correo": "ana@ejemplo.com",
  "password": "clave123"
}
```

Respuesta exitosa: `201 Created`
```json
{
  "usuario": {
    "id": "uuid",
    "nombre": "Ana",
    "correo": "ana@ejemplo.com"
  },
  "token": "jwt-token"
}
```

Errores:
- nombre vacío o inválido
- formato de correo inválido
- contraseña menor a 8 caracteres o sin letra/número
- cuenta duplicada

### POST /api/auth/login

Autentica a un usuario existente y devuelve un token JWT.

Cuerpo de la solicitud:
```json
{
  "correo": "ana@ejemplo.com",
  "password": "clave123"
}
```

Respuesta exitosa: `200 OK`
```json
{
  "usuario": {
    "id": "uuid",
    "nombre": "Ana",
    "correo": "ana@ejemplo.com"
  },
  "token": "jwt-token"
}
```

Errores:
- formato de correo inválido
- contraseña faltante
- credenciales incorrectas

## Reglas de redirección del lado del cliente

- los usuarios no autenticados pueden acceder a login y registro
- los usuarios autenticados son redirigidos al home cuando intentan acceder a login/register
- los usuarios no autenticados son redirigidos a login cuando intentan acceder a rutas protegidas del home
- el logout limpia el estado local de sesión y devuelve al usuario a login
