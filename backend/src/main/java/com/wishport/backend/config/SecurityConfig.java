package com.wishport.backend.config;

import com.wishport.backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

// Configuración de seguridad de Spring
// Define qué endpoints son públicos y cuáles requieren autenticación
@Configuration
@EnableWebSecurity // Activa la seguridad web de Spring
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter; // Filtro que valida tokens JWT

    /**
     * Configura la cadena de filtros de seguridad HTTP
     * Define qué rutas son públicas y cuáles requieren token JWT
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Desactiva CSRF (no necesario para API REST)
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Habilita CORS
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Sin sesión (stateless)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/usuarios/register", "/api/usuarios/login").permitAll() // Públicos
                .anyRequest().authenticated() // El resto requieren token
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class); // Añade filtro JWT antes del filtro de autenticación

        return http.build();
    }

    /**
     * Configura CORS para permitir peticiones desde otros dominios
     * Permite que el frontend (Android) se comunique con el backend
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); // Permite cualquier origen
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE")); // Métodos HTTP permitidos
        configuration.setAllowedHeaders(Arrays.asList("*")); // Cabeceras permitidas

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // Aplica a todas las rutas
        return source;
    }

    /**
     * Bean para encriptar passwords con BCrypt
     * Se usa en los controladores para encriptar passwords antes de guardarlas
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}