import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { useAuth } from '../context/AuthContext';
import { registerSchema } from '../validation/registerSchema';

const initialValues = {
  nombre: '',
  correo: '',
  password: '',
};

export default function RegisterPage() {
  const navigate = useNavigate();
  const { register } = useAuth();
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
      await registerSchema.validate(values, { abortEarly: false });
      await register(values);
      toast.success('Cuenta creada con éxito.');
      navigate('/');
    } catch (error) {
      const message = error?.inner?.[0]?.message || error?.message || 'No se pudo completar el registro.';
      toast.error(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <h1>Registrarse</h1>
        <p className="auth-subtitle">Creá tu cuenta para acceder al sitio.</p>

        <form onSubmit={handleSubmit} className="auth-form">
          <label>
            <span>Nombre</span>
            <input
              type="text"
              name="nombre"
              value={values.nombre}
              onChange={handleChange}
              placeholder="Ana"
            />
          </label>

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
            {submitting ? 'Registrando...' : 'Crear cuenta'}
          </button>
        </form>

        <p className="auth-switch">
          ¿Ya tenés una cuenta? <a href="/login">Iniciá sesión</a>
        </p>
      </section>
    </main>
  );
}
