package com.wishport.backend.controllers;

import com.wishport.backend.entities.Pista;
import com.wishport.backend.repositories.PistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestionar las pistas deportivas
 * Expone endpoints para listar, filtrar y actualizar pistas
 * Base path: /api/pistas
 */
@RestController
@RequestMapping("/api/pistas")
@CrossOrigin(origins = "*")
public class PistaController {

    /**
     * Repositorio de pistas para acceder a la base de datos
     * Proporciona métodos CRUD para la entidad Pista
     */
    @Autowired
    private PistaRepository pistaRepository;

    /**
     * Endpoint para obtener todas las pistas
     * GET /api/pistas
     * Requiere token JWT
     * 
     * @return ResponseEntity con lista de todas las pistas disponibles
     * 
     * Proceso:
     * 1. Busca todas las pistas en la base de datos
     * 2. Corrige automáticamente las URLs de imágenes si están incorrectas
     * 3. Retorna la lista completa de pistas con URLs corregidas
     */
    @GetMapping
    public ResponseEntity<List<Pista>> getAllPistas() {
        List<Pista> pistas = pistaRepository.findAll();
        
        // Corregir URLs de imágenes automáticamente
        for (Pista pista : pistas) {
            String deporte = pista.getDeporte();
            String urlCorrecta = null;
            
            if (deporte != null) {
                if (deporte.equalsIgnoreCase("Pádel")) {
                    urlCorrecta = "/images/padel.png";
                } else if (deporte.equalsIgnoreCase("Fútbol 7") || 
                           deporte.equalsIgnoreCase("Fútbol 11") || 
                           deporte.equalsIgnoreCase("Futsal")) {
                    urlCorrecta = "/images/futsal.png";
                } else if (deporte.equalsIgnoreCase("Baloncesto")) {
                    urlCorrecta = "/images/basket.png";
                }
            }
            
            // Si la URL es incorrecta, actualizarla en memoria (no en BD)
            if (urlCorrecta != null && !urlCorrecta.equals(pista.getFotoUrl())) {
                pista.setFotoUrl(urlCorrecta);
            }
        }
        
        return ResponseEntity.ok(pistas);
    }

    /**
     * Endpoint para obtener una pista por ID
     * GET /api/pistas/{id}
     * Requiere token JWT
     * 
     * @param id ID de la pista a buscar
     * @return ResponseEntity con la pista encontrada o 404 si no existe
     * 
     * Proceso:
     * 1. Busca pista por ID en la base de datos
     * 2. Si existe, retorna la pista
     * 3. Si no existe, retorna 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Pista> getPistaById(@PathVariable Integer id) {
        return pistaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint para obtener pistas filtradas por deporte
     * GET /api/pistas/deporte/{deporte}
     * Requiere token JWT
     * 
     * @param deporte Tipo de deporte a filtrar (ejemplo: "Pádel", "Fútbol 7", "Baloncesto")
     * @return ResponseEntity con lista de pistas del deporte especificado
     * 
     * Proceso:
     * 1. Busca pistas por tipo de deporte en la base de datos
     * 2. Retorna la lista de pistas filtradas
     */
    @GetMapping("/deporte/{deporte}")
    public ResponseEntity<List<Pista>> getPistasByDeporte(@PathVariable String deporte) {
        List<Pista> pistas = pistaRepository.findByDeporte(deporte);
        return ResponseEntity.ok(pistas);
    }

    /**
     * Endpoint para actualizar las rutas de imágenes de todas las pistas
     * PUT /api/pistas/actualizar-imagenes
     * Requiere token JWT
     * Este endpoint corrige las URLs de imágenes en la base de datos
     * 
     * @return ResponseEntity con mensaje de confirmación y número de pistas actualizadas
     * 
     * Proceso:
     * 1. Obtiene todas las pistas de la base de datos
     * 2. Para cada pista, determina la nueva URL según el tipo de deporte:
     *    - Pádel → /images/padel.png
     *    - Fútbol 7, Fútbol 11, Futsal → /images/futsal.png
     *    - Baloncesto → /images/basket.png
     * 3. Si la URL es diferente a la actual, la actualiza
     * 4. Guarda los cambios en la base de datos
     * 5. Retorna el número de pistas actualizadas
     */
    @PutMapping("/actualizar-imagenes")
    public ResponseEntity<String> actualizarImagenes() {
        // Obtener todas las pistas de la base de datos
        List<Pista> pistas = pistaRepository.findAll();
        int actualizadas = 0;
        
        // Iterar sobre cada pista para actualizar su imagen
        for (Pista pista : pistas) {
            String deporte = pista.getDeporte();
            String nuevaUrl = null;
            
            // Determinar la nueva URL según el tipo de deporte
            if (deporte != null) {
                if (deporte.equalsIgnoreCase("Pádel")) {
                    nuevaUrl = "/images/padel.png";
                } else if (deporte.equalsIgnoreCase("Fútbol 7") || 
                           deporte.equalsIgnoreCase("Fútbol 11") || 
                           deporte.equalsIgnoreCase("Futsal")) {
                    nuevaUrl = "/images/futsal.png";
                } else if (deporte.equalsIgnoreCase("Baloncesto")) {
                    nuevaUrl = "/images/basket.png";
                }
            }
            
            // Solo actualizar si la URL es diferente a la actual
            if (nuevaUrl != null && !nuevaUrl.equals(pista.getFotoUrl())) {
                pista.setFotoUrl(nuevaUrl);
                pistaRepository.save(pista);
                actualizadas++;
            }
        }
        
        return ResponseEntity.ok("Se actualizaron " + actualizadas + " pistas");
    }
}