package com.paujil.modelo; // Define el paquete del modelo.

public class lote { // Clase modelo para la entidad Lote.

    private int idLote; // ID primario del lote.
    private String nombreLote; // Nombre identificador del lote.

    public lote() {} // Constructor vacío para instanciación.

    // Constructor con campos esenciales para creación.
    public lote(String nombreLote) { // Constructor.
        this.nombreLote = nombreLote; // Asigna nombre.
    } // Fin constructor.

    public lote(int idLote, String nombreLote) { // Constructor completo, útil para listados con ID ya conocido.
        this.idLote = idLote; // Asigna ID.
        this.nombreLote = nombreLote; // Asigna nombre.
    } // Fin constructor.

    public int getIdLote() { return idLote; } // Getter idLote.
    public void setIdLote(int idLote) { this.idLote = idLote; } // Setter idLote.

    public String getNombreLote() { return nombreLote; } // Getter nombre.
    public void setNombreLote(String nombreLote) { this.nombreLote = nombreLote; } // Setter nombre.

} // Fin clase.