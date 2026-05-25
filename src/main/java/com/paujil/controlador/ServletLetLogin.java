package com.paujil.controlador;

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

@WebServlet("/ServletLetLogin")
public class ServletLetLogin extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String correoInput = request.getParameter("txtUsuario");      
        String contrasenaInput = request.getParameter("txtContrasena"); 
        String rolSeleccionado = request.getParameter("txtRol");       

        if (correoInput == null || contrasenaInput == null || rolSeleccionado == null) {
            response.sendRedirect(request.getContextPath() + "/templates/login.jsp?error=datos_incompletos");
            return;
        }

        correoInput = correoInput.trim().toLowerCase();

        String sql = "SELECT u.id_usuario, u.nombre_usuario, u.contrasena_usuario, r.nombre_rol, c.direccion_correo " +
                     "FROM usuarios u " +
                     "INNER JOIN correos c ON u.id_usuario = c.id_usuario " +
                     "INNER JOIN usuario_rol ur ON u.id_usuario = ur.id_usuario " +
                     "INNER JOIN roles r ON ur.id_rol = r.id_rol " +
                     "WHERE c.direccion_correo = ? AND u.contrasena_usuario = ? AND r.nombre_rol = ?";

        try (Connection con = clase_Conexion.MetodoConectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            if (con == null) {
                response.sendRedirect(request.getContextPath() + "/templates/login.jsp?error=conexion_bd");
                return;
            }

            ps.setString(1, correoInput);
            ps.setString(2, contrasenaInput);
            ps.setString(3, rolSeleccionado);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    HttpSession session = request.getSession(true);
                    session.setAttribute("idUsuario", rs.getInt("id_usuario"));
                    session.setAttribute("nombreUsuario", rs.getString("nombre_usuario")); 
                    session.setAttribute("rolUsuario", rs.getString("nombre_rol")); 

                    if ("administrador".equalsIgnoreCase(rs.getString("nombre_rol"))) {
                        response.sendRedirect(request.getContextPath() + "/templates/administrador/menu_administrador.jsp");
                    } else {
                        response.sendRedirect(request.getContextPath() + "/templates/trabajador/panel_trabajador.jsp");
                    }
                    return;
                    
                } else {
                    response.sendRedirect(request.getContextPath() + "/templates/login.jsp?error=1");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/templates/login.jsp?error=fatal");
        }
    }
}