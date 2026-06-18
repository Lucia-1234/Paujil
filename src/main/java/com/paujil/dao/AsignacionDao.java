package com.paujil.dao;

import com.paujil.modelo.asignacion;
import com.paujil.modelo.trabajo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import paujil.basedatos.clase_Conexion;

/**
 * DAO para la gestión de trabajos y asignaciones.
 *
 * Tablas involucradas (nombres reales del schema):
 *   trabajos          (id_trabajo, nombre_trabajo, descripcion_trabajo, id_tipo_trabajo)
 *   asignaciones      (id_asignacion, id_trabajo, id_cultivo, id_usuario,
 *                      fecha_asignacion, fecha_inicio, fecha_finalizacion,
 *                      estado_trabajo, observaciones)
 *   cultivos          (id_cultivo, nombre_cultivo)
 *   usuarios          (id_usuario, nombre_usuario)
 *   tipos_trabajo     (id_tipo_trabajo, nombre_tipo)
 */
public class AsignacionDao {

    // ── SQL de consulta completa (JOIN de todas las tablas) ───────────────────
    private static final String SQL_SELECT_COMPLETO =
        "SELECT a.id_asignacion, a.id_trabajo, a.id_cultivo, a.id_usuario, " +
        "       a.fecha_asignacion, a.fecha_inicio, a.fecha_finalizacion, " +
        "       a.estado_trabajo, a.observaciones, " +
        "       t.nombre_trabajo, " +
        "       t.descripcion_trabajo, " +
        "       c.nombre_cultivo, " +
        "       u.nombre_usuario, " +
        "       tp.nombre_tipo  AS nombre_tipo_trabajo " +
        "FROM   asignaciones  a " +
        "JOIN   trabajos      t  ON t.id_trabajo       = a.id_trabajo " +
        "JOIN   cultivos      c  ON c.id_cultivo       = a.id_cultivo " +
        "JOIN   usuarios      u  ON u.id_usuario       = a.id_usuario " +
        "JOIN   tipos_trabajo tp ON tp.id_tipo_trabajo = t.id_tipo_trabajo ";

    // ─────────────────────────────────────────────────────────────────────────
    // 1. Registrar trabajo + asignación en una misma transacción
    // ─────────────────────────────────────────────────────────────────────────
    public boolean registrarTrabajoCompleto(trabajo t, int idCultivo,
                                            int idUsuario, Date fechaAsignacion) {
        String sqlTrabajo =
            "INSERT INTO trabajos (nombre_trabajo, descripcion_trabajo, id_tipo_trabajo) " +
            "VALUES (?, ?, ?)";

        String sqlAsignacion =
            "INSERT INTO asignaciones " +
            "(id_trabajo, id_cultivo, id_usuario, fecha_asignacion, estado_trabajo) " +
            "VALUES (?, ?, ?, ?, 'Pendiente')";

        Connection con = null;
        try {
            con = clase_Conexion.MetodoConectar();
            con.setAutoCommit(false);                       // inicio transacción

            // 1a. Insertar trabajo y recuperar id generado
            int idTrabajo;
            try (PreparedStatement psTrabajo = con.prepareStatement(
                    sqlTrabajo, Statement.RETURN_GENERATED_KEYS)) {
                psTrabajo.setString(1, t.getNombre());
                psTrabajo.setString(2, t.getDescripcion());
                psTrabajo.setInt(3, t.getIdTipoTrabajo());
                psTrabajo.executeUpdate();

                try (ResultSet keys = psTrabajo.getGeneratedKeys()) {
                    if (!keys.next()) {
                        con.rollback();
                        return false;
                    }
                    idTrabajo = keys.getInt(1);
                }
            }

            // 1b. Insertar asignación con el id recién obtenido
            try (PreparedStatement psAsig = con.prepareStatement(sqlAsignacion)) {
                psAsig.setInt(1, idTrabajo);
                psAsig.setInt(2, idCultivo);
                psAsig.setInt(3, idUsuario);
                psAsig.setDate(4, fechaAsignacion);
                psAsig.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ignored) {}
            }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException ignored) {}
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 2. Listar todas las asignaciones (vista del administrador)
    // ─────────────────────────────────────────────────────────────────────────
    public List<asignacion> listarTodas() {
        String sql = SQL_SELECT_COMPLETO +
                     "ORDER BY a.fecha_asignacion DESC, a.id_asignacion DESC";
        return ejecutarConsulta(sql, null, null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 3. Listar asignaciones de un usuario (con filtro opcional de estado)
    // ─────────────────────────────────────────────────────────────────────────
    public List<asignacion> listarPorUsuario(int idUsuario, String estado) {
        String sql;
        if (estado != null && !estado.isBlank()) {
            sql = SQL_SELECT_COMPLETO +
                  "WHERE a.id_usuario = ? AND a.estado_trabajo = ? " +
                  "ORDER BY a.fecha_asignacion DESC";
            return ejecutarConsulta(sql, idUsuario, estado);
        } else {
            sql = SQL_SELECT_COMPLETO +
                  "WHERE a.id_usuario = ? " +
                  "ORDER BY a.fecha_asignacion DESC";
            return ejecutarConsulta(sql, idUsuario, null);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 4. Actualizar estado y/o observaciones de una asignación
    // ─────────────────────────────────────────────────────────────────────────
    public boolean actualizarEstadoAsignacion(int idAsignacion,
                                              String observaciones,
                                              String nuevoEstado) {
        // Construimos el UPDATE dinámicamente según si cambia estado o no
        StringBuilder sql = new StringBuilder(
            "UPDATE asignaciones SET observaciones = ? ");

        if ("En proceso".equals(nuevoEstado)) {
            sql.append(", estado_trabajo = 'En proceso', fecha_inicio = CURDATE() ");
        } else if ("Finalizado".equals(nuevoEstado)) {
            sql.append(", estado_trabajo = 'Finalizado', fecha_finalizacion = CURDATE() ");
        }
        sql.append("WHERE id_asignacion = ?");

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {

            ps.setString(1, observaciones != null ? observaciones.trim() : "");
            ps.setInt(2, idAsignacion);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 5. Eliminar trabajo y sus asignaciones (en transacción)
    // ─────────────────────────────────────────────────────────────────────────
    public boolean eliminarTrabajo(int idTrabajo) {
        String sqlAsig    = "DELETE FROM asignaciones WHERE id_trabajo = ?";
        String sqlTrabajo = "DELETE FROM trabajos     WHERE id_trabajo = ?";

        Connection con = null;
        try {
            con = clase_Conexion.MetodoConectar();
            con.setAutoCommit(false);

            try (PreparedStatement psAsig = con.prepareStatement(sqlAsig)) {
                psAsig.setInt(1, idTrabajo);
                psAsig.executeUpdate();           // puede ser 0 si no tiene asignaciones, eso está bien
            }

            int filas;
            try (PreparedStatement psTrab = con.prepareStatement(sqlTrabajo)) {
                psTrab.setInt(1, idTrabajo);
                filas = psTrab.executeUpdate();
            }

            if (filas == 0) {
                con.rollback();
                return false;                     // el trabajo no existía
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ignored) {}
            }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException ignored) {}
            }
        }
    }
    
    // ─────────────────────────────────────────────────────────────────────────
    // 6. Listar asignaciones de un cultivo (historial del cultivo)
    // ─────────────────────────────────────────────────────────────────────────
    public List<asignacion> listarPorCultivo(int idCultivo) {
        String sql = SQL_SELECT_COMPLETO +
                     "WHERE a.id_cultivo = ? " +
                     "ORDER BY a.fecha_asignacion DESC, a.id_asignacion DESC";
        return ejecutarConsulta(sql, idCultivo, null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 7. Contar asignaciones de un cultivo (badge en la vista)
    // ─────────────────────────────────────────────────────────────────────────
    public int contarPorCultivo(int idCultivo) {
        String sql = "SELECT COUNT(*) FROM asignaciones WHERE id_cultivo = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCultivo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al contar asignaciones por cultivo: " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // 8. Eliminar una asignación por su id (sin tocar el trabajo padre)
    // ─────────────────────────────────────────────────────────────────────────
    public boolean eliminarAsignacion(int idAsignacion) {
        String sql = "DELETE FROM asignaciones WHERE id_asignacion = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idAsignacion);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar asignacion id=" + idAsignacion + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper privado: ejecuta una consulta con 0, 1 o 2 parámetros
    //   param1 → int (id_usuario) o null
    //   param2 → String (estado)  o null
    // ─────────────────────────────────────────────────────────────────────────
    private List<asignacion> ejecutarConsulta(String sql,
                                              Integer param1,
                                              String  param2) {
        List<asignacion> lista = new ArrayList<>();
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int idx = 1;
            if (param1 != null) ps.setInt(idx++, param1);
            if (param2 != null) ps.setString(idx, param2);

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

    // ─────────────────────────────────────────────────────────────────────────
    // Helper privado: mapea una fila del ResultSet a un objeto asignacion
    // ─────────────────────────────────────────────────────────────────────────
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
        a.setNombreCultivo(rs.getString("nombre_cultivo"));
        a.setNombreUsuario(rs.getString("nombre_usuario"));
        a.setNombreTipoTrabajo(rs.getString("nombre_tipo_trabajo"));
        return a;
    }
}