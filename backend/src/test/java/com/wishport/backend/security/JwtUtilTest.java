package com.wishport.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para JwtUtil
 * Verifica la generación y validación de tokens JWT
 */
public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    public void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    public void testGenerateToken() {
        String email = "usuario@example.com";
        Integer idUsuario = 123;
        String rol = "USER";

        String token = jwtUtil.generateToken(email, idUsuario, rol);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void testExtractEmail() {
        String email = "usuario@example.com";
        Integer idUsuario = 123;
        String rol = "USER";

        String token = jwtUtil.generateToken(email, idUsuario, rol);
        String extractedEmail = jwtUtil.extractEmail(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    public void testExtractIdUsuario() {
        String email = "usuario@example.com";
        Integer idUsuario = 456;
        String rol = "ADMIN";

        String token = jwtUtil.generateToken(email, idUsuario, rol);
        Integer extractedId = jwtUtil.extractIdUsuario(token);

        assertEquals(idUsuario, extractedId);
    }

    @Test
    public void testExtractRol() {
        String email = "usuario@example.com";
        Integer idUsuario = 123;
        String rol = "ADMIN";

        String token = jwtUtil.generateToken(email, idUsuario, rol);
        String extractedRol = jwtUtil.extractRol(token);

        assertEquals(rol, extractedRol);
    }

    @Test
    public void testIsTokenValid_ConTokenValido() {
        String email = "usuario@example.com";
        Integer idUsuario = 123;
        String rol = "USER";

        String token = jwtUtil.generateToken(email, idUsuario, rol);
        boolean isValid = jwtUtil.isTokenValid(token);

        assertTrue(isValid);
    }

    @Test
    public void testIsTokenValid_ConTokenInvalido() {
        String tokenInvalido = "token.invalido.malformado";
        boolean isValid = jwtUtil.isTokenValid(tokenInvalido);

        assertFalse(isValid);
    }

    @Test
    public void testIsTokenValid_ConTokenVacio() {
        boolean isValid = jwtUtil.isTokenValid("");
        assertFalse(isValid);
    }

    @Test
    public void testIsTokenValid_ConTokenNull() {
        boolean isValid = jwtUtil.isTokenValid(null);
        assertFalse(isValid);
    }

    @Test
    public void testGeneracionTokenConRolUser() {
        String email = "user@example.com";
        Integer idUsuario = 1;
        String rol = "USER";

        String token = jwtUtil.generateToken(email, idUsuario, rol);
        String extractedRol = jwtUtil.extractRol(token);

        assertEquals("USER", extractedRol);
    }

    @Test
    public void testGeneracionTokenConRolAdmin() {
        String email = "admin@example.com";
        Integer idUsuario = 999;
        String rol = "ADMIN";

        String token = jwtUtil.generateToken(email, idUsuario, rol);
        String extractedRol = jwtUtil.extractRol(token);

        assertEquals("ADMIN", extractedRol);
    }

    @Test
    public void testExtractEmailConEmailComplejo() {
        String email = "usuario.nombre+tag@subdominio.dominio.com";
        Integer idUsuario = 123;
        String rol = "USER";

        String token = jwtUtil.generateToken(email, idUsuario, rol);
        String extractedEmail = jwtUtil.extractEmail(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    public void testIntegridadDatosToken() {
        String email = "test@example.com";
        Integer idUsuario = 789;
        String rol = "USER";

        String token = jwtUtil.generateToken(email, idUsuario, rol);

        assertEquals(email, jwtUtil.extractEmail(token));
        assertEquals(idUsuario, jwtUtil.extractIdUsuario(token));
        assertEquals(rol, jwtUtil.extractRol(token));
        assertTrue(jwtUtil.isTokenValid(token));
    }
}
