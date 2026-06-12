package com.paujil.modelo;

import java.sql.Date;

public class asignacion {

    private int    id;
    private int    idTrabajo;
    private int    idCultivo;
    private int    idUsuario;
    private Date   fechaAsignacion;
    private Date   fechaInicio;
    private Date   fechaFinalizacion;
    private String estadoTrabajo;
    private String observaciones;

    // Campos de solo lectura, llenados por JOIN
    private String nombreTrabajo;
    private String descripcionTrabajo;
    private String nombreCultivo;
    private String nombreUsuario;
    private String nombreTipoTrabajo;

    public asignacion() {}

    // Constructor para crear una asignación nueva (estado por defecto: Pendiente)
    public asignacion(int idTrabajo, int idCultivo, int idUsuario, Date fechaAsignacion) {
        this.idTrabajo       = idTrabajo;
        this.idCultivo       = idCultivo;
        this.idUsuario       = idUsuario;
        this.fechaAsignacion = fechaAsignacion;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdTrabajo() { return idTrabajo; }
    public void setIdTrabajo(int idTrabajo) { this.idTrabajo = idTrabajo; }

    public int getIdCultivo() { return idCultivo; }
    public void setIdCultivo(int idCultivo) { this.idCultivo = idCultivo; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public Date getFechaAsignacion() { return fechaAsignacion; }
    public void setFechaAsignacion(Date fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }

    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    public Date getFechaFinalizacion() { return fechaFinalizacion; }
    public void setFechaFinalizacion(Date fechaFinalizacion) { this.fechaFinalizacion = fechaFinalizacion; }

    public String getEstadoTrabajo() { return estadoTrabajo; }
    public void setEstadoTrabajo(String estadoTrabajo) { this.estadoTrabajo = estadoTrabajo; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    // Solo lectura (JOINs)
    public String getNombreTrabajo() { return nombreTrabajo; }
    public void setNombreTrabajo(String nombreTrabajo) { this.nombreTrabajo = nombreTrabajo; }

    public String getDescripcionTrabajo() { return descripcionTrabajo; }
    public void setDescripcionTrabajo(String descripcionTrabajo) { this.descripcionTrabajo = descripcionTrabajo; }

    public String getNombreCultivo() { return nombreCultivo; }
    public void setNombreCultivo(String nombreCultivo) { this.nombreCultivo = nombreCultivo; }

    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    public String getNombreTipoTrabajo() { return nombreTipoTrabajo; }
    public void setNombreTipoTrabajo(String nombreTipoTrabajo) { this.nombreTipoTrabajo = nombreTipoTrabajo; }
}