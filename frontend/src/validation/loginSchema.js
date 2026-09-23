import * as Yup from 'yup';

export const loginSchema = Yup.object({
  correo: Yup.string()
    .trim()
    .required('El correo es obligatorio.')
    .email('El correo tiene un formato inválido.'),
  password: Yup.string()
    .trim()
    .required('La contraseña es obligatoria.')
    .min(1, 'La contraseña es obligatoria.'),
});
