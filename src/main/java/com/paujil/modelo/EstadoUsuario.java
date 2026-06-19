package com.paujil.modelo; // Define el paquete del modelo.

public enum EstadoUsuario { // Definición del tipo enumerado para estados.

    ACTIVO("Activo"), // Constante para estado Activo con valor string.
    INACTIVO("Inactivo"), // Constante para estado Inactivo con valor string.
    PENDIENTE("Pendiente"); // Constante para estado Pendiente con valor string.

    private final String valor; // Atributo privado para el valor de BD.

    // Constructor que asocia el valor de BD al enum.
    EstadoUsuario(String valor) { this.valor = valor; } // Asignación de valor.

    // Método para obtener el valor string del enum.
    public String getValor() { return valor; } // Retorna el valor string.

    // Método estático para convertir un string al enum correspondiente.
    public static EstadoUsuario desde(String valor) { // Inicio método parseo.
        for (EstadoUsuario e : values()) { // Itera sobre los valores del enum.
            // Compara ignorando mayúsculas/minúsculas.
            if (e.valor.equalsIgnoreCase(valor)) return e; // Si coincide, retorna el enum.
        } // Fin for.
        // Lanza excepción si el valor no existe en el enum.
        throw new IllegalArgumentException("Estado invalido: " + valor); // Error de argumento.
    } // Fin método desde.
} // Fin clase.