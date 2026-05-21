package com.wishport.backend.entities;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

// Entidad que representa a un usuario en el sistema
// Mapea con la tabla "usuarios" de la base de datos MySQL
@Entity
@Table(name = "usuarios")
public class Usuario {

    // ID autogenerado por la base de datos
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    // Nombre del usuario (obligatorio)
    @Column(name = "nombre", nullable = false)
    private String nombre;

    // Email único del usuario (obligatorio)
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    // Password encriptada del usuario (obligatorio)
    @Column(name = "password", nullable = false)
    private String password;

    // Teléfono del usuario (obligatorio)
    @Column(name = "telefono", nullable = false)
    private String telefono;

    // Rol del usuario: "USER" o "ADMIN" (por defecto USER)
    @Column(name = "rol")
    private String rol = "USER";

    // Lista de reservas del usuario (relación uno a muchos)
    // @JsonIgnore evita que se envíe en las respuestas JSON para no sobrecargar
    @JsonIgnore
    @OneToMany(mappedBy = "idUsuario", cascade = CascadeType.ALL)
    private java.util.List<Reserva> reservas;

    /**
     * Constructor vacío requerido por JPA/Hibernate
     * JPA necesita un constructor vacío para instanciar entidades
     */
    public Usuario() {}

    /**
     * Constructor para crear nuevos usuarios
     * @param nombre Nombre completo del usuario
     * @param email Email único del usuario
     * @param password Contraseña (debe venir encriptada desde el controlador)
     * @param telefono Número de teléfono del usuario
     * El rol se establece por defecto a "USER"
     */
    public Usuario(String nombre, String email, String password, String telefono) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
        this.rol = "USER";
    }

    /**
     * Obtiene el ID del usuario
     * @return ID único autogenerado por la base de datos
     */
    public Integer getIdUsuario() { return idUsuario; }

    /**
     * Establece el ID del usuario
     * @param idUsuario ID único del usuario
     */
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    /**
     * Obtiene el nombre del usuario
     * @return Nombre completo del usuario
     */
    public String getNombre() { return nombre; }

    /**
     * Establece el nombre del usuario
     * @param nombre Nombre completo del usuario
     */
    public void setNombre(String nombre) { this.nombre = nombre; }

    /**
     * Obtiene el email del usuario
     * @return Email único del usuario
     */
    public String getEmail() { return email; }

    /**
     * Establece el email del usuario
     * @param email Email único del usuario
     */
    public void setEmail(String email) { this.email = email; }

    /**
     * Obtiene la contraseña del usuario (encriptada)
     * @return Contraseña encriptada con BCrypt
     */
    public String getPassword() { return password; }

    /**
     * Establece la contraseña del usuario
     * @param password Contraseña (debe venir encriptada)
     */
    public void setPassword(String password) { this.password = password; }

    /**
     * Obtiene el teléfono del usuario
     * @return Número de teléfono del usuario
     */
    public String getTelefono() { return telefono; }

    /**
     * Establece el teléfono del usuario
     * @param telefono Número de teléfono del usuario
     */
    public void setTelefono(String telefono) { this.telefono = telefono; }

    /**
     * Obtiene el rol del usuario
     * @return Rol del usuario ("USER" o "ADMIN")
     */
    public String getRol() { return rol; }

    /**
     * Establece el rol del usuario
     * @param rol Rol del usuario ("USER" o "ADMIN")
     */
    public void setRol(String rol) { this.rol = rol; }

    /**
     * Obtiene la lista de reservas del usuario
     * @return Lista de reservas asociadas al usuario
     */
    public java.util.List<Reserva> getReservas() { return reservas; }

    /**
     * Establece la lista de reservas del usuario
     * @param reservas Lista de reservas asociadas al usuario
     */
    public void setReservas(java.util.List<Reserva> reservas) { this.reservas = reservas; }
}
