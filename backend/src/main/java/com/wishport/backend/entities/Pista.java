package com.wishport.backend.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "pistas")
public class Pista {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pista")
    private Integer idPista;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "deporte", nullable = false)
    private String deporte;

    @Column(name = "foto_url")
    private String fotoUrl;

    @Column(name = "estado")
    private String estado = "ACTIVA";

    public Pista() {}

    public Pista(String nombre, String deporte, String fotoUrl) {
        this.nombre = nombre;
        this.deporte = deporte;
        this.fotoUrl = fotoUrl;
        this.estado = "ACTIVA";
    }

    public Integer getIdPista() { return idPista; }
    public void setIdPista(Integer idPista) { this.idPista = idPista; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDeporte() { return deporte; }
    public void setDeporte(String deporte) { this.deporte = deporte; }
    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

}