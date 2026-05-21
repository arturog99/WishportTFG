package com.wishport.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT
 * Este filtro se ejecuta en cada petición HTTP para validar el token JWT
 * Extiende OncePerRequestFilter para garantizar que se ejecute solo una vez por petición
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

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

        // 1. Leer el header Authorization de la petición HTTP
        // El header Authorization debe contener el token JWT
        String authHeader = request.getHeader("Authorization");

        // 2. Verificar que el header tiene el formato correcto "Bearer token"
        // Si no tiene el formato correcto, continuar sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extraer el token eliminando el prefijo "Bearer "
        // El token comienza después de los 7 caracteres de "Bearer "
        String token = authHeader.substring(7);

        // 4. Validar que el token sea correcto y no haya expirado
        // Si el token no es válido, continuar sin autenticar
        if (!jwtUtil.isTokenValid(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 5. Extraer los datos del usuario del token JWT
        // Extraemos el email y el ID del usuario para la autenticación
        String email = jwtUtil.extractEmail(token);
        Integer idUsuario = jwtUtil.extractIdUsuario(token);

        // 6. Crear objeto de autenticación de Spring Security
        // UsernamePasswordAuthenticationToken representa un usuario autenticado
        // El primer parámetro es el principal (ID del usuario)
        // El segundo parámetro son las credenciales (null porque ya validamos el token)
        // El tercer parámetro son los roles/autoridades (null por simplicidad)
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(idUsuario, null, null);
        
        // 7. Establecer detalles de la autenticación (dirección IP, sesión, etc.)
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
        // 8. Guardar la autenticación en el contexto de seguridad de Spring
        // Esto permite que otros filtros y controladores accedan a la autenticación
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // 9. Continuar con la cadena de filtros de Spring Security
        // Esto permite que la petición llegue al controlador correspondiente
        filterChain.doFilter(request, response);
    }
}