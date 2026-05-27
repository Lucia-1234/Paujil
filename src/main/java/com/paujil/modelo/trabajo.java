
package com.paujil.modelo;

import java.sql.Date;

public class trabajo {
    private int id;
    private String nombre;
    private String descripcion;
    private Date fechaAsignacion;
    private Date fechaFinalizacion;
    private String observaciones;

    // Constructor vacío
    public trabajo() {
    }

    // Constructor completo para crear un nuevo trabajo
    public trabajo(String nombre, String descripcion, Date fechaAsignacion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaAsignacion = fechaAsignacion;
    }

    // Constructor para cuando ya tienes el ID (ej. al editar)
    public trabajo(int id, String nombre, String descripcion, Date fechaAsignacion, Date fechaFinalizacion, String observaciones) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaAsignacion = fechaAsignacion;
        this.fechaFinalizacion = fechaFinalizacion;
        this.observaciones = observaciones;
    }

    // Getters y Setters
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
}