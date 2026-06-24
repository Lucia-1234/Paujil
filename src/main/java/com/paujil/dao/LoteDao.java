package com.paujil.dao;

import com.paujil.modelo.lote;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import paujil.basedatos.clase_Conexion;

public class LoteDao { // Clase DAO para operaciones CRUD de lotes y su relación con cultivos.

    // Método para listar todos los registros de la tabla lotes.
    public List<lote> listarLotes() { // Inicio del método.
        List<lote> lista = new ArrayList<>(); // Inicializa la lista resultado.
        String sql = "SELECT id_lote, nombre_lote FROM lotes ORDER BY nombre_lote"; // Query SQL.

        try (Connection con = clase_Conexion.MetodoConectar(); // Establece conexión.
             PreparedStatement ps = con.prepareStatement(sql); // Prepara sentencia.
             ResultSet rs = ps.executeQuery()) { // Ejecuta consulta.

            while (rs.next()) { // Itera mientras existan filas.
                lote l = new lote(); // Crea nueva instancia de modelo.
                l.setIdLote(rs.getInt("id_lote")); // Setea ID desde BD.
                l.setNombreLote(rs.getString("nombre_lote")); // Setea nombre.
                lista.add(l); // Agrega objeto a la lista.
            } // Fin while.
        } catch (SQLException e) { // Manejo de errores SQL.
            System.err.println("Error al listar lotes: " + e.getMessage()); // Imprime error en log.
            e.printStackTrace(); // Imprime traza del error.
        } // Fin catch.
        return lista; // Retorna lista poblada o vacía.
    } // Fin método listarLotes.

    // Verifica si ya existe un lote con el mismo nombre (ignorando el ID indicado al editar, 0 para crear nuevo).
    public boolean existeNombreLote(String nombre, int idExcluir) { // Inicio método.
        String sql = idExcluir > 0
            ? "SELECT COUNT(*) FROM lotes WHERE LOWER(nombre_lote) = LOWER(?) AND id_lote <> ?" // Al editar: excluye el propio registro.
            : "SELECT COUNT(*) FROM lotes WHERE LOWER(nombre_lote) = LOWER(?)"; // Al crear: busca cualquier coincidencia.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setString(1, nombre); // Setea nombre a verificar.
            if (idExcluir > 0) ps.setInt(2, idExcluir); // Si aplica, setea el ID a excluir.
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta.
                return rs.next() && rs.getInt(1) > 0; // Retorna true si ya existe otro lote con ese nombre.
            } // Cierra ResultSet.
        } catch (SQLException e) { // Manejo errores.
            System.err.println("Error al verificar nombre de lote: " + e.getMessage()); // Log.
            e.printStackTrace(); // Traza.
            return false; // Ante duda, no bloquea (el constraint de BD será la red de seguridad).
        } // Fin catch.
    } // Fin método existeNombreLote.

    // Método para insertar un nuevo lote en la tabla.
    public boolean registrarLote(lote l) { // Inicio método registrar.
        String sql = "INSERT INTO lotes (nombre_lote) VALUES (?)"; // SQL insert.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta a la BD.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara sentencia.
            ps.setString(1, l.getNombreLote()); // Setea parámetro 1.
            return ps.executeUpdate() > 0; // Ejecuta e indica si hubo éxito.
        } catch (SQLException e) { // Manejo de errores.
            System.err.println("Error al registrar lote: " + e.getMessage()); // Error log.
            e.printStackTrace(); // Imprime traza.
            return false; // Retorna fallo.
        } // Fin catch.
    } // Fin método registrarLote.

    // Método para actualizar el nombre de un lote existente.
    public boolean actualizarLote(int id, String nombre) { // Inicio actualizar.
        String sql = "UPDATE lotes SET nombre_lote = ? WHERE id_lote = ?"; // SQL update.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara statement.
            ps.setString(1, nombre); // Setea nombre.
            ps.setInt(2, id); // Setea ID para el WHERE.
            return ps.executeUpdate() > 0; // Ejecuta e indica éxito.
        } catch (SQLException e) { // Manejo errores.
            System.err.println("Error al actualizar lote id=" + id + ": " + e.getMessage()); // Error log.
            e.printStackTrace(); // Imprime traza.
            return false; // Retorna fallo.
        } // Fin catch.
    } // Fin método actualizarLote.

    // Verifica si un lote tiene cultivos asignados (FK id_lote en tabla cultivos).
    // Retorna true si existe al menos un cultivo que apunta a este lote.
    public boolean tieneCultivosAsignados(int idLote) { // Inicio método.
        String sql = "SELECT COUNT(*) FROM cultivos WHERE id_lote = ?"; // Cuenta cultivos que usan este lote.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setInt(1, idLote); // Setea ID del lote a verificar.
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta.
                return rs.next() && rs.getInt(1) > 0; // true si hay al menos un cultivo relacionado.
            } // Cierra ResultSet.
        } catch (SQLException e) { // Manejo errores.
            System.err.println("Error al verificar cultivos del lote id=" + idLote + ": " + e.getMessage()); // Log.
            e.printStackTrace(); // Traza.
            return true; // Ante duda, bloquea el borrado para proteger integridad.
        } // Fin catch.
    } // Fin método tieneCultivosAsignados.

    // Elimina un lote solo si NO tiene cultivos relacionados.
    // Retorna true si se eliminó, false si tiene cultivos asignados o si ocurrió un error.
    public boolean eliminarLote(int id) { // Inicio método eliminar.
        if (tieneCultivosAsignados(id)) { // Verifica integridad antes de borrar.
            return false; // Bloquea: el lote tiene cultivos asociados, no se puede eliminar.
        } // Fin if.
        String sql = "DELETE FROM lotes WHERE id_lote = ?"; // SQL delete del lote.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara statement.
            ps.setInt(1, id); // Setea ID a eliminar.
            return ps.executeUpdate() > 0; // Ejecuta y retorna true si se borró.
        } catch (SQLException e) { // Manejo errores.
            System.err.println("Error al eliminar lote id=" + id + ": " + e.getMessage()); // Error log.
            e.printStackTrace(); // Imprime traza.
            return false; // Retorna fallo.
        } // Fin catch.
    } // Fin método eliminarLote.

    // Método para buscar un lote específico por su ID.
    public lote buscarPorId(int id) { // Inicio método buscar.
        String sql = "SELECT id_lote, nombre_lote FROM lotes WHERE id_lote = ?"; // SQL select filtrado.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setInt(1, id); // Setea parámetro ID.
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta query.
                if (rs.next()) { // Verifica si hay resultado.
                    lote l = new lote(); // Crea objeto lote.
                    l.setIdLote(rs.getInt("id_lote")); // Mapea ID.
                    l.setNombreLote(rs.getString("nombre_lote")); // Mapea nombre.
                    return l; // Retorna objeto encontrado.
                } // Fin if.
            } // Cierre ResultSet.
        } catch (SQLException e) { // Manejo errores.
            System.err.println("Error al buscar lote id=" + id + ": " + e.getMessage()); // Error log.
            e.printStackTrace(); // Imprime traza.
        } // Fin catch.
        return null; // Retorna null si no se encuentra.
    } // Fin método buscarPorId.

    // ---------------------------------------------------------------
    // MÉTODOS DE RELACIÓN MUCHOS-A-MUCHOS (tabla cultivo_lote)
    // ---------------------------------------------------------------

    // Lista los lotes asociados a un cultivo específico.
    public List<lote> listarLotesPorCultivo(int idCultivo) { // Inicio método.
        List<lote> lista = new ArrayList<>(); // Inicializa lista resultado.
        String sql = "SELECT l.id_lote, l.nombre_lote " + // Trae datos del lote.
                     "FROM lotes l " +
                     "INNER JOIN cultivo_lote cl ON cl.id_lote = l.id_lote " + // Une por la tabla relacional.
                     "WHERE cl.id_cultivo = ? " +
                     "ORDER BY l.nombre_lote"; // Orden alfabético.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setInt(1, idCultivo); // Setea filtro por cultivo.
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta.
                while (rs.next()) { // Itera resultados.
                    lista.add(new lote(rs.getInt("id_lote"), rs.getString("nombre_lote"))); // Construye objeto.
                } // Fin while.
            } // Cierra ResultSet.
        } catch (SQLException e) { // Manejo errores.
            System.err.println("Error al listar lotes del cultivo id=" + idCultivo + ": " + e.getMessage()); // Log.
            e.printStackTrace(); // Traza.
        } // Fin catch.
        return lista; // Retorna lista.
    } // Fin método listarLotesPorCultivo.

    // Lista los IDs de lotes asociados a un cultivo (útil para marcar checkboxes en el formulario).
    public List<Integer> listarIdsLotesPorCultivo(int idCultivo) { // Inicio método.
        List<Integer> ids = new ArrayList<>(); // Lista de IDs.
        String sql = "SELECT id_lote FROM cultivo_lote WHERE id_cultivo = ?"; // SQL filtrado.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setInt(1, idCultivo); // Setea filtro.
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta.
                while (rs.next()) { // Itera.
                    ids.add(rs.getInt("id_lote")); // Agrega ID.
                } // Fin while.
            } // Cierra ResultSet.
        } catch (SQLException e) { // Manejo errores.
            System.err.println("Error al listar IDs de lotes del cultivo id=" + idCultivo + ": " + e.getMessage()); // Log.
            e.printStackTrace(); // Traza.
        } // Fin catch.
        return ids; // Retorna lista de IDs.
    } // Fin método listarIdsLotesPorCultivo.

    // Verifica si un lote ya está asignado a algún cultivo que comparte el mismo nombre que idCultivo dado.
    // Se usa antes de asignar lotes para impedir que dos cultivos homónimos compartan un mismo lote.
    // idCultivoActual se excluye de la búsqueda para no bloquearse a sí mismo al editar.
    public boolean loteYaAsignadoACultivoHomonimo(int idCultivoActual, String nombreCultivo, int idLote) { // Inicio método.
        String sql = "SELECT COUNT(*) FROM cultivo_lote cl " +
                     "INNER JOIN cultivos c ON c.id_cultivo = cl.id_cultivo " +
                     "WHERE LOWER(c.nombre_cultivo) = LOWER(?) " + // Busca cultivos con el mismo nombre.
                     "AND cl.id_lote = ? " +                        // Que tengan ese lote asignado.
                     "AND c.id_cultivo <> ?";                       // Excluye el cultivo que se está editando/creando.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setString(1, nombreCultivo); // Setea nombre del cultivo a comparar.
            ps.setInt(2, idLote); // Setea lote a verificar.
            ps.setInt(3, idCultivoActual); // Excluye el cultivo actual (0 si es nuevo, no afecta).
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta.
                return rs.next() && rs.getInt(1) > 0; // true = conflicto encontrado.
            } // Cierra ResultSet.
        } catch (SQLException e) { // Manejo errores.
            System.err.println("Error al verificar lote homónimo: " + e.getMessage()); // Log.
            e.printStackTrace(); // Traza.
            return false; // Ante duda, no bloquea.
        } // Fin catch.
    } // Fin método loteYaAsignadoACultivoHomonimo.

    // Reemplaza por completo la asignación de lotes de un cultivo (borra lo anterior e inserta lo nuevo).
    // Se usa una transacción manual para que la operación sea atómica: o se actualiza todo, o nada.
    public boolean asignarLotesACultivo(int idCultivo, List<Integer> idsLotes) { // Inicio método.
        String sqlBorrar = "DELETE FROM cultivo_lote WHERE id_cultivo = ?"; // Limpia asignaciones previas.
        String sqlInsertar = "INSERT INTO cultivo_lote (id_cultivo, id_lote) VALUES (?, ?)"; // Inserta nuevas.
        try (Connection con = clase_Conexion.MetodoConectar()) { // Conecta.
            con.setAutoCommit(false); // Inicia transacción manual.
            try (PreparedStatement psBorrar = con.prepareStatement(sqlBorrar)) { // Prepara borrado.
                psBorrar.setInt(1, idCultivo); // Setea cultivo.
                psBorrar.executeUpdate(); // Ejecuta limpieza de relaciones previas.
            } // Cierra statement de borrado.
            if (idsLotes != null) { // Verifica si hay lotes para asignar.
                try (PreparedStatement psInsertar = con.prepareStatement(sqlInsertar)) { // Prepara inserción.
                    for (Integer idLote : idsLotes) { // Itera cada lote seleccionado.
                        psInsertar.setInt(1, idCultivo); // Setea cultivo.
                        psInsertar.setInt(2, idLote); // Setea lote.
                        psInsertar.executeUpdate(); // Ejecuta inserción individual (sin batch automático).
                    } // Fin for.
                } // Cierra statement de inserción.
            } // Fin if.
            con.commit(); // Confirma transacción.
            return true; // Indica éxito.
        } catch (SQLException e) { // Manejo errores.
            System.err.println("Error al asignar lotes al cultivo id=" + idCultivo + ": " + e.getMessage()); // Log.
            e.printStackTrace(); // Traza.
            return false; // Retorna fallo.
        } // Fin catch.
    } // Fin método asignarLotesACultivo.

    // Desasocia un único lote de un único cultivo (operación puntual, sin afectar el resto).
    public boolean desasignarLoteDeCultivo(int idCultivo, int idLote) { // Inicio método.
        String sql = "DELETE FROM cultivo_lote WHERE id_cultivo = ? AND id_lote = ?"; // SQL delete puntual.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setInt(1, idCultivo); // Setea cultivo.
            ps.setInt(2, idLote); // Setea lote.
            return ps.executeUpdate() > 0; // Ejecuta e indica éxito.
        } catch (SQLException e) { // Manejo errores.
            System.err.println("Error al desasignar lote id=" + idLote + " del cultivo id=" + idCultivo + ": " + e.getMessage()); // Log.
            e.printStackTrace(); // Traza.
            return false; // Retorna fallo.
        } // Fin catch.
    } // Fin método desasignarLoteDeCultivo.

    // Cuenta cuántos cultivos tiene asignado un lote (útil para evitar borrados inconsistentes en la UI).
    public int contarCultivosPorLote(int idLote) { // Inicio método.
        String sql = "SELECT COUNT(*) AS total FROM cultivo_lote WHERE id_lote = ?"; // SQL conteo.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setInt(1, idLote); // Setea filtro.
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta.
                if (rs.next()) return rs.getInt("total"); // Retorna conteo si existe.
            } // Cierra ResultSet.
        } catch (SQLException e) { // Manejo errores.
            System.err.println("Error al contar cultivos del lote id=" + idLote + ": " + e.getMessage()); // Log.
            e.printStackTrace(); // Traza.
        } // Fin catch.
        return 0; // Retorna 0 por defecto.
    } // Fin método contarCultivosPorLote.

} // Fin clase.