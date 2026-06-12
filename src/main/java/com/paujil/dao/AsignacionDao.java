package com.paujil.dao;

import com.paujil.modelo.asignacion;
import com.paujil.modelo.trabajo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import paujil.basedatos.clase_Conexion;

public class AsignacionDao {

    // ── 1. Registrar trabajo (catálogo) + asignación, en una transacción ──────
    public boolean registrarTrabajoCompleto(trabajo t, int idCultivo, int idUsuario, Date fechaAsignacion) {
        String sqlTrabajo    = "INSERT INTO trabajos (nombre_trabajo, descripcion_trabajo, id_tipo_trabajo) VALUES (?, ?, ?)";
        String sqlAsignacion = "INSERT INTO asignaciones (id_trabajo, id_cultivo, id_usuario, fecha_asignacion, estado_trabajo) "
                             + "VALUES (?, ?, ?, ?, 'Pendiente')";

        Connection con = null;
        try {
            con = clase_Conexion.MetodoConectar();
            con.setAutoCommit(false);

            int idTrabajoGenerado;
            try (PreparedStatement ps = con.prepareStatement(sqlTrabajo, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, t.getNombre());
                ps.setString(2, t.getDescripcion());
                ps.setInt(3, t.getIdTipoTrabajo());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) {
                        con.rollback();
                        System.err.println("registrarTrabajoCompleto: no se obtuvo el ID generado del trabajo.");
                        return false;
                    }
                    idTrabajoGenerado = rs.getInt(1);
                }
            }

            try (PreparedStatement psAsig = con.prepareStatement(sqlAsignacion)) {
                psAsig.setInt(1, idTrabajoGenerado);
                psAsig.setInt(2, idCultivo);
                psAsig.setInt(3, idUsuario);
                psAsig.setDate(4, fechaAsignacion);
                psAsig.executeUpdate();
            }

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

    // ── 2. Listar todas las asignaciones (vista administrador) ────────────────
    public List<asignacion> listarTodas() {
        List<asignacion> lista = new ArrayList<>();
        String sql = "SELECT a.id_asignacion, a.id_trabajo, a.id_cultivo, a.id_usuario, "
                   + "a.fecha_asignacion, a.fecha_inicio, a.fecha_finalizacion, a.estado_trabajo, a.observaciones, "
                   + "t.nombre_trabajo, t.descripcion_trabajo, "
                   + "tt.nombre_tipo, "
                   + "u.nombre_usuario, c.nombre_cultivo "
                   + "FROM asignaciones a "
                   + "JOIN trabajos t       ON a.id_trabajo = t.id_trabajo "
                   + "JOIN tipos_trabajo tt ON t.id_tipo_trabajo = tt.id_tipo_trabajo "
                   + "JOIN usuarios u       ON a.id_usuario = u.id_usuario "
                   + "JOIN cultivos c       ON a.id_cultivo = c.id_cultivo "
                   + "ORDER BY a.fecha_asignacion DESC";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearAsignacion(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // ── 3. Listar asignaciones de un trabajador, filtrando por estado ─────────
    // estado puede ser "Pendiente", "En proceso", "Finalizado" o null para traer todas
    public List<asignacion> listarPorUsuario(int idUsuario, String estado) {
        List<asignacion> lista = new ArrayList<>();

        String sql = "SELECT a.id_asignacion, a.id_trabajo, a.id_cultivo, a.id_usuario, "
                   + "a.fecha_asignacion, a.fecha_inicio, a.fecha_finalizacion, a.estado_trabajo, a.observaciones, "
                   + "t.nombre_trabajo, t.descripcion_trabajo, "
                   + "tt.nombre_tipo, "
                   + "c.nombre_cultivo "
                   + "FROM asignaciones a "
                   + "JOIN trabajos t       ON a.id_trabajo = t.id_trabajo "
                   + "JOIN tipos_trabajo tt ON t.id_tipo_trabajo = tt.id_tipo_trabajo "
                   + "JOIN cultivos c       ON a.id_cultivo = c.id_cultivo "
                   + "WHERE a.id_usuario = ? ";

        if (estado != null) {
            sql += "AND a.estado_trabajo = ? ";
        }

        sql += "ORDER BY a.fecha_asignacion DESC";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            if (estado != null) {
                ps.setString(2, estado);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAsignacion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // ── 4. Actualizar estado / observaciones de una asignación ────────────────
    public boolean actualizarEstadoAsignacion(int idAsignacion, String observaciones, String nuevoEstado) {

        String sql;
        boolean conFechaInicio = "En proceso".equals(nuevoEstado);
        boolean conFechaFin    = "Finalizado".equals(nuevoEstado);

        if (conFechaInicio) {
            sql = "UPDATE asignaciones SET observaciones = ?, estado_trabajo = ?, fecha_inicio = CURRENT_DATE "
                + "WHERE id_asignacion = ?";
        } else if (conFechaFin) {
            sql = "UPDATE asignaciones SET observaciones = ?, estado_trabajo = ?, fecha_finalizacion = CURRENT_DATE "
                + "WHERE id_asignacion = ?";
        } else {
            sql = "UPDATE asignaciones SET observaciones = ? WHERE id_asignacion = ?";
        }

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, observaciones != null ? observaciones.trim() : "");

            if (conFechaInicio || conFechaFin) {
                ps.setString(2, nuevoEstado);
                ps.setInt(3, idAsignacion);
            } else {
                ps.setInt(2, idAsignacion);
            }

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── 5. Eliminar trabajo (CASCADE elimina asignaciones dependientes) ───────
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

    // ── 6. Listar asignaciones (historial) de un cultivo específico ──────────
    // Reemplaza a RegistroTrabajoDao.listarPorCultivo(idCultivo)
    public List<asignacion> listarPorCultivo(int idCultivo) {
        List<asignacion> lista = new ArrayList<>();
        String sql = "SELECT a.id_asignacion, a.id_trabajo, a.id_cultivo, a.id_usuario, "
                   + "a.fecha_asignacion, a.fecha_inicio, a.fecha_finalizacion, a.estado_trabajo, a.observaciones, "
                   + "t.nombre_trabajo, t.descripcion_trabajo, "
                   + "tt.nombre_tipo, "
                   + "u.nombre_usuario, c.nombre_cultivo "
                   + "FROM asignaciones a "
                   + "JOIN trabajos t       ON a.id_trabajo = t.id_trabajo "
                   + "JOIN tipos_trabajo tt ON t.id_tipo_trabajo = tt.id_tipo_trabajo "
                   + "JOIN usuarios u       ON a.id_usuario = u.id_usuario "
                   + "JOIN cultivos c       ON a.id_cultivo = c.id_cultivo "
                   + "WHERE a.id_cultivo = ? "
                   + "ORDER BY a.fecha_asignacion DESC";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCultivo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapearAsignacion(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // ── 7. Contar asignaciones (historial) de un cultivo específico ──────────
    // Reemplaza a RegistroTrabajoDao.contarPorCultivo(idCultivo)
    public int contarPorCultivo(int idCultivo) {
        String sql = "SELECT COUNT(*) FROM asignaciones WHERE id_cultivo = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCultivo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── 8. Eliminar una asignación puntual (registro de historial) ───────────
    // Reemplaza a RegistroTrabajoDao.eliminarLabor(id)
    public boolean eliminarAsignacion(int idAsignacion) {
        String sql = "DELETE FROM asignaciones WHERE id_asignacion = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAsignacion);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ── Helper privado: mapea una fila de ResultSet a un objeto asignacion ────
    private asignacion mapearAsignacion(ResultSet rs) throws SQLException {
        asignacion a = new asignacion();
        a.setId(rs.getInt("id_asignacion"));
        a.setIdTrabajo(rs.getInt("id_trabajo"));
        a.setIdCultivo(rs.getInt("id_cultivo"));
        a.setIdUsuario(rs.getInt("id_usuario"));
        a.setFechaAsignacion(rs.getDate("fecha_asignacion"));
        a.setFechaInicio(rs.getDate("fecha_inicio"));
        a.setFechaFinalizacion(rs.getDate("fecha_finalizacion"));
        a.setEstadoTrabajo(rs.getString("estado_trabajo"));
        a.setObservaciones(rs.getString("observaciones"));
        a.setNombreTrabajo(rs.getString("nombre_trabajo"));
        a.setDescripcionTrabajo(rs.getString("descripcion_trabajo"));
        a.setNombreTipoTrabajo(rs.getString("nombre_tipo"));
        a.setNombreCultivo(rs.getString("nombre_cultivo"));

        try {
            a.setNombreUsuario(rs.getString("nombre_usuario"));
        } catch (SQLException ignored) {
        }

        return a;
    }
}