package com.paujil.dao;

import com.paujil.modelo.registros;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import paujil.basedatos.clase_Conexion;

public class RegistroTrabajoDao {

    // ── Registrar un trabajo realizado ────────────────────────────────────────
    public boolean registrarLabor(registros lab) {
        String sql = "INSERT INTO trabajos_realizados "
                   + "(id_cultivo, descripcion_trabajo, id_usuario, fecha_inicio, fecha_finalizo, observaciones) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, lab.getIdCultivo());
            ps.setString(2, lab.getDescripcionTrabajo());
            // id_usuario vincula el registro al trabajador autenticado que reporto la labor
            ps.setInt(3, lab.getIdUsuario());
            ps.setDate(4, lab.getFechaInicio());
            ps.setDate(5, lab.getFechaFinalizo());
            // setNull evita almacenar cadenas vacias en la columna; diferencia ausencia de valor de texto real
            if (lab.getObservaciones() != null && !lab.getObservaciones().trim().isEmpty()) {
                ps.setString(6, lab.getObservaciones());
            } else {
                ps.setNull(6, Types.VARCHAR);
            }
            // executeUpdate > 0 confirma que la fila fue insertada correctamente
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar labor: " + e.getMessage());
            return false;
        }
    }

    // ── Listar trabajos realizados por cultivo ─────────────────────────────────
    public List<registros> listarPorCultivo(int idCultivo) {
        List<registros> lista = new ArrayList<>();
        // JOIN trae nombre_usuario en la misma consulta para evitar N consultas adicionales en el caller
        String sql = "SELECT tr.*, u.nombre_usuario "
                   + "FROM trabajos_realizados tr "
                   + "INNER JOIN usuarios u ON tr.id_usuario = u.id_usuario "
                   + "WHERE tr.id_cultivo = ? "
                   // DESC muestra los trabajos mas recientes primero en la vista de historial
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
                    // nombre_usuario proviene del JOIN; no existe como columna en trabajos_realizados
                    r.setNombreUsuario(rs.getString("nombre_usuario"));
                    r.setFechaInicio(rs.getDate("fecha_inicio"));
                    r.setFechaFinalizo(rs.getDate("fecha_finalizo"));
                    // observaciones puede ser null si no se registraron notas adicionales
                    r.setObservaciones(rs.getString("observaciones"));
                    lista.add(r);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar historial del cultivo: " + e.getMessage());
        }
        // Retorna lista vacia en caso de error para que el caller pueda iterar sin comprobar null
        return lista;
    }

    // Cuenta los registros de trabajo de un cultivo; usado para badges en la vista sin consultas en la JSP
    public int contarPorCultivo(int idCultivo) {
        String sql = "SELECT COUNT(*) FROM trabajos_realizados WHERE id_cultivo = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCultivo);
            try (ResultSet rs = ps.executeQuery()) {
                // COUNT(*) siempre retorna una fila; rs.getInt(1) lee el valor de la primera columna
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al contar registros: " + e.getMessage());
        }
        // 0 como valor seguro permite mostrar el badge sin que el caller maneje null
        return 0;
    }

    // ── Eliminar registro de trabajo realizado ────────────────────────────────
    public boolean eliminarLabor(int idTrabajoRealizado) {
        String sql = "DELETE FROM trabajos_realizados WHERE id_trabajo_realizado = ?";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTrabajoRealizado);
            // executeUpdate > 0 verifica que el registro existia; 0 indica que el ID no se encontro
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar labor: " + e.getMessage());
            return false;
        }
    }
}