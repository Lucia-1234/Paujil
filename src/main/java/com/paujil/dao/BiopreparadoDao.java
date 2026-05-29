package com.paujil.dao;

import com.paujil.modelo.biopreparado;
import com.paujil.modelo.biopreparado.ingredienteBio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import paujil.basedatos.clase_Conexion;

/**
 * DAO para las operaciones CRUD de biopreparados y sus ingredientes.
 * Cada operación que toca varias tablas usa transacción explícita.
 */
public class BiopreparadoDao {

    // ── Listar todos los biopreparados ────────────────────────────────────────

    public List<biopreparado> listarBiopreparados() {
        List<biopreparado> lista = new ArrayList<>();
        String sql = "SELECT id_biopreparado, nombre_biopreparado, descripcion_biopreparado, "
                   + "precio_biopreparado, fecha_creacion, fecha_vencimiento, preparacion_biopreparado "
                   + "FROM biopreparados ORDER BY id_biopreparado ASC";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                biopreparado b = mapear(rs);
                b.setIngredientes(listarIngredientes(con, b.getIdBiopreparado()));
                lista.add(b);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar biopreparados: " + e.getMessage());
        }
        return lista;
    }

    // ── Obtener biopreparado por ID ───────────────────────────────────────────

    public biopreparado obtenerPorId(int id) {
        String sql = "SELECT id_biopreparado, nombre_biopreparado, descripcion_biopreparado, "
                   + "precio_biopreparado, fecha_creacion, fecha_vencimiento, preparacion_biopreparado "
                   + "FROM biopreparados WHERE id_biopreparado = ?";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    biopreparado b = mapear(rs);
                    b.setIngredientes(listarIngredientes(con, id));
                    return b;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener biopreparado id=" + id + ": " + e.getMessage());
        }
        return null;
    }

    // ── Registrar biopreparado + ingredientes (transacción) ───────────────────

    public boolean registrarBiopreparado(biopreparado b, List<ingredienteBio> ingredientes) {
        Connection con = null;
        try {
            con = clase_Conexion.MetodoConectar();
            con.setAutoCommit(false);

            String sql = "INSERT INTO biopreparados "
                       + "(nombre_biopreparado, descripcion_biopreparado, precio_biopreparado, "
                       + " fecha_creacion, fecha_vencimiento, preparacion_biopreparado) "
                       + "VALUES (?, ?, ?, ?, ?, ?)";

            int idGenerado;
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, b.getNombre());
                ps.setString(2, b.getDescripcion());
                ps.setDouble(3, b.getPrecio());
                ps.setDate(4, b.getFechaCreacion());
                ps.setDate(5, b.getFechaVencimiento());
                ps.setString(6, b.getPreparacion());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                    } else {
                        con.rollback();
                        return false;
                    }
                }
            }

            insertarIngredientes(con, idGenerado, ingredientes);
            con.commit();
            return true;

        } catch (SQLException e) {
            rollback(con);
            System.err.println("Error al registrar biopreparado: " + e.getMessage());
            return false;
        } finally {
            cerrar(con);
        }
    }

    // ── Actualizar biopreparado + reemplazar ingredientes (transacción) ────────

    public boolean actualizarBiopreparado(int id, biopreparado b, List<ingredienteBio> ingredientes) {
        Connection con = null;
        try {
            con = clase_Conexion.MetodoConectar();
            con.setAutoCommit(false);

            String sql = "UPDATE biopreparados SET "
                       + "nombre_biopreparado = ?, descripcion_biopreparado = ?, "
                       + "precio_biopreparado = ?, fecha_creacion = ?, "
                       + "fecha_vencimiento = ?, preparacion_biopreparado = ? "
                       + "WHERE id_biopreparado = ?";

            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setString(1, b.getNombre());
                ps.setString(2, b.getDescripcion());
                ps.setDouble(3, b.getPrecio());
                ps.setDate(4, b.getFechaCreacion());
                ps.setDate(5, b.getFechaVencimiento());
                ps.setString(6, b.getPreparacion());
                ps.setInt(7, id);
                ps.executeUpdate();
            }

            // Reemplazar ingredientes: eliminar los viejos e insertar los nuevos
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM ingredientes_biopreparados WHERE id_biopreparado = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            insertarIngredientes(con, id, ingredientes);
            con.commit();
            return true;

        } catch (SQLException e) {
            rollback(con);
            System.err.println("Error al actualizar biopreparado id=" + id + ": " + e.getMessage());
            return false;
        } finally {
            cerrar(con);
        }
    }

    // ── Eliminar biopreparado (CASCADE elimina ingredientes) ──────────────────

    public boolean eliminarBiopreparado(int id) {
        String sql = "DELETE FROM biopreparados WHERE id_biopreparado = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar biopreparado id=" + id + ": " + e.getMessage());
            return false;
        }
    }

    // ── Métodos privados de apoyo ─────────────────────────────────────────────

    /** Mapea una fila del ResultSet a un objeto biopreparado. */
    private biopreparado mapear(ResultSet rs) throws SQLException {
        biopreparado b = new biopreparado();
        b.setIdBiopreparado(rs.getInt("id_biopreparado"));
        b.setNombre(rs.getString("nombre_biopreparado"));
        b.setDescripcion(rs.getString("descripcion_biopreparado"));
        b.setPrecio(rs.getDouble("precio_biopreparado"));
        b.setFechaCreacion(rs.getDate("fecha_creacion"));
        b.setFechaVencimiento(rs.getDate("fecha_vencimiento"));
        b.setPreparacion(rs.getString("preparacion_biopreparado"));
        return b;
    }

    /** Lista los ingredientes de un biopreparado reutilizando la conexión abierta. */
    private List<ingredienteBio> listarIngredientes(Connection con, int idBio) throws SQLException {
        List<ingredienteBio> lista = new ArrayList<>();
        String sql = "SELECT id_ingrediente, nombre_ingrediente, "
                   + "cantidad_ingredientes, unidad_ingredientes "
                   + "FROM ingredientes_biopreparados WHERE id_biopreparado = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idBio);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ingredienteBio ing = new ingredienteBio();
                    ing.setIdIngrediente(rs.getInt("id_ingrediente"));
                    ing.setNombre(rs.getString("nombre_ingrediente"));
                    ing.setCantidad(rs.getDouble("cantidad_ingredientes"));
                    ing.setUnidad(rs.getString("unidad_ingredientes"));
                    lista.add(ing);
                }
            }
        }
        return lista;
        
    }

    /** Inserta la lista de ingredientes para un biopreparado dado. */
    private void insertarIngredientes(Connection con, int idBio,
                                      List<ingredienteBio> ingredientes) throws SQLException {
        if (ingredientes == null || ingredientes.isEmpty()) return;
        String sql = "INSERT INTO ingredientes_biopreparados "
                   + "(nombre_ingrediente, cantidad_ingredientes, unidad_ingredientes, id_biopreparado) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (ingredienteBio ing : ingredientes) {
                if (ing.getNombre() == null || ing.getNombre().isBlank()) continue;
                ps.setString(1, ing.getNombre());
                ps.setDouble(2, ing.getCantidad());
                ps.setString(3, ing.getUnidad());
                ps.setInt(4, idBio);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void rollback(Connection con) {
        if (con != null) { try { con.rollback(); } catch (SQLException e) { e.printStackTrace(); } }
    }

    private void cerrar(Connection con) {
        if (con != null) { try { con.close(); } catch (SQLException e) { e.printStackTrace(); } }
    }
}