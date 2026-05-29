package com.paujil.modelo;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class biopreparado {

    private int idBiopreparado;
    private String nombre;
    private String descripcion;
    private double precio;
    private Date fechaCreacion;
    private Date fechaVencimiento;
    private String preparacion;
    private List<ingredienteBio> ingredientes;

    // Constructor vacío
    public biopreparado() {
        this.ingredientes = new ArrayList<>();
    }

    // Constructor completo para el Servlet
    public biopreparado(String nombre, String descripcion, double precio, 
                        Date fechaCreacion, Date fechaVencimiento, String preparacion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.fechaCreacion = fechaCreacion;
        this.fechaVencimiento = fechaVencimiento;
        this.preparacion = preparacion;
        this.ingredientes = new ArrayList<>();
    }

    // ── Clase interna para ingredientes ───────────────────────────────────────
    public static class ingredienteBio {
        private int idIngrediente;
        private String nombre;
        private double cantidad;
        private String unidad;

        public ingredienteBio() {}

        // Constructor para el Servlet
        public ingredienteBio(String nombre, double cantidad, String unidad) {
            this.nombre = nombre;
            this.cantidad = cantidad;
            this.unidad = unidad;
        }

        // Getters y Setters
        public int getIdIngrediente() { return idIngrediente; }
        public void setIdIngrediente(int idIngrediente) { this.idIngrediente = idIngrediente; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public double getCantidad() { return cantidad; }
        public void setCantidad(double cantidad) { this.cantidad = cantidad; }
        public String getUnidad() { return unidad; }
        public void setUnidad(String unidad) { this.unidad = unidad; }
    }

    // ── Getters y Setters principales ─────────────────────────────────────────
    public int getIdBiopreparado() { return idBiopreparado; }
    public void setIdBiopreparado(int idBiopreparado) { this.idBiopreparado = idBiopreparado; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    
    public Date getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Date fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    
    public Date getFechaVencimiento() { return fechaVencimiento; }
    public void setFechaVencimiento(Date fechaVencimiento) { this.fechaVencimiento = fechaVencimiento; }
    
    public String getPreparacion() { return preparacion; }
    public void setPreparacion(String preparacion) { this.preparacion = preparacion; }
    
    public List<ingredienteBio> getIngredientes() { return ingredientes; }
    public void setIngredientes(List<ingredienteBio> ingredientes) { this.ingredientes = ingredientes; }
}