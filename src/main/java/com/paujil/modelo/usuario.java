package com.paujil.modelo; // Define el paquete del modelo.

import java.sql.Date; // Importa clase Date para fechas SQL.

public class usuario { // Clase modelo para la entidad Usuario.

    public int id; // ID del usuario.
    public String nombre; // Nombre completo.
    public Date fechaNacimiento; // Fecha de nacimiento.
    public String direccion; // Dirección física.
    public String password; // Contraseña (hash).
    public String estado; // Estado (Pendiente, Activo, Inactivo).
    public String correo; // Correo electrónico.
    public String telefono; // Número de contacto.
    public String rol; // Rol del sistema.

    public usuario() {} // Constructor vacío.

    // Constructor de registro: incluye datos básicos de contacto.
    public usuario(String nombre, Date fechaNacimiento, String direccion, String telefono, String correo, String password) { // Constructor.
        this.nombre = nombre; // Asigna nombre.
        this.fechaNacimiento = fechaNacimiento; // Asigna fecha.
        this.direccion = direccion; // Asigna dirección.
        this.telefono = telefono; // Asigna teléfono.
        this.correo = correo; // Asigna correo.
        this.password = password; // Asigna password.
    } // Fin constructor.

    public int getIdUsuario() { return id; } // Getter ID semántico.
    public void setIdUsuario(int idUsuario) { this.id = idUsuario; } // Setter ID.

    public String getNombre() { return nombre; } // Getter nombre.
    public void setNombre(String nombre) { this.nombre = nombre; } // Setter nombre.

    public Date getFechaNacimiento() { return fechaNacimiento; } // Getter fecha.
    public void setFechaNacimiento(Date fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; } // Setter fecha.

    public String getDireccion() { return direccion; } // Getter dirección.
    public void setDireccion(String direccion) { this.direccion = direccion; } // Setter dirección.

    public String getPassword() { return password; } // Getter password.
    public void setPassword(String password) { this.password = password; } // Setter password.

    public String getEstado() { return estado; } // Getter estado.
    public void setEstado(String estado) { this.estado = estado; } // Setter estado.

    public String getCorreo() { return correo; } // Getter correo.
    public void setCorreo(String correo) { this.correo = correo; } // Setter correo.

    public String getTelefono() { return telefono; } // Getter teléfono.
    public void setTelefono(String telefono) { this.telefono = telefono; } // Setter teléfono.

    public String getRol() { return rol; } // Getter rol.
    public void setRol(String rol) { this.rol = rol; } // Setter rol.
} // Fin clase.