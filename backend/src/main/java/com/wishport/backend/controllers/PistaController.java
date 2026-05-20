package com.wishport.backend.controllers;

import com.wishport.backend.entities.Pista;
import com.wishport.backend.repositories.PistaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Controlador REST para gestionar las pistas deportivas
// Expone endpoints para listar y filtrar pistas
@RestController
@RequestMapping("/api/pistas")
@CrossOrigin(origins = "*")
public class PistaController {

    @Autowired
    private PistaRepository pistaRepository;

    /**
     * Endpoint para obtener todas las pistas
     * GET /api/pistas
     * Requiere token JWT
     */
    @GetMapping
    public ResponseEntity<List<Pista>> getAllPistas() {
        List<Pista> pistas = pistaRepository.findAll();
        return ResponseEntity.ok(pistas);
    }

    /**
     * Endpoint para obtener una pista por ID
     * GET /api/pistas/{id}
     * Requiere token JWT
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
     */
    @GetMapping("/deporte/{deporte}")
    public ResponseEntity<List<Pista>> getPistasByDeporte(@PathVariable String deporte) {
        List<Pista> pistas = pistaRepository.findByDeporte(deporte);
        return ResponseEntity.ok(pistas);
    }
}