package com.paujil.modelo;

import java.time.LocalDate;
import java.time.Period;

public class validador {

    // Mínimo 8 caracteres, al menos: una mayúscula, una minúscula, un número y un símbolo.
    // El valor 8 debe coincidir con el mensaje que muestra ServletRegistro al usuario.
    public static boolean esContrasenaSegura(String password) {
        if (password == null) return false;
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!.\\-_*]).{8,}$";
        return password.matches(regex);
    }

    public static boolean esMayorDeEdad(String fechaNacimientoStr) {
        try {
            LocalDate fechaNacimiento = LocalDate.parse(fechaNacimientoStr);
            LocalDate hoy = LocalDate.now();
            return Period.between(fechaNacimiento, hoy).getYears() >= 18;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean esTelefonoValido(String telefono) {
        return telefono != null && telefono.matches("\\d{10}");
    }

    public static boolean esCorreoValido(String correo) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return correo != null && correo.matches(regex);
    }
}
    

       