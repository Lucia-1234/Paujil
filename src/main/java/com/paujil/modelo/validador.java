
package com.paujil.modelo;

import java.time.LocalDate;
import java.time.Period;

/**
 *
 * @author Aprendiz
 */
public class validador {
    
    // 1. Validación de contraseña
    public static boolean esContrasenaSegura(String password) {
        if (password == null) return false;
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!\\.\\-_*]).{5,}$";
        return password.matches(regex);
    }

    // 2. Validación de edad
    public static boolean esMayorDeEdad(String fechaNacimientoStr) {
        try {
            LocalDate fechaNacimiento = LocalDate.parse(fechaNacimientoStr);
            LocalDate hoy = LocalDate.now();
            return Period.between(fechaNacimiento, hoy).getYears() >= 18;
        } catch (Exception e) {
            return false;
        }
    }

    // 3. Validación de formato de teléfono
    public static boolean esTelefonoValido(String telefono) {
        return telefono != null && telefono.matches("\\d{10}");
    }
    
    // 4. Validación de correo (opcional pero muy útil)
    public static boolean esCorreoValido(String correo) {
        String regex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return correo != null && correo.matches(regex);
    }
    
    
}
    

