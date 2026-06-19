package com.paujil.modelo;

/**
 * Modelo para la tabla `trabajos`.
 *
 * Columnas reales (después de eliminar nombre_trabajo):
 *   id_trabajo, descripcion_trabajo, id_tipo_trabajo
 */
public class trabajo {

    private int    id;
    private String descripcion;       // → descripcion_trabajo en BD
    private int    idTipoTrabajo;
    private String nombreTipoTrabajo; // JOIN con tipos_trabajo (solo lectura)

    public trabajo() {}

    public trabajo(String descripcion, int idTipoTrabajo) {
        this.descripcion   = descripcion;
        this.idTipoTrabajo = idTipoTrabajo;
    }

    public trabajo(int id, String descripcion, int idTipoTrabajo) {
        this.id            = id;
        this.descripcion   = descripcion;
        this.idTipoTrabajo = idTipoTrabajo;
    }

    public int    getId()           { return id; }
    public void   setId(int id)     { this.id = id; }

    public String getDescripcion()                   { return descripcion; }
    public void   setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public int  getIdTipoTrabajo()                 { return idTipoTrabajo; }
    public void setIdTipoTrabajo(int idTipoTrabajo) { this.idTipoTrabajo = idTipoTrabajo; }

    public String getNombreTipoTrabajo()                       { return nombreTipoTrabajo; }
    public void   setNombreTipoTrabajo(String nombreTipoTrabajo) { this.nombreTipoTrabajo = nombreTipoTrabajo; }
}