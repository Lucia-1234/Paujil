package com.paujil.controlador;

import com.paujil.dao.RegistroTrabajoDao;
import com.paujil.modelo.registros;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.sql.Date;

@WebServlet("/ServletLabor")
public class ServletLabor extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Verificar sesión (admin o trabajador pueden registrar labores)
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("idUsuario") == null) {
            response.sendRedirect(request.getContextPath() + "/templates/login.jsp");
            return;
        }
        int idUsuario = (Integer) session.getAttribute("idUsuario");

        // 2. Parámetros
        String idCultivoStr     = request.getParameter("idCultivo");
        String descripcion      = request.getParameter("descripcionTrabajo");
        String fechaInicioStr   = request.getParameter("fechaInicio");
        String fechaFinalizoStr = request.getParameter("fechaFinalizo");
        String observaciones    = request.getParameter("observaciones");

        // 3. Validación de entrada
        if (estaVacio(idCultivoStr) || estaVacio(descripcion)
                || estaVacio(fechaInicioStr) || estaVacio(fechaFinalizoStr)) {
            reenviarConError("Todos los campos obligatorios deben completarse.",
                    idCultivoStr, request, response);
            return;
        }

        int idCultivo;
        try { idCultivo = Integer.parseInt(idCultivoStr); }
        catch (NumberFormatException e) {
            reenviarConError("Cultivo no válido.", idCultivoStr, request, response);
            return;
        }

        // Sanitizar longitud de descripción (evitar textos excesivamente largos)
        if (descripcion.length() > 1000) {
            reenviarConError("La descripción no puede superar 1000 caracteres.",
                    idCultivoStr, request, response);
            return;
        }

        Date fechaInicio, fechaFinalizo;
        try {
            fechaInicio   = Date.valueOf(fechaInicioStr.trim());
            fechaFinalizo = Date.valueOf(fechaFinalizoStr.trim());
        } catch (IllegalArgumentException e) {
            reenviarConError("Formato de fecha inválido. Use yyyy-MM-dd.",
                    idCultivoStr, request, response);
            return;
        }

        if (fechaInicio.after(fechaFinalizo)) {
            reenviarConError("La fecha de inicio no puede ser posterior a la fecha de finalización.",
                    idCultivoStr, request, response);
            return;
        }

        // 4. Persistir
        registros reg = new registros(
            idCultivo, descripcion.trim(), idUsuario,
            fechaInicio, fechaFinalizo,
            (observaciones != null && !observaciones.trim().isEmpty())
                ? observaciones.trim() : null
        );
        boolean exito = new RegistroTrabajoDao().registrarLabor(reg);

        // 5. Redirect-After-POST
        String status = exito ? "registrado" : "error";
        response.sendRedirect(request.getContextPath()
                + "/ServletCultivo?status=" + status + "&idCultivoActivo=" + idCultivo);
    }

    private boolean estaVacio(String v) {
        return v == null || v.trim().isEmpty();
    }

    private void reenviarConError(String mensaje, String idCultivo,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("mensajeError", mensaje);
        request.setAttribute("idCultivo", idCultivo);
        request.getRequestDispatcher("/templates/administrador/cultivos.jsp")
               .forward(request, response);
    }
}