package com.paujil.controlador;

import com.paujil.utils.Seguridad; // Importamos tu clase de seguridad
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import paujil.basedatos.clase_Conexion;

@WebServlet("/ServletLogin")
public class ServletLogin extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String correoInput = request.getParameter("txtUsuario");
        String contrasenaInput = request.getParameter("txtContrasena");
        String rolSeleccionado = request.getParameter("txtRol");

        // Normalización de datos
        if (correoInput != null) correoInput = correoInput.trim().toLowerCase();
        if (contrasenaInput != null) contrasenaInput = contrasenaInput.trim();
        if (rolSeleccionado != null) rolSeleccionado = rolSeleccionado.trim().toLowerCase();

        // 1. Consulta SQL corregida: Sin filtrar la contraseña aquí
        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.contrasena_usuario, u.estado_usuario, r.nombre_rol, c.direccion_correo, t.numero_telefono " +
                     "FROM usuarios u " +
                     "INNER JOIN correos c ON u.id_usuario = c.id_usuario " +
                     "INNER JOIN usuario_rol ur ON u.id_usuario = ur.id_usuario " +
                     "INNER JOIN roles r ON ur.id_rol = r.id_rol " +
                     "LEFT JOIN telefonos t ON u.id_usuario = t.id_usuario " +
                     "WHERE TRIM(LOWER(c.direccion_correo)) = ? " +
                     "AND TRIM(LOWER(r.nombre_rol)) = ? " +
                     "AND u.estado_usuario = 'Activo'";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, correoInput);
            ps.setString(2, rolSeleccionado);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashEnBD = rs.getString("contrasena_usuario");

                    // 2. VERIFICACIÓN DE SEGURIDAD CON BCRYPT
                    if (Seguridad.verificar(contrasenaInput, hashEnBD)) {
                        
                        HttpSession oldSession = request.getSession(false);
                        if (oldSession != null) {
                            oldSession.invalidate();
                        }
                        HttpSession session = request.getSession(true);
                        
                        session.setAttribute("idUsuario", rs.getInt("id_usuario"));
                        session.setAttribute("nombreUsuario", rs.getString("nombre_usuario"));
                        session.setAttribute("rolUsuario", rs.getString("nombre_rol"));
                        session.setAttribute("telefonoUsuario", rs.getString("numero_telefono"));

                        if ("administrador".equalsIgnoreCase(rs.getString("nombre_rol"))) {
                            response.sendRedirect(request.getContextPath() + "/templates/administrador/menu_administrador.jsp");
                        } else {
                            response.sendRedirect(request.getContextPath() + "/templates/trabajador/panel_trabajador.jsp");
                        }
                    } else {
                        // Contraseña incorrecta
                        response.sendRedirect(request.getContextPath() + "/templates/login.jsp?error=1");
                    }
                } else {
                    // Usuario no encontrado o no activo
                    response.sendRedirect(request.getContextPath() + "/templates/login.jsp?error=1");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/templates/login.jsp?error=fatal");
        }
    }
}