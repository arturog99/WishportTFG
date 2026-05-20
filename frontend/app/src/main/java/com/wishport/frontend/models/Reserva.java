package com.wishport.frontend.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Reserva implements Serializable {
    private Integer idReserva;
    private LocalDateTime fecha;
    private String horaInicio;
    private String horaFin;
    private String codigoQr;
    private String estadoReserva;
    private Pista idPista;
    private Usuario idUsuario;

    public Reserva() {}

    public Integer getIdReserva() { return idReserva; }
    public void setIdReserva(Integer idReserva) { this.idReserva = idReserva; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getHoraInicio() { return horaInicio; }
    public void setHoraInicio(String horaInicio) { this.horaInicio = horaInicio; }
    public String getHoraFin() { return horaFin; }
    public void setHoraFin(String horaFin) { this.horaFin = horaFin; }
    public String getCodigoQr() { return codigoQr; }
    public void setCodigoQr(String codigoQr) { this.codigoQr = codigoQr; }
    public String getEstadoReserva() { return estadoReserva; }
    public void setEstadoReserva(String estadoReserva) { this.estadoReserva = estadoReserva; }
    public Pista getIdPista() { return idPista; }
    public void setIdPista(Pista idPista) { this.idPista = idPista; }
    public Usuario getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Usuario idUsuario) { this.idUsuario = idUsuario; }

}