package com.wishport.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracion MVC general del backend.
 *
 * Permite que el servidor sirva archivos estaticos (imagenes de pistas)
 * directamente desde la carpeta src/main/resources/static/images/.
 *
 * Sin esta configuracion, las peticiones GET /images/padel.jpg
 * devolverian 404 aunque el archivo existiera en el classpath.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Registra un manejador de recursos estaticos.
     *
     * Mapeo:
     *   URL: /images/**
     *   Ruta fisica: classpath:/static/images/
     *   (es decir, src/main/resources/static/images/)
     *
     * Ejemplo:
     *   GET https://wishport-backend.onrender.com/images/padel.jpg
     *   -> Sirve el archivo src/main/resources/static/images/padel.jpg
     *
     * En SecurityConfig esta ruta esta marcada como publica (.permitAll())
     * para que las imagenes sean accesibles sin token JWT.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("classpath:/static/images/");
    }
}
