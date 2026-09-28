import * as Yup from 'yup';

export const registerSchema = Yup.object({
  nombre: Yup.string()
    .trim()
    .required('El nombre es obligatorio.'),
  correo: Yup.string()
    .trim()
    .required('El correo es obligatorio.')
    .email('El correo tiene un formato inválido.'),
  password: Yup.string()
    .trim()
    .required('La contraseña es obligatoria.')
    .min(8, 'La contraseña debe tener al menos 8 caracteres.')
    .matches(/[A-Za-z]/, 'La contraseña debe incluir al menos una letra.')
    .matches(/\d/, 'La contraseña debe incluir al menos un número.'),
});
