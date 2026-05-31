package com.paujil.modelo;

public enum Rol {

    // idBd debe coincidir exactamente con los valores de la tabla 'roles' en BD
    TRABAJADOR(1, "trabajador"),
    ADMINISTRADOR(2, "administrador");

    // idBd es la clave foranea que se inserta en usuario_rol al registrar un usuario
    private final int idBd;
    // nombre es el string que llega del formulario de registro y se compara en desde()
    private final String nombre;

    // Constructor vincula cada constante con su ID de BD y su representacion textual
    Rol(int idBd, String nombre) {
        this.idBd   = idBd;
        this.nombre = nombre;
    }

    // Usado por el DAO para insertar la clave foranea sin hardcodear el entero en el servlet
    public int getIdBd()      { return idBd; }

    // Permite mostrar el nombre del rol en la vista sin exponer la constante del enum directamente
    public String getNombre() { return nombre; }

    // Convierte el string del formulario al enum; centraliza el parseo para no repetirlo en cada servlet
    public static Rol desde(String nombre) {
        for (Rol r : values()) {
            // equalsIgnoreCase tolera variaciones de capitalizacion enviadas desde el formulario
            if (r.nombre.equalsIgnoreCase(nombre)) return r;
        }
        // Excepcion explicita obliga al caller a manejar roles desconocidos en lugar de recibir null
        throw new IllegalArgumentException("Rol invalido: " + nombre);
    }
}