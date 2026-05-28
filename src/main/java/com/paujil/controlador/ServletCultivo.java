package com.paujil.controlador;

import com.paujil.dao.CultivoDao;
import com.paujil.modelo.cultivo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

@WebServlet("/ServletCultivo")
public class ServletCultivo extends HttpServlet {

    // --- MANEJO DE VISTA Y ELIMINACIÓN ---
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String accion = request.getParameter("accion");
        CultivoDao dao = new CultivoDao();

        if ("eliminar".equals(accion)) {
            int id = Integer.parseInt(request.getParameter("id"));
            dao.eliminarCultivo(id);
            // Redirigimos al mismo servlet para recargar la lista
            response.sendRedirect("ServletCultivo"); 
            return;
        }

        // Listar cultivos
        List<cultivo> lista = dao.listarCultivos(); 
        request.setAttribute("listaCultivos", lista);
        request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
               .forward(request, response);
    }

    // --- MANEJO DE REGISTRO Y EDICIÓN ---
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String idStr = request.getParameter("id");
        String nombre = request.getParameter("nombreCultivo");
        String tipo = request.getParameter("tipoCultivo");
        Date fSiembra = Date.valueOf(request.getParameter("fechaSiembra"));
        String fCosechaStr = request.getParameter("fechaCosecha");
        Date fCosecha = (fCosechaStr != null && !fCosechaStr.isEmpty()) ? Date.valueOf(fCosechaStr) : null;

        CultivoDao dao = new CultivoDao();

        if (idStr != null && !idStr.isEmpty()) {
            dao.actualizarCultivo(Integer.parseInt(idStr), nombre, tipo, fSiembra, fCosecha);
        } else {
            cultivo c = new cultivo(nombre, tipo, fSiembra, fCosecha);
            dao.registrarCultivo(c);
        }

        // REDIRIGIMOS AL SERVLET (doGet), NO AL JSP, para que recargue la lista
        response.sendRedirect("ServletCultivo");
    }
}