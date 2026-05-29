package com.paujil.controlador;

import com.paujil.dao.UsuarioDao;
import com.paujil.modelo.usuario;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/GestionarRoles")
public class GestionarRoles extends HttpServlet {

    // ── Guarda de sesión (administrador) ─────────────────────────────────────
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

    // ── GET: cargar la lista de usuarios pendientes ───────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!sesionAdminValida(request, response)) return;

        UsuarioDao dao = new UsuarioDao();
        List<usuario> pendientes = dao.listarUsuariosPendientes();

        request.setAttribute("usuariosPendientes", pendientes);
        request.getRequestDispatcher("/templates/administrador/asignar_rol.jsp")
               .forward(request, response);
    }

    // ── POST: aprobar o denegar un usuario pendiente ──────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!sesionAdminValida(request, response)) return;

        String accion = request.getParameter("accion");
        String idStr  = request.getParameter("id_usuario");

        if (idStr == null || idStr.trim().isEmpty()
                || accion == null || accion.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath()
                    + "/GestionarRoles?error=parametros_invalidos");
            return;
        }

        int idUsuario;
        try {
            idUsuario = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath()
                    + "/GestionarRoles?error=id_invalido");
            return;
        }

        UsuarioDao dao = new UsuarioDao();
        boolean ok;

        switch (accion.trim().toLowerCase()) {
            case "aceptar":
                ok = dao.actualizarEstado(idUsuario, "Activo");
                response.sendRedirect(request.getContextPath()
                        + "/GestionarRoles?status=" + (ok ? "aceptado" : "error"));
                break;

            case "denegar":
                ok = dao.eliminarUsuario(idUsuario);
                response.sendRedirect(request.getContextPath()
                        + "/GestionarRoles?status=" + (ok ? "denegado" : "error"));
                break;

            default:
                response.sendRedirect(request.getContextPath()
                        + "/GestionarRoles?error=accion_invalida");
        }
    }
}