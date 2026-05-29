package com.paujil.dao;

import com.paujil.modelo.trabajo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import paujil.basedatos.clase_Conexion;

public class TrabajoDao {

    // ── 1. Registrar trabajo y asignarlo (Transacción segura) ─────────────────
    public boolean registrarTrabajoCompleto(trabajo t, int idCultivo, int idUsuario) {
        String sqlTrabajo    = "INSERT INTO trabajos (nombre_trabajo, descripcion_trabajo, fecha_asignacion) VALUES (?, ?, ?)";
        String sqlAsignacion = "INSERT INTO asignaciones (id_trabajo, id_cultivo, id_usuario, fecha_asignacion) VALUES (?, ?, ?, ?)";

        Connection con = null;
        try {
            con = clase_Conexion.MetodoConectar();
            con.setAutoCommit(false);

            // A. Insertar trabajo y recuperar el ID generado
            int idTrabajoGenerado;
            try (PreparedStatement ps = con.prepareStatement(sqlTrabajo, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, t.getNombre());
                ps.setString(2, t.getDescripcion());
                ps.setDate(3, t.getFechaAsignacion());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        // El motor no devolvió el ID — revertir y abortar
                        con.rollback();
                        System.err.println("registrarTrabajoCompleto: no se obtuvo el ID generado del trabajo.");
                        return false;
                    }
                    idTrabajoGenerado = rs.getInt(1);
                }
            }

            // B. Insertar en asignaciones usando el ID confirmado
            try (PreparedStatement psAsig = con.prepareStatement(sqlAsignacion)) {
                psAsig.setInt(1, idTrabajoGenerado);
                psAsig.setInt(2, idCultivo);
                psAsig.setInt(3, idUsuario);
                psAsig.setDate(4, t.getFechaAsignacion());
                psAsig.executeUpdate();
            }

            // Solo se llega aquí si ambos INSERTs tuvieron éxito
            con.commit();
            return true;

        } catch (SQLException e) {
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            try { if (con != null) con.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ── 2. Listar trabajos (El corazón de la vista) ──────────────────────────
    public List<trabajo> listarTrabajosCompletos() {
        List<trabajo> lista = new ArrayList<>();
        // JOIN entre las 4 tablas relacionadas
        String sql = "SELECT a.id_asignacion, t.id_trabajo, t.nombre_trabajo, t.descripcion_trabajo, "
                   + "t.fecha_asignacion, u.nombre_usuario, c.nombre_cultivo "
                   + "FROM asignaciones a "
                   + "JOIN trabajos t ON a.id_trabajo = t.id_trabajo "
                   + "JOIN usuarios u ON a.id_usuario = u.id_usuario "
                   + "JOIN cultivos c ON a.id_cultivo = c.id_cultivo "
                   + "ORDER BY t.fecha_asignacion DESC";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                trabajo t = new trabajo();
                t.setId(rs.getInt("id_trabajo")); // ID de la tabla trabajos
                t.setNombre(rs.getString("nombre_trabajo"));
                t.setDescripcion(rs.getString("descripcion_trabajo"));
                t.setFechaAsignacion(rs.getDate("fecha_asignacion"));
                t.setNombreUsuario(rs.getString("nombre_usuario"));
                t.setNombreCultivo(rs.getString("nombre_cultivo"));
                lista.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // ── 3. Eliminar (CASCADE se encarga de las dependencias) ──────────────────
    public boolean eliminarTrabajo(int idTrabajo) {
        String sql = "DELETE FROM trabajos WHERE id_trabajo = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTrabajo);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}