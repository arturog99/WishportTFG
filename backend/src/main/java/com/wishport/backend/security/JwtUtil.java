package com.wishport.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Clave secreta para firmar el token (en producción debería estar en variables de entorno)
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Tiempo de validez del token: 24 horas
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    // Generar token con el email del usuario
    public String generateToken(String email, Integer idUsuario, String rol) {
        return Jwts.builder()
                .setSubject(email)
                .claim("idUsuario", idUsuario)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
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
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}