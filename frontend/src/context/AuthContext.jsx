import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { loginRequest, registerRequest } from '../api/authApi';

const AuthContext = createContext(null);

function getStoredState() {
  if (typeof window === 'undefined') {
    return { token: '', user: null };
  }

  const token = localStorage.getItem('auth-token') || '';
  const rawUser = localStorage.getItem('auth-user');

  return {
    token,
    user: rawUser ? JSON.parse(rawUser) : null,
  };
}

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => getStoredState().token);
  const [user, setUser] = useState(() => getStoredState().user);

  useEffect(() => {
    if (token) {
      localStorage.setItem('auth-token', token);
    } else {
      localStorage.removeItem('auth-token');
    }
  }, [token]);

  useEffect(() => {
    if (user) {
      localStorage.setItem('auth-user', JSON.stringify(user));
    } else {
      localStorage.removeItem('auth-user');
    }
  }, [user]);

  const login = async (credentials) => {
    const data = await loginRequest(credentials);
    setToken(data.token);
    setUser(data.usuario || null);
    return data;
  };

  const register = async (credentials) => {
    const data = await registerRequest(credentials);
    setToken(data.token);
    setUser(data.usuario || null);
    return data;
  };

  const logout = () => {
    setToken('');
    setUser(null);
  };

  const value = useMemo(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token),
      login,
      register,
      logout,
    }),
    [token, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuth debe usarse dentro de AuthProvider');
  }

  return context;
}
