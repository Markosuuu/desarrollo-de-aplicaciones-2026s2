import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import Sidebar from "../components/Sidebar";

const mocks = vi.hoisted(() => ({
  useAuth: vi.fn(),
  useTheme: vi.fn(),
}));

vi.mock("../context/AuthContext", () => ({
  useAuth: mocks.useAuth,
}));

vi.mock("../context/ThemeContext", () => ({
  useTheme: mocks.useTheme,
}));

vi.mock("@blobatar/react", () => ({
  Blobatar: ({ name }) => <span>{name}</span>,
}));

function renderSidebar() {
  return render(
    <MemoryRouter initialEntries={["/home"]}>
      <Routes>
        <Route
          path="/home"
          element={
            <Sidebar>
              <p>Contenido principal</p>
            </Sidebar>
          }
        />
        <Route path="/login" element={<p>Página de inicio de sesión</p>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe("Sidebar", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    mocks.useAuth.mockReturnValue({
      user: { nombre: "Ana" },
      logout: vi.fn(),
    });
    mocks.useTheme.mockReturnValue({
      isDark: false,
      toggleTheme: vi.fn(),
    });
  });

  afterEach(() => {
    cleanup();
  });

  test("renderiza el contenido, enlaces y datos del usuario", () => {
    renderSidebar();

    expect(screen.getByText("Contenido principal")).toBeInTheDocument();
    expect(screen.getAllByText("Ana")).toHaveLength(2);
    expect(screen.getByRole("link", { name: "HomeInicio" })).toHaveAttribute(
      "href",
      "/",
    );
    expect(screen.getByRole("link", { name: "Mercado" })).toHaveAttribute(
      "href",
      "/mercado",
    );
    expect(screen.getByRole("link", { name: "Ranking" })).toHaveAttribute(
      "href",
      "/ranking",
    );
    expect(screen.getByRole("link", { name: "Subasta" })).toHaveAttribute(
      "href",
      "/subasta",
    );
  });

  test("usa el nombre predeterminado cuando no hay un usuario", () => {
    mocks.useAuth.mockReturnValue({ user: null, logout: vi.fn() });

    renderSidebar();

    expect(screen.getByRole("link", { name: "Usuario" })).toBeInTheDocument();
  });

  test("pliega y expande el menú", () => {
    const { container } = renderSidebar();

    const collapseButton = screen.getByRole("button", { name: "Plegar menú" });
    fireEvent.click(collapseButton);

    expect(container.querySelector(".app-layout")).toHaveClass(
      "app-layout--collapsed",
    );
    expect(
      screen.getByRole("button", { name: "Expandir menú" }),
    ).toHaveAttribute("aria-expanded", "false");

    fireEvent.click(screen.getByRole("button", { name: "Expandir menú" }));

    expect(container.querySelector(".app-layout")).not.toHaveClass(
      "app-layout--collapsed",
    );
  });

  test("cambia el tema solicitado", () => {
    renderSidebar();

    fireEvent.click(screen.getByRole("button", { name: "Cambiar a tema oscuro" }));
    expect(mocks.useTheme.mock.results[0].value.toggleTheme).toHaveBeenCalled();

    cleanup();
    mocks.useTheme.mockReturnValue({
      isDark: true,
      toggleTheme: vi.fn(),
    });
    renderSidebar();

    fireEvent.click(screen.getByRole("button", { name: "Cambiar a tema claro" }));
    expect(mocks.useTheme.mock.results.at(-1).value.toggleTheme).toHaveBeenCalled();
  });

  test("cierra la sesión y navega a login", async () => {
    const logout = vi.fn();
    mocks.useAuth.mockReturnValue({ user: { nombre: "Ana" }, logout });
    renderSidebar();

    fireEvent.click(screen.getByRole("button", { name: "Cerrar sesión" }));

    expect(logout).toHaveBeenCalledOnce();
    expect(
      await screen.findByText("Página de inicio de sesión"),
    ).toBeInTheDocument();
  });
});
