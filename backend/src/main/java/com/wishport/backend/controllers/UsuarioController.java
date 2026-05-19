package com.wishport.backend.controllers;

import com.wishport.backend.entities.Usuario;
import com.wishport.backend.repositories.UsuarioRepository;
import com.wishport.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// Controlador REST para gestionar los usuarios
// Expone endpoints para registro, login y obtención de datos de usuario
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*") // Permite peticiones desde cualquier origen (CORS)
public class UsuarioController {

    // Inyección de dependencias de Spring
    @Autowired
    private UsuarioRepository usuarioRepository; // Repositorio para acceder a la BD

    @Autowired
    private JwtUtil jwtUtil; // Utilidad para generar y validar tokens JWT

    @Autowired
    private BCryptPasswordEncoder passwordEncoder; // Para encriptar passwords

    /**
     * Endpoint para registrar un nuevo usuario
     * POST /api/usuarios/register
     * Público (no requiere token)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        // Verificar si el email ya existe en la BD
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            return ResponseEntity.badRequest().body("El email ya está registrado");
        }

        // Encriptar la password antes de guardarla (seguridad)
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRol("USER"); // Por defecto, todos son usuarios normales

        // Guardar el usuario en la base de datos
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return ResponseEntity.ok(usuarioGuardado);
    }

    /**
     * Endpoint para hacer login
     * POST /api/usuarios/login
     * Público (no requiere token)
     * Devuelve un token JWT si las credenciales son correctas
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        // Buscar usuario por email en la BD
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar que la password coincida (compara la password introducida con la encriptada en BD)
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            return ResponseEntity.badRequest().body("Password incorrecta");
        }

        // Generar token JWT con los datos del usuario
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getIdUsuario(), usuario.getRol());

        // Devolver el token y los datos del usuario
        Map<String, Object> response = new HashMap<>();
        response.put("token", token); // Token para autenticar futuras peticiones
        response.put("idUsuario", usuario.getIdUsuario());
        response.put("nombre", usuario.getNombre());
        response.put("email", usuario.getEmail());
        response.put("rol", usuario.getRol());

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para obtener datos de un usuario por ID
     * GET /api/usuarios/{id}
     * Requiere token JWT
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUsuarioById(@PathVariable Integer id) {
        Optional<Usuario> usuario = usuarioRepository.findById(id);

        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(usuario.get());
    }
}