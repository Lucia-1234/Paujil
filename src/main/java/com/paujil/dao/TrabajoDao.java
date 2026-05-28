package com.paujil.dao;

import com.paujil.modelo.trabajo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import paujil.basedatos.clase_Conexion;

public class TrabajoDao {

    // ── Registrar trabajo completo (transacción de 3 tablas) ─────────────────
    public boolean registrarTrabajoCompleto(trabajo t, int idCultivo, int idUsuario) {

        String sqlTrabajo = "INSERT INTO trabajos (nombre_trabajo, descripcion_trabajo, fecha_asignacion) "
                          + "VALUES (?, ?, ?)";

        // CORRECCIÓN: 'con' se declara fuera del try-with-resources para que
        // sea accesible en el bloque catch y poder hacer rollback.
        Connection con = null;

        try {
            con = clase_Conexion.MetodoConectar();
            con.setAutoCommit(false);

            // 1. Insertar en 'trabajos' y recuperar la PK generada
            try (PreparedStatement ps = con.prepareStatement(
                    sqlTrabajo, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, t.getNombre());
                ps.setString(2, t.getDescripcion());
                ps.setDate(3, t.getFechaAsignacion());
                ps.executeUpdate();

                int idTrabajo;
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idTrabajo = rs.getInt(1);
                    } else {
                        // No se generó ID — algo falló silenciosamente
                        con.rollback();
                        return false;
                    }
                }

                // 2. Insertar en 'trabajos_cultivo'
                try (PreparedStatement ps2 = con.prepareStatement(
                        "INSERT INTO trabajos_cultivo (id_cultivo, id_trabajo) VALUES (?, ?)")) {
                    ps2.setInt(1, idCultivo);
                    ps2.setInt(2, idTrabajo);
                    ps2.executeUpdate();
                }

                // 3. Insertar en 'asignar_trabajos'
                try (PreparedStatement ps3 = con.prepareStatement(
                        "INSERT INTO asignar_trabajos (id_usuario, id_trabajo) VALUES (?, ?)")) {
                    ps3.setInt(1, idUsuario);
                    ps3.setInt(2, idTrabajo);
                    ps3.executeUpdate();
                }
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            // Rollback solo si la conexión se abrió correctamente
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("Error al registrar trabajo completo: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    // ── Listar todos los trabajos con su cultivo y usuario asignado ───────────
    public List<trabajo> listarTrabajosCompletos() {
        List<trabajo> lista = new ArrayList<>();
        String sql = "SELECT t.id_trabajo, t.nombre_trabajo, t.descripcion_trabajo, "
                   + "       t.fecha_asignacion, t.fecha_finalizacion, t.observaciones_trabajo, "
                   + "       u.nombre_usuario, c.nombre_cultivo "
                   + "FROM trabajos t "
                   + "INNER JOIN asignar_trabajos at2 ON t.id_trabajo = at2.id_trabajo "
                   + "INNER JOIN usuarios u           ON at2.id_usuario = u.id_usuario "
                   + "INNER JOIN trabajos_cultivo tc  ON t.id_trabajo = tc.id_trabajo "
                   + "INNER JOIN cultivos c           ON tc.id_cultivo = c.id_cultivo "
                   + "ORDER BY t.fecha_asignacion DESC";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                trabajo t = new trabajo();
                t.setId(rs.getInt("id_trabajo"));
                t.setNombre(rs.getString("nombre_trabajo"));
                t.setDescripcion(rs.getString("descripcion_trabajo"));
                t.setFechaAsignacion(rs.getDate("fecha_asignacion"));
                t.setFechaFinalizacion(rs.getDate("fecha_finalizacion"));
                t.setObservaciones(rs.getString("observaciones_trabajo"));
                t.setNombreUsuario(rs.getString("nombre_usuario"));   // para vista
                t.setNombreCultivo(rs.getString("nombre_cultivo"));   // para vista
                lista.add(t);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar trabajos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    // ── Listar trabajos por usuario ───────────────────────────────────────────
    // Útil para el panel del trabajador: solo ve sus propios trabajos.
    public List<trabajo> listarPorUsuario(int idUsuario) {
        List<trabajo> lista = new ArrayList<>();
        String sql = "SELECT t.id_trabajo, t.nombre_trabajo, t.descripcion_trabajo, "
                   + "       t.fecha_asignacion, t.fecha_finalizacion, t.observaciones_trabajo, "
                   + "       c.nombre_cultivo "
                   + "FROM trabajos t "
                   + "INNER JOIN asignar_trabajos at2 ON t.id_trabajo = at2.id_trabajo "
                   + "INNER JOIN trabajos_cultivo tc  ON t.id_trabajo = tc.id_trabajo "
                   + "INNER JOIN cultivos c           ON tc.id_cultivo = c.id_cultivo "
                   + "WHERE at2.id_usuario = ? "
                   + "ORDER BY t.fecha_asignacion DESC";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    trabajo t = new trabajo();
                    t.setId(rs.getInt("id_trabajo"));
                    t.setNombre(rs.getString("nombre_trabajo"));
                    t.setDescripcion(rs.getString("descripcion_trabajo"));
                    t.setFechaAsignacion(rs.getDate("fecha_asignacion"));
                    t.setFechaFinalizacion(rs.getDate("fecha_finalizacion"));
                    t.setObservaciones(rs.getString("observaciones_trabajo"));
                    t.setNombreCultivo(rs.getString("nombre_cultivo"));
                    lista.add(t);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar trabajos del usuario id=" + idUsuario + ": " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    // ── Eliminar trabajo (CASCADE elimina trabajos_cultivo y asignar_trabajos) ─
    public boolean eliminarTrabajo(int idTrabajo) {
        String sql = "DELETE FROM trabajos WHERE id_trabajo = ?";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTrabajo);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar trabajo id=" + idTrabajo + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}