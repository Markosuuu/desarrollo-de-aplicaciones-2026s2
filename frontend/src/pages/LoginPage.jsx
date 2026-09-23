import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';
import { loginSchema } from '../validation/loginSchema';

const initialValues = {
  correo: '',
  password: '',
};

export default function LoginPage() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [values, setValues] = useState(initialValues);
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setValues((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);

    try {
      await loginSchema.validate(values, { abortEarly: false });
      await login(values);
      toast.success('Inicio de sesión correcto.');
      navigate('/');
    } catch (error) {
      const message = error?.inner?.[0]?.message || error?.message || 'No se pudo iniciar sesión.';
      toast.error(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <h1>Iniciar sesión</h1>
        <p className="auth-subtitle">Ingresá tus credenciales para continuar.</p>

        <form onSubmit={handleSubmit} className="auth-form">
          <label>
            <span>Correo electrónico</span>
            <input
              type="email"
              name="correo"
              value={values.correo}
              onChange={handleChange}
              placeholder="ana@ejemplo.com"
            />
          </label>

          <label>
            <span>Contraseña</span>
            <input
              type="password"
              name="password"
              value={values.password}
              onChange={handleChange}
              placeholder="********"
            />
          </label>

          <button type="submit" disabled={submitting}>
            {submitting ? 'Ingresando...' : 'Ingresar'}
          </button>
        </form>

        <p className="auth-switch">
          ¿No tenés cuenta? <a href="/register">Registrate acá</a>
        </p>
      </section>
    </main>
  );
}
