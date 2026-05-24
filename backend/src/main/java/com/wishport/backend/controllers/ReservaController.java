package com.wishport.backend.controllers;

import com.wishport.backend.entities.Pista;
import com.wishport.backend.entities.Reserva;
import com.wishport.backend.entities.Usuario;
import com.wishport.backend.repositories.PistaRepository;
import com.wishport.backend.repositories.ReservaRepository;
import com.wishport.backend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Controlador REST para gestionar las reservas
 * Expone endpoints para crear, listar, filtrar y cancelar reservas
 * Base path: /api/reservas
 */
@RestController
@RequestMapping("/api/reservas")
@CrossOrigin(origins = "*")
public class ReservaController {

    private static final Logger logger = LoggerFactory.getLogger(ReservaController.class);

    /**
     * Repositorio de reservas para acceder a la base de datos
     * Proporciona métodos CRUD para la entidad Reserva
     */
    @Autowired
    private ReservaRepository reservaRepository;

    /**
     * Repositorio de usuarios para validar que el usuario existe
     */
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Repositorio de pistas para validar que la pista existe
     */
    @Autowired
    private PistaRepository pistaRepository;

    /**
     * Endpoint para obtener todas las reservas
     * GET /api/reservas
     * Requiere token JWT
     * 
     * @return ResponseEntity con lista de todas las reservas del sistema
     * 
     * Proceso:
     * 1. Busca todas las reservas en la base de datos
     * 2. Retorna la lista completa de reservas
     */
    @GetMapping
    public ResponseEntity<List<Reserva>> getAllReservas() {
        List<Reserva> reservas = reservaRepository.findAll();
        return ResponseEntity.ok(reservas);
    }

    /**
     * Endpoint para obtener reservas de un usuario específico
     * GET /api/reservas/usuario/{idUsuario}
     * Requiere token JWT
     * 
     * @param idUsuario ID del usuario cuyas reservas se quieren obtener
     * @return ResponseEntity con lista de reservas del usuario especificado
     * 
     * Proceso:
     * 1. Busca reservas por ID de usuario en la base de datos
     * 2. Retorna la lista de reservas del usuario
     */
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Reserva>> getReservasByUsuario(@PathVariable Integer idUsuario) {
        List<Reserva> reservas = reservaRepository.findByIdUsuario_IdUsuario(idUsuario);
        return ResponseEntity.ok(reservas);
    }

    /**
     * Endpoint para obtener reservas de una pista específica
     * GET /api/reservas/pista/{idPista}
     * Requiere token JWT
     * 
     * @param idPista ID de la pista cuyas reservas se quieren obtener
     * @return ResponseEntity con lista de reservas de la pista especificada
     * 
     * Proceso:
     * 1. Busca reservas por ID de pista en la base de datos
     * 2. Retorna la lista de reservas de la pista
     */
    @GetMapping("/pista/{idPista}")
    public ResponseEntity<List<Reserva>> getReservasByPista(@PathVariable Integer idPista) {
        List<Reserva> reservas = reservaRepository.findByIdPista_IdPista(idPista);
        return ResponseEntity.ok(reservas);
    }

    /**
     * Endpoint para crear una nueva reserva
     * POST /api/reservas
     * Requiere token JWT
     * Genera un código QR único automáticamente para validar el acceso
     * 
     * @param reserva Objeto Reserva con los datos de la nueva reserva
     * @return ResponseEntity con la reserva creada o error si los datos son inválidos
     * 
     * Proceso:
     * 1. Verifica que el usuario existe en la base de datos
     * 2. Verifica que la pista existe en la base de datos
     * 3. Genera un código QR único usando UUID
     * 4. Establece la fecha (si no viene en la petición) y el estado a "ACTIVA"
     * 5. Guarda la reserva en la base de datos
     * 6. Retorna la reserva creada con su código QR
     */
    @PostMapping
    public ResponseEntity<?> createReserva(@RequestBody Reserva reserva) {
        logger.info("=== CREAR RESERVA ===");
        logger.info("Fecha: {}", reserva.getFecha());
        logger.info("Hora inicio: {}", reserva.getHoraInicio());
        logger.info("Hora fin: {}", reserva.getHoraFin());
        logger.info("ID usuario recibido: {}", reserva.getIdUsuario() != null ? reserva.getIdUsuario().getIdUsuario() : null);
        logger.info("ID pista recibido: {}", reserva.getIdPista() != null ? reserva.getIdPista().getIdPista() : null);

        // Verificar que el usuario existe
        if (reserva.getIdUsuario() == null || reserva.getIdUsuario().getIdUsuario() == null) {
            logger.warn("Error creando reserva: ID de usuario requerido");
            return ResponseEntity.badRequest().body("ID de usuario requerido");
        }

        Usuario usuario = usuarioRepository.findById(reserva.getIdUsuario().getIdUsuario())
                .orElse(null);
        if (usuario == null) {
            logger.warn("Error creando reserva: Usuario no encontrado con ID {}", reserva.getIdUsuario().getIdUsuario());
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }

        // Verificar que la pista existe
        if (reserva.getIdPista() == null || reserva.getIdPista().getIdPista() == null) {
            logger.warn("Error creando reserva: ID de pista requerido");
            return ResponseEntity.badRequest().body("ID de pista requerido");
        }

        Pista pista = pistaRepository.findById(reserva.getIdPista().getIdPista())
                .orElse(null);
        if (pista == null) {
            logger.warn("Error creando reserva: Pista no encontrada con ID {}", reserva.getIdPista().getIdPista());
            return ResponseEntity.badRequest().body("Pista no encontrada");
        }

        // Generar código QR único para la reserva
        // UUID.randomUUID() genera un identificador único universal
        String codigoQr = UUID.randomUUID().toString();
        reserva.setCodigoQr(codigoQr);
        
        // Mantener la fecha enviada por el cliente; solo poner ahora si no se envía
        if (reserva.getFecha() == null) {
            reserva.setFecha(LocalDateTime.now());
        }
        
        // Establecer el estado de la reserva como ACTIVA
        reserva.setEstadoReserva("ACTIVA");

        // Guardar la reserva en la base de datos
        Reserva reservaGuardada = reservaRepository.save(reserva);
        logger.info("Reserva creada correctamente con ID {}", reservaGuardada.getIdReserva());

        return ResponseEntity.ok(reservaGuardada);
    }

    /**
     * Endpoint para cancelar una reserva
     * DELETE /api/reservas/{id}
     * Requiere token JWT
     * Elimina permanentemente la reserva de la base de datos
     * 
     * @param id ID de la reserva a cancelar
     * @return ResponseEntity con 200 si se canceló correctamente o 404 si no existe
     * 
     * Proceso:
     * 1. Verifica que la reserva existe
     * 2. Si existe, la elimina de la base de datos
     * 3. Si no existe, retorna 404 Not Found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelReserva(@PathVariable Integer id) {
        // Verificar que la reserva existe antes de eliminarla
        if (!reservaRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        // Eliminar la reserva de la base de datos
        reservaRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint para actualizar el estado de una reserva
     * PUT /api/reservas/{id}/estado
     * Requiere token JWT y rol ADMIN
     * Se usa cuando el admin escanea el QR para confirmar el acceso
     * 
     * @param id ID de la reserva a actualizar
     * @param body Map con el nuevo estado (ej: {"estado": "CONFIRMADA"})
     * @return ResponseEntity con la reserva actualizada o error si no existe
     * 
     * Proceso:
     * 1. Verifica que la reserva existe
     * 2. Extrae el nuevo estado del body
     * 3. Valida que el estado sea válido (ACTIVA, CONFIRMADA, CANCELADA)
     * 4. Actualiza el estado de la reserva
     * 5. Guarda los cambios en la base de datos
     * 6. Retorna la reserva actualizada
     */
    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstadoReserva(@PathVariable Integer id, @RequestBody Map<String, String> body) {
        // Verificar que la reserva existe
        Reserva reserva = reservaRepository.findById(id).orElse(null);
        if (reserva == null) {
            return ResponseEntity.notFound().build();
        }

        // Extraer el nuevo estado del body
        String nuevoEstado = body.get("estado");
        if (nuevoEstado == null || nuevoEstado.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Estado requerido");
        }

        // Validar que el estado sea válido
        if (!nuevoEstado.equals("ACTIVA") && !nuevoEstado.equals("CONFIRMADA") && !nuevoEstado.equals("CANCELADA")) {
            return ResponseEntity.badRequest().body("Estado inválido. Valores válidos: ACTIVA, CONFIRMADA, CANCELADA");
        }

        // Actualizar el estado de la reserva
        reserva.setEstadoReserva(nuevoEstado);

        // Guardar los cambios en la base de datos
        Reserva reservaActualizada = reservaRepository.save(reserva);

        return ResponseEntity.ok(reservaActualizada);
    }
}