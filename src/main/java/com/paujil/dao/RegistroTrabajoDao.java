package com.paujil.dao;

import com.paujil.modelo.registros;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import paujil.basedatos.clase_Conexion;

public class RegistroTrabajoDao {

    // ── Registrar un trabajo realizado ────────────────────────────────────────
    // CORRECCIÓN PRINCIPAL: la tabla es 'trabajos_realizados', no 'labores'.
    // El campo 'responsable' (String) se reemplaza por 'id_usuario' (FK a usuarios),
    // que es lo que exige el esquema definitivo.
    public boolean registrarLabor(registros lab) {
        String sql = "INSERT INTO trabajos_realizados "
                   + "(id_cultivo, descripcion_trabajo, id_usuario, fecha_inicio, fecha_finalizo, observaciones) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, lab.getIdCultivo());
            ps.setString(2, lab.getDescripcionTrabajo());
            ps.setInt(3, lab.getIdUsuario());          // FK — viene de la sesión del servlet
            ps.setDate(4, lab.getFechaInicio());
            ps.setDate(5, lab.getFechaFinalizo());

            // observaciones es opcional
            if (lab.getObservaciones() != null && !lab.getObservaciones().trim().isEmpty()) {
                ps.setString(6, lab.getObservaciones());
            } else {
                ps.setNull(6, Types.VARCHAR);
            }

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar trabajo realizado: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ── Listar trabajos realizados por cultivo ────────────────────────────────
    // Útil para mostrar el historial en la vista de detalle del cultivo.
    public List<registros> listarPorCultivo(int idCultivo) {
        List<registros> lista = new ArrayList<>();
        String sql = "SELECT tr.id_trabajo_realizado, tr.id_cultivo, tr.descripcion_trabajo, "
                   + "       tr.id_usuario, u.nombre_usuario, "
                   + "       tr.fecha_inicio, tr.fecha_finalizo, tr.observaciones "
                   + "FROM trabajos_realizados tr "
                   + "INNER JOIN usuarios u ON tr.id_usuario = u.id_usuario "
                   + "WHERE tr.id_cultivo = ? "
                   + "ORDER BY tr.fecha_inicio DESC";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idCultivo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    registros r = new registros();
                    r.setIdTrabajoRealizado(rs.getInt("id_trabajo_realizado"));
                    r.setIdCultivo(rs.getInt("id_cultivo"));
                    r.setDescripcionTrabajo(rs.getString("descripcion_trabajo"));
                    r.setIdUsuario(rs.getInt("id_usuario"));
                    r.setNombreUsuario(rs.getString("nombre_usuario")); // solo para mostrar en vista
                    r.setFechaInicio(rs.getDate("fecha_inicio"));
                    r.setFechaFinalizo(rs.getDate("fecha_finalizo"));
                    r.setObservaciones(rs.getString("observaciones"));
                    lista.add(r);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al listar trabajos del cultivo id=" + idCultivo + ": " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    // ── Eliminar un trabajo realizado ─────────────────────────────────────────
    public boolean eliminarLabor(int idTrabajoRealizado) {
        String sql = "DELETE FROM trabajos_realizados WHERE id_trabajo_realizado = ?";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTrabajoRealizado);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar trabajo realizado id=" + idTrabajoRealizado + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}