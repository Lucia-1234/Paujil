package com.paujil.modelo;

public enum EstadoUsuario {

    // Cada constante encapsula el string exacto que se almacena en la columna estado_usuario de BD
    ACTIVO("Activo"),
    INACTIVO("Inactivo"),
    PENDIENTE("Pendiente");

    private final String valor;

    // Constructor asocia cada constante del enum con su representacion en BD
    EstadoUsuario(String valor) { this.valor = valor; }

    // Expone el string de BD para usarlo en consultas SQL y comparaciones sin hardcodear literales
    public String getValor() { return valor; }

    // Convierte un string arbitrario al enum correspondiente; centraliza el parseo para evitar repetirlo
    public static EstadoUsuario desde(String valor) {
        for (EstadoUsuario e : values()) {
            // equalsIgnoreCase tolera variaciones de capitalizacion en el valor recibido del formulario o BD
            if (e.valor.equalsIgnoreCase(valor)) return e;
        }
        // Excepcion explicita en lugar de retornar null; obliga al caller a manejar valores invalidos
        throw new IllegalArgumentException("Estado invalido: " + valor);
    }
}