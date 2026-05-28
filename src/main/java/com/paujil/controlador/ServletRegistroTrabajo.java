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

@WebServlet("/ServletLabor")
public class ServletRegistroTrabajo extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // ── 1. Obtener id del usuario desde la sesión ─────────────────────────
        // CORRECCIÓN PRINCIPAL: el responsable NO viene del formulario (cualquiera
        // podría manipularlo). Se toma de la sesión que creó el login.
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("idUsuario") == null) {
            response.sendRedirect(request.getContextPath()
                    + "/templates/login.jsp?error=sesion_expirada");
            return;
        }
        int idUsuario = (int) session.getAttribute("idUsuario");

        // ── 2. Recolección de parámetros del formulario ───────────────────────
        String idCultivoStr        = request.getParameter("idCultivo");
        String descripcion         = request.getParameter("descripcionTrabajo");
        String fechaInicioStr      = request.getParameter("fechaInicio");
        String fechaFinalizoStr    = request.getParameter("fechaFinalizo");
        String observaciones       = request.getParameter("observaciones");

        // ── 3. Validaciones ───────────────────────────────────────────────────

        // 3a. Campos obligatorios
        if (estaVacio(idCultivoStr) || estaVacio(descripcion)
                || estaVacio(fechaInicioStr) || estaVacio(fechaFinalizoStr)) {
            reenviarConError("Todos los campos obligatorios deben completarse.",
                    idCultivoStr, request, response);
            return;
        }

        // 3b. idCultivo debe ser numérico
        int idCultivo;
        try {
            idCultivo = Integer.parseInt(idCultivoStr);
        } catch (NumberFormatException e) {
            reenviarConError("Cultivo no válido.", idCultivoStr, request, response);
            return;
        }

        // 3c. Fechas con formato válido (esperado: yyyy-MM-dd)
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

        // 3d. Fecha de inicio no puede ser posterior a fecha de finalización
        if (fechaInicio.after(fechaFinalizo)) {
            reenviarConError("La fecha de inicio no puede ser posterior a la fecha de finalización.",
                    idCultivoStr, request, response);
            return;
        }

        // ── 4. Construir el objeto con el modelo actualizado ──────────────────
        registros reg = new registros(
                idCultivo,
                descripcion.trim(),
                idUsuario,       // FK desde sesión — nunca del formulario
                fechaInicio,
                fechaFinalizo,
                (observaciones != null && !observaciones.trim().isEmpty())
                        ? observaciones.trim() : null
        );

        // ── 5. Persistir ──────────────────────────────────────────────────────
        RegistroTrabajoDao dao = new RegistroTrabajoDao();
        boolean exito = dao.registrarLabor(reg);

        // ── 6. Redirección ────────────────────────────────────────────────────
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

    /**
     * Reenvía al formulario de registro de labor conservando el idCultivo
     * en la URL para que el JSP pueda pre-seleccionar el cultivo correcto.
     */
    private void reenviarConError(String mensaje, String idCultivo,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("mensajeError", mensaje);
        request.getRequestDispatcher(
                "/templates/trabajador/agregar_trabajos.jsp?idCultivo=" + idCultivo)
                .forward(request, response);
    }
}