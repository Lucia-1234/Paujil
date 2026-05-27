package com.paujil.dao;

import com.paujil.modelo.registros;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import paujil.basedatos.clase_Conexion;

public class RegistroTrabajoDao {

    public boolean registrarLabor(registros lab) {
        String sql = "INSERT INTO labores (id_cultivo, fecha, labor, responsable, comentarios) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, lab.getIdCultivo());
            ps.setDate(2, lab.getFecha());
            ps.setString(3, lab.getLabor());
            ps.setString(4, lab.getResponsable());
            ps.setString(5, lab.getComentarios());
            
            // Ejecutamos y verificamos si se insertó al menos una fila
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}