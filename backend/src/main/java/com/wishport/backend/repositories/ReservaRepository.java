package com.wishport.backend.repositories;

import com.wishport.backend.entities.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Integer> {

    // Buscar reservas por usuario
    List<Reserva> findByIdUsuario_IdUsuario(Integer idUsuario);

    // Buscar reservas por pista
    List<Reserva> findByIdPista_IdPista(Integer idPista);

    // Buscar reservas por estado
    List<Reserva> findByEstadoReserva(String estadoReserva);


}