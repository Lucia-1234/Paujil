package com.paujil.controlador;

import com.paujil.modelo.trabajo; // Asegúrate que tu modelo se llame 'trabajo' (minúscula)
import com.paujil.dao.TrabajoDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;

@WebServlet("/ServletTrabajo")
public class ServletTrabajo extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. Captura los datos del formulario (deben coincidir con el name en tu JSP)
        String nombre = request.getParameter("nombreTrabajo");
        String desc = request.getParameter("descripcion");
        Date fAsignacion = Date.valueOf(request.getParameter("fechaAsignacion"));
        
        int idCultivo = Integer.parseInt(request.getParameter("idCultivo"));
        int idUsuario = Integer.parseInt(request.getParameter("idUsuario"));
        
        // 2. Crear el objeto con el constructor correcto
        trabajo t = new trabajo(nombre, desc, fAsignacion);
        
        // 3. Llamar al método del DAO que maneja la transacción
        TrabajoDao dao = new TrabajoDao();
        boolean ok = dao.registrarTrabajoCompleto(t, idCultivo, idUsuario);
        
        // 4. Redirección
        response.sendRedirect("lista_trabajos.jsp?status=" + (ok ? "success" : "error"));
    }
}