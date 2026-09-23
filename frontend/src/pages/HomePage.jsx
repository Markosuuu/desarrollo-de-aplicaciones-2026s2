import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTheme } from '../context/ThemeContext';

export default function HomePage() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { isDark, toggleTheme } = useTheme();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <main className="home-shell">
      <header className="topbar">
        <div>
          <p className="eyebrow">ProntaEntrega</p>
          <h1>Home</h1>
        </div>

        <div className="topbar-actions">
          <button type="button" className="theme-toggle" onClick={toggleTheme}>
            {isDark ? 'Tema claro' : 'Tema oscuro'}
          </button>
          <button type="button" className="logout-button" onClick={handleLogout}>
            Cerrar sesión
          </button>
        </div>
      </header>

      <section className="home-card">
        <p className="welcome">Bienvenido/a{user?.nombre ? `, ${user.nombre}` : ''}</p>
        <p className="placeholder-message">home</p>
      </section>
    </main>
  );
}
