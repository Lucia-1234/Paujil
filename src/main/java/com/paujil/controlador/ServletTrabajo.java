package com.paujil.controlador;

import com.paujil.modelo.trabajo;
import com.paujil.dao.TrabajoDao;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;

@WebServlet("/ServletTrabajo")
public class ServletTrabajo extends HttpServlet {

    // ── POST: registrar nuevo trabajo ─────────────────────────────────────────
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // 1. Verificar sesión activa y rol administrador
        // Solo el administrador puede asignar trabajos a usuarios
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("idUsuario") == null) {
            response.sendRedirect(request.getContextPath()
                    + "/templates/login.jsp?error=sesion_expirada");
            return;
        }

        // 2. Recolección de parámetros
        String nombre         = request.getParameter("nombreTrabajo");
        String desc           = request.getParameter("descripcion");
        String fechaStr       = request.getParameter("fechaAsignacion");
        String idCultivoStr   = request.getParameter("idCultivo");
        String idUsuarioStr   = request.getParameter("idUsuario"); // trabajador asignado

        // 3. Validaciones
        if (estaVacio(nombre) || estaVacio(desc) || estaVacio(fechaStr)
                || estaVacio(idCultivoStr) || estaVacio(idUsuarioStr)) {
            enviarError("Todos los campos son obligatorios.", request, response);
            return;
        }

        int idCultivo;
        int idUsuario;
        try {
            idCultivo = Integer.parseInt(idCultivoStr);
            idUsuario = Integer.parseInt(idUsuarioStr);
        } catch (NumberFormatException e) {
            enviarError("Cultivo o usuario no válido.", request, response);
            return;
        }

        Date fechaAsignacion;
        try {
            fechaAsignacion = Date.valueOf(fechaStr); // Espera yyyy-MM-dd
        } catch (IllegalArgumentException e) {
            enviarError("Formato de fecha inválido.", request, response);
            return;
        }

        // 4. Construir objeto y persistir
        trabajo t = new trabajo(nombre.trim(), desc.trim(), fechaAsignacion);

        TrabajoDao dao = new TrabajoDao();
        boolean ok = dao.registrarTrabajoCompleto(t, idCultivo, idUsuario);

        // 5. Redirección
        if (ok) {
            response.sendRedirect(request.getContextPath()
                    + "/templates/administrador/lista_trabajos.jsp?status=success");
        } else {
            enviarError("Error al guardar en base de datos. Intente nuevamente.", request, response);
        }
    }

    // ── GET: eliminar trabajo ─────────────────────────────────────────────────
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        if ("eliminar".equals(accion)) {
            String idStr = request.getParameter("id");
            if (estaVacio(idStr)) {
                response.sendRedirect(request.getContextPath()
                        + "/templates/administrador/lista_trabajos.jsp?error=id_invalido");
                return;
            }

            try {
                int id = Integer.parseInt(idStr);
                TrabajoDao dao = new TrabajoDao();
                boolean eliminado = dao.eliminarTrabajo(id);

                response.sendRedirect(request.getContextPath()
                        + "/templates/administrador/lista_trabajos.jsp?status="
                        + (eliminado ? "eliminado" : "error"));

            } catch (NumberFormatException e) {
                response.sendRedirect(request.getContextPath()
                        + "/templates/administrador/lista_trabajos.jsp?error=id_invalido");
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean estaVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private void enviarError(String mensaje, HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("mensajeError", mensaje);
        request.getRequestDispatcher(
                "/templates/administrador/agregar_trabajo.jsp")
                .forward(request, response);
    }
}