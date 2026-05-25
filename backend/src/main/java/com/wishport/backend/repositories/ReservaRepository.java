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

    /**
     * Busca reservas existentes que se solapen con el horario propuesto en una pista determinada.
     * Dos reservas se solapan si comparten el mismo día (truncando a fecha) y los rangos
     * [horaInicio, horaFin) se intersectan. Se ignoran las reservas en estado CANCELADA.
     *
     * Condición de solapamiento: nuevaInicio < existenteFin AND nuevaFin > existenteInicio
     */
    @Query("SELECT r FROM Reserva r " +
           "WHERE r.idPista.idPista = :idPista " +
           "AND FUNCTION('DATE', r.fecha) = FUNCTION('DATE', :fecha) " +
           "AND r.estadoReserva <> 'CANCELADA' " +
           "AND r.horaInicio < :horaFin " +
           "AND r.horaFin > :horaInicio")
    List<Reserva> findSolapadas(@Param("idPista") Integer idPista,
                                @Param("fecha") LocalDateTime fecha,
                                @Param("horaInicio") String horaInicio,
                                @Param("horaFin") String horaFin);
}