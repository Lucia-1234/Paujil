package com.paujil.modelo; 
import java.sql.Date; 

public class asignacion { // Clase modelo para la entidad Asignación.

    private int id; // ID único de la asignación.
    private int idTrabajo; // Clave foránea hacia Trabajos.
    private int idCultivo; // Clave foránea hacia Cultivos.
    private int idUsuario; // Clave foránea hacia Usuarios.
    private Date fechaAsignacion; // Fecha en que se asignó.
    private Date fechaInicio; // Fecha real de inicio.
    private Date fechaFinalizacion; // Fecha de finalización.
    private String estadoTrabajo; // Estado actual (Pendiente, etc).
    private String observaciones; // Notas adicionales.

    private String descripcionTrabajo; // Campo leído vía JOIN.
    private String nombreCultivo; // Campo leído vía JOIN.
    private String nombreUsuario; // Campo leído vía JOIN.
    private String nombreTipoTrabajo; // Campo leído vía JOIN.

    public asignacion() {} // Constructor vacío para instanciación.

    // Constructor para inicializar una nueva asignación.
    public asignacion(int idTrabajo, int idCultivo, int idUsuario, Date fechaAsignacion) { // Constructor parámetros.
        this.idTrabajo = idTrabajo; // Asigna idTrabajo.
        this.idCultivo = idCultivo; // Asigna idCultivo.
        this.idUsuario = idUsuario; // Asigna idUsuario.
        this.fechaAsignacion = fechaAsignacion; // Asigna fecha.
    } // Fin constructor.

    public int getId() { return id; } // Getter ID.
    public void setId(int id) { this.id = id; } // Setter ID.

    public int getIdTrabajo() { return idTrabajo; } // Getter idTrabajo.
    public void setIdTrabajo(int idTrabajo) { this.idTrabajo = idTrabajo; } // Setter idTrabajo.

    public int getIdCultivo() { return idCultivo; } // Getter idCultivo.
    public void setIdCultivo(int idCultivo) { this.idCultivo = idCultivo; } // Setter idCultivo.

    public int getIdUsuario() { return idUsuario; } // Getter idUsuario.
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; } // Setter idUsuario.

    public Date getFechaAsignacion() { return fechaAsignacion; } // Getter fechaAsignacion.
    public void setFechaAsignacion(Date fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; } // Setter.

    public Date getFechaInicio() { return fechaInicio; } // Getter fechaInicio.
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; } // Setter.

    public Date getFechaFinalizacion() { return fechaFinalizacion; } // Getter fechaFinalizacion.
    public void setFechaFinalizacion(Date fechaFinalizacion) { this.fechaFinalizacion = fechaFinalizacion; } // Setter.

    public String getEstadoTrabajo() { return estadoTrabajo; } // Getter estado.
    public void setEstadoTrabajo(String estadoTrabajo) { this.estadoTrabajo = estadoTrabajo; } // Setter.

    public String getObservaciones() { return observaciones; } // Getter obs.
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; } // Setter.

    public String getDescripcionTrabajo() { return descripcionTrabajo; } // Getter descTrabajo.
    public void setDescripcionTrabajo(String descripcionTrabajo) { this.descripcionTrabajo = descripcionTrabajo; } // Setter.

    public String getNombreCultivo() { return nombreCultivo; } // Getter nombreCultivo.
    public void setNombreCultivo(String nombreCultivo) { this.nombreCultivo = nombreCultivo; } // Setter.

    public String getNombreUsuario() { return nombreUsuario; } // Getter nombreUsuario.
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; } // Setter.

    public String getNombreTipoTrabajo() { return nombreTipoTrabajo; } // Getter nombreTipo.
    public void setNombreTipoTrabajo(String nombreTipoTrabajo) { this.nombreTipoTrabajo = nombreTipoTrabajo; } // Setter.
} // Fin clase.