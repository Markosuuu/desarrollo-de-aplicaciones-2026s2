import AuthView from "../components/AuthView";
import { useAuth } from "../context/AuthContext";
import { loginSchema } from "../validation/loginSchema";

export default function LoginPage() {
  const { login } = useAuth();

  return (
    <AuthView
      schema={loginSchema}
      onSubmit={login}
      title="Iniciar sesión"
      subtitle="Ingresá tus credenciales para continuar."
      fields={[
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
      ]}
      submitLabel="Ingresar"
      submittingLabel="Ingresando..."
      successMessage="Inicio de sesión correcto."
      errorMessage="No se pudo iniciar sesión."
      switchMessage="¿No tenés cuenta?"
      switchLink={{ href: "/register", label: "Registrate acá" }}
    />
  );
}
