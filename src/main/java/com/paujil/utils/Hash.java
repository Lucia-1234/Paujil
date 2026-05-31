package com.paujil.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import paujil.basedatos.clase_Conexion;

public class Hash {

    public static void main(String[] args) throws Exception {

        // Genera el hash seguro de la contrasena antes de cualquier operacion de BD; garantiza que el texto plano nunca toque la base de datos
        String hash = Seguridad.encriptar("Admin123.4");

        // Obtiene una conexion activa desde el pool o fabrica de conexiones del proyecto
        Connection con = clase_Conexion.MetodoConectar();
        // Desactiva el commit automatico para que las 4 inserciones (usuario, correo, telefono, rol) se confirmen o reviertan como una unidad atomica
        con.setAutoCommit(false);

        try {
            //  1. Insertar el registro principal del usuario administrador 

            // Sentencia parametrizada para insertar en 'usuarios'; el id es autoincremental y se recupera despues
            String sqlUser = "INSERT INTO usuarios (nombre_usuario, fecha_nacimiento, direccion_usuario, contrasena_usuario, estado_usuario) VALUES (?, ?, ?, ?, ?)";
            // RETURN_GENERATED_KEYS indica al driver JDBC que retenga la PK generada para recuperarla inmediatamente tras el INSERT
            PreparedStatement psU = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            psU.setString(1, "Administrador Pujil");
            // Fecha de nacimiento de relleno; solo cumple el requisito NOT NULL de la columna, no tiene valor semantico real
            psU.setDate(2, java.sql.Date.valueOf("2004-01-23"));
            psU.setString(3, "Vereda planadas");
            // Almacena el hash y nunca el texto plano; si esta linea usara la contrasena directa seria una vulnerabilidad critica
            psU.setString(4, hash);
            // Estado inicial "Activo" para que el admin pueda autenticarse inmediatamente tras la semilla
            psU.setString(5, "Activo");
            psU.executeUpdate();

            // Recupera las claves generadas por el INSERT para usar el id del admin como FK en las tablas relacionadas
            ResultSet rs = psU.getGeneratedKeys();
            // Si el ResultSet tiene al menos una fila toma el primer entero (la PK); de lo contrario asigna 0 como centinela de error
            int idAdmin = rs.next() ? rs.getInt(1) : 0;

            //  2. Insertar el correo del administrador vinculado por FK 

            String sqlCorreo = "INSERT INTO correos (id_correo, id_usuario, direccion_correo) VALUES (?, ?, ?)";
            // try-with-resources cierra el PreparedStatement automaticamente al salir del bloque, evitando fugas de recursos
            try (PreparedStatement psC = con.prepareStatement(sqlCorreo)) {
                // Pasa null en id_correo para que el motor de BD genere el valor autoincremental de la PK
                psC.setNull(1, java.sql.Types.INTEGER);
                // Vincula el correo con el usuario recien creado usando su PK generada
                psC.setInt(2, idAdmin);
                psC.setString(3, "administrador@gmail.com");
                psC.executeUpdate();
            }

            //  3. Insertar el telefono del administrador vinculado por FK 

            String sqlTelefono = "INSERT INTO telefonos (id_telefono, id_usuario, numero_telefono) VALUES (?, ?, ?)";
            try (PreparedStatement psT = con.prepareStatement(sqlTelefono)) {
                // Permite que la BD asigne el id_telefono automaticamente
                psT.setNull(1, java.sql.Types.INTEGER);
                // Reutiliza el id del admin generado en el paso 1 para mantener la integridad referencial
                psT.setInt(2, idAdmin);
                psT.setString(3, "3115856772");
                psT.executeUpdate();
            }

            //  4. Asignar el rol de administrador al usuario creado 

            String sqlRol = "INSERT INTO usuario_rol (id_usuario_rol, id_usuario, id_rol) VALUES (?, ?, ?)";
            try (PreparedStatement psR = con.prepareStatement(sqlRol)) {
                // Deja que la BD genere el id de la relacion usuario-rol automaticamente
                psR.setNull(1, java.sql.Types.INTEGER);
                psR.setInt(2, idAdmin);
                // El valor 2 corresponde al rol 'administrador' segun la tabla 'roles'; debe verificarse si cambia el esquema
                psR.setInt(3, 2);
                psR.executeUpdate();
            }

            // Confirma las 4 inserciones como una unidad; si llega aqui todas tuvieron exito y el admin queda completamente configurado
            con.commit();
            System.out.println(" Admin creado con ID: " + idAdmin);

        } catch (Exception e) {
            // Revierte todas las inserciones ante cualquier fallo; evita datos huerfanos o un usuario sin correo, telefono o rol
            con.rollback();
            // Imprime la traza completa para diagnosticar cual de las 4 operaciones fallo
            e.printStackTrace();
        } finally {
            // Cierra la conexion en cualquier escenario (exito o error) para liberar el recurso del pool inmediatamente
            con.close();
        }
    }
}