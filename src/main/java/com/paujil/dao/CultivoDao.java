package com.paujil.dao;

import com.paujil.modelo.cultivo;
import java.sql.*; 
import java.util.ArrayList; 
import java.util.List; 
import paujil.basedatos.clase_Conexion; 

public class CultivoDao { // Clase DAO para operaciones CRUD de cultivos.

    // Método para listar todos los registros de la tabla cultivos.
    public List<cultivo> listarCultivos() { // Inicio del método.
        List<cultivo> lista = new ArrayList<>(); // Inicializa la lista resultado.
        String sql = "SELECT id_cultivo, nombre_cultivo, tipo_cultivo, fecha_siembra, fecha_cosecha FROM cultivos"; // Query SQL.

        try (Connection con = clase_Conexion.MetodoConectar(); // Establece conexión.
             PreparedStatement ps = con.prepareStatement(sql); // Prepara sentencia.
             ResultSet rs = ps.executeQuery()) { // Ejecuta consulta.

            while (rs.next()) { // Itera mientras existan filas.
                cultivo c = new cultivo(); // Crea nueva instancia de modelo.
                c.setIdCultivo(rs.getInt("id_cultivo")); // Setea ID desde BD.
                c.setNombreCultivo(rs.getString("nombre_cultivo")); // Setea nombre.
                c.setTipoCultivo(rs.getString("tipo_cultivo")); // Setea tipo.
                c.setFechaSiembra(rs.getDate("fecha_siembra")); // Setea fecha siembra.
                c.setFechaCosecha(rs.getDate("fecha_cosecha")); // Setea fecha cosecha.
                lista.add(c); // Agrega objeto a la lista.
            } // Fin while.
        } catch (SQLException e) { // Manejo de errores SQL.
            System.err.println("Error al listar cultivos: " + e.getMessage()); // Imprime error en log.
            e.printStackTrace(); // Imprime traza del error.
        } // Fin catch.
        return lista; // Retorna lista poblada o vacía.
    } // Fin método listarCultivos.

    // Método para insertar un nuevo cultivo en la tabla.
    public boolean registrarCultivo(cultivo c) { // Inicio método registrar.
        String sql = "INSERT INTO cultivos (nombre_cultivo, tipo_cultivo, fecha_siembra, fecha_cosecha) " + "VALUES (?, ?, ?, ?)"; // SQL insert.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta a la BD.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara sentencia.
            ps.setString(1, c.getNombreCultivo()); // Setea parámetro 1.
            ps.setString(2, c.getTipoCultivo()); // Setea parámetro 2.
            ps.setDate(3, c.getFechaSiembra()); // Setea parámetro 3.
            if (c.getFechaCosecha() != null) { // Verifica si hay fecha de cosecha.
                ps.setDate(4, c.getFechaCosecha()); // Si existe, la setea.
            } else { // Si es nula.
                ps.setNull(4, Types.DATE); // Setea valor null en BD.
            } // Fin if.
            return ps.executeUpdate() > 0; // Ejecuta e indica si hubo éxito.
        } catch (SQLException e) { // Manejo de errores.
            System.err.println("Error al registrar cultivo: " + e.getMessage()); // Error log.
            e.printStackTrace(); // Imprime traza.
            return false; // Retorna fallo.
        } // Fin catch.
    } // Fin método registrarCultivo.

    // Método para actualizar los datos de un cultivo existente.
    public boolean actualizarCultivo(int id, String nombre, String tipo, Date siembra, Date cosecha) { // Inicio actualizar.
        String sql = "UPDATE cultivos SET nombre_cultivo = ?, tipo_cultivo = ?, fecha_siembra = ?, fecha_cosecha = ? WHERE id_cultivo = ?"; // SQL update.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara statement.
            ps.setString(1, nombre); // Setea nombre.
            ps.setString(2, tipo); // Setea tipo.
            ps.setDate(3, siembra); // Setea siembra.
            if (cosecha != null) { // Verifica fecha cosecha.
                ps.setDate(4, cosecha); // Setea fecha cosecha.
            } else { // Si es nula.
                ps.setNull(4, Types.DATE); // Setea null.
            } // Fin if.
            ps.setInt(5, id); // Setea ID para el WHERE.
            return ps.executeUpdate() > 0; // Ejecuta e indica éxito.
        } catch (SQLException e) { // Manejo errores.
            System.err.println("Error al actualizar cultivo id=" + id + ": " + e.getMessage()); // Error log.
            e.printStackTrace(); // Imprime traza.
            return false; // Retorna fallo.
        } // Fin catch.
    } // Fin método actualizarCultivo.

    // Método para eliminar un cultivo por su ID.
    public boolean eliminarCultivo(int id) { // Inicio método eliminar.
        String sql = "DELETE FROM cultivos WHERE id_cultivo = ?"; // SQL delete.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setInt(1, id); // Setea ID para eliminar.
            return ps.executeUpdate() > 0; // Ejecuta e indica éxito.
        } catch (SQLException e) { // Manejo errores.
            System.err.println("Error al eliminar cultivo id=" + id + ": " + e.getMessage()); // Error log.
            e.printStackTrace(); // Imprime traza.
            return false; // Retorna fallo.
        } // Fin catch.
    } // Fin método eliminarCultivo.

    // Método para buscar un cultivo específico por su ID.
    public cultivo buscarPorId(int id) { // Inicio método buscar.
        String sql = "SELECT id_cultivo, nombre_cultivo, tipo_cultivo, fecha_siembra, fecha_cosecha FROM cultivos WHERE id_cultivo = ?"; // SQL select filtrado.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setInt(1, id); // Setea parámetro ID.
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta query.
                if (rs.next()) { // Verifica si hay resultado.
                    cultivo c = new cultivo(); // Crea objeto cultivo.
                    c.setIdCultivo(rs.getInt("id_cultivo")); // Mapea ID.
                    c.setNombreCultivo(rs.getString("nombre_cultivo")); // Mapea nombre.
                    c.setTipoCultivo(rs.getString("tipo_cultivo")); // Mapea tipo.
                    c.setFechaSiembra(rs.getDate("fecha_siembra")); // Mapea siembra.
                    c.setFechaCosecha(rs.getDate("fecha_cosecha")); // Mapea cosecha.
                    return c; // Retorna objeto encontrado.
                } // Fin if.
            } // Cierre ResultSet.
        } catch (SQLException e) { // Manejo errores.
            System.err.println("Error al buscar cultivo id=" + id + ": " + e.getMessage()); // Error log.
            e.printStackTrace(); // Imprime traza.
        } // Fin catch.
        return null; // Retorna null si no se encuentra.
    } // Fin método buscarPorId.
} // Fin clase CultivoDao.