package com.wishport.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utilidad para generar y validar tokens JWT (JSON Web Token)
 * Los tokens JWT se usan para autenticar a los usuarios en el sistema
 * Un token JWT contiene información del usuario (email, ID, rol) y tiene un tiempo de expiración
 */
@Component
public class JwtUtil {

    /**
     * Clave secreta para firmar y validar los tokens JWT
     * Esta clave debe mantenerse en secreto y no compartirse
     * Se usa HMAC SHA para firmar los tokens
     */
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("mi-clave-secreta-super-segura-para-jwt-tokens".getBytes());

    /**
     * Tiempo de validez del token en milisegundos
     * 24 horas = 24 * 60 * 60 * 1000 milisegundos
     * Después de este tiempo, el token expira y el usuario debe volver a hacer login
     */
    private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;

    /**
     * Genera un token JWT con los datos del usuario
     * El token incluye email, ID de usuario y rol
     * 
     * @param email Email del usuario
     * @param idUsuario ID del usuario en la base de datos
     * @param rol Rol del usuario ("USER" o "ADMIN")
     * @return Token JWT como String
     * 
     * Proceso:
     * 1. Establece el subject del token (email del usuario)
     * 2. Agrega claims personalizados (idUsuario, rol)
     * 3. Establece la fecha de emisión (ahora)
     * 4. Establece la fecha de expiración (ahora + 24 horas)
     * 5. Firma el token con la clave secreta
     * 6. Compacta el token a formato String
     */
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

    /**
     * Extrae el email del token JWT
     * El email está almacenado en el subject del token
     * 
     * @param token Token JWT del cual extraer el email
     * @return Email del usuario
     * 
     * Proceso:
     * 1. Obtiene todos los claims del token
     * 2. Retorna el subject que contiene el email
     */
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extrae el ID del usuario del token JWT
     * El ID está almacenado como un claim personalizado
     * 
     * @param token Token JWT del cual extraer el ID
     * @return ID del usuario como Integer
     * 
     * Proceso:
     * 1. Obtiene todos los claims del token
     * 2. Retorna el claim "idUsuario" como Integer
     */
    public Integer extractIdUsuario(String token) {
        return getClaims(token).get("idUsuario", Integer.class);
    }

    /**
     * Extrae el rol del usuario del token JWT
     * El rol está almacenado como un claim personalizado
     * 
     * @param token Token JWT del cual extraer el rol
     * @return Rol del usuario como String ("USER" o "ADMIN")
     * 
     * Proceso:
     * 1. Obtiene todos los claims del token
     * 2. Retorna el claim "rol" como String
     */
    public String extractRol(String token) {
        return getClaims(token).get("rol", String.class);
    }

    /**
     * Verifica si el token JWT es válido
     * Un token es válido si no ha expirado y está correctamente firmado
     * 
     * @param token Token JWT a validar
     * @return true si el token es válido, false si no lo es
     * 
     * Proceso:
     * 1. Intenta verificar que el token no haya expirado
     * 2. Si ocurre alguna excepción (token malformado, firma inválida, etc.), retorna false
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Verifica si el token JWT ha expirado
     * 
     * @param token Token JWT a verificar
     * @return true si el token ha expirado, false si aún es válido
     * 
     * Proceso:
     * 1. Obtiene todos los claims del token
     * 2. Compara la fecha de expiración con la fecha actual
     * 3. Retorna true si la fecha actual es posterior a la expiración
     */
    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

    /**
     * Extrae todos los claims (información) del token JWT
     * Este método es privado y usado internamente por los otros métodos
     * 
     * @param token Token JWT del cual extraer los claims
     * @return Claims con toda la información del token
     * 
     * Proceso:
     * 1. Crea un parser JWT
     * 2. Configura la clave secreta para verificar la firma
     * 3. Parsea el token y extrae el payload con los claims
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}