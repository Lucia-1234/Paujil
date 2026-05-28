package com.paujil.modelo;

import java.sql.Date;

public class usuario {

    // Columnas de la tabla 'usuarios'
    private int    id;
    private String nombre;
    private Date   fechaNacimiento;
    private String direccion;
    private String password;
    private String estado;

    // Campos de tablas relacionadas — se llenan con JOINs en el DAO, no se persisten
    private String correo;
    private String telefono;
    private String rol;           // nombre_rol desde la tabla 'roles'

    // ── Constructores ─────────────────────────────────────────────────────────

    public usuario() {}

    public usuario(String nombre, Date fechaNacimiento, String direccion,
                   String telefono, String correo, String password) {
        this.nombre          = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion       = direccion;
        this.telefono        = telefono;
        this.correo          = correo;
        this.password        = password;
    }

    // ── Getters y Setters ─────────────────────────────────────────────────────

    public int getIdUsuario() { return id; }
    public void setIdUsuario(int idUsuario) { this.id = idUsuario; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Date getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(Date fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    // Campos de vista
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}