package com.wishport.backend.config;

import com.wishport.backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
private JwtAuthenticationFilter jwtAuthenticationFilter;

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
http
// Deshabilitar CSRF (no necesario para APIs REST)
.csrf().disable()
// Configurar CORS para permitir peticiones desde Android
.cors().configurationSource(corsConfigurationSource())
.and()
// Sin sesiones (stateless, usamos JWT)
.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
.and()
// Configurar qué rutas son públicas y cuáles privadas
.authorizeHttpRequests()
// Rutas públicas (no requieren token)
.requestMatchers("/api/usuarios/register", "/api/usuarios/login").permitAll()
// Rutas privadas (requieren token)
.anyRequest().authenticated()
.and()
// Añadir nuestro filtro JWT antes del filtro de autenticación por defecto
.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

return http.build();
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {
CorsConfiguration configuration = new CorsConfiguration();
configuration.setAllowedOrigins(Arrays.asList("*")); // En producción, poner la IP del Android
configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
configuration.setAllowedHeaders(Arrays.asList("*"));
configuration.setAllowCredentials(false);

UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
source.registerCorsConfiguration("/**", configuration);
return source;
 }
}