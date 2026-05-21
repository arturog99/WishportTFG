package com.wishport.backend.entities;

import jakarta.persistence.*;

/**
 * Entidad que representa una pista deportiva en el sistema
 * Mapea con la tabla "pistas" de la base de datos MySQL
 * Representa las instalaciones deportivas disponibles para reservar
 */
@Entity
@Table(name = "pistas")
public class Pista {

    /**
     * ID autogenerado por la base de datos
     * Identificador único de la pista
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pista")
    private Integer idPista;

    /**
     * Nombre de la pista (obligatorio)
     * Ejemplo: "Pista Central Pádel 1"
     */
    @Column(name = "nombre", nullable = false)
    private String nombre;

    /**
     * Tipo de deporte de la pista (obligatorio)
     * Ejemplo: "Pádel", "Fútbol 7", "Baloncesto"
     */
    @Column(name = "deporte", nullable = false)
    private String deporte;

    /**
     * URL de la foto de la pista
     * Ruta donde se almacena la imagen de la instalación
     */
    @Column(name = "foto_url")
    private String fotoUrl;

    /**
     * Estado de la pista (por defecto ACTIVA)
     * Puede ser "ACTIVA" o "MANTENIMIENTO"
     */
    @Column(name = "estado")
    private String estado = "ACTIVA";

    /**
     * Constructor vacío requerido por JPA/Hibernate
     * JPA necesita un constructor vacío para instanciar entidades
     */
    public Pista() {}

    /**
     * Constructor para crear nuevas pistas
     * @param nombre Nombre de la pista
     * @param deporte Tipo de deporte
     * @param fotoUrl URL de la foto de la pista
     * El estado se establece por defecto a "ACTIVA"
     */
    public Pista(String nombre, String deporte, String fotoUrl) {
        this.nombre = nombre;
        this.deporte = deporte;
        this.fotoUrl = fotoUrl;
        this.estado = "ACTIVA";
    }

    /**
     * Obtiene el ID de la pista
     * @return ID único autogenerado por la base de datos
     */
    public Integer getIdPista() { return idPista; }

    /**
     * Establece el ID de la pista
     * @param idPista ID único de la pista
     */
    public void setIdPista(Integer idPista) { this.idPista = idPista; }

    /**
     * Obtiene el nombre de la pista
     * @return Nombre de la pista
     */
    public String getNombre() { return nombre; }

    /**
     * Establece el nombre de la pista
     * @param nombre Nombre de la pista
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Obtiene el tipo de deporte de la pista
     * @return Tipo de deporte
     */
    public String getDeporte() { return deporte; }

    /**
     * Establece el tipo de deporte de la pista
     * @param deporte Tipo de deporte
     */
    public void setDeporte(String deporte) { this.deporte = deporte; }

    /**
     * Obtiene la URL de la foto de la pista
     * @return URL de la foto
     */
    public String getFotoUrl() { return fotoUrl; }

    /**
     * Establece la URL de la foto de la pista
     * @param fotoUrl URL de la foto
     */
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    /**
     * Obtiene el estado de la pista
     * @return Estado de la pista ("ACTIVA" o "MANTENIMIENTO")
     */
    public String getEstado() { return estado; }

    /**
     * Establece el estado de la pista
     * @param estado Estado de la pista ("ACTIVA" o "MANTENIMIENTO")
     */
    public void setEstado(String estado) { this.estado = estado; }

}