package com.paujil.modelo;

import java.sql.Date; // Cambiado para manejar el tipo DATE de SQL de forma nativa
import java.sql.Timestamp; 

public class usuario {
    private int id;
    private String nombre;
    private Date fechaNacimiento; 
    private String direccion;
    private String telefono;
    private String correo;
    private String password;
    private Timestamp fechaRegistro;
    private int activo;
    
    public usuario (){}
    
    // Constructor adaptado (reemplazando edad por fechaNacimiento)
    public usuario(String nombre, Date fechaNacimiento, String direccion, String telefono, String correo, String password) {
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion = direccion;
        this.telefono = telefono;
        this.correo = correo;
        this.password = password;
    }

    // Getters y Setters actualizados
    public int getIdUsuario() { return id; }
    public void setIdUsuario(int idUsuario) { this.id = idUsuario; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    // Getter y Setter específicos de la Fecha de Nacimiento
    public Date getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(Date fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    
    public String getDireccion(){ return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    
    public String getTelefono(){ return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    
    public String getCorreo(){ return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public Timestamp getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(Timestamp fechaRegistro) { this.fechaRegistro = fechaRegistro; }
    
    public int getActivo() { return activo; }
    public void setActivo(int activo) { this.activo = activo; }
}