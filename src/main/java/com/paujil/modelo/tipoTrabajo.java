package com.paujil.modelo; // Define el paquete del modelo.

public class tipoTrabajo { // Clase modelo para la entidad TipoTrabajo.

    private int idTipoTrabajo; // Identificador único del tipo de trabajo.
    private String nombreTipo; // Nombre descriptivo de la categoría.

    public tipoTrabajo() {} // Constructor vacío para instanciación.

    // Constructor con parámetros para inicializar el objeto.
    public tipoTrabajo(int idTipoTrabajo, String nombreTipo) { // Constructor.
        this.idTipoTrabajo = idTipoTrabajo; // Asigna ID.
        this.nombreTipo = nombreTipo; // Asigna nombre.
    } // Fin constructor.

    public int getIdTipoTrabajo() { return idTipoTrabajo; } // Getter ID.
    public void setIdTipoTrabajo(int idTipoTrabajo) { this.idTipoTrabajo = idTipoTrabajo; } // Setter ID.

    public String getNombreTipo() { return nombreTipo; } // Getter nombre.
    public void setNombreTipo(String nombreTipo) { this.nombreTipo = nombreTipo; } // Setter nombre.
} // Fin clase.