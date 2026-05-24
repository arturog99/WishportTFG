package com.wishport.backend.config;

import com.wishport.backend.security.JwtAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuración de seguridad de Spring Security
 * Define qué endpoints son públicos y cuáles requieren autenticación JWT
 * Configura CORS, CSRF y la política de sesiones
 */
@Configuration
@EnableWebSecurity // Activa la seguridad web de Spring
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * Filtro personalizado para validar tokens JWT
     * Se inyecta automáticamente por Spring
     */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configura la cadena de filtros de seguridad HTTP
     * Define qué rutas son públicas y cuáles requieren token JWT
     * 
     * @param http Objeto HttpSecurity para configurar la seguridad
     * @return Cadena de filtros de seguridad configurada
     * @throws Exception Si ocurre un error durante la configuración
     * 
     * Configuración:
     * 1. Desactiva CSRF (no necesario para API REST con JWT)
     * 2. Habilita CORS para permitir peticiones desde el frontend
     * 3. Configura sesiones como STATELESS (sin estado, solo JWT)
     * 4. Define endpoints públicos: /api/usuarios/register, /api/usuarios/login, /images/**
     * 5. Todos los demás endpoints requieren autenticación JWT
     * 6. Añade el filtro JWT antes del filtro de autenticación por defecto
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configurando SecurityFilterChain");
        
        http
            // Desactiva CSRF (Cross-Site Request Forgery)
            // No es necesario para APIs REST que usan JWT para autenticación
            .csrf(csrf -> csrf.disable())
            
            // Habilita CORS (Cross-Origin Resource Sharing)
            // Permite que el frontend (Android) se comunique con el backend
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Configura la política de sesiones como STATELESS
            // No se usan sesiones HTTP, solo tokens JWT para autenticación
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configura las reglas de autorización
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos (no requieren token JWT)
                .requestMatchers("/api/usuarios/register", "/api/usuarios/login", "/images/**", "/").permitAll()
                // GET para obtener todas las reservas requiere rol ADMIN
                .requestMatchers(HttpMethod.GET, "/api/reservas").hasRole("ADMIN")
                // PUT para actualizar estado de reserva requiere rol ADMIN
                .requestMatchers("/api/reservas/*/estado").hasRole("ADMIN")
                // POST para crear admin requiere rol ADMIN
                .requestMatchers("/api/usuarios/admin").hasRole("ADMIN")
                // Todos los demás endpoints requieren autenticación JWT (cualquier usuario autenticado)
                .anyRequest().authenticated()
            )
            // Agregar un handler para ver qué petición se está procesando
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    logger.error("ACCESO DENEGADO - Método: {}, URI: {}", request.getMethod(), request.getRequestURI());
                    logger.error("Error: {}", accessDeniedException.getMessage());
                    response.setStatus(403);
                    response.getWriter().write("Acceso denegado: " + accessDeniedException.getMessage());
                })
                .authenticationEntryPoint((request, response, authException) -> {
                    logger.error("NO AUTENTICADO - Método: {}, URI: {}", request.getMethod(), request.getRequestURI());
                    logger.error("Error: {}", authException.getMessage());
                    response.setStatus(401);
                    response.getWriter().write("No autenticado: " + authException.getMessage());
                })
            )
            
            // Añade el filtro JWT antes del filtro de autenticación por defecto
            // Esto permite que el filtro JWT valide el token antes de intentar autenticar
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        logger.info("SecurityFilterChain configurado exitosamente");
        
        // Construye y retorna la cadena de filtros de seguridad
        return http.build();
    }

    /**
     * Configura CORS para permitir peticiones desde otros dominios
     * Permite que el frontend (Android) se comunique con el backend
     * 
     * @return Configuración de CORS
     * 
     * Configuración:
     * 1. Permite peticiones desde cualquier origen (*)
     * 2. Permite métodos HTTP: GET, POST, PUT, DELETE
     * 3. Permite cualquier cabecera
     * 4. Aplica a todas las rutas de la aplicación
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // Crea una nueva configuración de CORS
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permite peticiones desde cualquier origen (en producción debería restringirse)
        configuration.setAllowedOrigins(Arrays.asList("*"));
        
        // Permite métodos HTTP específicos
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        
        // Permite cualquier cabecera HTTP
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // Crea una fuente de configuración basada en URL
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        
        // Aplica la configuración de CORS a todas las rutas (/**)
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }

    /**
     * Bean para encriptar passwords con BCrypt
     * Se usa en los controladores para encriptar passwords antes de guardarlas
     * BCrypt es un algoritmo de hashing seguro para passwords
     * 
     * @return Instancia de BCryptPasswordEncoder
     * 
     * Uso:
     * - En UsuarioController para encriptar passwords al registrar usuarios
     * - BCrypt genera un hash único cada vez, incluso para la misma contraseña
     * - BCrypt.matches() verifica si una contraseña coincide con un hash
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}