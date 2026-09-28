export function resolveTheme(preference) {
  if (typeof preference === 'string' && preference.toLowerCase() === 'dark') {
    return 'dark';
  }

  return 'light';
}

export function getInitialTheme() {
  if (typeof window === 'undefined') {
    return 'light';
  }

  const storedTheme = window.localStorage.getItem('theme-preference');
  return resolveTheme(storedTheme);
}
