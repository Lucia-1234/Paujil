package com.paujil.controlador;

import com.paujil.dao.CultivoDao;
import com.paujil.modelo.cultivo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

@WebServlet("/ServletCultivo")
public class ServletCultivo extends HttpServlet {

    // ── Guarda de sesión compartida por doGet y doPost ────────────────────────
    // Segunda línea de defensa: el FiltroAdministrador ya bloquea peticiones
    // sin sesión, pero esta guarda protege en caso de que el filtro no cubra
    // alguna ruta futura o sea desactivado por error.
    private boolean sesionAdminValida(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null
                || session.getAttribute("idUsuario") == null
                || !"administrador".equalsIgnoreCase((String) session.getAttribute("rolUsuario"))) {
            response.sendRedirect(request.getContextPath() + "/templates/login.jsp?error=acceso_denegado");
            return false;
        }
        return true;
    }

    // --- MANEJO DE VISTA Y ELIMINACIÓN ---
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!sesionAdminValida(request, response)) return;

        String accion = request.getParameter("accion");
        CultivoDao dao = new CultivoDao();

        if ("eliminar".equals(accion)) {
            String idStr = request.getParameter("id");
            if (idStr == null || idStr.trim().isEmpty()) {
                response.sendRedirect("ServletCultivo");
                return;
            }
            try {
                int id = Integer.parseInt(idStr);
                dao.eliminarCultivo(id);
            } catch (NumberFormatException e) {
                // id no numérico — ignorar y redirigir al listado
            }
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

        if (!sesionAdminValida(request, response)) return;

        String idStr       = request.getParameter("id");
        String nombre      = request.getParameter("nombreCultivo");
        String tipo        = request.getParameter("tipoCultivo");
        String fSiembraStr = request.getParameter("fechaSiembra");
        String fCosechaStr = request.getParameter("fechaCosecha");

        // Validar campos obligatorios antes de parsear fechas
        if (nombre == null || nombre.trim().isEmpty()
                || tipo == null || tipo.trim().isEmpty()
                || fSiembraStr == null || fSiembraStr.trim().isEmpty()) {
            request.setAttribute("mensajeError", "Nombre, tipo y fecha de siembra son obligatorios.");
            List<cultivo> lista = new CultivoDao().listarCultivos();
            request.setAttribute("listaCultivos", lista);
            request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
                   .forward(request, response);
            return;
        }

        Date fSiembra;
        Date fCosecha;
        try {
            fSiembra = Date.valueOf(fSiembraStr);
            fCosecha = (fCosechaStr != null && !fCosechaStr.isEmpty()) ? Date.valueOf(fCosechaStr) : null;
        } catch (IllegalArgumentException e) {
            request.setAttribute("mensajeError", "Formato de fecha inválido. Use el selector de fechas.");
            List<cultivo> lista = new CultivoDao().listarCultivos();
            request.setAttribute("listaCultivos", lista);
            request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
                   .forward(request, response);
            return;
        }

        CultivoDao dao = new CultivoDao();

        if (idStr != null && !idStr.isEmpty()) {
            dao.actualizarCultivo(Integer.parseInt(idStr), nombre.trim(), tipo.trim(), fSiembra, fCosecha);
        } else {
            cultivo c = new cultivo(nombre.trim(), tipo.trim(), fSiembra, fCosecha);
            dao.registrarCultivo(c);
        }

        // Redirigimos al Servlet (doGet), no al JSP, para que recargue la lista
        response.sendRedirect("ServletCultivo");
    }
}