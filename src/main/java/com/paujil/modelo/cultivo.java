package com.paujil.modelo;

import java.sql.Date;

public class cultivo {

    private int idCultivo;
    private String nombreCultivo;
    private String tipoCultivo;
    private Date fechaSiembra;
    private Date fechaCosecha;
    private int idLote; // NUEVO: Campo para establecer la relación 1:N

    public cultivo() {}

    // Constructor actualizado que incluye idLote
    public cultivo(String nombreCultivo, String tipoCultivo, Date fechaSiembra, Date fechaCosecha, int idLote) {
        this.nombreCultivo = nombreCultivo;
        this.tipoCultivo = tipoCultivo;
        this.fechaSiembra = fechaSiembra;
        this.fechaCosecha = fechaCosecha;
        this.idLote = idLote;
    }

    // Getters y Setters
    public int getIdCultivo() { return idCultivo; }
    public void setIdCultivo(int idCultivo) { this.idCultivo = idCultivo; }

    public String getNombreCultivo() { return nombreCultivo; }
    public void setNombreCultivo(String nombreCultivo) { this.nombreCultivo = nombreCultivo; }

    public String getTipoCultivo() { return tipoCultivo; }
    public void setTipoCultivo(String tipoCultivo) { this.tipoCultivo = tipoCultivo; }

    public Date getFechaSiembra() { return fechaSiembra; }
    public void setFechaSiembra(Date fechaSiembra) { this.fechaSiembra = fechaSiembra; }

    public Date getFechaCosecha() { return fechaCosecha; }
    public void setFechaCosecha(Date fechaCosecha) { this.fechaCosecha = fechaCosecha; }

    // NUEVO: Métodos para manejar el idLote
    public int getIdLote() { return idLote; }
    public void setIdLote(int idLote) { this.idLote = idLote; }
}