package com.paujil.dao;

import com.paujil.modelo.cultivo;
import java.sql.*; 
import java.util.ArrayList; 
import java.util.List; 
import paujil.basedatos.clase_Conexion; 

public class CultivoDao {

    // Método para listar todos los registros incluyendo el id_lote
    public List<cultivo> listarCultivos() {
        List<cultivo> lista = new ArrayList<>();
        // CAMBIO: Se añadió id_lote a la consulta
        String sql = "SELECT id_cultivo, nombre_cultivo, tipo_cultivo, fecha_siembra, fecha_cosecha, id_lote FROM cultivos";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                cultivo c = new cultivo();
                c.setIdCultivo(rs.getInt("id_cultivo"));
                c.setNombreCultivo(rs.getString("nombre_cultivo"));
                c.setTipoCultivo(rs.getString("tipo_cultivo"));
                c.setFechaSiembra(rs.getDate("fecha_siembra"));
                c.setFechaCosecha(rs.getDate("fecha_cosecha"));
                c.setIdLote(rs.getInt("id_lote")); // CAMBIO: Seteamos el id_lote
                lista.add(c);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar cultivos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    // Método para insertar incluyendo el id_lote
    // Cambia esto en registrarCultivo y actualizarCultivo
        public int registrarCultivo(cultivo c) throws SQLException { // <--- AÑADE throws SQLException
            String sql = "INSERT INTO cultivos (nombre_cultivo, tipo_cultivo, fecha_siembra, fecha_cosecha, id_lote) VALUES (?, ?, ?, ?, ?)";

            // Eliminamos el try-catch interno aquí. 
            // La conexión se cierra sola gracias al try-with-resources.
            try (Connection con = clase_Conexion.MetodoConectar();
                 PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                ps.setString(1, c.getNombreCultivo());
                ps.setString(2, c.getTipoCultivo());
                ps.setDate(3, c.getFechaSiembra());
                if (c.getFechaCosecha() != null) ps.setDate(4, c.getFechaCosecha());
                else ps.setNull(4, Types.DATE);
                ps.setInt(5, c.getIdLote());

                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    return rs.next() ? rs.getInt(1) : -1;
                }
            }
            // Si ocurre un error, la excepción sube automáticamente al Servlet
        }

    // Método para actualizar incluyendo el id_lote
        // Quitamos el try-catch interno y añadimos 'throws SQLException'
        public boolean actualizarCultivo(int id, String nombre, String tipo, Date siembra, Date cosecha, int idLote) throws SQLException {
            String sql = "UPDATE cultivos SET nombre_cultivo = ?, tipo_cultivo = ?, fecha_siembra = ?, fecha_cosecha = ?, id_lote = ? WHERE id_cultivo = ?";

            // El try-with-resources se encarga de cerrar la conexión incluso si hay error
            try (Connection con = clase_Conexion.MetodoConectar();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, nombre);
                ps.setString(2, tipo);
                ps.setDate(3, siembra);

                if (cosecha != null) ps.setDate(4, cosecha);
                else ps.setNull(4, Types.DATE);

                ps.setInt(5, idLote);
                ps.setInt(6, id);

                return ps.executeUpdate() > 0;
            } 
            // NO hay catch aquí, la SQLException sube al Servlet automáticamente
        }

    // Método para eliminar simplificado
    public boolean eliminarCultivo(int id) {
        // CAMBIO: Ya no necesitamos limpiar tablas intermedias (cultivo_lote)
        String sql = "DELETE FROM cultivos WHERE id_cultivo = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Método buscarPorId ajustado
    public cultivo buscarPorId(int id) {
        String sql = "SELECT * FROM cultivos WHERE id_cultivo = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cultivo c = new cultivo();
                    c.setIdCultivo(rs.getInt("id_cultivo"));
                    c.setNombreCultivo(rs.getString("nombre_cultivo"));
                    c.setTipoCultivo(rs.getString("tipo_cultivo"));
                    c.setFechaSiembra(rs.getDate("fecha_siembra"));
                    c.setFechaCosecha(rs.getDate("fecha_cosecha"));
                    c.setIdLote(rs.getInt("id_lote")); // CAMBIO: Mapeamos id_lote
                    return c;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}