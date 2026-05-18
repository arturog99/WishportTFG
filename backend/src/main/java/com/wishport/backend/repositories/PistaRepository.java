package com.wishport.backend.repositories;

import com.wishport.backend.entities.Pista;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PistaRepository extends JpaRepository<Pista, Integer> {

    // Buscar pistas por deporte (ej: "Fútbol", "Pádel")
    List<Pista> findByDeporte(String deporte);

    // Buscar pistas activas
    List<Pista> findByEstado(String estado);
}