package com.paujil.dao;

import com.paujil.modelo.cultivo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import paujil.basedatos.clase_Conexion;

public class CultivoDao {

    // ── Listar todos los cultivos ─────────────────────────────────────────────
    public List<cultivo> listarCultivos() {
        List<cultivo> lista = new ArrayList<>();
        String sql = "SELECT id_cultivo, nombre_cultivo, tipo_cultivo, fecha_siembra, fecha_cosecha FROM cultivos";

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
                lista.add(c);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar cultivos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    // ── Registrar cultivo nuevo ───────────────────────────────────────────────
    public boolean registrarCultivo(cultivo c) {
        String sql = "INSERT INTO cultivos (nombre_cultivo, tipo_cultivo, fecha_siembra, fecha_cosecha) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombreCultivo());
            ps.setString(2, c.getTipoCultivo());
            ps.setDate(3, c.getFechaSiembra());

            // fecha_cosecha es opcional en el esquema (puede ser NULL)
            if (c.getFechaCosecha() != null) {
                ps.setDate(4, c.getFechaCosecha());
            } else {
                ps.setNull(4, Types.DATE);
            }

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar cultivo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── Actualizar cultivo existente ──────────────────────────────────────────
    // CORRECCIÓN PRINCIPAL: los nombres de columna deben coincidir exactamente
    // con el esquema SQL (snake_case), no con los getters del modelo (camelCase).
    public boolean actualizarCultivo(int id, String nombre, String tipo,
                                     Date siembra, Date cosecha) {

        String sql = "UPDATE cultivos "
                   + "SET nombre_cultivo = ?, tipo_cultivo = ?, fecha_siembra = ?, fecha_cosecha = ? "
                   + "WHERE id_cultivo = ?";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, tipo);
            ps.setDate(3, siembra);

            // fecha_cosecha puede llegar null si el usuario no la indicó
            if (cosecha != null) {
                ps.setDate(4, cosecha);
            } else {
                ps.setNull(4, Types.DATE);
            }

            ps.setInt(5, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar cultivo id=" + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── Eliminar cultivo ──────────────────────────────────────────────────────
    public boolean eliminarCultivo(int id) {
        String sql = "DELETE FROM cultivos WHERE id_cultivo = ?";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar cultivo id=" + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── Buscar cultivo por ID ─────────────────────────────────────────────────
    // Útil para pre-llenar el modal de edición desde el servlet.
    public cultivo buscarPorId(int id) {
        String sql = "SELECT id_cultivo, nombre_cultivo, tipo_cultivo, fecha_siembra, fecha_cosecha "
                   + "FROM cultivos WHERE id_cultivo = ?";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cultivo c = new cultivo();
                    c.setIdCultivo(rs.getInt("id_cultivo"));
                    c.setNombreCultivo(rs.getString("nombre_cultivo"));
                    c.setTipoCultivo(rs.getString("tipo_cultivo"));
                    c.setFechaSiembra(rs.getDate("fecha_siembra"));
                    c.setFechaCosecha(rs.getDate("fecha_cosecha"));
                    return c;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar cultivo id=" + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}