import assert from 'node:assert/strict';
import test from 'node:test';
import { loginSchema } from '../validation/loginSchema.js';

test('loginSchema acepta correo y contraseña válidos', async () => {
  const input = { correo: 'ana@ejemplo.com', password: 'clave123' };

  const result = await loginSchema.validate(input);

  assert.deepEqual(result, input);
});

test('loginSchema rechaza un correo malformado', async () => {
  await assert.rejects(
    () => loginSchema.validate({ correo: 'correo-malo', password: 'clave123' }),
    /formato inválido/i,
  );
});

test('loginSchema exige contraseña', async () => {
  await assert.rejects(
    () => loginSchema.validate({ correo: 'ana@ejemplo.com', password: '' }),
    /contraseña/i,
  );
});
