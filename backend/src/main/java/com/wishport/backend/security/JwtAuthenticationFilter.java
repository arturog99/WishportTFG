package com.wishport.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filtro de autenticación JWT
 * Este filtro se ejecuta en cada petición HTTP para validar el token JWT
 * Extiende OncePerRequestFilter para garantizar que se ejecute solo una vez por petición
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    /**
     * Utilidad para generar y validar tokens JWT
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Método principal del filtro que se ejecuta en cada petición
     * Valida el token JWT si está presente en el header Authorization
     * 
     * @param request Petición HTTP entrante
     * @param response Respuesta HTTP saliente
     * @param filterChain Cadena de filtros de Spring Security
     * @throws ServletException Si ocurre un error de servlet
     * @throws IOException Si ocurre un error de I/O
     * 
     * Proceso:
     * 1. Lee el header Authorization de la petición
     * 2. Verifica que tiene el formato "Bearer token"
     * 3. Extrae el token eliminando el prefijo "Bearer "
     * 4. Valida que el token sea correcto y no haya expirado
     * 5. Extrae el email e ID del usuario del token
     * 6. Crea un objeto de autenticación de Spring Security
     * 7. Guarda la autenticación en el contexto de seguridad
     * 8. Continúa con la cadena de filtros
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        logger.info("=== FILTRO JWT ===");
        logger.info("Método: {}, URI: {}", request.getMethod(), request.getRequestURI());

        // 1. Leer el header Authorization de la petición HTTP
        // El header Authorization debe contener el token JWT
        String authHeader = request.getHeader("Authorization");
        logger.info("Header Authorization: {}", authHeader != null ? authHeader.substring(0, Math.min(20, authHeader.length())) + "..." : "null");

        // 2. Verificar que el header tiene el formato correcto "Bearer token"
        // Si no tiene el formato correcto, continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Header Authorization no tiene formato Bearer, continuando sin autenticar");
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token eliminando el prefijo "Bearer "
        // El token comienza después de los 7 caracteres de "Bearer "
        String token = authHeader.substring(7);
        logger.info("Token extraído (primeros 20 chars): {}", token.substring(0, Math.min(20, token.length())));

        // 4. Validar que el token sea correcto y no haya expirado
        // Si el token no es válido, continuar sin autenticar
        if (!jwtUtil.isTokenValid(token)) {
            logger.warn("Token inválido o expirado, continuando sin autenticar");
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Extraer los datos del usuario del token JWT
        // Extraemos el email, el ID del usuario y el rol para la autenticación
        String email = jwtUtil.extractEmail(token);
        Integer idUsuario = jwtUtil.extractIdUsuario(token);
        String rol = jwtUtil.extractRol(token);
        logger.info("Token válido - Email: {}, ID: {}, Rol: {}", email, idUsuario, rol);

        // 6. Crear objeto de autenticación de Spring Security
        // UsernamePasswordAuthenticationToken representa un usuario autenticado
        // El primer parámetro es el principal (ID del usuario)
        // El segundo parámetro son las credenciales (null porque ya validamos el token)
        // El tercer parámetro son los roles/autoridades (incluimos el rol del usuario)
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                    idUsuario, 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + rol))
                );
        logger.info("Autoridades: ROLE_{}", rol);
        
        // 7. Establecer detalles de la autenticación (dirección IP, sesión, etc.)
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
        // 8. Guardar la autenticación en el contexto de seguridad de Spring
        // Esto permite que otros filtros y controladores accedan a la autenticación
        SecurityContextHolder.getContext().setAuthentication(authToken);
        logger.info("Autenticación establecida en SecurityContext");

        // 9. Continuar con la cadena de filtros de Spring Security
        // Esto permite que la petición llegue al controlador correspondiente
        filterChain.doFilter(request, response);
    }
}