package com.paujil.dao;

import com.paujil.modelo.usuario; 
import java.sql.*; 
import java.util.ArrayList; 
import java.util.List; 
import paujil.basedatos.clase_Conexion; 

public class UsuarioDao { // Clase DAO para gestión de usuarios.

    // Método para verificar si un correo ya existe en BD.
    public boolean correoExiste(String correo) { // Inicio método.
        String sql = "SELECT id_correo FROM correos WHERE direccion_correo = ?"; // SQL búsqueda.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setString(1, correo); // Setea correo.
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta consulta.
                return rs.next(); // Retorna true si encuentra coincidencia.
            } // Cierra RS.
        } catch (SQLException e) { // Manejo errores.
            System.err.println("Error al verificar correo: " + e.getMessage()); // Log error.
        } // Fin catch.
        return false; // Retorna false por defecto.
    } // Fin método.

    // Método para verificar si un teléfono ya existe.
    public boolean telefonoExiste(String numero) { // Inicio método.
        String sql = "SELECT id_telefono FROM telefonos WHERE numero_telefono = ?"; // SQL búsqueda.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setString(1, numero); // Setea número.
            try (ResultSet rs = ps.executeQuery()) { // Ejecuta consulta.
                return rs.next(); // Retorna existencia.
            } // Cierra RS.
        } catch (SQLException e) { // Manejo errores.
            System.err.println("Error al verificar telefono: " + e.getMessage()); // Log error.
        } // Fin catch.
        return false; // Retorna false por defecto.
    } // Fin método.

    // Lista usuarios en estado 'Pendiente'.
    public List<usuario> listarUsuariosPendientes() { // Inicio método.
        List<usuario> lista = new ArrayList<>(); // Inicializa lista.
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.fecha_nacimiento, u.direccion_usuario, " +
                     "(SELECT c.direccion_correo FROM correos c WHERE c.id_usuario = u.id_usuario LIMIT 1) AS direccion_correo, " +
                     "(SELECT t.numero_telefono FROM telefonos t WHERE t.id_usuario = u.id_usuario LIMIT 1) AS numero_telefono, " +
                     "(SELECT r.nombre_rol FROM usuario_rol ur INNER JOIN roles r ON ur.id_rol = r.id_rol WHERE ur.id_usuario = u.id_usuario LIMIT 1) AS nombre_rol " +
                     "FROM usuarios u WHERE u.estado_usuario = 'Pendiente' ORDER BY u.id_usuario ASC";
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
                u.setRol(rs.getString("nombre_rol"));
                lista.add(u);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    } // Fin método.

    // Lista usuarios activos.
    public List<usuario> listarUsuariosActivos() { // Inicio método.
        List<usuario> lista = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.estado_usuario, " +
                     "(SELECT c.direccion_correo FROM correos c WHERE c.id_usuario = u.id_usuario LIMIT 1) AS direccion_correo, " +
                     "(SELECT t.numero_telefono FROM telefonos t WHERE t.id_usuario = u.id_usuario LIMIT 1) AS numero_telefono, " +
                     "(SELECT r.nombre_rol FROM usuario_rol ur INNER JOIN roles r ON ur.id_rol = r.id_rol WHERE ur.id_usuario = u.id_usuario LIMIT 1) AS nombre_rol " +
                     "FROM usuarios u WHERE u.estado_usuario = 'Activo' ORDER BY u.nombre_usuario ASC";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuario u = new usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNombre(rs.getString("nombre_usuario"));
                u.setEstado(rs.getString("estado_usuario"));
                u.setCorreo(rs.getString("direccion_correo"));
                u.setTelefono(rs.getString("numero_telefono"));
                u.setRol(rs.getString("nombre_rol"));
                lista.add(u);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    } // Fin método.

    // Lista todos los usuarios (Activos e Inactivos).
    public List<usuario> listarTodosLosUsuarios() { // Inicio método.
        List<usuario> lista = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.estado_usuario, " +
                     "(SELECT c.direccion_correo FROM correos c WHERE c.id_usuario = u.id_usuario LIMIT 1) AS direccion_correo, " +
                     "(SELECT t.numero_telefono FROM telefonos t WHERE t.id_usuario = u.id_usuario LIMIT 1) AS numero_telefono, " +
                     "(SELECT r.nombre_rol FROM usuario_rol ur INNER JOIN roles r ON ur.id_rol = r.id_rol WHERE ur.id_usuario = u.id_usuario LIMIT 1) AS nombre_rol " +
                     "FROM usuarios u WHERE u.estado_usuario IN ('Activo', 'Inactivo') ORDER BY u.nombre_usuario ASC";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                usuario u = new usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNombre(rs.getString("nombre_usuario"));
                u.setEstado(rs.getString("estado_usuario"));
                u.setCorreo(rs.getString("direccion_correo"));
                u.setTelefono(rs.getString("numero_telefono"));
                u.setRol(rs.getString("nombre_rol"));
                lista.add(u);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return lista;
    } // Fin método.

    // Actualiza estado del usuario.
    public boolean actualizarEstado(int idUsuario, String nuevoEstado) { // Inicio método.
        String sql = "UPDATE usuarios SET estado_usuario = ? WHERE id_usuario = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    } // Fin método.
    
    
    // Verifica si el usuario tiene asignaciones en estado activo (no finalizadas).
    // Se usa antes de eliminar para evitar borrar trabajadores con trabajo pendiente.
    public boolean tieneAsignacionesActivas(int idUsuario) {
        String sql = "SELECT COUNT(*) FROM asignaciones " +
                     "WHERE id_usuario = ? " +
                     "AND estado_trabajo IN ('Pendiente', 'En proceso', 'En revisión')";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0; // True si hay al menos una.
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar asignaciones activas: " + e.getMessage());
        }
        return false; // Por seguridad, si falla la consulta no bloquea.
    }

    // Elimina usuario y todos sus registros dependientes (correos, teléfonos, rol).
    // Las tablas correos, telefonos y usuario_rol no tienen ON DELETE CASCADE,
    // por lo que se borran manualmente en transacción antes de eliminar el usuario.
    public boolean eliminarUsuario(int idUsuario) { // Inicio método.
        Connection con = null; // Conexión nula para manejo manual.
        try { // Try inicio.
            con = clase_Conexion.MetodoConectar(); // Conecta.
            con.setAutoCommit(false); // Inicia transacción manual.
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM usuario_rol WHERE id_usuario = ?")) { // Elimina roles.
                ps.setInt(1, idUsuario); ps.executeUpdate(); // Ejecuta.
            } // Cierra PS.
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM correos WHERE id_usuario = ?")) { // Elimina correos.
                ps.setInt(1, idUsuario); ps.executeUpdate(); // Ejecuta.
            } // Cierra PS.
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM telefonos WHERE id_usuario = ?")) { // Elimina teléfonos.
                ps.setInt(1, idUsuario); ps.executeUpdate(); // Ejecuta.
            } // Cierra PS.
            int filas; // Variable para verificar si el usuario existía.
            try (PreparedStatement ps = con.prepareStatement("DELETE FROM usuarios WHERE id_usuario = ?")) { // Elimina usuario.
                ps.setInt(1, idUsuario); filas = ps.executeUpdate(); // Ejecuta.
            } // Cierra PS.
            if (filas == 0) { con.rollback(); return false; } // Rollback si no existía el usuario.
            con.commit(); // Confirma transacción.
            return true; // Éxito.
        } catch (SQLException e) { // Manejo error.
            if (con != null) try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } // Rollback.
            e.printStackTrace(); return false; // Error.
        } finally { // Cierra.
            if (con != null) try { con.setAutoCommit(true); con.close(); } catch (SQLException e) { e.printStackTrace(); } // Cierra.
        } // Fin finally.
    } // Fin método.

    // Registra usuario completo mediante transacción.
    public boolean registrarUsuarioCompleto(String nombre, String correo, String telefono, Date fechaNac, String dir, String passHash, int idRol) { // Inicio.
        Connection con = null;
        try {
            con = clase_Conexion.MetodoConectar();
            con.setAutoCommit(false);
            int idGenerado;
            String sqlUser = "INSERT INTO usuarios (nombre_usuario, fecha_nacimiento, direccion_usuario, contrasena_usuario, estado_usuario) VALUES (?, ?, ?, ?, 'Pendiente')";
            try (PreparedStatement ps = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombre); ps.setDate(2, fechaNac); ps.setString(3, dir); ps.setString(4, passHash);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) idGenerado = rs.getInt(1); else { con.rollback(); return false; }
                }
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO correos (id_usuario, direccion_correo) VALUES (?, ?)")) {
                ps.setInt(1, idGenerado); ps.setString(2, correo); ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO telefonos (id_usuario, numero_telefono) VALUES (?, ?)")) {
                ps.setInt(1, idGenerado); ps.setString(2, telefono); ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO usuario_rol (id_usuario, id_rol) VALUES (?, ?)")) {
                ps.setInt(1, idGenerado); ps.setInt(2, idRol); ps.executeUpdate();
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace(); return false;
        } finally {
            if (con != null) try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    } // Fin método.
} // Fin clase UsuarioDao.