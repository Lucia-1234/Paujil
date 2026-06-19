package com.paujil.modelo; // Define el paquete del modelo.

import java.sql.Date; // Importa clase Date para fechas SQL.

public class cultivo { // Clase modelo para la entidad Cultivo.

    private int idCultivo; // ID primario del cultivo.
    private String nombreCultivo; // Nombre descriptivo del cultivo.
    private String tipoCultivo; // Clasificación del cultivo.
    private Date fechaSiembra; // Fecha de inicio del ciclo.
    private Date fechaCosecha; // Fecha de fin del ciclo.

    public cultivo() {} // Constructor vacío para instanciación.

    // Constructor con campos esenciales para creación.
    public cultivo(String nombreCultivo, String tipoCultivo, Date fechaSiembra, Date fechaCosecha) { // Constructor.
        this.nombreCultivo = nombreCultivo; // Asigna nombre.
        this.tipoCultivo = tipoCultivo; // Asigna tipo.
        this.fechaSiembra = fechaSiembra; // Asigna siembra.
        this.fechaCosecha = fechaCosecha; // Asigna cosecha.
    } // Fin constructor.

    public int getIdCultivo() { return idCultivo; } // Getter idCultivo.
    public void setIdCultivo(int idCultivo) { this.idCultivo = idCultivo; } // Setter idCultivo.

    public String getNombreCultivo() { return nombreCultivo; } // Getter nombre.
    public void setNombreCultivo(String nombreCultivo) { this.nombreCultivo = nombreCultivo; } // Setter nombre.

    public String getTipoCultivo() { return tipoCultivo; } // Getter tipo.
    public void setTipoCultivo(String tipoCultivo) { this.tipoCultivo = tipoCultivo; } // Setter tipo.

    public Date getFechaSiembra() { return fechaSiembra; } // Getter fechaSiembra.
    public void setFechaSiembra(Date fechaSiembra) { this.fechaSiembra = fechaSiembra; } // Setter siembra.

    public Date getFechaCosecha() { return fechaCosecha; } // Getter fechaCosecha.
    public void setFechaCosecha(Date fechaCosecha) { this.fechaCosecha = fechaCosecha; } // Setter cosecha.
} // Fin clase.