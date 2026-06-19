package com.paujil.dao; // Define el paquete al que pertenece la clase.

import com.paujil.modelo.asignacion; // Importa el modelo de asignación.
import com.paujil.modelo.trabajo; // Importa el modelo de trabajo.
import java.sql.*; // Importa todas las clases del paquete SQL.
import java.util.ArrayList; // Importa la clase ArrayList.
import java.util.List; // Importa la interfaz List.
import paujil.basedatos.clase_Conexion; // Importa la clase de conexión.

public class AsignacionDao { // Define la clase AsignacionDao.

    private static final String SQL_SELECT_COMPLETO = // Define la constante SQL.
        "SELECT a.id_asignacion, a.id_trabajo, a.id_cultivo, a.id_usuario, " + // Select campos asignación.
        "       a.fecha_asignacion, a.fecha_inicio, a.fecha_finalizacion, " + // Select fechas.
        "       a.estado_trabajo, a.observaciones, " + // Select estado y obs.
        "       t.descripcion_trabajo, " + // Select descripción trabajo.
        "       c.nombre_cultivo, " + // Select nombre cultivo.
        "       u.nombre_usuario, " + // Select nombre usuario.
        "       tp.nombre_tipo  AS nombre_tipo_trabajo " + // Select tipo trabajo alias.
        "FROM   asignaciones  a " + // From tabla asignaciones.
        "JOIN   trabajos      t  ON t.id_trabajo       = a.id_trabajo " + // Join tabla trabajos.
        "JOIN   cultivos      c  ON c.id_cultivo       = a.id_cultivo " + // Join tabla cultivos.
        "JOIN   usuarios      u  ON u.id_usuario       = a.id_usuario " + // Join tabla usuarios.
        "JOIN   tipos_trabajo tp ON tp.id_tipo_trabajo = t.id_tipo_trabajo "; // Join tabla tipos.

    public boolean registrarTrabajoCompleto(trabajo t, int idCultivo, int idUsuario, Date fechaAsignacion) { // Método registro.
        String sqlTrabajo = "INSERT INTO trabajos (descripcion_trabajo, id_tipo_trabajo) VALUES (?, ?)"; // SQL insert trabajo.
        String sqlAsignacion = "INSERT INTO asignaciones (id_trabajo, id_cultivo, id_usuario, fecha_asignacion, estado_trabajo) VALUES (?, ?, ?, ?, 'Pendiente')"; // SQL insert asignación.
        Connection con = null; // Inicializa conexión nula.
        try { // Inicia bloque try.
            con = clase_Conexion.MetodoConectar(); // Conecta a la BD.
            con.setAutoCommit(false); // Desactiva el auto-commit.
            int idTrabajo; // Declara variable idTrabajo.
            try (PreparedStatement psTrabajo = con.prepareStatement(sqlTrabajo, Statement.RETURN_GENERATED_KEYS)) { // Prepara insert trabajo.
                psTrabajo.setString(1, t.getDescripcion()); // Setea descripción.
                psTrabajo.setInt(2, t.getIdTipoTrabajo()); // Setea tipo trabajo.
                psTrabajo.executeUpdate(); // Ejecuta insert trabajo.
                try (ResultSet keys = psTrabajo.getGeneratedKeys()) { // Recupera claves generadas.
                    if (!keys.next()) { con.rollback(); return false; } // Rollback si falla.
                    idTrabajo = keys.getInt(1); // Obtiene ID generado.
                } // Cierra resultSet de claves.
            } // Cierra PreparedStatement trabajo.
            try (PreparedStatement psAsig = con.prepareStatement(sqlAsignacion)) { // Prepara insert asignación.
                psAsig.setInt(1, idTrabajo); // Setea ID trabajo.
                psAsig.setInt(2, idCultivo); // Setea ID cultivo.
                psAsig.setInt(3, idUsuario); // Setea ID usuario.
                psAsig.setDate(4, fechaAsignacion); // Setea fecha.
                psAsig.executeUpdate(); // Ejecuta insert asignación.
            } // Cierra PreparedStatement asignación.
            con.commit(); // Confirma transacción.
            return true; // Retorna true.
        } catch (SQLException e) { // Catch de excepciones SQL.
            if (con != null) { try { con.rollback(); } catch (SQLException ignored) {} } // Rollback en error.
            e.printStackTrace(); // Imprime traza error.
            return false; // Retorna false.
        } finally { // Bloque finally para cerrar.
            if (con != null) { try { con.setAutoCommit(true); con.close(); } catch (SQLException ignored) {} } // Cierra conexión.
        } // Fin bloque finally.
    } // Fin método registrar.

    public List<asignacion> listarTodas() { // Método listar todo.
        String sql = SQL_SELECT_COMPLETO + "ORDER BY a.fecha_asignacion DESC, a.id_asignacion DESC"; // SQL con ordenamiento.
        return ejecutarConsulta(sql, null, null); // Retorna lista consulta.
    } // Fin método listar.

    public List<asignacion> listarPorUsuario(int idUsuario, String estado) { // Método listar por usuario.
        String sql; // Declaración SQL.
        if (estado != null && !estado.isBlank()) { // Verifica estado no nulo.
            sql = SQL_SELECT_COMPLETO + "WHERE a.id_usuario = ? AND a.estado_trabajo = ? " + "ORDER BY a.fecha_asignacion DESC"; // SQL con estado.
            return ejecutarConsulta(sql, idUsuario, estado); // Retorna lista con filtros.
        } else { // Caso sin estado.
            sql = SQL_SELECT_COMPLETO + "WHERE a.id_usuario = ? " + "ORDER BY a.fecha_asignacion DESC"; // SQL sin estado.
            return ejecutarConsulta(sql, idUsuario, null); // Retorna lista simple.
        } // Fin else.
    } // Fin método listarPorUsuario.

    public boolean actualizarEstadoAsignacion(int idAsignacion, String observaciones, String nuevoEstado) { // Método actualizar.
        StringBuilder sql = new StringBuilder("UPDATE asignaciones SET observaciones = ? "); // Crea builder SQL.
        if ("En proceso".equals(nuevoEstado)) { // Verifica nuevo estado.
            sql.append(", estado_trabajo = 'En proceso', fecha_inicio = CURDATE() "); // Agrega campos proceso.
        } else if ("Finalizado".equals(nuevoEstado)) { // Verifica nuevo estado.
            sql.append(", estado_trabajo = 'Finalizado', fecha_finalizacion = CURDATE() "); // Agrega campos finalizado.
        } // Fin if/else.
        sql.append("WHERE id_asignacion = ?"); // Agrega where.
        try (Connection con = clase_Conexion.MetodoConectar(); PreparedStatement ps = con.prepareStatement(sql.toString())) { // Prepara update.
            ps.setString(1, observaciones != null ? observaciones.trim() : ""); // Setea observaciones.
            ps.setInt(2, idAsignacion); // Setea ID asignación.
            return ps.executeUpdate() > 0; // Ejecuta y valida afectación.
        } catch (SQLException e) { // Catch excepciones.
            e.printStackTrace(); // Imprime error.
            return false; // Retorna false.
        } // Fin try-catch.
    } // Fin método actualizar.

    public boolean eliminarTrabajo(int idTrabajo) { // Método eliminar.
        String sqlAsig = "DELETE FROM asignaciones WHERE id_trabajo = ?"; // SQL eliminar asignación.
        String sqlTrabajo = "DELETE FROM trabajos WHERE id_trabajo = ?"; // SQL eliminar trabajo.
        Connection con = null; // Inicia conexión.
        try { // Inicia try.
            con = clase_Conexion.MetodoConectar(); // Conecta.
            con.setAutoCommit(false); // Inicia transacción.
            try (PreparedStatement psAsig = con.prepareStatement(sqlAsig)) { // Prepara borrar asignaciones.
                psAsig.setInt(1, idTrabajo); // Setea ID trabajo.
                psAsig.executeUpdate(); // Ejecuta delete.
            } // Cierra psAsig.
            int filas; // Declara variable filas.
            try (PreparedStatement psTrab = con.prepareStatement(sqlTrabajo)) { // Prepara borrar trabajo.
                psTrab.setInt(1, idTrabajo); // Setea ID trabajo.
                filas = psTrab.executeUpdate(); // Ejecuta delete.
            } // Cierra psTrab.
            if (filas == 0) { con.rollback(); return false; } // Rollback si no hay filas.
            con.commit(); // Confirma.
            return true; // Retorna éxito.
        } catch (SQLException e) { // Catch error.
            if (con != null) { try { con.rollback(); } catch (SQLException ignored) {} } // Rollback en error.
            e.printStackTrace(); // Imprime error.
            return false; // Retorna error.
        } finally { // Bloque finally.
            if (con != null) { try { con.setAutoCommit(true); con.close(); } catch (SQLException ignored) {} } // Cierra conexión.
        } // Fin finally.
    } // Fin método eliminar.

    public List<asignacion> listarPorCultivo(int idCultivo) { // Método listar por cultivo.
        String sql = SQL_SELECT_COMPLETO + "WHERE a.id_cultivo = ? " + "ORDER BY a.fecha_asignacion DESC, a.id_asignacion DESC"; // SQL cultivo.
        return ejecutarConsulta(sql, idCultivo, null); // Retorna consulta.
    } // Fin método listarPorCultivo.

    public int contarPorCultivo(int idCultivo) { // Método contar.
        String sql = "SELECT COUNT(*) FROM asignaciones WHERE id_cultivo = ?"; // SQL contar.
        try (Connection con = clase_Conexion.MetodoConectar(); PreparedStatement ps = con.prepareStatement(sql)) { // Prepara count.
            ps.setInt(1, idCultivo); // Setea idCultivo.
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta.
                if (rs.next()) return rs.getInt(1); // Retorna count.
            } // Cierra rs.
        } catch (SQLException e) { // Catch error.
            System.err.println("Error al contar asignaciones por cultivo: " + e.getMessage()); // Log error.
            e.printStackTrace(); // Imprime error.
        } // Fin catch.
        return 0; // Retorna 0 si falla.
    } // Fin método contar.

    public boolean eliminarAsignacion(int idAsignacion) { // Método eliminar asig.
        String sql = "DELETE FROM asignaciones WHERE id_asignacion = ?"; // SQL delete.
        try (Connection con = clase_Conexion.MetodoConectar(); PreparedStatement ps = con.prepareStatement(sql)) { // Prepara delete.
            ps.setInt(1, idAsignacion); // Setea ID.
            return ps.executeUpdate() > 0; // Retorna resultado.
        } catch (SQLException e) { // Catch.
            System.err.println("Error al eliminar asignacion id=" + idAsignacion + ": " + e.getMessage()); // Log error.
            e.printStackTrace(); // Imprime error.
            return false; // Retorna false.
        } // Fin catch.
    } // Fin método eliminarAsignacion.

    private List<asignacion> ejecutarConsulta(String sql, Integer param1, String param2) { // Método ejecutar consulta.
        List<asignacion> lista = new ArrayList<>(); // Crea lista.
        try (Connection con = clase_Conexion.MetodoConectar(); PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            int idx = 1; // Contador params.
            if (param1 != null) ps.setInt(idx++, param1); // Setea param1.
            if (param2 != null) ps.setString(idx, param2); // Setea param2.
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta consulta.
                while (rs.next()) { // Recorre resultados.
                    lista.add(mapearAsignacion(rs)); // Mapea objeto.
                } // Fin while.
            } // Cierra rs.
        } catch (SQLException e) { // Catch error.
            e.printStackTrace(); // Imprime error.
        } // Fin catch.
        return lista; // Retorna lista.
    } // Fin método ejecutarConsulta.

    private asignacion mapearAsignacion(ResultSet rs) throws SQLException { // Método mapear.
        asignacion a = new asignacion(); // Crea objeto.
        a.setId(rs.getInt("id_asignacion")); // Setea ID.
        a.setIdTrabajo(rs.getInt("id_trabajo")); // Setea idTrabajo.
        a.setIdCultivo(rs.getInt("id_cultivo")); // Setea idCultivo.
        a.setIdUsuario(rs.getInt("id_usuario")); // Setea idUsuario.
        a.setFechaAsignacion(rs.getDate("fecha_asignacion")); // Setea fechaAsignacion.
        a.setFechaInicio(rs.getDate("fecha_inicio")); // Setea fechaInicio.
        a.setFechaFinalizacion(rs.getDate("fecha_finalizacion")); // Setea fechaFinalizacion.
        a.setEstadoTrabajo(rs.getString("estado_trabajo")); // Setea estado.
        a.setObservaciones(rs.getString("observaciones")); // Setea observaciones.
        a.setDescripcionTrabajo(rs.getString("descripcion_trabajo")); // Setea descripcion.
        a.setNombreCultivo(rs.getString("nombre_cultivo")); // Setea nombreCultivo.
        a.setNombreUsuario(rs.getString("nombre_usuario")); // Setea nombreUsuario.
        a.setNombreTipoTrabajo(rs.getString("nombre_tipo_trabajo")); // Setea nombreTipo.
        return a; // Retorna objeto.
    } // Fin método mapearAsignacion.
} // Fin clase.