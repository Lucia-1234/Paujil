package com.paujil.controlador;

import com.paujil.dao.UsuarioDao;
import com.paujil.modelo.usuario;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Gestión de usuarios desde el panel de administrador.
 *
 *  GET  /ServletUsuario               → lista todos los usuarios activos
 *  GET  /ServletUsuario?accion=pendientes → lista usuarios pendientes de aprobación
 *  GET  /ServletUsuario?accion=activar&id=X    → activa usuario X
 *  GET  /ServletUsuario?accion=desactivar&id=X → desactiva usuario X
 *  GET  /ServletUsuario?accion=aprobar&id=X    → aprueba usuario pendiente (pasa a Activo)
 *  GET  /ServletUsuario?accion=denegar&id=X    → deniega usuario pendiente (lo elimina)
 *  GET  /ServletUsuario?accion=eliminar&id=X   → elimina usuario activo X
 *  POST /ServletUsuario                → guarda cambios de edición de usuario
 */
@WebServlet("/ServletUsuario")
public class ServletUsuarios extends HttpServlet {

    // ── Guarda de sesión ──────────────────────────────────────────────────────
    private boolean sesionAdminValida(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null
                || session.getAttribute("idUsuario") == null
                || !"administrador".equalsIgnoreCase((String) session.getAttribute("rolUsuario"))) {
            res.sendRedirect(req.getContextPath() + "/templates/login.jsp?error=acceso_denegado");
            return false;
        }
        return true;
    }

    // ── GET ───────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if (!sesionAdminValida(req, res)) return;

        String accion = req.getParameter("accion");
        String idStr  = req.getParameter("id");
        UsuarioDao dao = new UsuarioDao();

        // Acciones que cambian estado y redirigen
        if (accion != null) {
            switch (accion) {

                case "activar":
                    cambiarEstado(dao, idStr, "Activo", res, req);
                    return;

                case "desactivar":
                    cambiarEstado(dao, idStr, "Inactivo", res, req);
                    return;

                case "aprobar":
                    // Aprobar = poner en Activo un usuario Pendiente
                    cambiarEstado(dao, idStr, "Activo", res, req);
                    return;

                case "denegar":
                    // Denegar = eliminar el usuario pendiente
                    eliminar(dao, idStr, res, req);
                    return;

                case "eliminar":
                    eliminar(dao, idStr, res, req);
                    return;

                case "pendientes":
                    List<usuario> pendientes = dao.listarUsuariosPendientes();
                    req.setAttribute("listaUsuarios", pendientes);
                    req.setAttribute("vista", "pendientes");
                    req.getRequestDispatcher("/templates/administrador/gestion_usuarios.jsp")
                       .forward(req, res);
                    return;

                default:
                    break;
            }
        }

        // Vista por defecto: usuarios activos
        List<usuario> activos = dao.listarUsuariosActivos();
        req.setAttribute("listaUsuarios", activos);
        req.setAttribute("vista", "activos");
        req.getRequestDispatcher("/templates/administrador/gestion_usuarios.jsp")
           .forward(req, res);
    }

    // ── POST: actualizar datos del usuario ────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if (!sesionAdminValida(req, res)) return;

        req.setCharacterEncoding("UTF-8");

        String idStr  = req.getParameter("id");
        String estado = req.getParameter("estadoUsuario");

        if (idStr == null || idStr.isBlank()) {
            res.sendRedirect("ServletUsuario?status=error");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            UsuarioDao dao = new UsuarioDao();
            boolean ok = false;

            if (estado != null && !estado.isBlank()) {
                ok = dao.actualizarEstado(id, estado.trim());
            }

            res.sendRedirect("ServletUsuario?status=" + (ok ? "success" : "error"));
        } catch (NumberFormatException e) {
            res.sendRedirect("ServletUsuario?status=error");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void cambiarEstado(UsuarioDao dao, String idStr, String nuevoEstado,
                               HttpServletResponse res, HttpServletRequest req)
            throws IOException {
        if (idStr != null && !idStr.isBlank()) {
            try {
                dao.actualizarEstado(Integer.parseInt(idStr), nuevoEstado);
            } catch (NumberFormatException ignored) {}
        }
        // Volvemos a la vista desde donde se llamó (activos o pendientes)
        String vista = req.getParameter("vista");
        String redirect = "ServletUsuario" + ("pendientes".equals(vista) ? "?accion=pendientes" : "");
        res.sendRedirect(redirect + (redirect.contains("?") ? "&" : "?") + "status=success");
    }

    private void eliminar(UsuarioDao dao, String idStr,
                          HttpServletResponse res, HttpServletRequest req)
            throws IOException {
        if (idStr != null && !idStr.isBlank()) {
            try {
                dao.eliminarUsuario(Integer.parseInt(idStr));
            } catch (NumberFormatException ignored) {}
        }
        String vista = req.getParameter("vista");
        String redirect = "ServletUsuario" + ("pendientes".equals(vista) ? "?accion=pendientes" : "");
        res.sendRedirect(redirect + (redirect.contains("?") ? "&" : "?") + "status=success");
    }
}