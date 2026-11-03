import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useTheme } from "../context/ThemeContext";
import { Blobatar } from "@blobatar/react";
import "blobatar/motion.css";

import sol from "../assets/icono-theme-sol.png";
import luna from "../assets/icono-theme-luna.png";
import casa from "../assets/casa.png";
import pelota from "../assets/pelota-futbol.png";

export default function Sidebar({ children }) {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { isDark, toggleTheme } = useTheme();

  const handleLogout = () => {
    logout();
    void navigate("/login");
  };

  return (
    <div className={`app-layout${collapsed ? " app-layout--collapsed" : ""}`}>
      <aside className="sidebar" aria-label="Menú de usuario">
        <button
          type="button"
          className="sidebar__collapse"
          onClick={() => setCollapsed((current) => !current)}
          aria-label={collapsed ? "Expandir menú" : "Plegar menú"}
          aria-expanded={!collapsed}
        >
          <span aria-hidden="true">{collapsed ? "»" : "«"}</span>
        </button>

        {/* Enlace al inicio */}
        <Link to="/" className="sidebar__logo" title="Inicio">
          <img src={casa} alt="Home" width={50} height={50} />
          <span className="sidebar__username">Inicio</span>
        </Link>

        {/* Enlace al perfil de usuario */}
        <Link
          to="/profile"
          className="sidebar__user"
          title={user?.nombre || "Usuario"}
        >
          <span className="sidebar__avatar" aria-hidden="true">
            <Blobatar
              name={user?.nombre || "Usuario"}
              animate="hover"
              width={50}
            />
          </span>
          <span className="sidebar__username">{user?.nombre || "Usuario"}</span>
        </Link>

        {/* Enlace al perfil de usuario */}
        <Link to="/mercado" className="sidebar__user" title="Mercado">
          <span className="sidebar__avatar" aria-hidden="true">
            <img src={pelota} alt="mercado" width={50} />
          </span>
          <span className="sidebar__username">Mercado</span>
        </Link>

        <div className="sidebar__actions">
          <button
            type="button"
            className="sidebar__action sidebar__theme"
            onClick={toggleTheme}
            aria-label={`Cambiar a tema ${isDark ? "claro" : "oscuro"}`}
            title={`Tema ${isDark ? "oscuro" : "claro"}`}
          >
            <img src={isDark ? sol : luna} alt="" width={25} />
            <span className="sidebar__label">
              Tema {isDark ? "claro" : "oscuro"}
            </span>
          </button>
          <button
            type="button"
            className="sidebar__action sidebar__logout"
            onClick={handleLogout}
            title="Cerrar sesión"
          >
            <span className="sidebar__logout-icon" aria-hidden="true">
              ↪
            </span>
            <span className="sidebar__label">Cerrar sesión</span>
          </button>
        </div>
      </aside>
      {children}
    </div>
  );
}
