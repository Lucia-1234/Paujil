package com.paujil.modelo; // Define el paquete del modelo.

import java.time.LocalDate; // Importa API de fecha.
import java.time.Period; // Importa API para cálculos de tiempo.

public class validador { // Clase de utilidades para validación de datos.

    // Valida complejidad de contraseñas usando expresiones regulares.
    public static boolean esContrasenaSegura(String password) { // Inicio método.
        if (password == null) return false; // Verifica nulidad.
        // Regex: min 8 caracteres, 1 may, 1 min, 1 num, 1 símbolo.
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!.\\-_*]).{8,}$"; // Regla.
        return password.matches(regex); // Retorna validación.
    } // Fin método.

    // Valida mayoría de edad basada en la fecha de nacimiento.
    public static boolean esMayorDeEdad(String fechaNacimientoStr) { // Inicio método.
        try { // Bloque try.
            LocalDate fechaNacimiento = LocalDate.parse(fechaNacimientoStr); // Parsea string.
            LocalDate hoy = LocalDate.now(); // Obtiene fecha actual.
            return Period.between(fechaNacimiento, hoy).getYears() >= 18; // Valida 18+.
        } catch (Exception e) { // Captura error formato.
            return false; // Retorna falso en caso de error.
        } // Fin catch.
    } // Fin método.

    // Valida que el número de teléfono tenga exactamente 10 dígitos.
    public static boolean esTelefonoValido(String telefono) { // Inicio método.
        return telefono != null && telefono.matches("\\d{10}"); // Regex 10 dígitos.
    } // Fin método.

    // Valida la estructura básica de una dirección de correo electrónico.
    public static boolean esCorreoValido(String correo) { // Inicio método.
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$"; // Estructura de email.
        return correo != null && correo.matches(regex); // Retorna validación.
    } // Fin método.
} // Fin clase.