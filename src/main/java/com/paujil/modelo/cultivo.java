package com.paujil.modelo;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class cultivo {
    private int idCultivo;
    private String nombreCultivo;
    private String tipoCultivo;
    private Date fechaSiembra;
    private Date fechaCosecha;
    
    // Ahora usaremos una lista para manejar la relación con Biopreparados
    private List<String> biopreparados; 

    public cultivo() {
        this.biopreparados = new ArrayList<>();
    }
    
    // Constructor limpio
    public cultivo(String nombreCultivo, String tipoCultivo, Date fechaSiembra, Date fechaCosecha) {
        this.nombreCultivo = nombreCultivo;
        this.tipoCultivo = tipoCultivo;
        this.fechaSiembra = fechaSiembra;
        this.fechaCosecha = fechaCosecha;
        this.biopreparados = new ArrayList<>();
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

    // Métodos para manejar la relación de biopreparados
    public List<String> getBiopreparados() { return biopreparados; }
    public void addBiopreparado(String nombre) { this.biopreparados.add(nombre); }
}