/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paujil.utils;

import org.mindrot.jbcrypt.BCrypt; 
 // Importa la librería jBCrypt, implementación de BCrypt para Java usada para hashear contraseñas de forma segura

public class Seguridad {

    // Recibe la contraseña en texto plano y retorna su hash BCrypt listo para almacenar en base de datos
    // BCrypt.gensalt(12) genera un salt aleatorio con factor de coste 12: a mayor factor, más lento el hash y más resistente a ataques de fuerza bruta
    // BCrypt.hashpw() combina el password con el salt generado y produce el hash final irreversible
    public static String encriptar(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    // Recibe la contraseña ingresada por el usuario en texto plano y el hash almacenado en base de datos
    // BCrypt.checkpw() extrae el salt del propio hash, rehashea el password recibido y compara ambos hashes internamente
    // Retorna true si coinciden, false si no, sin necesidad de desencriptar (BCrypt es unidireccional)
    // El try-catch captura cualquier excepción que lance BCrypt (ej: hash malformado o corrupto) y retorna false de forma segura en lugar de romper el flujo
    public static boolean verificar(String password, String hash) {
        try {
            return BCrypt.checkpw(password, hash);
        } catch (Exception e) {
            return false; // Si el hash está corrupto o el input es inválido, se trata como verificación fallida sin lanzar excepción al llamador
        }
    }
}
