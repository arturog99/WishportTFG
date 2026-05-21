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

/**
 * Controlador REST para gestionar los usuarios
 * Expone endpoints para registro, login y gestión de usuarios
 * Base path: /api/usuarios
 */
@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*") // Permite peticiones desde cualquier origen (CORS)
public class UsuarioController {

    /**
     * Repositorio de usuarios para acceder a la base de datos
     * Proporciona métodos CRUD para la entidad Usuario
     */
    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Utilidad para generar y validar tokens JWT
     * Se usa para autenticar a los usuarios mediante tokens
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Codificador de contraseñas con BCrypt
     * Encripta las contraseñas antes de almacenarlas en la BD
     */
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Endpoint para registrar un nuevo usuario
     * POST /api/usuarios/register
     * Público (no requiere token JWT)
     * 
     * @param usuario Objeto Usuario con los datos del nuevo usuario
     * @return ResponseEntity con el usuario creado o error si el email ya existe
     * 
     * Proceso:
     * 1. Verifica si el email ya está registrado
     * 2. Encripta la contraseña con BCrypt
     * 3. Establece el rol por defecto a "USER"
     * 4. Guarda el usuario en la base de datos
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        // Verificar si el email ya existe en la BD para evitar duplicados
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            return ResponseEntity.badRequest().body("El email ya está registrado");
        }

        // Encriptar la password antes de guardarla (seguridad)
        // BCrypt genera un hash único cada vez, incluso para la misma contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        // Por defecto, todos los nuevos usuarios son usuarios normales (no administradores)
        usuario.setRol("USER");

        // Guardar el usuario en la base de datos
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        return ResponseEntity.ok(usuarioGuardado);
    }

    /**
     * Endpoint para crear un nuevo administrador
     * POST /api/usuarios/admin
     * Requiere token JWT de administrador
     * Solo un usuario con rol ADMIN puede crear otros administradores
     * 
     * @param usuario Objeto Usuario con los datos del nuevo administrador
     * @param token Token JWT del usuario que realiza la petición (header Authorization)
     * @return ResponseEntity con el administrador creado o error si no tiene permisos
     * 
     * Proceso:
     * 1. Extrae el email del token JWT
     * 2. Busca al usuario solicitante
     * 3. Verifica que el solicitante tenga rol ADMIN
     * 4. Verifica que el email no exista
     * 5. Encripta la contraseña
     * 6. Establece el rol a "ADMIN"
     * 7. Guarda el nuevo administrador
     */
    @PostMapping("/admin")
    public ResponseEntity<?> crearAdmin(@RequestBody Usuario usuario, @RequestHeader("Authorization") String token) {
        // Extraer el email del token JWT eliminando el prefijo "Bearer "
        String email = jwtUtil.extractEmail(token.replace("Bearer ", ""));
        
        // Buscar al usuario solicitante en la base de datos
        Optional<Usuario> solicitanteOpt = usuarioRepository.findByEmail(email);
        
        if (solicitanteOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }
        
        Usuario solicitante = solicitanteOpt.get();
        
        // Verificar que el solicitante sea administrador
        // Solo los administradores pueden crear otros administradores
        if (!"ADMIN".equals(solicitante.getRol())) {
            return ResponseEntity.status(403).body("Solo administradores pueden crear administradores");
        }
        
        // Verificar si el email ya está registrado
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            return ResponseEntity.badRequest().body("El email ya está registrado");
        }
        
        // Encriptar la contraseña antes de guardarla
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        
        // Establecer el rol a ADMIN
        usuario.setRol("ADMIN");
        
        // Guardar el nuevo administrador en la base de datos
        Usuario adminGuardado = usuarioRepository.save(usuario);
        return ResponseEntity.ok(adminGuardado);
    }

    /**
     * Endpoint para hacer login (inicio de sesión)
     * POST /api/usuarios/login
     * Público (no requiere token JWT)
     * 
     * @param credentials Map con email y password
     * @return ResponseEntity con token JWT y datos del usuario, o error si credenciales incorrectas
     * 
     * Proceso:
     * 1. Extrae email y password del cuerpo de la petición
     * 2. Busca usuario por email en la BD
     * 3. Verifica que la contraseña coincida (compara hash BCrypt)
     * 4. Genera token JWT con email, ID y rol
     * 5. Devuelve token y datos del usuario
     * 
     * El token JWT se usa para autenticar futuras peticiones
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        // Extraer email y password del mapa de credenciales
        String email = credentials.get("email");
        String password = credentials.get("password");

        // Buscar usuario por email en la base de datos
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

        // Si no se encuentra el usuario, retornar error
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar que la password coincida
        // BCrypt.matches() compara la password introducida con el hash almacenado
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            return ResponseEntity.badRequest().body("Password incorrecta");
        }

        // Generar token JWT con los datos del usuario
        // El token incluye email, ID de usuario y rol
        String token = jwtUtil.generateToken(usuario.getEmail(), usuario.getIdUsuario(), usuario.getRol());

        // Construir respuesta con token y datos del usuario
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
     * 
     * @param id ID del usuario a buscar
     * @return ResponseEntity con el usuario encontrado o 404 si no existe
     * 
     * Proceso:
     * 1. Busca usuario por ID en la base de datos
     * 2. Si existe, retorna los datos del usuario
     * 3. Si no existe, retorna 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUsuarioById(@PathVariable Integer id) {
        // Buscar usuario por ID en la base de datos
        Optional<Usuario> usuario = usuarioRepository.findById(id);

        // Si no se encuentra el usuario, retornar 404
        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Retornar el usuario encontrado
        return ResponseEntity.ok(usuario.get());
    }
}