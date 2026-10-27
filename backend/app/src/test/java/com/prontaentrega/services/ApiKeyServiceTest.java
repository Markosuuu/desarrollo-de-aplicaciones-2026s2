package com.prontaentrega.services;

import com.prontaentrega.persistence.dto.ApiKey;
import com.prontaentrega.persistence.repository.dao.ApiKeyDAO;
import com.prontaentrega.utils.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.nio.charset.StandardCharsets;


import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest
@Tag("integracion")
class ApiKeyServiceTest extends AbstractIntegrationTest {

    @Autowired
    private ApiKeyService apiKeyService;

    @Autowired
    private ApiKeyDAO apiKeyDAO;

    @BeforeEach
    void setUp() {
        apiKeyDAO.deleteAll();
    }

    /**
     * Se verifica que al generar una API Key se persista correctamente su hash en la base de datos
     * y se devuelva la clave original.
     */
    @Test
    void generar_deberiaGuardarHashYDevolverClaveOriginal() {
        String apiKey = apiKeyService.generar("Sistema de prueba");

        assertNotNull(apiKey);
        assertFalse(apiKey.isBlank());

        String hash = calcularHash(apiKey);

        ApiKey guardada = apiKeyDAO.findByKeyHashAndActivaTrue(hash)
                .orElseThrow();

        assertEquals("Sistema de prueba", guardada.getNombre());
        assertTrue(guardada.isActiva());
        assertNotNull(guardada.getFechaCreacion());
        assertNotEquals(apiKey, guardada.getKeyHash());
    }

    /**
     * Se verifica que generar dos API Keys produzca claves diferentes y persista ambos registros.
     */
    @Test
    void generar_dosVeces_deberiaCrearClavesDiferentes() {
        String primera = apiKeyService.generar("Sistema A");
        String segunda = apiKeyService.generar("Sistema B");

        assertNotEquals(primera, segunda);
        assertEquals(2, apiKeyDAO.count());
    }

    /**
     * Se verifica que una API Key existente y activa sea validada correctamente.
     */
    @Test
    void validar_conClaveExistente_deberiaRetornarTrue() {
        String apiKey = apiKeyService.generar("Sistema de prueba");

        assertTrue(apiKeyService.validar(apiKey));
    }

    /**
     * Se verifica que una API Key inexistente sea rechazada.
     */
    @Test
    void validar_conClaveInexistente_deberiaRetornarFalse() {
        assertFalse(apiKeyService.validar("clave-inexistente"));
    }

    /**
     * Se verifica que una API Key nula sea rechazada sin provocar una excepción.
     */
    @Test
    void validar_conClaveNula_deberiaRetornarFalse() {
        assertFalse(apiKeyService.validar(null));
    }

    /**
     * Se verifica que una API Key vacía o compuesta por espacios sea rechazada.
     */
    @Test
    void validar_conClaveVacia_deberiaRetornarFalse() {
        assertFalse(apiKeyService.validar(" "));
    }

    /**
     * Se verifica que una API Key previamente revocada no pueda validarse.
     */
    @Test
    void validar_conClaveRevocada_deberiaRetornarFalse() {
        String apiKey = apiKeyService.generar("Sistema de prueba");

        ApiKey guardada = apiKeyDAO
                .findByKeyHashAndActivaTrue(calcularHash(apiKey))
                .orElseThrow();

        guardada.desactivar();
        apiKeyDAO.saveAndFlush(guardada);

        assertFalse(apiKeyService.validar(apiKey));
    }

    /**
     * Se verifica que intentar generar una API Key con un nombre vacío provoque una excepción.
     */
    @Test
    void generar_conNombreVacio_deberiaLanzarExcepcion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> apiKeyService.generar(" ")
        );
    }

    private String calcularHash(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

