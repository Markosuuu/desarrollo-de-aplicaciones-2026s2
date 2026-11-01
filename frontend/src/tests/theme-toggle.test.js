import { expect, test } from 'vitest';
import { resolveTheme } from '../context/themeUtils.js';

test('resolveTheme devuelve dark cuando se solicita dark', () => {
  expect(resolveTheme('dark')).toBe('dark');
});

test('resolveTheme devuelve light por defecto', () => {
  expect(resolveTheme('light')).toBe('light');
  expect(resolveTheme(undefined)).toBe('light');
});