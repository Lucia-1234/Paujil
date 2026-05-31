package com.paujil.modelo;

import java.sql.Date;

public class registros {

    // PK autogenerada por la BD; no se incluye en el constructor de insercion porque la asigna el motor SQL
    private int    idTrabajoRealizado;   
    // FK hacia la tabla 'cultivos'; vincula el trabajo registrado con el cultivo sobre el que se ejecuto
    private int    idCultivo;           
    // Texto libre que describe la actividad agricola realizada (ej: "aplicacion de fungicida", "poda")
    private String descripcionTrabajo;  
    // FK hacia la tabla 'usuarios'; sustituye el campo String 'responsable' para garantizar integridad referencial
    private int    idUsuario;           
    // Fecha en que inicio la actividad; usada para calcular duracion y ordenar cronologicamente los trabajos
    private Date   fechaInicio;          
    // Fecha en que concluyo la actividad; null indicaria trabajo aun en progreso segun logica de negocio
    private Date   fechaFinalizo;        
    // Notas adicionales sobre el trabajo; columna nullable en BD, puede ser null sin romper la entidad
    private String observaciones;      

    // Campo de proyeccion de vista: almacena el nombre del usuario obtenido via JOIN en el DAO; no tiene columna propia en 'trabajos_realizados' y nunca se persiste
    private String nombreUsuario;

    //  Constructores 

    // Constructor vacio requerido para instanciacion por reflexion y para poblar el objeto campo a campo tras una consulta
    public registros() {}

    // Constructor de insercion: omite idTrabajoRealizado (lo genera la BD) y nombreUsuario (es solo de lectura para vistas)
    public registros(int idCultivo, String descripcionTrabajo, int idUsuario,
                     Date fechaInicio, Date fechaFinalizo, String observaciones) {
        this.idCultivo          = idCultivo;
        this.descripcionTrabajo = descripcionTrabajo;
        // Asigna la FK del usuario responsable en lugar de un nombre en texto plano para mantener integridad referencial
        this.idUsuario          = idUsuario;
        this.fechaInicio        = fechaInicio;
        this.fechaFinalizo      = fechaFinalizo;
        // Puede recibir null si no hay notas adicionales; la BD acepta este campo como opcional
        this.observaciones      = observaciones;
    }

    //  Getters y Setters 

    // Retorna la PK del trabajo; usada como referencia en operaciones de UPDATE, DELETE o como parametro de consultas por id
    public int getIdTrabajoRealizado() { return idTrabajoRealizado; }
    // Asigna la PK generada por la BD tras el INSERT, permitiendo que el objeto refleje su identidad persistida
    public void setIdTrabajoRealizado(int idTrabajoRealizado) {
        this.idTrabajoRealizado = idTrabajoRealizado;
    }

    // Expone la FK del cultivo para vincular el trabajo en consultas relacionadas o validaciones de pertenencia
    public int getIdCultivo() { return idCultivo; }
    // Permite reasignar el cultivo asociado ante correcciones de datos o reasignacion del trabajo
    public void setIdCultivo(int idCultivo) { this.idCultivo = idCultivo; }

    // Retorna la descripcion del trabajo para mostrar en vistas de detalle o listados de actividades
    public String getDescripcionTrabajo() { return descripcionTrabajo; }
    // Actualiza la descripcion en flujos de edicion del registro desde el Servlet
    public void setDescripcionTrabajo(String descripcionTrabajo) {
        this.descripcionTrabajo = descripcionTrabajo;
    }

    // Expone la FK del usuario responsable; util para filtrar trabajos por usuario o validar permisos de edicion
    public int getIdUsuario() { return idUsuario; }
    // Permite reasignar la responsabilidad del trabajo a otro usuario registrado en el sistema
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    // Retorna la fecha de inicio para calculos de duracion, filtros por rango de fechas o visualizacion en cronogramas
    public Date getFechaInicio() { return fechaInicio; }
    // Asigna la fecha de inicio al mapear el ResultSet o procesar el formulario del Servlet
    public void setFechaInicio(Date fechaInicio) { this.fechaInicio = fechaInicio; }

    // Retorna la fecha de finalizacion; si es null puede interpretarse como trabajo pendiente segun reglas de negocio
    public Date getFechaFinalizo() { return fechaFinalizo; }
    // Permite registrar o corregir la fecha de cierre del trabajo tras su ejecucion real
    public void setFechaFinalizo(Date fechaFinalizo) { this.fechaFinalizo = fechaFinalizo; }

    // Expone las observaciones opcionales para mostrar contexto adicional del trabajo en la vista de detalle
    public String getObservaciones() { return observaciones; }
    // Actualiza o limpia las notas del trabajo; acepta null porque el campo es nullable en la BD
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    // Getter de solo lectura conceptual: el DAO lo llena via JOIN con 'usuarios'; nunca proviene de columna propia en 'trabajos_realizados'
    public String getNombreUsuario() { return nombreUsuario; }
    // Setter usado exclusivamente por el DAO al mapear el ResultSet del JOIN; no debe invocarse desde logica de negocio ni formularios
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }
}