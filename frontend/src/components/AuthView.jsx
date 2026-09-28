import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { toast } from "react-toastify";

export default function AuthView({
  schema,
  onSubmit,
  title,
  subtitle,
  fields,
  submitLabel,
  submittingLabel,
  successMessage,
  errorMessage,
  switchMessage,
  switchLink,
}) {
  const navigate = useNavigate();
  const [values, setValues] = useState(() =>
    Object.fromEntries(fields.map(({ name }) => [name, ""])),
  );
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (event) => {
    const { name, value } = event.target;
    setValues((current) => ({ ...current, [name]: value }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);

    try {
      await schema.validate(values, { abortEarly: false });
      await onSubmit(values);
      toast.success(successMessage);
      navigate("/");
    } catch (error) {
      const message =
        error?.inner?.[0]?.message || error?.message || errorMessage;
      toast.error(message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <main className="auth-shell">
      <section className="auth-card">
        <h1>{title}</h1>
        <p className="auth-subtitle">{subtitle}</p>

        <form onSubmit={handleSubmit} className="auth-form">
          {fields.map(({ name, label, type, placeholder }) => (
            <label key={name}>
              <span>{label}</span>
              <input
                type={type}
                name={name}
                value={values[name]}
                onChange={handleChange}
                placeholder={placeholder}
              />
            </label>
          ))}

          <button type="submit" disabled={submitting}>
            {submitting ? submittingLabel : submitLabel}
          </button>
        </form>

        <p className="auth-switch">
          {switchMessage} <Link to={switchLink.href}>{switchLink.label}</Link>
        </p>
      </section>
    </main>
  );
}
