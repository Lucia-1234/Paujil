package com.paujil.controlador; // Define el paquete lógico del controlador.

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

// Mapea la clase a la URL /ServletLogout para que pueda ser invocada desde el navegador.
@WebServlet("/ServletLogout")
public class ServletLogout extends HttpServlet {

    @Override
    // El cierre de sesión se maneja mediante GET porque no implica modificar datos en la BD, solo limpiar el servidor.
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Obtiene la sesión actual. El parámetro 'false' es crucial:
        // Si no existe una sesión previa, no crea una nueva, simplemente devuelve null.
        HttpSession session = request.getSession(false);
        
        // Verifica si la sesión realmente existe antes de intentar cerrarla.
        if (session != null) {
            // session.invalidate() es la instrucción clave.
            // Elimina los datos del usuario en memoria del servidor y marca la sesión como obsoleta.
            session.invalidate();
        }
        
        // Finaliza la petición redirigiendo al usuario al formulario de login.
        // getContextPath() es una buena práctica para evitar errores si la app cambia de nombre de despliegue.
        response.sendRedirect(request.getContextPath() + "/templates/login.jsp");
    }
}