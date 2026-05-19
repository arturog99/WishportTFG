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

    // Constructor vacío (requerido por JPA)
    public Usuario() {}

    // Constructor para crear nuevos usuarios
    public Usuario(String nombre, String email, String password, String telefono) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.telefono = telefono;
        this.rol = "USER";
    }

    // Getters y Setters
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public java.util.List<Reserva> getReservas() { return reservas; }
    public void setReservas(java.util.List<Reserva> reservas) { this.reservas = reservas; }
}
