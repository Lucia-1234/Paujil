package com.paujil.modelo;

public enum EstadoUsuario {
    ACTIVO("Activo"),
    INACTIVO("Inactivo"),
    PENDIENTE("Pendiente");

    private final String valor;

    EstadoUsuario(String valor) { this.valor = valor; }

    public String getValor() { return valor; }

    public static EstadoUsuario desde(String valor) {
        for (EstadoUsuario e : values()) {
            if (e.valor.equalsIgnoreCase(valor)) return e;
        }
        throw new IllegalArgumentException("Estado inválido: " + valor);
    }
}
