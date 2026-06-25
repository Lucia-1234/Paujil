package com.paujil.dao;

import com.paujil.modelo.lote;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import paujil.basedatos.clase_Conexion;

/**
 * DAO para operaciones CRUD sobre la tabla lotes.
 *
 * Relación con cultivos: cada cultivo tiene una columna id_lote (FK directa).
 * No existe tabla intermedia cultivo_lote; la relación es uno-a-muchos
 * (un lote puede tener varios cultivos, un cultivo pertenece a un solo lote).
 */
public class LoteDao {

    // ─────────────────────────────────────────────────────────────
    // CRUD BÁSICO
    // ─────────────────────────────────────────────────────────────

    /**
     * Retorna todos los lotes ordenados alfabéticamente.
     */
    public List<lote> listarLotes() {
        List<lote> lista = new ArrayList<>();
        String sql = "SELECT id_lote, nombre_lote FROM lotes ORDER BY nombre_lote";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new lote(rs.getInt("id_lote"), rs.getString("nombre_lote")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar lotes: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Busca un lote por su ID. Retorna null si no existe.
     */
    public lote buscarPorId(int id) {
        String sql = "SELECT id_lote, nombre_lote FROM lotes WHERE id_lote = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new lote(rs.getInt("id_lote"), rs.getString("nombre_lote"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar lote id=" + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Inserta un nuevo lote. Retorna true si se creó correctamente.
     */
    public boolean registrarLote(lote l) {
        String sql = "INSERT INTO lotes (nombre_lote) VALUES (?)";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, l.getNombreLote());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al registrar lote: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza el nombre de un lote existente.
     */
    public boolean actualizarLote(int id, String nombre) {
        String sql = "UPDATE lotes SET nombre_lote = ? WHERE id_lote = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar lote id=" + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina un lote solo si no tiene cultivos asignados.
     * Retorna false si el lote tiene cultivos o si ocurrió un error.
     */
    public boolean eliminarLote(int id) {
        if (tieneCultivosAsignados(id)) {
            return false;
        }
        String sql = "DELETE FROM lotes WHERE id_lote = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar lote id=" + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────
    // VALIDACIONES
    // ─────────────────────────────────────────────────────────────

    // Verifica si ya existe un lote con el mismo nombre en la base de datos.
    // Si se pasa idExcluir > 0, excluye ese registro (sirve para edición: un lote no se bloquea a sí mismo).
    // Si idExcluir = 0, busca en todos los registros (sirve para creación).
    public boolean existeNombreLote(String nombre, int idExcluir) {

        // Construye la consulta SQL según si se excluye un id o no.
        // LOWER() en ambos lados hace la comparación insensible a mayúsculas ("Lote A" == "lote a").
        String sql = idExcluir > 0
            ? "SELECT COUNT(*) FROM lotes WHERE LOWER(nombre_lote) = LOWER(?) AND id_lote <> ?"
            : "SELECT COUNT(*) FROM lotes WHERE LOWER(nombre_lote) = LOWER(?)";

        // try-with-resources: Connection y PreparedStatement se cierran automáticamente
        // al salir del bloque, sin importar si hay excepción o no.
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Asigna el nombre al primer parámetro (?) de la consulta.
            ps.setString(1, nombre);

            // Solo asigna el segundo parámetro si realmente hay un id a excluir.
            // Si la consulta no tiene segundo ?, llamar setInt(2,...) lanzaría una excepción.
            if (idExcluir > 0) ps.setInt(2, idExcluir);

            // Ejecuta la consulta y abre el ResultSet también con try-with-resources.
            try (ResultSet rs = ps.executeQuery()) {

                // COUNT(*) siempre retorna exactamente una fila, así que rs.next() casi siempre es true.
                // rs.getInt(1) lee el valor de la primera (y única) columna de esa fila.
                // Si el conteo es > 0, el nombre ya existe → retorna true.
                return rs.next() && rs.getInt(1) > 0;
            }

        } catch (SQLException e) {

            // Registra el error en consola para que aparezca en los logs del servidor.
            System.err.println("Error al verificar nombre de lote: " + e.getMessage());

            // Imprime el stack trace completo para facilitar el diagnóstico en desarrollo.
            e.printStackTrace();

            // Ante una falla de BD, retorna false (no bloquea la operación).
            // El constraint UNIQUE definido en la tabla lotes actúa como red de seguridad final:
            // si duplicado llega a la BD de todas formas, MySQL lanza su propio error.
            return false;
        }
    }

    /**
     * Verifica si un lote tiene cultivos asociados mediante la FK id_lote en cultivos.
     * Se usa para proteger el borrado de lotes con cultivos activos.
     * Ante un error de BD retorna true para bloquear el borrado por precaución.
     */
    public boolean tieneCultivosAsignados(int idLote) {
        String sql = "SELECT COUNT(*) FROM cultivos WHERE id_lote = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idLote);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar cultivos del lote id=" + idLote + ": " + e.getMessage());
            e.printStackTrace();
            return true; // Bloquea ante la duda para proteger integridad.
        }
    }

    /**
     * Cuenta cuántos cultivos tiene asignados un lote.
     * Útil para mostrar información en la UI antes de intentar borrar.
     */
    public int contarCultivosPorLote(int idLote) {
        String sql = "SELECT COUNT(*) AS total FROM cultivos WHERE id_lote = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idLote);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error al contar cultivos del lote id=" + idLote + ": " + e.getMessage());
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Lista los cultivos asociados a un lote (relación inversa).
     * Útil para mostrar en la UI qué cultivos usa un lote antes de editarlo o eliminarlo.
     *
     * @return Lista de nombres de cultivo que pertenecen al lote.
     */
    public List<String> listarNombresCultivosPorLote(int idLote) {
        List<String> nombres = new ArrayList<>();
        String sql = "SELECT nombre_cultivo FROM cultivos WHERE id_lote = ? ORDER BY nombre_cultivo";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idLote);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    nombres.add(rs.getString("nombre_cultivo"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al listar cultivos del lote id=" + idLote + ": " + e.getMessage());
            e.printStackTrace();
        }
        return nombres;
    }

} // Fin clase LoteDao.