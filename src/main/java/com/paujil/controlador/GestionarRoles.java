package com.paujil.controlador;

import com.paujil.dao.UsuarioDao;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/GestionarRoles")
public class GestionarRoles extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
        throws ServletException, IOException {
    
            String accion = request.getParameter("accion");
            int id = Integer.parseInt(request.getParameter("id_usuario"));

            UsuarioDao dao = new UsuarioDao();

            if ("aceptar".equals(accion)) {
                // Lógica para cambiar estado a 'Activo' y asignar rol
                dao.actualizarEstado(id, "Activo");
            } else if ("denegar".equals(accion)) {
                // Lógica para borrar el registro o cambiar a 'Rechazado'
                dao.eliminarUsuario(id);
            }

            // Redirigir de vuelta a la lista
            response.sendRedirect("templates/administrador/asignar_rol.jsp");
        }
    }
