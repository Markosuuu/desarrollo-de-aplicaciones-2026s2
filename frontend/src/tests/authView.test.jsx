import { afterEach, beforeEach, describe, expect, test, vi } from "vitest";
import {
  cleanup,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { toast } from "react-toastify";
import AuthView from "../components/AuthView";

const toastMocks = vi.hoisted(() => ({
  success: vi.fn(),
  error: vi.fn(),
}));

vi.mock("react-toastify", () => ({
  toast: toastMocks,
}));

describe("AuthView", () => {
  const props = {
    schema: { validate: vi.fn() },
    onSubmit: vi.fn(),
    title: "Iniciar sesión",
    subtitle: "Ingresá tus credenciales.",
    fields: [
      {
        name: "correo",
        label: "Correo electrónico",
        type: "email",
        placeholder: "ana@ejemplo.com",
      },
      {
        name: "password",
        label: "Contraseña",
        type: "password",
        placeholder: "********",
      },
    ],
    submitLabel: "Ingresar",
    submittingLabel: "Ingresando...",
    successMessage: "Inicio de sesión correcto.",
    errorMessage: "No se pudo iniciar sesión.",
    switchMessage: "¿No tenés cuenta?",
    switchLink: { href: "/register", label: "Registrate acá" },
  };

  function renderAuthView() {
    return render(
      <MemoryRouter initialEntries={["/login"]}>
        <Routes>
          <Route path="/login" element={<AuthView {...props} />} />
          <Route path="/" element={<p>Página de inicio</p>} />
        </Routes>
      </MemoryRouter>,
    );
  }

  beforeEach(() => {
    vi.clearAllMocks();
    props.schema.validate.mockResolvedValue(undefined);
    props.onSubmit.mockResolvedValue(undefined);
  });

  afterEach(() => {
    cleanup();
  });

  test("renderiza el título, los campos y el enlace alternativo", () => {
    renderAuthView();

    expect(
      screen.getByRole("heading", { name: "Iniciar sesión" }),
    ).toBeTruthy();
    expect(screen.getByLabelText("Correo electrónico")).toHaveAttribute(
      "placeholder",
      "ana@ejemplo.com",
    );
    expect(screen.getByLabelText("Contraseña")).toHaveAttribute(
      "type",
      "password",
    );
    expect(screen.getByRole("link", { name: "Registrate acá" })).toHaveAttribute(
      "href",
      "/register",
    );
  });

  test("valida y envía los valores ingresados, luego muestra éxito y navega al inicio", async () => {
    renderAuthView();

    fireEvent.change(screen.getByLabelText("Correo electrónico"), {
      target: { value: "ana@ejemplo.com" },
    });
    fireEvent.change(screen.getByLabelText("Contraseña"), {
      target: { value: "clave123" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Ingresar" }));

    await waitFor(() => {
      expect(props.schema.validate).toHaveBeenCalledWith(
        { correo: "ana@ejemplo.com", password: "clave123" },
        { abortEarly: false },
      );
      expect(props.onSubmit).toHaveBeenCalledWith({
        correo: "ana@ejemplo.com",
        password: "clave123",
      });
      expect(toast.success).toHaveBeenCalledWith("Inicio de sesión correcto.");
      expect(screen.getByText("Página de inicio")).toBeInTheDocument();
    });
  });

  test("muestra el error de validación y no envía los datos inválidos", async () => {
    props.schema.validate.mockRejectedValue({
      inner: [{ message: "El correo no es válido." }],
    });
    renderAuthView();

    fireEvent.click(screen.getByRole("button", { name: "Ingresar" }));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("El correo no es válido.");
      expect(props.onSubmit).not.toHaveBeenCalled();
    });
  });

  test("muestra el mensaje del error cuando no hay errores internos", async () => {
    props.schema.validate.mockRejectedValue(new Error("Error de autenticación."));
    renderAuthView();

    fireEvent.click(screen.getByRole("button", { name: "Ingresar" }));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith("Error de autenticación.");
    });
  });

  test("muestra el mensaje genérico cuando el error no tiene mensaje", async () => {
    props.schema.validate.mockRejectedValue({});
    renderAuthView();

    fireEvent.click(screen.getByRole("button", { name: "Ingresar" }));

    await waitFor(() => {
      expect(toast.error).toHaveBeenCalledWith(
        "No se pudo iniciar sesión.",
      );
    });
  });

  test("deshabilita el botón y muestra el texto de envío mientras espera", async () => {
    let finishSubmit;
    props.onSubmit.mockReturnValue(
      new Promise((resolve) => {
        finishSubmit = resolve;
      }),
    );
    renderAuthView();

    fireEvent.click(screen.getByRole("button", { name: "Ingresar" }));

    const submittingButton = await screen.findByRole("button", {
      name: "Ingresando...",
    });
    expect(submittingButton).toBeDisabled();

    finishSubmit();

    await waitFor(() => {
      expect(toast.success).toHaveBeenCalledWith("Inicio de sesión correcto.");
      expect(screen.getByText("Página de inicio")).toBeInTheDocument();
    });
  });
});
