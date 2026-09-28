import AuthView from "../components/AuthView";
import { useAuth } from "../context/AuthContext";
import { registerSchema } from "../validation/registerSchema";

export default function RegisterPage() {
  const { register } = useAuth();

  return (
    <AuthView
      schema={registerSchema}
      onSubmit={register}
      title="Registrarse"
      subtitle="Creá tu cuenta para acceder al sitio."
      fields={[
        {
          name: "nombre",
          label: "Nombre",
          type: "text",
          placeholder: "Ana",
        },
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
      submitLabel="Crear cuenta"
      submittingLabel="Registrando..."
      successMessage="Cuenta creada con éxito."
      errorMessage="No se pudo completar el registro."
      switchMessage="¿Ya tenés una cuenta?"
      switchLink={{ href: "/login", label: "Iniciá sesión" }}
    />
  );
}
