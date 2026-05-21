package com.wishport.backend.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad que representa una reserva de pista deportiva
 * Mapea con la tabla "reservas" de la base de datos MySQL
 * Representa una reserva realizada por un usuario para una pista específica
 */
@Entity
@Table(name = "reservas")
public class Reserva {

    /**
     * ID autogenerado por la base de datos
     * Identificador único de la reserva
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_reserva")
    private Integer idReserva;

    /**
     * Fecha y hora de la reserva (obligatorio)
     * Utiliza LocalDateTime para almacenar fecha y hora
     */
    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    /**
     * Hora de inicio de la reserva (obligatorio)
     * Formato: "HH:mm" (ejemplo: "10:00")
     */
    @Column(name = "hora_inicio", nullable = false)
    private String horaInicio;

    /**
     * Hora de fin de la reserva (obligatorio)
     * Formato: "HH:mm" (ejemplo: "11:00")
     */
    @Column(name = "hora_fin", nullable = false)
    private String horaFin;

    /**
     * Código QR único de la reserva (obligatorio y único)
     * Generado con UUID para garantizar unicidad
     * Se usa para validar el acceso a la pista
     */
    @Column(name = "codigo_qr", nullable = false, unique = true)
    private String codigoQr;

    /**
     * Estado de la reserva (por defecto ACTIVA)
     * Puede ser "ACTIVA" o "CANCELADA"
     */
    @Column(name = "estado_reserva")
    private String estadoReserva = "ACTIVA";

    /**
     * Relación muchos a uno con Pista
     * Una reserva pertenece a una pista
     */
    @ManyToOne
    @JoinColumn(name = "id_pista", nullable = false)
    private Pista idPista;

    /**
     * Relación muchos a uno con Usuario
     * Una reserva pertenece a un usuario
     */
    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario idUsuario;

    /**
     * Constructor vacío requerido por JPA/Hibernate
     * JPA necesita un constructor vacío para instanciar entidades
     */
    public Reserva() {}

    /**
     * Constructor para crear nuevas reservas
     * @param fecha Fecha y hora de la reserva
     * @param horaInicio Hora de inicio (formato "HH:mm")
     * @param horaFin Hora de fin (formato "HH:mm")
     * @param codigoQr Código QR único generado
     * @param idPista Pista reservada
     * @param idUsuario Usuario que realiza la reserva
     * El estado se establece por defecto a "ACTIVA"
     */
    public Reserva(LocalDateTime fecha, String horaInicio, String horaFin, String codigoQr, Pista idPista, Usuario idUsuario) {
        this.fecha = fecha;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.codigoQr = codigoQr;
        this.idPista = idPista;
        this.idUsuario = idUsuario;
        this.estadoReserva = "ACTIVA";
    }

    /**
     * Obtiene el ID de la reserva
     * @return ID único autogenerado por la base de datos
     */
    public Integer getIdReserva() { return idReserva; }

    /**
     * Establece el ID de la reserva
     * @param idReserva ID único de la reserva
     */
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }

    /**
     * Obtiene la fecha de la reserva
     * @return Fecha y hora de la reserva
     */
    public LocalDateTime getFecha() { return fecha; }

    /**
     * Establece la fecha de la reserva
     * @param fecha Fecha y hora de la reserva
     */
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    /**
     * Obtiene la hora de inicio de la reserva
     * @return Hora de inicio (formato "HH:mm")
     */
    public String getHoraInicio() { return horaInicio; }

    /**
     * Establece la hora de inicio de la reserva
     * @param horaInicio Hora de inicio (formato "HH:mm")
     */
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }

    /**
     * Obtiene la hora de fin de la reserva
     * @return Hora de fin (formato "HH:mm")
     */
    public String getHoraFin() { return horaFin; }

    /**
     * Establece la hora de fin de la reserva
     * @param horaFin Hora de fin (formato "HH:mm")
     */
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }

    /**
     * Obtiene el código QR de la reserva
     * @return Código QR único
     */
    public String getCodigoQr() { return codigoQr; }

    /**
     * Establece el código QR de la reserva
     * @param codigoQr Código QR único
     */
    public void setCodigoQr(String codigoQr) { this.codigoQr = codigoQr; }

    /**
     * Obtiene el estado de la reserva
     * @return Estado de la reserva ("ACTIVA" o "CANCELADA")
     */
    public String getEstadoReserva() { return estadoReserva; }

    /**
     * Establece el estado de la reserva
     * @param estadoReserva Estado de la reserva ("ACTIVA" o "CANCELADA")
     */
    public void setEstadoReserva(String estadoReserva) { this.estadoReserva = estadoReserva; }

    /**
     * Obtiene la pista asociada a la reserva
     * @return Pista reservada
     */
    public Pista getIdPista() { return idPista; }

    /**
     * Establece la pista asociada a la reserva
     * @param idPista Pista reservada
     */
    public void setIdPista(Pista idPista) { this.idPista = idPista; }

    /**
     * Obtiene el usuario asociado a la reserva
     * @return Usuario que realizó la reserva
     */
    public Usuario getIdUsuario() { return idUsuario; }

    /**
     * Establece el usuario asociado a la reserva
     * @param idUsuario Usuario que realizó la reserva
     */
    public void setIdUsuario(Usuario idUsuario) { this.idUsuario = idUsuario; }

}
