package com.paujil.controlador;

import com.paujil.dao.UsuarioDao;
import com.paujil.modelo.validador; // <-- IMPORTANTE: Importamos nuestra clase de utilidades
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/ServletRegistro")
public class ServletRegistro extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Recolección
        String pass = request.getParameter("txtContrasena");
        String passConf = request.getParameter("txtConfirmarContrasena");
        
        // 2. Validación rápida
        if (!pass.equals(passConf)) {
            enviarError("Las contraseñas no coinciden.", request, response);
            return;
        }
        if (!validador.esContrasenaSegura(pass)) {
            enviarError("La contraseña no es segura.", request, response);
            return;
        }

        // 3. Ejecución vía DAO
        UsuarioDao dao = new UsuarioDao();
        // Ciframos la contraseña ANTES de enviarla al DAO
        String passHash = com.paujil.utils.Seguridad.encriptar(pass);
        
        boolean exito = dao.registrarUsuarioCompleto(
            request.getParameter("txtNombre"),
            request.getParameter("txtEmail").toLowerCase().trim(),
            request.getParameter("txtTelefono"),
            java.sql.Date.valueOf(request.getParameter("txtFechaNacimiento")),
            request.getParameter("txtDireccion"),
            passHash,
            Integer.parseInt(request.getParameter("txtRol"))
        );

        if (exito) {
            response.sendRedirect(request.getContextPath() + "/templates/login.jsp?registro=success");
        } else {
            enviarError("Error al guardar en base de datos. Intente nuevamente.", request, response);
        }
    }

    private void enviarError(String mensaje, HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setAttribute("mensaje", mensaje);
        request.getRequestDispatcher("/templates/registro_usuario.jsp").forward(request, response);
    }
}