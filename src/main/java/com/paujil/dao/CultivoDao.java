package com.paujil.dao;

import com.paujil.modelo.cultivo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import paujil.basedatos.clase_Conexion;

public class CultivoDao {

    public List<cultivo> listarCultivos() {
        List<cultivo> lista = new ArrayList<>();
        // Asegúrate de que el nombre de las columnas coincida exactamente con tu tabla
        String sql = "SELECT * FROM cultivos"; 

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cultivo c = new cultivo();
                c.setIdCultivo(rs.getInt("id_cultivo"));
                c.setNombreCultivo(rs.getString("nombre_cultivo"));
                c.setTipoCultivo(rs.getString("tipo_cultivo"));
                c.setFechaSiembra(rs.getDate("fecha_siembra"));
                c.setFechaCosecha(rs.getDate("fecha_cosecha"));
                // El campo biopreparado lo manejaremos después con la tabla relacional
                lista.add(c);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    public boolean registrarCultivo(cultivo c) {
        // SQL limpio (sin url ni bioprep directo aquí por ahora)
        String sql = "INSERT INTO cultivos (nombre_cultivo, tipo_cultivo, fecha_siembra, fecha_cosecha) VALUES (?, ?, ?, ?)";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombreCultivo());
            ps.setString(2, c.getTipoCultivo());
            ps.setDate(3, c.getFechaSiembra());
            ps.setDate(4, c.getFechaCosecha());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    public boolean actualizarCultivo(int id, String nombre, String tipo, Date siembra, Date cosecha) {
        String sql = "UPDATE cultivos SET nombreCultivo = ?, tipoCultivo = ?, fechaSiembra = ?, fechaCosecha = ? WHERE idCultivo = ?";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, tipo);
            ps.setDate(3, siembra);
            ps.setDate(4, cosecha);
            ps.setInt(5, id);

            // executeUpdate devuelve el número de filas afectadas. 
            // Si es mayor a 0, significa que se actualizó correctamente.
            return ps.executeUpdate() > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean eliminarCultivo(int id) {
        String sql = "DELETE FROM cultivos WHERE id_cultivo = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}