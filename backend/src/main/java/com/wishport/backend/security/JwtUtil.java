package com.wishport.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // Clave secreta para firmar el token
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("mi-clave-secreta-super-segura-para-jwt-tokens".getBytes());

    // Tiempo de validez del token: 24 horas
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    // Generar token con el email del usuario
    public String generateToken(String email, Integer idUsuario, String rol) {
        return Jwts.builder()
                .subject(email)
                .claim("idUsuario", idUsuario)
                .claim("rol", rol)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SECRET_KEY)
                .compact();
    }

    // Extraer el email del token
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // Extraer el ID del usuario del token
    public Integer extractIdUsuario(String token) {
        return getClaims(token).get("idUsuario", Integer.class);
    }

    // Extraer el rol del token
    public String extractRol(String token) {
        return getClaims(token).get("rol", String.class);
    }

    // Verificar si el token es válido
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    // Verificar si el token ha expirado
    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    // Extraer todos los claims del token
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}