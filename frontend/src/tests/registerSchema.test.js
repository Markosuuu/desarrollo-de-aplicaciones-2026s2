import assert from 'node:assert/strict';
import test from 'node:test';
import { registerSchema } from '../validation/registerSchema.js';

test('registerSchema acepta nombre, correo y contraseña válidos', async () => {
  await registerSchema.validate({ nombre: 'Ana', correo: 'ana@ejemplo.com', password: 'clave123' });
});

test('registerSchema rechaza correo inválido', async () => {
  await assert.rejects(
    () => registerSchema.validate({ nombre: 'Ana', correo: 'ana', password: 'clave123' }),
    /formato inválido/i,
  );
});

test('registerSchema exige contraseña segura', async () => {
  await assert.rejects(
    () => registerSchema.validate({ nombre: 'Ana', correo: 'ana@ejemplo.com', password: 'short' }),
    /contraseña/i,
  );
});
