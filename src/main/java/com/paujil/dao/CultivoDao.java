package com.paujil.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import paujil.basedatos.clase_Conexion;
import com.paujil.modelo.cultivo;

public class CultivoDao {

    // 1. REGISTRAR CULTIVO PRINCIPAL (Retorna el ID autoincremental generado)
    public int registrarCultivoBase(cultivo c) {
        String sql = "INSERT INTO cultivos (nombre_cultivo, tipo_cultivo, fecha_siembra, fecha_cosecha, area) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (con == null) return 0;
            
            ps.setString(1, c.getNombreCultivo());
            ps.setString(2, c.getTipoCultivo());
            ps.setDate(3, c.getFechaSiembra());
            if (c.getFechaCosecha() != null) {
                ps.setDate(4, c.getFechaCosecha());
            } else {
                ps.setNull(4, Types.DATE);
            }
            ps.setBigDecimal(5, c.getArea());
            
            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // Métodos de inserción en tablas intermedias
    public boolean asignarLote(int idC, int idL) {
        String sql = "INSERT INTO cultivo_lote (id_cultivo, id_lote) VALUES (?, ?)";
        try (Connection con = clase_Conexion.MetodoConectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idC); ps.setInt(2, idL); return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public boolean asignarTrabajador(int idC, int idU) {
        String sql = "INSERT INTO cultivo_trabajador (id_cultivo, id_usuario) VALUES (?, ?)";
        try (Connection con = clase_Conexion.MetodoConectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idC); ps.setInt(2, idU); return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    public boolean asignarInsumo(int idC, int idI) {
        String sql = "INSERT INTO cultivo_insumo (id_cultivo, id_insumo) VALUES (?, ?)";
        try (Connection con = clase_Conexion.MetodoConectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idC); ps.setInt(2, idI); return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    // 2. LISTAR CON UNIÓN DE TABLAS (Para mostrar la info real en tus Tarjetas BEM)
    // Usamos JOINS manuales sin ORM para saber el lote y el insumo de cada cultivo
    public List<cultivo> listarConRelaciones() {
        List<cultivo> lista = new ArrayList<>();
        String sql = "SELECT c.*, l.nombre_lote, i.nombre_insumo FROM cultivos c " +
                     "LEFT JOIN cultivo_lote cl ON c.id_cultivo = cl.id_cultivo " +
                     "LEFT JOIN lotes l ON cl.id_lote = l.id_lote " +
                     "LEFT JOIN cultivo_insumo ci ON c.id_cultivo = ci.id_cultivo " +
                     "LEFT JOIN insumos i ON ci.id_insumo = i.id_insumo";
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
                c.setArea(rs.getBigDecimal("area"));
                
                // Estos campos los transportaremos de forma temporal usando variables extendidas en el modelo
                c.setNombreLoteAux(rs.getString("nombre_lote"));
                c.setNombreInsumoAux(rs.getString("nombre_insumo"));
                lista.add(c);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    // 3. ACTUALIZAR PRODUCTO (EDITAR)
    public boolean actualizar(cultivo c) {
        String sql = "UPDATE cultivos SET nombre_cultivo=?, tipo_cultivo=?, fecha_siembra=?, fecha_cosecha=?, area=? WHERE id_cultivo=?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, c.getNombreCultivo());
            ps.setString(2, c.getTipoCultivo());
            ps.setDate(3, c.getFechaSiembra());
            if (c.getFechaCosecha() != null) ps.setDate(4, c.getFechaCosecha()); else ps.setNull(4, Types.DATE);
            ps.setBigDecimal(5, c.getArea());
            ps.setInt(6, c.getIdCultivo());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 4. ELIMINAR CULTIVO (En cascada manual para limpiar tablas intermedias primero)
    public boolean eliminar(int idCultivo) {
        try (Connection con = clase_Conexion.MetodoConectar()) {
            if (con == null) return false;
            con.setAutoCommit(false); // Transacción manual
            
            // Limpiamos referencias de tablas intermedias primero para evitar errores de Foreign Key
            try (PreparedStatement ps1 = con.prepareStatement("DELETE FROM cultivo_lote WHERE id_cultivo=?")) { ps1.setInt(1, idCultivo); ps1.executeUpdate(); }
            try (PreparedStatement ps2 = con.prepareStatement("DELETE FROM cultivo_trabajador WHERE id_cultivo=?")) { ps2.setInt(1, idCultivo); ps2.executeUpdate(); }
            try (PreparedStatement ps3 = con.prepareStatement("DELETE FROM cultivo_insumo WHERE id_cultivo=?")) { ps3.setInt(1, idCultivo); ps3.executeUpdate(); }
            
            // Finalmente eliminamos el cultivo base
            try (PreparedStatement psBase = con.prepareStatement("DELETE FROM cultivos WHERE id_cultivo=?")) {
                psBase.setInt(1, idCultivo);
                psBase.executeUpdate();
            }
            con.commit();
            return true;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
    
    // Buscar un solo cultivo por su ID para precargar el formulario de edición
    public cultivo buscarPorId(int id) {
        String sql = "SELECT * FROM cultivos WHERE id_cultivo = ?";
        try (Connection con = clase_Conexion.MetodoConectar(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    cultivo c = new cultivo();
                    c.setIdCultivo(rs.getInt("id_cultivo"));
                    c.setNombreCultivo(rs.getString("nombre_cultivo"));
                    c.setTipoCultivo(rs.getString("tipo_cultivo"));
                    c.setFechaSiembra(rs.getDate("fecha_siembra"));
                    c.setFechaCosecha(rs.getDate("fecha_cosecha"));
                    c.setArea(rs.getBigDecimal("area"));
                    return c;
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }
}

