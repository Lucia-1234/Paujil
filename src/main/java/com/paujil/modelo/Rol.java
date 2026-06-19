package com.paujil.modelo; 

public enum Rol { // Definición del tipo enumerado para roles de usuario.

    TRABAJADOR(1, "trabajador"), // Constante TRABAJADOR con ID 1 y nombre técnico.
    ADMINISTRADOR(2, "administrador"); // Constante ADMINISTRADOR con ID 2 y nombre técnico.

    private final int idBd; // Campo privado para el ID en base de datos.
    private final String nombre; // Campo privado para el nombre del rol.

    // Constructor que vincula los atributos al enum.
    Rol(int idBd, String nombre) { // Inicio constructor.
        this.idBd = idBd; // Asignación de ID.
        this.nombre = nombre; // Asignación de nombre.
    } // Fin constructor.

    // Método para obtener el ID de la base de datos.
    public int getIdBd() { return idBd; } // Retorna el entero de ID.

    // Método para obtener el nombre del rol.
    public String getNombre() { return nombre; } // Retorna el nombre string.

    // Método estático para convertir un string al enum correspondiente.
    public static Rol desde(String nombre) { // Inicio método parseo.
        for (Rol r : values()) { // Itera sobre los roles registrados.
            // Compara ignorando mayúsculas/minúsculas.
            if (r.nombre.equalsIgnoreCase(nombre)) return r; // Si coincide, retorna rol.
        } // Fin for.
        // Lanza excepción si el rol no es válido.
        throw new IllegalArgumentException("Rol invalido: " + nombre); // Error de argumento.
    } // Fin método desde.
} // Fin clase.