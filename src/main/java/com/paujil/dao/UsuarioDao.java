package com.paujil.dao;

import com.paujil.modelo.usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import paujil.basedatos.clase_Conexion;

public class UsuarioDao {

    // ── Verificar duplicados ──────────────────────────────────────────────────

    public boolean correoExiste(String correo) {
        String sql = "SELECT id_correo FROM correos WHERE direccion_correo = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar correo: " + e.getMessage());
        }
        return false;
    }

    public boolean telefonoExiste(String numero) {
        String sql = "SELECT id_telefono FROM telefonos WHERE numero_telefono = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar teléfono: " + e.getMessage());
        }
        return false;
    }

    // ── Listar usuarios pendientes de aprobación ──────────────────────────────
    // Se usa en la vista del administrador para aprobar o denegar registros.
    // Incluye correo y rol solicitado para que el admin tenga contexto completo.
    public List<usuario> listarUsuariosPendientes() {
        List<usuario> lista = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.fecha_nacimiento, "
                   + "       u.direccion_usuario, c.direccion_correo, "
                   + "       t.numero_telefono, r.nombre_rol "
                   + "FROM usuarios u "
                   + "INNER JOIN correos c    ON u.id_usuario = c.id_usuario "
                   + "INNER JOIN usuario_rol ur ON u.id_usuario = ur.id_usuario "
                   + "INNER JOIN roles r      ON ur.id_rol = r.id_rol "
                   + "LEFT  JOIN telefonos t  ON u.id_usuario = t.id_usuario "
                   + "WHERE u.estado_usuario = 'Pendiente'";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuario u = new usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNombre(rs.getString("nombre_usuario"));
                u.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                u.setDireccion(rs.getString("direccion_usuario"));
                u.setCorreo(rs.getString("direccion_correo"));
                u.setTelefono(rs.getString("numero_telefono"));
                u.setRol(rs.getString("nombre_rol"));   // campo de vista
                lista.add(u);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar usuarios pendientes: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    // ── Listar usuarios activos (para asignar trabajos, etc.) ─────────────────
    public List<usuario> listarUsuariosActivos() {
        List<usuario> lista = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.nombre_usuario, c.direccion_correo, "
                   + "       t.numero_telefono, r.nombre_rol "
                   + "FROM usuarios u "
                   + "INNER JOIN correos c      ON u.id_usuario = c.id_usuario "
                   + "INNER JOIN usuario_rol ur  ON u.id_usuario = ur.id_usuario "
                   + "INNER JOIN roles r         ON ur.id_rol = r.id_rol "
                   + "LEFT  JOIN telefonos t     ON u.id_usuario = t.id_usuario "
                   + "WHERE u.estado_usuario = 'Activo' "
                   + "ORDER BY u.nombre_usuario ASC";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuario u = new usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNombre(rs.getString("nombre_usuario"));
                u.setCorreo(rs.getString("direccion_correo"));
                u.setTelefono(rs.getString("numero_telefono"));
                u.setRol(rs.getString("nombre_rol"));
                lista.add(u);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar usuarios activos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    // ── Actualizar estado del usuario ─────────────────────────────────────────
    // Estados válidos según el esquema: 'Activo', 'Pendiente', 'Rechazado'
    public boolean actualizarEstado(int idUsuario, String nuevoEstado) {
        String sql = "UPDATE usuarios SET estado_usuario = ? WHERE id_usuario = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al actualizar estado del usuario id="
                    + idUsuario + ": " + e.getMessage());
            return false;
        }
    }

    // ── Eliminar usuario completo ─────────────────────────────────────────────
    // ON DELETE CASCADE en el esquema se encarga de limpiar correos, telefonos
    // y usuario_rol automáticamente. No hace falta borrarlos manualmente.
    public boolean eliminarUsuario(int idUsuario) {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario id=" + idUsuario
                    + ": " + e.getMessage());
            return false;
        }
    }

    // ── Registrar usuario completo (transacción de 4 tablas) ─────────────────
    public boolean registrarUsuarioCompleto(String nombre, String correo, String telefono,
                                            Date fechaNac, String dir,
                                            String passHash, int idRol) {
        Connection con = null;
        try {
            con = clase_Conexion.MetodoConectar();
            con.setAutoCommit(false);

            // 1. Insertar usuario — estado inicial 'Pendiente' hasta que el admin apruebe
            int idGenerado;
            String sqlUser = "INSERT INTO usuarios "
                           + "(nombre_usuario, fecha_nacimiento, direccion_usuario, "
                           + " contrasena_usuario, estado_usuario) "
                           + "VALUES (?, ?, ?, ?, 'Pendiente')";

            try (PreparedStatement ps = con.prepareStatement(
                    sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombre);
                ps.setDate(2, fechaNac);
                ps.setString(3, dir);
                ps.setString(4, passHash);
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

            // 2. Insertar correo
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO correos (id_usuario, direccion_correo) VALUES (?, ?)")) {
                ps.setInt(1, idGenerado);
                ps.setString(2, correo);
                ps.executeUpdate();
            }

            // 3. Insertar teléfono
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO telefonos (id_usuario, numero_telefono) VALUES (?, ?)")) {
                ps.setInt(1, idGenerado);
                ps.setString(2, telefono);
                ps.executeUpdate();
            }

            // 4. Insertar rol
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO usuario_rol (id_usuario, id_rol) VALUES (?, ?)")) {
                ps.setInt(1, idGenerado);
                ps.setInt(2, idRol);
                ps.executeUpdate();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("Error al registrar usuario: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            if (con != null) {
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}