package com.paujil.dao;

import com.paujil.modelo.biopreparado;
import com.paujil.modelo.biopreparado.ingredienteBio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import paujil.basedatos.clase_Conexion;

public class BiopreparadoDao {

    //  Listar todos los biopreparados 

    public List<biopreparado> listarBiopreparados() {
        List<biopreparado> lista = new ArrayList<>();
        // ORDER BY garantiza un orden estable en la UI independientemente del orden fisico en BD
        String sql = "SELECT id_biopreparado, nombre_biopreparado, descripcion_biopreparado, "
                   + "precio_biopreparado, fecha_creacion, fecha_vencimiento, preparacion_biopreparado "
                   + "FROM biopreparados ORDER BY id_biopreparado ASC";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // mapear() encapsula la traduccion de columnas SQL a propiedades del objeto
                biopreparado b = mapear(rs);
                // Reutiliza la misma conexion abierta para cargar los ingredientes sin abrir otra
                b.setIngredientes(listarIngredientes(con, b.getIdBiopreparado()));
                lista.add(b);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar biopreparados: " + e.getMessage());
        }
        return lista;
    }

    //  Obtener biopreparado por ID 

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
                    // Carga los ingredientes en la misma conexion para evitar abrir una nueva transaccion
                    b.setIngredientes(listarIngredientes(con, id));
                    return b;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener biopreparado id=" + id + ": " + e.getMessage());
        }
        // Retorna null si el ID no existe; el caller debe comprobar antes de usar el resultado
        return null;
    }

    //  Registrar biopreparado + ingredientes (transaccion) 

    public boolean registrarBiopreparado(biopreparado b, List<ingredienteBio> ingredientes) {
        Connection con = null;
        try {
            con = clase_Conexion.MetodoConectar();
            // Desactiva el autocommit para envolver biopreparado e ingredientes en una sola transaccion
            con.setAutoCommit(false);

            String sql = "INSERT INTO biopreparados "
                       + "(nombre_biopreparado, descripcion_biopreparado, precio_biopreparado, "
                       + " fecha_creacion, fecha_vencimiento, preparacion_biopreparado) "
                       + "VALUES (?, ?, ?, ?, ?, ?)";

            int idGenerado;
            // RETURN_GENERATED_KEYS recupera el ID autoincremental asignado por BD
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
                        // El ID generado es necesario para asociar los ingredientes al biopreparado recien creado
                        idGenerado = rs.getInt(1);
                    } else {
                        // Si BD no devuelve el ID, no se pueden insertar ingredientes; se revierte todo
                        con.rollback();
                        return false;
                    }
                }
            }

            // Inserta los ingredientes usando el ID obtenido; falla aqui revierte tambien el biopreparado
            insertarIngredientes(con, idGenerado, ingredientes);
            // Confirma ambas inserciones como una unidad atomica
            con.commit();
            return true;

        } catch (SQLException e) {
            // Cualquier excepcion deshace biopreparado e ingredientes para evitar registros huerfanos
            rollback(con);
            System.err.println("Error al registrar biopreparado: " + e.getMessage());
            return false;
        } finally {
            // Cierra la conexion en finally para garantizar liberacion aunque ocurra una excepcion
            cerrar(con);
        }
    }

    //  Actualizar biopreparado + reemplazar ingredientes (transaccion) 

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
                // El ID va al final para coincidir con la posicion del parametro en el WHERE
                ps.setInt(7, id);
                ps.executeUpdate();
            }

            // Estrategia delete+insert: mas simple que comparar diferencias entre lista vieja y nueva
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM ingredientes_biopreparados WHERE id_biopreparado = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            // Inserta la lista nueva tras borrar la anterior; ambas operaciones forman una sola transaccion
            insertarIngredientes(con, id, ingredientes);
            con.commit();
            return true;

        } catch (SQLException e) {
            // Revierte tanto el UPDATE del biopreparado como el DELETE de ingredientes si algo falla
            rollback(con);
            System.err.println("Error al actualizar biopreparado id=" + id + ": " + e.getMessage());
            return false;
        } finally {
            cerrar(con);
        }
    }

    //  Eliminar biopreparado (CASCADE elimina ingredientes) 

    public boolean eliminarBiopreparado(int id) {
        String sql = "DELETE FROM biopreparados WHERE id_biopreparado = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            // executeUpdate > 0 confirma que el registro existia y fue eliminado
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar biopreparado id=" + id + ": " + e.getMessage());
            return false;
        }
    }

    //  Metodos privados de apoyo 

    // Centraliza el mapeo de columnas a propiedades para no repetirlo en cada metodo de consulta
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

    // Recibe la conexion del caller para participar en la transaccion activa sin abrir una nueva
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

    // Usa addBatch/executeBatch para insertar todos los ingredientes en una sola ida a BD
    private void insertarIngredientes(Connection con, int idBio,
                                      List<ingredienteBio> ingredientes) throws SQLException {
        // Lista vacia o nula es valida; un biopreparado puede no tener ingredientes registrados
        if (ingredientes == null || ingredientes.isEmpty()) return;
        String sql = "INSERT INTO ingredientes_biopreparados "
                   + "(nombre_ingrediente, cantidad_ingredientes, unidad_ingredientes, id_biopreparado) "
                   + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (ingredienteBio ing : ingredientes) {
                // Ingredientes sin nombre se omiten para evitar registros invalidos en BD
                if (ing.getNombre() == null || ing.getNombre().isBlank()) continue;
                ps.setString(1, ing.getNombre());
                ps.setDouble(2, ing.getCantidad());
                ps.setString(3, ing.getUnidad());
                // Vincula cada ingrediente al biopreparado padre mediante su ID
                ps.setInt(4, idBio);
                // addBatch acumula el insert; executeBatch los envia todos juntos al final
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // Intenta revertir la transaccion activa; imprime el error si el rollback mismo falla
    private void rollback(Connection con) {
        if (con != null) { try { con.rollback(); } catch (SQLException e) { e.printStackTrace(); } }
    }

    // Libera la conexion de vuelta al pool; se llama siempre en el bloque finally
    private void cerrar(Connection con) {
        if (con != null) { try { con.close(); } catch (SQLException e) { e.printStackTrace(); } }
    }
}