package com.wishport.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracion para servir archivos estaticos (imagenes de pistas)
 * Mapea la URL /images/** a la carpeta fisica uploads/images/ del proyecto
 * Las rutas en BD son /images/pistas/padel.png -> busca en uploads/images/pistas/padel.png
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Permite acceder a http://localhost:8080/images/pistas/padel.jpg
        // buscando en la carpeta fisica "uploads/images" del proyecto
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:uploads/images/");
    }
}
