package com.paujil.modelo;

import java.sql.Date;
import java.math.BigDecimal;

public class cultivo {
    // 1. Atributos Privados (Mapeo milimétrico de la tabla 'cultivos')
    private int idCultivo;
    private String nombreCultivo;
    private String tipoCultivo;
    private Date fechaSiembra;
    private Date fechaCosecha; // Admite valores nulos en BD si no se ha recolectado
    private BigDecimal area;   // Usamos BigDecimal para no perder precisión con las hectáreas

    // 2. Atributos Auxiliares de Relación
    // No pertenecen a la tabla 'cultivos' en sí, pero sirven para pintar el Lote 
    // y el Tratamiento directamente en las tarjetas (.crop-card) del Frontend.
    private String nombreLoteAux;
    private String nombreInsumoAux;

    // 3. Constructor Vacío (Obligatorio para la arquitectura Java Web / Servlets)
    public cultivo() {}

    // 4. Constructor Lleno (Útil para operaciones de inserción y clonación de datos)
    public cultivo(String nombreCultivo, String tipoCultivo, Date fechaSiembra, Date fechaCosecha, BigDecimal area) {
        this.nombreCultivo = nombreCultivo;
        this.tipoCultivo = tipoCultivo;
        this.fechaSiembra = fechaSiembra;
        this.fechaCosecha = fechaCosecha;
        this.area = area;
    }

    // =================================================================
    // 5. Métodos Getters y Setters (Encapsulamiento Obligatorio)
    // =================================================================
    
    public int getIdCultivo() {
        return idCultivo;
    }

    public void setIdCultivo(int idCultivo) {
        this.idCultivo = idCultivo;
    }

    public String getNombreCultivo() {
        return nombreCultivo;
    }

    public void setNombreCultivo(String nombreCultivo) {
        this.nombreCultivo = nombreCultivo;
    }

    public String getTipoCultivo() {
        return tipoCultivo;
    }

    public void setTipoCultivo(String tipoCultivo) {
        this.tipoCultivo = tipoCultivo;
    }

    public Date getFechaSiembra() {
        return fechaSiembra;
    }

    public void setFechaSiembra(Date fechaSiembra) {
        this.fechaSiembra = fechaSiembra;
    }

    public Date getFechaCosecha() {
        return fechaCosecha;
    }

    public void setFechaCosecha(Date fechaCosecha) {
        this.fechaCosecha = fechaCosecha;
    }

    public BigDecimal getArea() {
        return area;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    // =================================================================
    // Getters y Setters de los Campos Auxiliares (Para los JOINs del DAO)
    // =================================================================

    public String getNombreLoteAux() {
        return nombreLoteAux;
    }

    public void setNombreLoteAux(String nombreLoteAux) {
        this.nombreLoteAux = nombreLoteAux;
    }

    public String getNombreInsumoAux() {
        return nombreInsumoAux;
    }

    public void setNombreInsumoAux(String nombreInsumoAux) {
        this.nombreInsumoAux = nombreInsumoAux;
    }
}