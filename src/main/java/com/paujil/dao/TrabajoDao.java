package com.paujil.dao;

import com.paujil.modelo.trabajo;
import java.sql.*;
import paujil.basedatos.clase_Conexion;

public class TrabajoDao {

    public boolean registrarTrabajoCompleto(trabajo t, int idCultivo, int idUsuario) {
        // SQL para la tabla principal
        String sqlTrabajo = "INSERT INTO trabajos (nombre_trabajo, descripcion_trabajo, fecha_asignacion) VALUES (?, ?, ?)";
        
        try (Connection con = clase_Conexion.MetodoConectar()) {
            con.setAutoCommit(false); // Inicia la transacción para asegurar consistencia
            
            // 1. Insertar trabajo y recuperar el ID generado
            PreparedStatement ps = con.prepareStatement(sqlTrabajo, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, t.getNombre());
            ps.setString(2, t.getDescripcion());
            ps.setDate(3, t.getFechaAsignacion());
            ps.executeUpdate();
            
            ResultSet rs = ps.getGeneratedKeys();
            int idTrabajo = rs.next() ? rs.getInt(1) : 0;
            
            // 2. Insertar en tabla intermedia trabajos_cultivo
            PreparedStatement ps2 = con.prepareStatement("INSERT INTO trabajos_cultivo (id_cultivo, id_trabajo) VALUES (?, ?)");
            ps2.setInt(1, idCultivo);
            ps2.setInt(2, idTrabajo);
            ps2.executeUpdate();
            
            // 3. Insertar en tabla intermedia asignar_trabajos
            PreparedStatement ps3 = con.prepareStatement("INSERT INTO asignar_trabajos (id_usuario, id_trabajo) VALUES (?, ?)");
            ps3.setInt(1, idUsuario);
            ps3.setInt(2, idTrabajo);
            ps3.executeUpdate();
            
            con.commit(); // Si todo sale bien, aplicamos los cambios
            return true;
            
        } catch (SQLException e) {
            try {
                con.rollback(); // Deshace todo si algo falló
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }
}