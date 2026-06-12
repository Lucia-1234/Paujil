package com.paujil.dao;

import com.paujil.modelo.cultivo;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import paujil.basedatos.clase_Conexion;

public class CultivoDao {

    //  Listar todos los cultivos 
    public List<cultivo> listarCultivos() {
        List<cultivo> lista = new ArrayList<>();
        String sql = "SELECT id_cultivo, nombre_cultivo, tipo_cultivo, fecha_siembra, fecha_cosecha FROM cultivos";

        // try-with-resources garantiza cierre de conexion, statement y resultset aunque ocurra una excepcion
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Cada iteracion construye un objeto cultivo mapeando las columnas por nombre
                cultivo c = new cultivo();
                c.setIdCultivo(rs.getInt("id_cultivo"));
                c.setNombreCultivo(rs.getString("nombre_cultivo"));
                c.setTipoCultivo(rs.getString("tipo_cultivo"));
                c.setFechaSiembra(rs.getDate("fecha_siembra"));
                // fecha_cosecha puede ser null si el cultivo aun no ha sido cosechado
                c.setFechaCosecha(rs.getDate("fecha_cosecha"));
                lista.add(c);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar cultivos: " + e.getMessage());
            e.printStackTrace();
        }
        // Retorna lista vacia en caso de error para que el caller no necesite comprobar null
        return lista;
    }

    // Cuenta los trabajos (asignaciones) registrados para un cultivo; usado para mostrar
    // badges en la vista sin consultas adicionales en la JSP.
    // NOTA DE MIGRACIÓN: la tabla "trabajos_realizados" fue reemplazada por "asignaciones".
    // Este conteo ahora se hace sobre la tabla asignaciones (cada fila = un trabajo
    // asignado/realizado sobre el cultivo). Equivalente a AsignacionDao.contarPorCultivo.
    public int contarPorCultivo(int idCultivo) {
        String sql = "SELECT COUNT(*) FROM asignaciones WHERE id_cultivo = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idCultivo);
            try (ResultSet rs = ps.executeQuery()) {
                // COUNT(*) siempre devuelve una fila; rs.getInt(1) lee la primera columna del resultado
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error al contar: " + e.getMessage());
        }
        // 0 como valor seguro permite que la vista muestre el badge sin manejar null
        return 0;
    }

    //  Registrar cultivo nuevo 
    public boolean registrarCultivo(cultivo c) {
        String sql = "INSERT INTO cultivos (nombre_cultivo, tipo_cultivo, fecha_siembra, fecha_cosecha) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombreCultivo());
            ps.setString(2, c.getTipoCultivo());
            ps.setDate(3, c.getFechaSiembra());
            // setNull es necesario porque setDate(null) lanza NullPointerException en algunos drivers JDBC
            if (c.getFechaCosecha() != null) {
                ps.setDate(4, c.getFechaCosecha());
            } else {
                ps.setNull(4, Types.DATE);
            }

            // executeUpdate > 0 confirma que la insercion afecto al menos una fila
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al registrar cultivo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    //  Actualizar cultivo existente 
    // Los nombres de columna usan snake_case del esquema SQL, no camelCase de los getters del modelo
    public boolean actualizarCultivo(int id, String nombre, String tipo,
                                     Date siembra, Date cosecha) {

        String sql = "UPDATE cultivos "
                   + "SET nombre_cultivo = ?, tipo_cultivo = ?, fecha_siembra = ?, fecha_cosecha = ? "
                   + "WHERE id_cultivo = ?";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setString(2, tipo);
            ps.setDate(3, siembra);
            // Permite borrar una fecha de cosecha previamente registrada pasando null al UPDATE
            if (cosecha != null) {
                ps.setDate(4, cosecha);
            } else {
                ps.setNull(4, Types.DATE);
            }
            // El ID va en la ultima posicion para coincidir con el WHERE al final del SQL
            ps.setInt(5, id);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar cultivo id=" + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    //  Eliminar cultivo 
    public boolean eliminarCultivo(int id) {
        String sql = "DELETE FROM cultivos WHERE id_cultivo = ?";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            // executeUpdate > 0 verifica que el cultivo existia; 0 indica que el ID no se encontro
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar cultivo id=" + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    //  Buscar cultivo por ID 
    // Usado principalmente para pre-llenar el modal de edicion desde el servlet
    public cultivo buscarPorId(int id) {
        String sql = "SELECT id_cultivo, nombre_cultivo, tipo_cultivo, fecha_siembra, fecha_cosecha "
                   + "FROM cultivos WHERE id_cultivo = ?";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                // Se espera como maximo una fila ya que id_cultivo es clave primaria
                if (rs.next()) {
                    cultivo c = new cultivo();
                    c.setIdCultivo(rs.getInt("id_cultivo"));
                    c.setNombreCultivo(rs.getString("nombre_cultivo"));
                    c.setTipoCultivo(rs.getString("tipo_cultivo"));
                    c.setFechaSiembra(rs.getDate("fecha_siembra"));
                    c.setFechaCosecha(rs.getDate("fecha_cosecha"));
                    return c;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar cultivo id=" + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        // Retorna null si el ID no existe; el caller debe verificarlo antes de usar el resultado
        return null;
    }
}