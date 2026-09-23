import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { getInitialTheme, resolveTheme } from './themeUtils';

const ThemeContext = createContext(null);

export function ThemeProvider({ children }) {
  const [isDark, setIsDark] = useState(() => getInitialTheme() === 'dark');

  useEffect(() => {
    const themeName = resolveTheme(isDark ? 'dark' : 'light');
    document.body.setAttribute('data-theme', themeName);
    window.localStorage.setItem('theme-preference', themeName);
  }, [isDark]);

  const toggleTheme = () => setIsDark((current) => !current);

  const value = useMemo(
    () => ({
      isDark,
      toggleTheme,
      themeLabel: isDark ? 'oscuro' : 'claro',
    }),
    [isDark],
  );

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}

export function useTheme() {
  const context = useContext(ThemeContext);

  if (!context) {
    throw new Error('useTheme debe usarse dentro de ThemeProvider');
  }

  return context;
}
