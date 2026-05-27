package com.paujil.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.paujil.modelo.usuario;
import paujil.basedatos.clase_Conexion;

public class UsuarioDao {

    /**
     * Verifica si un correo ya existe en la base de datos.
     * Es llamado por el ServletRegistro antes de intentar cualquier inserción.
     */
    public boolean correoExiste(String correo) {
        String sql = "SELECT id_correo FROM correos WHERE direccion_correo = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            if (con == null) return false;
            
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Retorna true si encuentra un registro
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar existencia de correo: " + e.getMessage());
        }
        return false;
    }

    /**
     * Verifica si un número de teléfono ya existe en la base de datos.
     */
    public boolean telefonoExiste(String numero) {
        String sql = "SELECT id_telefono FROM telefonos WHERE numero_telefono = ?";
        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            if (con == null) return false;
            
            ps.setString(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar existencia de teléfono: " + e.getMessage());
        }
        return false;
    }
    
        public List<usuario> listarUsuariosPendientes() {
            List<usuario> lista = new ArrayList<>();
            Connection con = paujil.basedatos.clase_Conexion.MetodoConectar();

            // AQUÍ ESTÁ EL CAMBIO: Verificamos si la conexión es null
            if (con == null) {
                System.out.println("ERROR CRÍTICO: La conexión a la BD es NULL.");
                return lista; // Retornamos lista vacía en lugar de dejar que explote
            }

            String sql = "SELECT u.id_usuario, u.nombre_usuario FROM usuarios u WHERE u.estado_usuario = 'Pendiente'";

            try (PreparedStatement ps = con.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    usuario u = new usuario();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    u.setNombre(rs.getString("nombre_usuario"));
                    lista.add(u);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        return lista;
    }

        public void actualizarEstado(int idUsuario, String nuevoEstado) {
            String sql = "UPDATE usuarios SET estado_usuario = ? WHERE id_usuario = ?";

            try (Connection con = paujil.basedatos.clase_Conexion.MetodoConectar();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, nuevoEstado);
                ps.setInt(2, idUsuario);
                ps.executeUpdate();

            } catch (SQLException e) {
                System.err.println("Error al actualizar estado: " + e.getMessage());
            }
        }
        
        public void eliminarUsuario(int idUsuario) {
            // Primero borramos de la tabla correos
            String sqlCorreo = "DELETE FROM correos WHERE id_usuario = ?";
            // Luego borramos al usuario
            String sqlUsuario = "DELETE FROM usuarios WHERE id_usuario = ?";

            try (Connection con = paujil.basedatos.clase_Conexion.MetodoConectar()) {
                con.setAutoCommit(false); // Iniciamos transacción

                try (PreparedStatement psCorreo = con.prepareStatement(sqlCorreo);
                     PreparedStatement psUsuario = con.prepareStatement(sqlUsuario)) {

                    psCorreo.setInt(1, idUsuario);
                    psCorreo.executeUpdate();

                    psUsuario.setInt(1, idUsuario);
                    psUsuario.executeUpdate();

                    con.commit(); // Guardamos cambios si todo salió bien
                } catch (SQLException e) {
                    con.rollback(); // Revertimos si algo falló
                    throw e;
                }
            } catch (SQLException e) {
                System.err.println("Error al eliminar usuario: " + e.getMessage());
            }
        }
        
        public boolean registrarUsuarioCompleto(String nombre, String correo, String telefono, 
                                                java.sql.Date fechaNac, String dir, 
                                                String passHash, int idRol) {
            Connection con = null;
            try {
                con = clase_Conexion.MetodoConectar();
                con.setAutoCommit(false); // Iniciamos transacción

                // 1. Insertar Usuario
                String sqlUser = "INSERT INTO usuarios (nombre_usuario, fecha_nacimiento, direccion_usuario, contrasena_usuario, estado_usuario) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement ps = con.prepareStatement(sqlUser, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, nombre);
                ps.setDate(2, fechaNac);
                ps.setString(3, dir);
                ps.setString(4, passHash); // Ya viene cifrado desde el Servlet
                ps.setString(5, "Pendiente");
                ps.executeUpdate();

                int idGenerado = 0;
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) idGenerado = rs.getInt(1);
                }

                // 2. Insertar Correo
                String sqlCorreo = "INSERT INTO correos (id_usuario, direccion_correo) VALUES (?, ?)";
                PreparedStatement psC = con.prepareStatement(sqlCorreo);
                psC.setInt(1, idGenerado);
                psC.setString(2, correo);
                psC.executeUpdate();

                // 3. Insertar Teléfono
                String sqlTelefono = "INSERT INTO telefonos (id_usuario, numero_telefono) VALUES (?, ?)";
                PreparedStatement psT = con.prepareStatement(sqlTelefono);
                psT.setInt(1, idGenerado);
                psT.setString(2, telefono);
                psT.executeUpdate();

                // 4. Insertar Rol
                String sqlRol = "INSERT INTO usuario_rol (id_usuario, id_rol) VALUES (?, ?)";
                PreparedStatement psR = con.prepareStatement(sqlRol);
                psR.setInt(1, idGenerado);
                psR.setInt(2, idRol);
                psR.executeUpdate();

                con.commit(); // Se guarda todo si nada falló
                return true;
            } catch (Exception e) {
                if (con != null) try { con.rollback(); } catch (Exception ex) { ex.printStackTrace(); }
                e.printStackTrace();
                return false;
            } finally {
                if (con != null) try { con.close(); } catch (Exception e) {}
            }
        }
    
}