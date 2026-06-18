package com.paujil.dao;

import com.paujil.modelo.tipoTrabajo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import paujil.basedatos.clase_Conexion;

public class TipoTrabajoDao {

    public List<tipoTrabajo> listarTipos() {
        List<tipoTrabajo> lista = new ArrayList<>();
        String sql = "SELECT * FROM tipos_trabajo";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                tipoTrabajo tipo = new tipoTrabajo();
                tipo.setIdTipoTrabajo(rs.getInt("id_tipo_trabajo"));
                tipo.setNombreTipo(rs.getString("nombre_tipo"));
                lista.add(tipo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }
    
    public boolean registrarTipo(String nombre) {
        String sql = "INSERT INTO tipos_trabajo (nombre_tipo) VALUES (?)";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminarTipo(int id) {
        String sql = "DELETE FROM tipos_trabajo WHERE id_tipo_trabajo = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

