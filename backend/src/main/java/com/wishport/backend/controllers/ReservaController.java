package com.wishport.backend.controllers;

import com.wishport.backend.entities.Pista;
import com.wishport.backend.entities.Reserva;
import com.wishport.backend.entities.Usuario;
import com.wishport.backend.repositories.PistaRepository;
import com.wishport.backend.repositories.ReservaRepository;
import com.wishport.backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Controlador REST para gestionar las reservas
@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "*")
public class ReservaController {

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PistaRepository pistaRepository;

    // Endpoint para obtener todas las reservas
    @GetMapping
    public ResponseEntity<List<Reserva>> getAllReservas() {
        List<Reserva> reservas = reservaRepository.findAll();
        return ResponseEntity.ok(reservas);
    }

    // Endpoint para obtener reservas de un usuario
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Reserva>> getReservasByUsuario(@PathVariable Integer idUsuario) {
        List<Reserva> reservas = reservaRepository.findByIdUsuario_IdUsuario(idUsuario);
        return ResponseEntity.ok(reservas);
    }

    // Endpoint para obtener reservas de una pista
    @GetMapping("/pista/{idPista}")
    public ResponseEntity<List<Reserva>> getReservasByPista(@PathVariable Integer idPista) {
        List<Reserva> reservas = reservaRepository.findByIdPista_IdPista(idPista);
        return ResponseEntity.ok(reservas);
    }

    // Endpoint para crear una reserva
    @PostMapping
    public ResponseEntity<?> createReserva(@RequestBody Reserva reserva) {
        // Verificar usuario
        if (reserva.getIdUsuario() == null || reserva.getIdUsuario().getIdUsuario() == null) {
            return ResponseEntity.badRequest().body("ID de usuario requerido");
        }

        Usuario usuario = usuarioRepository.findById(reserva.getIdUsuario().getIdUsuario())
                .orElse(null);
        if (usuario == null) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }

        // Verificar pista
        if (reserva.getIdPista() == null || reserva.getIdPista().getIdPista() == null) {
            return ResponseEntity.badRequest().body("ID de pista requerido");
        }

        Pista pista = pistaRepository.findById(reserva.getIdPista().getIdPista())
                .orElse(null);
        if (pista == null) {
            return ResponseEntity.badRequest().body("Pista no encontrada");
        }

        // Generar código QR único
        String codigoQr = UUID.randomUUID().toString();
        reserva.setCodigoQr(codigoQr);
        reserva.setFecha(LocalDateTime.now());
        reserva.setEstadoReserva("ACTIVA");

        Reserva reservaGuardada = reservaRepository.save(reserva);

        return ResponseEntity.ok(reservaGuardada);
    }

    // Endpoint para cancelar una reserva
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReserva(@PathVariable Integer id) {
        if (!reservaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        reservaRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}