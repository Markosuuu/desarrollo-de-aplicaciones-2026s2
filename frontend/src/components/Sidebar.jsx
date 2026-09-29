import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { useTheme } from "../context/ThemeContext";

import sol from "../assets/icono-theme-sol.png";
import luna from "../assets/icono-theme-luna.png";

export default function Sidebar({ children }) {
  const [collapsed, setCollapsed] = useState(false);
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { isDark, toggleTheme } = useTheme();

  const handleLogout = () => {
    logout();
    navigate("/login");
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

        <div className="sidebar__user" title={user?.nombre || "Usuario"}>
          <span className="sidebar__avatar" aria-hidden="true">
            {(user?.nombre || "U").charAt(0).toUpperCase()}
          </span>
          <span className="sidebar__username">{user?.nombre || "Usuario"}</span>
        </div>

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
              Tema {isDark ? "oscuro" : "claro"}
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
