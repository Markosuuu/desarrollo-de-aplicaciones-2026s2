import { expect, test } from 'vitest';
import { registerSchema } from '../validation/registerSchema.js';

test('registerSchema acepta nombre, correo y contraseña válidos', async () => {
  const input = { nombre: 'Ana', correo: 'ana@ejemplo.com', password: 'clave123' };

  const result = await registerSchema.validate(input);

  expect(result).toEqual(input);
});

test('registerSchema rechaza correo inválido', async () => {
  await expect(
    registerSchema.validate({
      nombre: 'Ana',
      correo: 'ana',
      password: 'clave123',
    }),
  ).rejects.toThrow(/formato inválido/i);
});

test('registerSchema exige contraseña segura', async () => {
  await expect(
    registerSchema.validate({
      nombre: 'Ana',
      correo: 'ana@ejemplo.com',
      password: 'short',
    }),
  ).rejects.toThrow(/contraseña/i);
});
