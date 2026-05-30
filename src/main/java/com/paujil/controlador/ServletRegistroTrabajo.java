package com.paujil.controlador;

import com.paujil.dao.RegistroTrabajoDao;
import com.paujil.modelo.registros;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;

@WebServlet("/ServletRegistroTrabajo")
public class ServletRegistroTrabajo extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ── 1. Verificar sesión ───────────────────────────────────────────────
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("idUsuario") == null) {
            response.sendRedirect(request.getContextPath()
                    + "/templates/login.jsp?error=sesion_expirada");
            return;
        }
        int idUsuario = (int) session.getAttribute("idUsuario");

        // ── 2. Recolección de parámetros ──────────────────────────────────────
        String idCultivoStr     = request.getParameter("idCultivo");
        String descripcion      = request.getParameter("descripcionTrabajo");
        String fechaInicioStr   = request.getParameter("fechaInicio");
        String fechaFinalizoStr = request.getParameter("fechaFinalizo");
        String observaciones    = request.getParameter("observaciones");

        // ── 3. Validaciones ───────────────────────────────────────────────────

        if (estaVacio(idCultivoStr) || estaVacio(descripcion)
                || estaVacio(fechaInicioStr) || estaVacio(fechaFinalizoStr)) {
            reenviarConError("Todos los campos obligatorios deben completarse.",
                    idCultivoStr, request, response);
            return;
        }

        int idCultivo;
        try {
            idCultivo = Integer.parseInt(idCultivoStr);
        } catch (NumberFormatException e) {
            reenviarConError("Cultivo no válido.", idCultivoStr, request, response);
            return;
        }

        Date fechaInicio;
        Date fechaFinalizo;
        try {
            fechaInicio   = Date.valueOf(fechaInicioStr);
            fechaFinalizo = Date.valueOf(fechaFinalizoStr);
        } catch (IllegalArgumentException e) {
            reenviarConError("Formato de fecha inválido. Use el selector de fechas.",
                    idCultivoStr, request, response);
            return;
        }

        if (fechaInicio.after(fechaFinalizo)) {
            reenviarConError("La fecha de inicio no puede ser posterior a la fecha de finalización.",
                    idCultivoStr, request, response);
            return;
        }

        // ── 4. Persistir ──────────────────────────────────────────────────────
        registros reg = new registros(
                idCultivo,
                descripcion.trim(),
                idUsuario,
                fechaInicio,
                fechaFinalizo,
                (observaciones != null && !observaciones.trim().isEmpty())
                        ? observaciones.trim() : null
        );

        RegistroTrabajoDao dao = new RegistroTrabajoDao();
        boolean exito = dao.registrarLabor(reg);

        // ── 5. Redirección ────────────────────────────────────────────────────
        if (exito) {
            response.sendRedirect(request.getContextPath()
                    + "/templates/trabajador/cultivos.jsp?status=success&idCultivo=" + idCultivo);
        } else {
            reenviarConError("Error al guardar en base de datos. Intente nuevamente.",
                    idCultivoStr, request, response);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean estaVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    // Usa request.setAttribute para pasar idCultivo al JSP.
    // RequestDispatcher.forward() ignora los parámetros en la query string de la ruta,
    // por lo que el idCultivo debe viajar como atributo del request, no como ?param=valor.
    private void reenviarConError(String mensaje, String idCultivo,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("mensajeError", mensaje);
        request.setAttribute("idCultivo", idCultivo);
        request.getRequestDispatcher("/templates/trabajador/agregar_trabajos.jsp")
               .forward(request, response);
    }
}