package com.prontaentrega.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.prontaentrega.repository.TokenEstadoRepository;
import com.prontaentrega.repository.UsuarioRepository;
import com.prontaentrega.utils.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenEstadoRepository tokenEstadoRepository;

    @BeforeEach
    void cleanDatabase() {
        tokenEstadoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    /** Validar que al registrar se retorne status 201 y se devuelva el token y el correo del usuario.*/
    @Test
    void registerShouldCreateUserAndReturnToken() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nombre": "Ana",
                                "correo": "ana@mail.com",
                                "password": "clave123"
                            }
                            """))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.usuario.correo").value("ana@mail.com"));
    }

    /** Validar que al loguearse con un usuario previamente registrado se devuelva el token y status 200 */
    @Test
    void loginShouldReturnTokenForValidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nombre": "Ana",
                                "correo": "ana@mail.com",
                                "password": "clave123"
                            }
                            """));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "correo": "ana@mail.com",
                                "password": "clave123"
                            }
                            """))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.usuario.correo").value("ana@mail.com"));
    }

    /** Validar que al loguearse con credenciales invalidas se retorne 401 */
    @Test
    void loginShouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "nombre": "Ana",
                                "correo": "ana@mail.com",
                                "password": "clave123"
                            }
                            """));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "correo": "ana@mail.com",
                                "password": "contraseñaerrada"
                            }
                            """))

                .andExpect(status().isUnauthorized());
    }

    /** Validar que al registrar un email ya registrado arroje status 409.*/
    @Test
    void registerShouldReturnConflictWhenEmailAlreadyExists() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "nombre": "Ana",
                            "correo": "ana@mail.com",
                            "password": "clave123"
                        }
                        """));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "nombre": "Maria",
                            "correo": "ana@mail.com",
                            "password": "clave456"
                        }
                        """))

                .andExpect(status().isConflict());
    }

    /** Validar que al registrar cuando la contraseña sea menor a 8 caracteres retorne status 400 */
    @Test
    void registerShouldReturnBadRequestWhenPasswordIsTooShort() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "nombre": "Ana",
                            "correo": "ana@mail.com",
                            "password": "1234567"
                        }
                        """))

                .andExpect(status().isBadRequest());
    }

    /** Validar que al registrar cuando el formato de email sea invalido retorne status 400 */
    @Test
    void registerShouldReturnBadRequestWhenEmailIsInvalid() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "nombre": "Ana",
                            "correo": "ana",
                            "password": "12345678"
                        }
                        """))

                .andExpect(status().isBadRequest());
    }
}
