package com.paujil.modelo;

import java.sql.Date;

public class registros {

    // Campos que reflejan exactamente las columnas de 'trabajos_realizados'
    private int    idTrabajoRealizado;   // id_trabajo_realizado (PK)
    private int    idCultivo;            // id_cultivo (FK)
    private String descripcionTrabajo;   // descripcion_trabajo
    private int    idUsuario;            // id_usuario (FK) — reemplaza 'responsable' String
    private Date   fechaInicio;          // fecha_inicio
    private Date   fechaFinalizo;        // fecha_finalizo
    private String observaciones;        // observaciones (nullable)

    // Campo extra solo para vistas (no se persiste en BD)
    // Se llena con un JOIN en el DAO al listar
    private String nombreUsuario;

    // ── Constructores ─────────────────────────────────────────────────────────

    public registros() {}

    // Constructor para insertar un registro nuevo
    public registros(int idCultivo, String descripcionTrabajo, int idUsuario,
                     Date fechaInicio, Date fechaFinalizo, String observaciones) {
        this.idCultivo          = idCultivo;
        this.descripcionTrabajo = descripcionTrabajo;
        this.idUsuario          = idUsuario;
        this.fechaInicio        = fechaInicio;
        this.fechaFinalizo      = fechaFinalizo;
        this.observaciones      = observaciones;
    }

    // ── Getters y Setters ─────────────────────────────────────────────────────

    public int getIdTrabajoRealizado() { return idTrabajoRealizado; }
    public void setIdTrabajoRealizado(int idTrabajoRealizado) {
        this.idTrabajoRealizado = idTrabajoRealizado;
    }

    public int getIdCultivo() { return idCultivo; }
    public void setIdCultivo(int idCultivo) { this.idCultivo = idCultivo; }

    public String getDescripcionTrabajo() { return descripcionTrabajo; }
    public void setDescripcionTrabajo(String descripcionTrabajo) {
        this.descripcionTrabajo = descripcionTrabajo;
    }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public Date getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    public Date getFechaFinalizo() { return fechaFinalizo; }
    public void setFechaFinalizo(Date fechaFinalizo) { this.fechaFinalizo = fechaFinalizo; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    // Solo lectura — se asigna desde el DAO al hacer JOIN con usuarios
    public String getNombreUsuario() { return nombreUsuario; }
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
}