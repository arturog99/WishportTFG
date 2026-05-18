package com.wishport.backend.repositories;

import com.wishport.backend.entities.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Buscar usuario por email
    Optional<Usuario> findByEmail(String email);

    // Buscar usuario por email y password (para login)
    Optional<Usuario> findByEmailAndPassword(String email, String password);

    // Existe usuario con este email?
    boolean existsByEmail(String email);
}