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
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.fecha_nacimiento, u.direccion_usuario, " + // Select campos.
                     "(SELECT c.direccion_correo FROM correos c WHERE c.id_usuario = u.id_usuario LIMIT 1) AS direccion_correo, " + // Subconsulta correo.
                     "(SELECT t.numero_telefono FROM telefonos t WHERE t.id_usuario = u.id_usuario LIMIT 1) AS numero_telefono, " + // Subconsulta telf.
                     "(SELECT r.nombre_rol FROM usuario_rol ur INNER JOIN roles r ON ur.id_rol = r.id_rol WHERE ur.id_usuario = u.id_usuario LIMIT 1) AS nombre_rol " + // Subconsulta rol.
                     "FROM usuarios u WHERE u.estado_usuario = 'Pendiente' ORDER BY u.id_usuario ASC"; // Condición.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql); // Prepara.
             ResultSet rs = ps.executeQuery()) { // Ejecuta.
            while (rs.next()) { // Itera resultados.
                usuario u = new usuario(); // Crea objeto.
                u.setIdUsuario(rs.getInt("id_usuario")); // Mapea ID.
                u.setNombre(rs.getString("nombre_usuario")); // Mapea nombre.
                u.setFechaNacimiento(rs.getDate("fecha_nacimiento")); // Mapea fecha.
                u.setDireccion(rs.getString("direccion_usuario")); // Mapea dirección.
                u.setCorreo(rs.getString("direccion_correo")); // Mapea correo.
                u.setTelefono(rs.getString("numero_telefono")); // Mapea teléfono.
                u.setRol(rs.getString("nombre_rol")); // Mapea rol.
                lista.add(u); // Agrega a lista.
            } // Fin while.
        } catch (SQLException e) { e.printStackTrace(); } // Log error.
        return lista; // Retorna lista.
    } // Fin método.

    // Lista usuarios activos.
    public List<usuario> listarUsuariosActivos() { // Inicio método.
        List<usuario> lista = new ArrayList<>(); // Inicializa lista.
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.estado_usuario, " + // Select base.
                     "(SELECT c.direccion_correo FROM correos c WHERE c.id_usuario = u.id_usuario LIMIT 1) AS direccion_correo, " + // Subconsulta correo.
                     "(SELECT t.numero_telefono FROM telefonos t WHERE t.id_usuario = u.id_usuario LIMIT 1) AS numero_telefono, " + // Subconsulta telf.
                     "(SELECT r.nombre_rol FROM usuario_rol ur INNER JOIN roles r ON ur.id_rol = r.id_rol WHERE ur.id_usuario = u.id_usuario LIMIT 1) AS nombre_rol " + // Subconsulta rol.
                     "FROM usuarios u WHERE u.estado_usuario = 'Activo' ORDER BY u.nombre_usuario ASC"; // Filtro activos.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql); // Prepara.
             ResultSet rs = ps.executeQuery()) { // Ejecuta.
            while (rs.next()) { // Itera.
                usuario u = new usuario(); // Crea objeto.
                u.setIdUsuario(rs.getInt("id_usuario")); // Mapea ID.
                u.setNombre(rs.getString("nombre_usuario")); // Mapea nombre.
                u.setEstado(rs.getString("estado_usuario")); // Mapea estado.
                u.setCorreo(rs.getString("direccion_correo")); // Mapea correo.
                u.setTelefono(rs.getString("numero_telefono")); // Mapea teléfono.
                u.setRol(rs.getString("nombre_rol")); // Mapea rol.
                lista.add(u); // Agrega a lista.
            } // Fin while.
        } catch (SQLException e) { e.printStackTrace(); } // Log error.
        return lista; // Retorna lista.
    } // Fin método.

    // Lista todos los usuarios (Activos e Inactivos).
    public List<usuario> listarTodosLosUsuarios() { // Inicio método.
        List<usuario> lista = new ArrayList<>(); // Inicializa lista.
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.estado_usuario, " + // Select base.
                     "(SELECT c.direccion_correo FROM correos c WHERE c.id_usuario = u.id_usuario LIMIT 1) AS direccion_correo, " + // Correo.
                     "(SELECT t.numero_telefono FROM telefonos t WHERE t.id_usuario = u.id_usuario LIMIT 1) AS numero_telefono, " + // Teléfono.
                     "(SELECT r.nombre_rol FROM usuario_rol ur INNER JOIN roles r ON ur.id_rol = r.id_rol WHERE ur.id_usuario = u.id_usuario LIMIT 1) AS nombre_rol " + // Rol.
                     "FROM usuarios u WHERE u.estado_usuario IN ('Activo', 'Inactivo') ORDER BY u.nombre_usuario ASC"; // Filtro.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql); // Prepara.
             ResultSet rs = ps.executeQuery()) { // Ejecuta.
            while (rs.next()) { // Itera.
                usuario u = new usuario(); // Crea objeto.
                u.setIdUsuario(rs.getInt("id_usuario")); // Mapea ID.
                u.setNombre(rs.getString("nombre_usuario")); // Mapea nombre.
                u.setEstado(rs.getString("estado_usuario")); // Mapea estado.
                u.setCorreo(rs.getString("direccion_correo")); // Mapea correo.
                u.setTelefono(rs.getString("numero_telefono")); // Mapea teléfono.
                u.setRol(rs.getString("nombre_rol")); // Mapea rol.
                lista.add(u); // Agrega a lista.
            } // Fin while.
        } catch (SQLException e) { e.printStackTrace(); } // Log error.
        return lista; // Retorna lista.
    } // Fin método.

    // Actualiza estado del usuario.
    public boolean actualizarEstado(int idUsuario, String nuevoEstado) { // Inicio método.
        String sql = "UPDATE usuarios SET estado_usuario = ? WHERE id_usuario = ?"; // SQL update.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setString(1, nuevoEstado); // Setea estado.
            ps.setInt(2, idUsuario); // Setea ID.
            return ps.executeUpdate() > 0; // Ejecuta e indica éxito.
        } catch (SQLException e) { e.printStackTrace(); return false; } // Error.
    } // Fin método.

    // Elimina usuario (Cascada BD).
    public boolean eliminarUsuario(int idUsuario) { // Inicio método.
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?"; // SQL delete.
        try (Connection con = clase_Conexion.MetodoConectar(); // Conecta.
             PreparedStatement ps = con.prepareStatement(sql)) { // Prepara.
            ps.setInt(1, idUsuario); // Setea ID.
            return ps.executeUpdate() > 0; // Ejecuta.
        } catch (SQLException e) { e.printStackTrace(); return false; } // Error.
    } // Fin método.

    // Registra usuario completo mediante transacción.
    public boolean registrarUsuarioCompleto(String nombre, String correo, String telefono, Date fechaNac, String dir, String passHash, int idRol) { // Inicio.
        Connection con = null; // Conexión nula.
        try { // Try inicio.
            con = clase_Conexion.MetodoConectar(); // Conecta.
            con.setAutoCommit(false); // Transacción manual.
            int idGenerado; // Variable id.
            String sqlUser = "INSERT INTO usuarios (nombre_usuario, fecha_nacimiento, direccion_usuario, contrasena_usuario, estado_usuario) VALUES (?, ?, ?, ?, 'Pendiente')"; // SQL insert.
            try (PreparedStatement ps = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS)) { // Prepara.
                ps.setString(1, nombre); ps.setDate(2, fechaNac); ps.setString(3, dir); ps.setString(4, passHash); // Setea params.
                ps.executeUpdate(); // Ejecuta.
                try (ResultSet rs = ps.getGeneratedKeys()) { // Obtiene ID generado.
                    if (rs.next()) idGenerado = rs.getInt(1); else { con.rollback(); return false; } // Valida.
                } // Cierra RS.
            } // Cierra PS.
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO correos (id_usuario, direccion_correo) VALUES (?, ?)")) { // Insert correo.
                ps.setInt(1, idGenerado); ps.setString(2, correo); ps.executeUpdate(); // Ejecuta.
            } // Cierra PS.
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO telefonos (id_usuario, numero_telefono) VALUES (?, ?)")) { // Insert telf.
                ps.setInt(1, idGenerado); ps.setString(2, telefono); ps.executeUpdate(); // Ejecuta.
            } // Cierra PS.
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO usuario_rol (id_usuario, id_rol) VALUES (?, ?)")) { // Insert rol.
                ps.setInt(1, idGenerado); ps.setInt(2, idRol); ps.executeUpdate(); // Ejecuta.
            } // Cierra PS.
            con.commit(); // Confirma transacción.
            return true; // Éxito.
        } catch (SQLException e) { // Manejo error.
            if (con != null) try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); } // Rollback.
            e.printStackTrace(); return false; // Error.
        } finally { // Cierra.
            if (con != null) try { con.close(); } catch (SQLException e) { e.printStackTrace(); } // Cierra.
        } // Fin finally.
    } // Fin método.
} // Fin clase UsuarioDao.