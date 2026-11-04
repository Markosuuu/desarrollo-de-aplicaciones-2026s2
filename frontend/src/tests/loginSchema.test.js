import { expect, test } from 'vitest';
import { loginSchema } from '../validation/loginSchema.js';

test('loginSchema acepta correo y contraseña válidos', async () => {
  const input = { correo: 'ana@ejemplo.com', password: 'clave123' };

  const result = await loginSchema.validate(input);

  expect(result).toEqual(input);
});

test('loginSchema rechaza un correo malformado', async () => {
  await expect(
    loginSchema.validate({ correo: 'correo-malo', password: 'clave123' }),
  ).rejects.toThrow(/formato inválido/i);
});

test('loginSchema exige contraseña', async () => {
  await expect(
    loginSchema.validate({ correo: 'ana@ejemplo.com', password: '' }),
  ).rejects.toThrow(/contraseña/i);
});