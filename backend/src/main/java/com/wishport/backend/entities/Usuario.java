package com.wishport.backend.entities;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity

@Table(name = "usuarios")

public class Usuario {

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "id_usuario")

    private Integer idUsuario;

    @Column(name = "nombre", nullable = false)

    private String nombre;

    @Column(name = "email", nullable = false, unique = true)

    private String email;

    @Column(name = "password", nullable = false)

    private String password;

    @Column(name = "telefono", nullable = false)

    private String telefono;

    @Column(name = "rol")

    private String rol = "USER";

    @JsonIgnore

    @OneToMany(mappedBy = "idUsuario", cascade = CascadeType.ALL)

    private java.util.List<Reserva> reservas;

    public Usuario() {}

    public Usuario(String nombre, String email, String password, String telefono) {

        this.nombre = nombre;

        this.email = email;

        this.password = password;

        this.telefono = telefono;

        this.rol = "USER";

    }

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
