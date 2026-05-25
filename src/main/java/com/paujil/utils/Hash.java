
package com.paujil.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import paujil.basedatos.clase_Conexion;

public class Hash {

    public static void main(String[] args) throws Exception {
        String hash = Seguridad.encriptar("Admin123.4"); // Usando tu clase de seguridad

        Connection con = clase_Conexion.MetodoConectar();
        con.setAutoCommit(false); // Transacción para asegurar integridad

        try {
            // 1. Insertar Usuario
            String sqlUser = "INSERT INTO usuarios (nombre_usuario, fecha_nacimiento, direccion_usuario, contrasena_usuario, estado_usuario) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement psU = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
            psU.setString(1, "Administrador Pujil");
            psU.setDate(2, java.sql.Date.valueOf("2004-01-23")); // Fecha dummy
            psU.setString(3, "Vereda planadas");
            psU.setString(4, hash); // El hash, NUNCA texto plano
            psU.setString(5, "Activo");
            psU.executeUpdate();

            ResultSet rs = psU.getGeneratedKeys();
            int idAdmin = rs.next() ? rs.getInt(1) : 0;

            String sqlCorreo = "INSERT INTO correos (id_correo, id_usuario, direccion_correo) VALUES (?, ?, ?)";
            try (PreparedStatement psC = con.prepareStatement(sqlCorreo)) {
                psC.setNull(1, java.sql.Types.INTEGER);
                psC.setInt(2, idAdmin);
                psC.setString(3, "administrador@gmail.com");
                psC.executeUpdate();
            }

            // 2. Insertar Teléfono
            String sqlTelefono = "INSERT INTO telefonos (id_telefono, id_usuario, numero_telefono) VALUES (?, ?, ?)";
            try (PreparedStatement psT = con.prepareStatement(sqlTelefono)) {
                psT.setNull(1, java.sql.Types.INTEGER);
                psT.setInt(2, idAdmin);
                psT.setString(3, "3115856772");
                psT.executeUpdate();
            }

            // 3. Insertar Rol (Asumiendo que 2 es admin)
            String sqlRol = "INSERT INTO usuario_rol (id_usuario_rol, id_usuario, id_rol) VALUES (?, ?, ?)";
            try (PreparedStatement psR = con.prepareStatement(sqlRol)) {
                psR.setNull(1, java.sql.Types.INTEGER);
                psR.setInt(2, idAdmin);
                psR.setInt(3, 2);
                psR.executeUpdate();
            }


            con.commit();
            System.out.println(" Admin creado con ID: " + idAdmin);
        } catch (Exception e) {
            con.rollback();
            e.printStackTrace();
        } finally {
            con.close();
        }
    }
    
}
