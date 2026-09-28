import assert from 'node:assert/strict';
import test from 'node:test';
import { resolveTheme } from '../context/themeUtils.js';

test('resolveTheme devuelve dark cuando se solicita dark', () => {
  assert.equal(resolveTheme('dark'), 'dark');
});

test('resolveTheme devuelve light por defecto', () => {
  assert.equal(resolveTheme('light'), 'light');
  assert.equal(resolveTheme(undefined), 'light');
});
