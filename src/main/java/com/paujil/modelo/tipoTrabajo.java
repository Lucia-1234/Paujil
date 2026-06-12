package com.paujil.modelo;

public class tipoTrabajo {

    private int    idTipoTrabajo;
    private String nombreTipo;

    public tipoTrabajo() {}

    public tipoTrabajo(int idTipoTrabajo, String nombreTipo) {
        this.idTipoTrabajo = idTipoTrabajo;
        this.nombreTipo    = nombreTipo;
    }

    public int getIdTipoTrabajo() { return idTipoTrabajo; }
    public void setIdTipoTrabajo(int idTipoTrabajo) { this.idTipoTrabajo = idTipoTrabajo; }

    public String getNombreTipo() { return nombreTipo; }
    public void setNombreTipo(String nombreTipo) { this.nombreTipo = nombreTipo; }
}