package com.paujil.modelo;

public enum Rol {
    TRABAJADOR(1, "trabajador"),
    ADMINISTRADOR(2, "administrador");

    private final int idBd;
    private final String nombre;

    Rol(int idBd, String nombre) {
        this.idBd   = idBd;
        this.nombre = nombre;
    }

    public int getIdBd()      { return idBd; }
    public String getNombre() { return nombre; }

    public static Rol desde(String nombre) {
        for (Rol r : values()) {
            if (r.nombre.equalsIgnoreCase(nombre)) return r;
        }
        throw new IllegalArgumentException("Rol inválido: " + nombre);
    }
}
