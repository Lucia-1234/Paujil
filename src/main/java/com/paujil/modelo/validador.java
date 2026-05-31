package com.paujil.modelo;

import java.time.LocalDate;
import java.time.Period;

public class validador {

    // Minimo 8 caracteres con al menos una mayuscula, una minuscula, un numero y un simbolo
    // El mensaje de error en ServletRegistro debe mantenerse sincronizado con estas reglas
    public static boolean esContrasenaSegura(String password) {
        if (password == null) return false;
        // Lookaheads independientes validan cada requisito sin importar el orden de los caracteres
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!.\\-_*]).{8,}$";
        return password.matches(regex);
    }

    public static boolean esMayorDeEdad(String fechaNacimientoStr) {
        try {
            // LocalDate.parse espera formato ISO "yyyy-MM-dd"; cualquier otro formato lanza excepcion
            LocalDate fechaNacimiento = LocalDate.parse(fechaNacimientoStr);
            LocalDate hoy = LocalDate.now();
            // Period.between calcula la diferencia exacta en anos teniendo en cuenta anos bisiestos
            return Period.between(fechaNacimiento, hoy).getYears() >= 18;
        } catch (Exception e) {
            // Fecha malformada o nula se trata como no valida; el caller rechazara el registro
            return false;
        }
    }

    // Patron exacto de 10 digitos; rechaza guiones, espacios o codigos de pais
    public static boolean esTelefonoValido(String telefono) {
        return telefono != null && telefono.matches("\\d{10}");
    }

    // Regex permisiva que valida estructura basica de correo; no verifica existencia real del dominio
    public static boolean esCorreoValido(String correo) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return correo != null && correo.matches(regex);
    }
}