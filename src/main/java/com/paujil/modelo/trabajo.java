package com.paujil.modelo; // Define el paquete del modelo.

/**
 * Modelo para la tabla `trabajos`.
 * Columnas reales: id_trabajo, descripcion_trabajo, id_tipo_trabajo.
 */
public class trabajo { // Clase modelo para la entidad Trabajo.

    private int id; // ID único del trabajo.
    private String descripcion; // Descripción del trabajo (antes descripcion_trabajo).
    private int idTipoTrabajo; // Clave foránea al tipo de trabajo.
    private String nombreTipoTrabajo; // Campo JOIN para lectura.

    public trabajo() {} // Constructor vacío.

    // Constructor para registros nuevos.
    public trabajo(String descripcion, int idTipoTrabajo) { // Constructor.
        this.descripcion = descripcion; // Asigna descripción.
        this.idTipoTrabajo = idTipoTrabajo; // Asigna tipo.
    } // Fin constructor.

    // Constructor completo para mapear objetos desde BD.
    public trabajo(int id, String descripcion, int idTipoTrabajo) { // Constructor.
        this.id = id; // Asigna ID.
        this.descripcion = descripcion; // Asigna descripción.
        this.idTipoTrabajo = idTipoTrabajo; // Asigna tipo.
    } // Fin constructor.

    public int getId() { return id; } // Getter ID.
    public void setId(int id) { this.id = id; } // Setter ID.

    public String getDescripcion() { return descripcion; } // Getter descripción.
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; } // Setter descripción.

    public int getIdTipoTrabajo() { return idTipoTrabajo; } // Getter idTipoTrabajo.
    public void setIdTipoTrabajo(int idTipoTrabajo) { this.idTipoTrabajo = idTipoTrabajo; } // Setter idTipoTrabajo.

    public String getNombreTipoTrabajo() { return nombreTipoTrabajo; } // Getter nombreTipo.
    public void setNombreTipoTrabajo(String nombreTipoTrabajo) { this.nombreTipoTrabajo = nombreTipoTrabajo; } // Setter nombreTipo.
} // Fin clase.