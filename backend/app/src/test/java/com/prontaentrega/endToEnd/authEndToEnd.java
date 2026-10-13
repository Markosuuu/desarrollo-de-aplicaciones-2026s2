package com.prontaentrega.endToEnd;


import com.prontaentrega.controllers.dtos.AuthResponse;
import com.prontaentrega.controllers.dtos.LoginRequest;
import com.prontaentrega.controllers.dtos.RegisterRequest;
import com.prontaentrega.controllers.exceptionHandler.ErrorResponse;
import com.prontaentrega.repository.JugadorRepository;
import com.prontaentrega.repository.UsuarioRepository;
import com.prontaentrega.utils.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class authEndToEnd extends AbstractIntegrationTest {
    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    void clean() {
        usuarioRepository.deleteAll();


        RegisterRequest request = new RegisterRequest("Markosu", "marcos@gmail.com", "terraria2002");
        restTemplate.postForEntity(
                "/api/auth/register",
                request,
                AuthResponse.class
        );
    }

    @Test
    void registerShouldReturnStatusCreatedWhenRequestIsValid() {
        RegisterRequest request = new RegisterRequest("Ian", "ian@mail.com", "abcd1234");

        ResponseEntity<AuthResponse> registerResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        request,
                        AuthResponse.class
                );

        assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());
    }

    @Test
    void registerShouldPersistNewUserAndReturnItsData() {
        RegisterRequest request = new RegisterRequest("Ian", "ian@mail.com", "abcd1234");

        ResponseEntity<AuthResponse> registerResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        request,
                        AuthResponse.class
                );
        assertNotNull(registerResponse.getBody());
        assertNotNull(registerResponse.getBody().usuario());
        assertTrue(usuarioRepository.existsById(registerResponse.getBody().usuario().id()));
    }

    @Test
    void registerShouldReturnToken() {
        RegisterRequest request = new RegisterRequest("Ian", "ian@mail.com", "abcd1234");

        ResponseEntity<AuthResponse> registerResponse =
                restTemplate.postForEntity(
                        "/api/auth/register",
                        request,
                        AuthResponse.class
                );
        assertNotNull(registerResponse.getBody());
        assertNotNull(registerResponse.getBody().token());
    }

    @Test
    void registerShouldReturnConflictWhenUserIsDuplicated() {
        RegisterRequest request = new RegisterRequest("Ian", "ian@mail.com", "abcd1234");

        restTemplate.postForEntity(
                        "/api/auth/register",
                        request,
                        AuthResponse.class);

        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/auth/register",
                request,
                ErrorResponse.class);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
    }

    @Test
    void registerShouldReturnBadRequestWhenPasswordIsShort() {
        RegisterRequest request = new RegisterRequest("Ian", "ian@mail.com", "abcd123");


        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/auth/register",
                request,
                ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void registerShouldReturnBadRequestWhenPasswordOnlyHasLetters() {
        RegisterRequest request = new RegisterRequest("Ian", "ian@mail.com", "abcdtuvwxyz");


        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/auth/register",
                request,
                ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void registerShouldReturnBadRequestWhenPasswordOnlyHasNumbers() {
        RegisterRequest request = new RegisterRequest("Ian", "ian@mail.com", "1234567891011112");


        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/auth/register",
                request,
                ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void registerShouldReturnBadRequestWhenMailIsInvalid() {
        RegisterRequest request = new RegisterRequest("Ian", "ian@", "abcd123456");


        ResponseEntity<ErrorResponse> response = restTemplate.postForEntity("/api/auth/register",
                request,
                ErrorResponse.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void loginShouldReturnOkStatusWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest("marcos@gmail.com", "terraria2002");
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity("/api/auth/login",
                request,
                AuthResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

}
