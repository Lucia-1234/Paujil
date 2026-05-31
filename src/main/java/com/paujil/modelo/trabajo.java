package com.paujil.modelo;

import java.sql.Date;

public class trabajo {

    private int    id;
    private String nombre;
    private String descripcion;
    private Date   fechaAsignacion;
    private Date   fechaFinalizacion;
    private String observaciones_trabajo;

    private String nombreUsuario;   
    private String nombreCultivo;   

    // ── Constructores 

    // Constructor vacio requerido por frameworks de reflexion y para instancias que se pueblan campo a campo desde un ResultSet
    public trabajo() {}

    // Constructor minimo para insertar un trabajo nuevo desde el Servlet; omite id (lo genera la BD), fechaFinalizacion (aun no existe) y observaciones (opcionales)
    public trabajo(String nombre, String descripcion, Date fechaAsignacion) {
        this.nombre          = nombre;
        this.descripcion     = descripcion;
        // Registra el momento de asignacion; punto de partida para calcular tiempos de ejecucion y vencimiento
        this.fechaAsignacion = fechaAsignacion;
    }

    // Constructor completo para operaciones de edicion donde ya se conoce el id y todos los campos del registro existente
    public trabajo(int id, String nombre, String descripcion,
                   Date fechaAsignacion, Date fechaFinalizacion, String observaciones) {
        // Asigna el id existente para que las operaciones UPDATE usen la PK correcta en el WHERE
        this.id                = id;
        this.nombre            = nombre;
        this.descripcion       = descripcion;
        this.fechaAsignacion   = fechaAsignacion;
        // Se asigna en edicion porque el trabajo ya tiene o puede tener fecha de cierre registrada
        this.fechaFinalizacion = fechaFinalizacion;
        // El parametro se llama 'observaciones' para simplificar la firma, pero internamente se almacena en 'observaciones_trabajo' para evitar colision de nombres
        this.observaciones_trabajo = observaciones;
    }

    // ── Getters y Setters 

    // Retorna la PK del trabajo; necesaria para construir URLs de edicion, peticiones DELETE y relaciones con otras tablas
    public int getId() { return id; }
    // Asigna el id generado por la BD tras el INSERT o al reconstruir el objeto desde un ResultSet
    public void setId(int id) { this.id = id; }

    // Expone el nombre del trabajo para mostrar en listados, cabeceras de detalle y selectores de asignacion
    public String getNombre() { return nombre; }
    // Permite actualizar el titulo del trabajo en flujos de edicion desde el Servlet
    public void setNombre(String nombre) { this.nombre = nombre; }

    // Retorna la descripcion completa para renderizar en vistas de detalle o instrucciones al trabajador
    public String getDescripcion() { return descripcion; }
    // Actualiza el detalle del trabajo; util al modificar instrucciones sin cambiar la identidad del registro
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    // Expone la fecha de asignacion para calculos de antiguedad, filtros por periodo y ordenamiento cronologico
    public Date getFechaAsignacion() { return fechaAsignacion; }
    // Asigna la fecha de asignacion al mapear el ResultSet o al crear el objeto desde el formulario del Servlet
    public void setFechaAsignacion(Date fechaAsignacion) { this.fechaAsignacion = fechaAsignacion; }

    // Retorna la fecha de finalizacion; un valor null en la vista indica que el trabajo sigue abierto
    public Date getFechaFinalizacion() { return fechaFinalizacion; }
    // Permite registrar el cierre del trabajo o corregir la fecha en una edicion posterior
    public void setFechaFinalizacion(Date fechaFinalizacion) { this.fechaFinalizacion = fechaFinalizacion; }

    // Retorna observaciones desde el campo interno 'observaciones_trabajo'; el nombre del getter es generico para simplificar el acceso desde JSPs
    public String getObservaciones() { return observaciones_trabajo; }
    // El parametro se recibe como 'observaciones' y se almacena en 'observaciones_trabajo' manteniendo consistencia con el nombre del campo privado
    public void setObservaciones(String observaciones) { this.observaciones_trabajo = observaciones; }

    // Retorna el nombre del usuario asignado para mostrar en tablas y vistas sin necesidad de una segunda consulta desde la JSP
    public String getNombreUsuario() { return nombreUsuario; }
    // Setter invocado exclusivamente por el DAO al mapear el JOIN con la tabla 'usuarios'; no debe usarse en logica de negocio
    public void setNombreUsuario(String nombreUsuario) { this.nombreUsuario = nombreUsuario; }

    // Retorna el nombre del cultivo relacionado para presentar contexto agricola del trabajo en la vista sin consultas adicionales
    public String getNombreCultivo() { return nombreCultivo; }
    // Setter invocado exclusivamente por el DAO al mapear el JOIN con la tabla 'cultivos'; campo de solo lectura para las JSPs
    public void setNombreCultivo(String nombreCultivo) { this.nombreCultivo = nombreCultivo; }
}