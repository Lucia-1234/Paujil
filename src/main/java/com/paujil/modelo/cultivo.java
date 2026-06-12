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
    
    public cultivo() {}

    // Constructor parametrizado con los campos esenciales del cultivo; excluye id porque lo genera la base de datos y biopreparados porque se agregan de forma incremental
    public cultivo(String nombreCultivo, String tipoCultivo, Date fechaSiembra, Date fechaCosecha) {
        this.nombreCultivo = nombreCultivo;
        this.tipoCultivo = tipoCultivo;
        this.fechaSiembra = fechaSiembra;
        this.fechaCosecha = fechaCosecha;

    }

    // Retorna el id de base de datos; necesario para operaciones de UPDATE, DELETE o como FK en tablas relacionadas
    public int getIdCultivo() { return idCultivo; }
    // Permite asignar el id generado por la BD tras un INSERT o al mapear un ResultSet
    public void setIdCultivo(int idCultivo) { this.idCultivo = idCultivo; }

    // Expone el nombre del cultivo para uso en vistas, reportes y validaciones de negocio
    public String getNombreCultivo() { return nombreCultivo; }
    // Actualiza el nombre; util en flujos de edicion desde el Servlet
    public void setNombreCultivo(String nombreCultivo) { this.nombreCultivo = nombreCultivo; }

    // Retorna la categoria del cultivo; usada para filtros, agrupaciones y estadisticas por tipo
    public String getTipoCultivo() { return tipoCultivo; }
    // Permite cambiar la clasificacion del cultivo ante correcciones o reclasificaciones agronomicas
    public void setTipoCultivo(String tipoCultivo) { this.tipoCultivo = tipoCultivo; }

    // Expone la fecha de siembra para calculos de duracion del ciclo o validacion de rangos de fechas
    public Date getFechaSiembra() { return fechaSiembra; }
    // Asigna la fecha de siembra al construir el objeto desde un formulario o resultado de consulta SQL
    public void setFechaSiembra(Date fechaSiembra) { this.fechaSiembra = fechaSiembra; }

    // Retorna la fecha de cosecha; clave para alertas de proximidad de recoleccion y planificacion logistica
    public Date getFechaCosecha() { return fechaCosecha; }
    // Permite actualizar la fecha de cosecha ante cambios de estimacion o ajustes del ciclo real del cultivo
    public void setFechaCosecha(Date fechaCosecha) { this.fechaCosecha = fechaCosecha; }

}