package com.wishport.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración MVC general del backend
 * Permite que el servidor sirva archivos estáticos (imágenes de pistas)
 * directamente desde la carpeta src/main/resources/static/images/
 * 
 * Sin esta configuración, las peticiones GET /images/padel.jpg
 * devolverían 404 aunque el archivo existiera en el classpath
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Registra un manejador de recursos estáticos
     * Mapea URLs a archivos en el sistema de archivos
     * 
     * @param registry Registro de manejadores de recursos de Spring MVC
     * 
     * Mapeo:
     * - URL: /images/**
     * - Ruta física: classpath:/static/images/
     * - (es decir, src/main/resources/static/images/)
     * 
     * Ejemplo:
     * - GET https://wishport-backend.onrender.com/images/padel.jpg
     * - Sirve el archivo src/main/resources/static/images/padel.jpg
     * 
     * Nota:
     * - En SecurityConfig esta ruta está marcada como pública (.permitAll())
     * - Esto permite que las imágenes sean accesibles sin token JWT
     * - Necesario para que el frontend pueda cargar las imágenes de las pistas
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Registra un manejador para la URL /images/**
        // Apunta a la carpeta static/images en el classpath
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
    }
}
