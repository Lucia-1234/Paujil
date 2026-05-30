package com.paujil.controlador;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/ServletLogout")
public class ServletLogout extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // false evita crear una sesion nueva si el usuario ya no tenia una activa
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Destruye la sesion y todos sus atributos, invalidando el ID en el servidor
            session.invalidate();
        }
        // Redirige al login tras el cierre; getContextPath() asegura la ruta correcta en cualquier contexto de despliegue
        response.sendRedirect(request.getContextPath() + "/templates/login.jsp");
    }
}