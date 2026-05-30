package com.paujil.dao;

import com.paujil.modelo.usuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import paujil.basedatos.clase_Conexion;

public class UsuarioDao {

    //  Verificar duplicados 

    // Consulta solo el ID para minimizar datos transferidos; rs.next() indica existencia sin leer columnas
    public boolean correoExiste(String correo) {
        String sql = "SELECT id_correo FROM correos WHERE direccion_correo = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                // true si hay al menos una fila; el correo ya esta registrado en BD
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar correo: " + e.getMessage());
        }
        // false por defecto en caso de error; el servlet decidira como manejarlo
        return false;
    }

    // Misma estrategia que correoExiste; ambos metodos previenen violaciones de UNIQUE antes de insertar
    public boolean telefonoExiste(String numero) {
        String sql = "SELECT id_telefono FROM telefonos WHERE numero_telefono = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar telefono: " + e.getMessage());
        }
        return false;
    }

    //  Listar usuarios pendientes de aprobacion 
    public List<usuario> listarUsuariosPendientes() {
        List<usuario> lista = new ArrayList<>();
        // Subconsultas correlacionadas traen correo, telefono y rol sin JOINs que multipliquen filas
        // LIMIT 1 garantiza una sola fila por usuario aunque tenga multiples correos o telefonos
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.fecha_nacimiento, "
                   + "u.direccion_usuario, "
                   + "(SELECT c.direccion_correo FROM correos c "
                   + " WHERE c.id_usuario = u.id_usuario LIMIT 1) AS direccion_correo, "
                   + "(SELECT t.numero_telefono FROM telefonos t "
                   + " WHERE t.id_usuario = u.id_usuario LIMIT 1) AS numero_telefono, "
                   + "(SELECT r.nombre_rol FROM usuario_rol ur "
                   + " INNER JOIN roles r ON ur.id_rol = r.id_rol "
                   + " WHERE ur.id_usuario = u.id_usuario LIMIT 1) AS nombre_rol "
                   + "FROM usuarios u "
                   // Filtra exclusivamente pendientes; activos e inactivos se gestionan en otros metodos
                   + "WHERE u.estado_usuario = 'Pendiente' "
                   + "ORDER BY u.id_usuario ASC";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuario u = new usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNombre(rs.getString("nombre_usuario"));
                u.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                u.setDireccion(rs.getString("direccion_usuario"));
                // Estos campos vienen de las subconsultas; pueden ser null si el usuario no tiene datos asociados
                u.setCorreo(rs.getString("direccion_correo"));
                u.setTelefono(rs.getString("numero_telefono"));
                u.setRol(rs.getString("nombre_rol"));
                lista.add(u);
            }

            // Log de diagnostico; util para confirmar que la consulta retorna resultados en desarrollo
            System.out.println("Usuarios pendientes encontrados: " + lista.size());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    //  Listar usuarios activos 
    // Usado por TrabajoDao para poblar el selector de asignacion; solo activos son asignables
    public List<usuario> listarUsuariosActivos() {
        List<usuario> lista = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.estado_usuario, "
                   + "(SELECT c.direccion_correo FROM correos c "
                   + " WHERE c.id_usuario = u.id_usuario LIMIT 1) AS direccion_correo, "
                   + "(SELECT t.numero_telefono FROM telefonos t "
                   + " WHERE t.id_usuario = u.id_usuario LIMIT 1) AS numero_telefono, "
                   + "(SELECT r.nombre_rol FROM usuario_rol ur "
                   + " INNER JOIN roles r ON ur.id_rol = r.id_rol "
                   + " WHERE ur.id_usuario = u.id_usuario LIMIT 1) AS nombre_rol "
                   + "FROM usuarios u "
                   + "WHERE u.estado_usuario = 'Activo' "
                   // Orden alfabetico facilita la busqueda en selectores y tablas de la vista
                   + "ORDER BY u.nombre_usuario ASC";

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

        } catch (SQLException e) {
            System.err.println("Error al listar usuarios activos: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    //  Listar todos los usuarios (Activo + Inactivo) 
    // El filtrado entre estados se realiza en el cliente via JS para evitar multiples consultas al servidor
    public List<usuario> listarTodosLosUsuarios() {
        List<usuario> lista = new ArrayList<>();
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.estado_usuario, "
                   + "(SELECT c.direccion_correo FROM correos c "
                   + " WHERE c.id_usuario = u.id_usuario LIMIT 1) AS direccion_correo, "
                   + "(SELECT t.numero_telefono FROM telefonos t "
                   + " WHERE t.id_usuario = u.id_usuario LIMIT 1) AS numero_telefono, "
                   + "(SELECT r.nombre_rol FROM usuario_rol ur "
                   + " INNER JOIN roles r ON ur.id_rol = r.id_rol "
                   + " WHERE ur.id_usuario = u.id_usuario LIMIT 1) AS nombre_rol "
                   + "FROM usuarios u "
                   // Excluye pendientes intencionalmente; tienen su propia vista de gestion
                   + "WHERE u.estado_usuario IN ('Activo', 'Inactivo') "
                   + "ORDER BY u.nombre_usuario ASC";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuario u = new usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNombre(rs.getString("nombre_usuario"));
                // estado_usuario es necesario aqui para que el filtro del cliente pueda clasificar las filas
                u.setEstado(rs.getString("estado_usuario"));
                u.setCorreo(rs.getString("direccion_correo"));
                u.setTelefono(rs.getString("numero_telefono"));
                u.setRol(rs.getString("nombre_rol"));
                lista.add(u);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar todos los usuarios: " + e.getMessage());
            e.printStackTrace();
        }
        return lista;
    }

    //  Actualizar estado del usuario 
    // Reutilizado para activar, desactivar y aprobar; el estado final lo decide el caller
    public boolean actualizarEstado(int idUsuario, String nuevoEstado) {
        String sql = "UPDATE usuarios SET estado_usuario = ? WHERE id_usuario = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idUsuario);
            // executeUpdate > 0 confirma que el usuario existia y fue modificado
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar estado del usuario id="
                    + idUsuario + ": " + e.getMessage());
            return false;
        }
    }

    //  Eliminar usuario completo 
    // CASCADE en el esquema elimina automaticamente correos, telefonos y roles asociados
    public boolean eliminarUsuario(int idUsuario) {
        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            // executeUpdate > 0 verifica que el usuario existia; 0 indica ID no encontrado
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario id=" + idUsuario
                    + ": " + e.getMessage());
            return false;
        }
    }

    //  Registrar usuario completo (transaccion de 4 tablas) 
    public boolean registrarUsuarioCompleto(String nombre, String correo, String telefono,
                                            Date fechaNac, String dir,
                                            String passHash, int idRol) {
        Connection con = null;
        try {
            con = clase_Conexion.MetodoConectar();
            // Desactiva autocommit para que las 4 inserciones sean atomicas
            con.setAutoCommit(false);

            int idGenerado;
            String sqlUser = "INSERT INTO usuarios "
                           + "(nombre_usuario, fecha_nacimiento, direccion_usuario, "
                           + " contrasena_usuario, estado_usuario) "
                           // Estado 'Pendiente' por defecto; el admin debe aprobar antes de que el usuario acceda
                           + "VALUES (?, ?, ?, ?, 'Pendiente')";

            // RETURN_GENERATED_KEYS recupera el ID necesario para las inserciones en tablas relacionadas
            try (PreparedStatement ps = con.prepareStatement(
                    sqlUser, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nombre);
                ps.setDate(2, fechaNac);
                ps.setString(3, dir);
                // passHash es el resultado de BCrypt; nunca se almacena la contrasena en texto plano
                ps.setString(4, passHash);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idGenerado = rs.getInt(1);
                    } else {
                        // Sin ID no se pueden crear los registros dependientes; se revierte todo
                        con.rollback();
                        return false;
                    }
                }
            }

            // Inserta el correo vinculado al usuario recien creado
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO correos (id_usuario, direccion_correo) VALUES (?, ?)")) {
                ps.setInt(1, idGenerado);
                ps.setString(2, correo);
                ps.executeUpdate();
            }

            // Inserta el telefono; tabla separada para permitir multiples telefonos por usuario en el futuro
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO telefonos (id_usuario, numero_telefono) VALUES (?, ?)")) {
                ps.setInt(1, idGenerado);
                ps.setString(2, telefono);
                ps.executeUpdate();
            }

            // Asigna el rol en la tabla intermedia; idRol proviene del enum Rol resuelto en el servlet
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO usuario_rol (id_usuario, id_rol) VALUES (?, ?)")) {
                ps.setInt(1, idGenerado);
                ps.setInt(2, idRol);
                ps.executeUpdate();
            }

            // Confirma las 4 inserciones como unidad; cualquier fallo previa revertio todo via rollback
            con.commit();
            return true;

        } catch (SQLException e) {
            // Deshace todas las inserciones si cualquiera de las 4 falla
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.err.println("Error al registrar usuario: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            // Cierra la conexion en finally para garantizar liberacion aunque ocurra una excepcion
            if (con != null) {
                try { con.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}