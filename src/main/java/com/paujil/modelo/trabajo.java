package com.paujil.modelo;

import java.sql.Date;

public class trabajo {

    // Columnas de la tabla 'trabajos'
    private int    id;
    private String nombre;
    private String descripcion;
    private Date   fechaAsignacion;
    private Date   fechaFinalizacion;
    private String observaciones;


    // Campos extra solo para vistas — se llenan con JOINs en el DAO, no se persisten
    private String nombreUsuario;   // nombre del trabajador asignado
    private String nombreCultivo;   // nombre del cultivo asociado

    // ── Constructores ─────────────────────────────────────────────────────────

    public trabajo() {}

    // Para registrar un trabajo nuevo desde el servlet
    public trabajo(String nombre, String descripcion, Date fechaAsignacion) {
        this.nombre          = nombre;
        this.descripcion     = descripcion;
        this.fechaAsignacion = fechaAsignacion;
    }

    // Para cuando ya se tiene el ID completo (edición)
    public trabajo(int id, String nombre, String descripcion,
                   Date fechaAsignacion, Date fechaFinalizacion, String observaciones) {
        this.id                = id;
        this.nombre            = nombre;
        this.descripcion       = descripcion;
        this.fechaAsignacion   = fechaAsignacion;
        this.fechaFinalizacion = fechaFinalizacion;
        this.observaciones     = observaciones;
    }

    // ── Getters y Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Date getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(Date fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }

    public Date getFechaFinalizacion() { return fechaFinalizacion; }
    public void setFechaFinalizacion(Date fechaFinalizacion) { this.fechaFinalizacion = fechaFinalizacion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    // Campos de vista — solo lectura desde JSPs
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getNombreCultivo() { return nombreCultivo; }
    public void setNombreCultivo(String nombreCultivo) { this.nombreCultivo = nombreCultivo; }
}