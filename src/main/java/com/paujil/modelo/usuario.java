package com.paujil.modelo;

import java.sql.Date;

public class usuario {

    public int    id;
    public String nombre;
    public Date   fechaNacimiento;
    public String direccion;
    public String password;
    public String estado;

    public String correo;
    public String telefono;
    public String rol;        

    //  Constructores 

    public usuario() {}

    // Constructor de registro: omite id (lo genera la BD), estado (se asigna por defecto en BD) y rol (se gestiona en tabla separada)
    public usuario(String nombre, Date fechaNacimiento, String direccion,
                   String telefono, String correo, String password) {
        this.nombre          = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.direccion       = direccion;
        // telefono y correo se incluyen aqui aunque puedan venir de JOIN; se aceptan en creacion para persistirlos junto al usuario
        this.telefono        = telefono;
        this.correo          = correo;
        // Se asigna directamente; la responsabilidad de hashear debe recaer en el Servlet o en la capa de servicio antes de invocar este constructor
        this.password        = password;
    }

    //  Getters y Setters 

    // Retorna la PK bajo el alias 'idUsuario' para mantener consistencia con el nombre semantico usado en el resto del sistema
    public int getIdUsuario() { return id; }
    // Asigna el id generado por la BD o recuperado del ResultSet; el parametro usa el nombre largo para claridad en el DAO
    public void setIdUsuario(int idUsuario) { this.id = idUsuario; }

    // Expone el nombre del usuario para vistas, listados de asignacion y cabeceras de sesion
    public String getNombre() { return nombre; }
    // Permite actualizar el nombre en flujos de edicion de perfil desde el Servlet
    public void setNombre(String nombre) { this.nombre = nombre; }

    // Retorna la fecha de nacimiento para mostrar en perfil o calcular edad en reportes demograficos
    public Date getFechaNacimiento() { return fechaNacimiento; }
    // Asigna la fecha de nacimiento al crear o editar el usuario; recibe el tipo Date de SQL directamente
    public void setFechaNacimiento(Date fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }

    // Expone la direccion para vistas de perfil o procesos administrativos que requieran ubicacion del usuario
    public String getDireccion() { return direccion; }
    // Actualiza la direccion del usuario; tipicamente invocado desde el Servlet de edicion de perfil
    public void setDireccion(String direccion) { this.direccion = direccion; }

    // Retorna la contrasena; debe usarse solo en la capa de autenticacion y nunca exponerse en vistas o respuestas JSON
    public String getPassword() { return password; }
    // Asigna la contrasena; el hash deberia aplicarse antes de llamar este setter para no almacenar texto plano
    public void setPassword(String password) { this.password = password; }

    // Expone el estado para que el Servlet o la JSP decidan si el usuario puede acceder al sistema
    public String getEstado() { return estado; }
    // Permite activar o desactivar el usuario sin eliminarlo; preserva historial de trabajos y registros asociados
    public void setEstado(String estado) { this.estado = estado; }

    // Retorna el correo para mostrarlo en el perfil o usarlo como canal de notificaciones y recuperacion de cuenta
    public String getCorreo() { return correo; }
    // Setter invocado por el DAO al mapear el correo desde la columna del JOIN o directamente desde 'usuarios'
    public void setCorreo(String correo) { this.correo = correo; }

    // Expone el telefono de contacto para vistas administrativas o procesos de comunicacion con el usuario
    public String getTelefono() { return telefono; }
    // Permite actualizar el telefono en edicion de perfil; acepta null si el dato es opcional en la BD
    public void setTelefono(String telefono) { this.telefono = telefono; }

    // Retorna el nombre del rol para controlar acceso en JSPs mediante condicionales sobre permisos (ej: "admin", "trabajador")
    public String getRol() { return rol; }
    // Setter invocado exclusivamente por el DAO al resolver el JOIN con la tabla 'roles'; no debe usarse para asignar roles desde logica de negocio
    public void setRol(String rol) { this.rol = rol; }
}