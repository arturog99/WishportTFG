package com.wishport.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Clase principal de la aplicación Spring Boot.
 * 
 * Esta es el punto de entrada del backend de WishPort.
 * Al ejecutar esta clase, Spring Boot inicia el servidor embebido (Tomcat)
 * en el puerto 8080 y configura automáticamente todos los componentes.
 */
@SpringBootApplication
public class WishportBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WishportBackendApplication.class, args);
        System.out.println("========================================");
        System.out.println("WishPort Backend iniciado correctamente");
        System.out.println("========================================");
    }
}
