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

@WebServlet("/ServletUsuario")
public class ServletUsuarios extends HttpServlet {

    // ── Guarda de sesion ──────────────────────────────────────────────────────

    // Centraliza la verificacion de autorizacion para no repetirla en cada handler
    private boolean sesionAdminValida(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        // false evita crear sesion fantasma si el usuario no esta autenticado
        HttpSession session = req.getSession(false);
        // Triple condicion: sesion existente + usuario identificado + rol correcto
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

        // El parametro "accion" discrimina la operacion; su ausencia carga la vista por defecto
        String accion = req.getParameter("accion");
        String idStr  = req.getParameter("id");
        UsuarioDao dao = new UsuarioDao();

        if (accion != null) {
            switch (accion) {

                case "activar":
                    // Reactiva una cuenta previamente desactivada
                    cambiarEstado(dao, idStr, "Activo", res, req);
                    return;

                case "desactivar":
                    // Suspende la cuenta sin eliminarla; el usuario no podra iniciar sesion
                    cambiarEstado(dao, idStr, "Inactivo", res, req);
                    return;

                case "aprobar":
                    // Aprueba un registro pendiente; comparte logica con "activar" pero semanticamente distinto
                    cambiarEstado(dao, idStr, "Activo", res, req);
                    return;

                case "denegar":
                    // El rechazo es definitivo: elimina el registro en lugar de cambiar su estado
                    eliminar(dao, idStr, res, req);
                    return;

                case "eliminar":
                    // Eliminacion directa de usuario activo o inactivo por el administrador
                    eliminar(dao, idStr, res, req);
                    return;

                case "pendientes":
                    // Muestra solo usuarios en estado 'Pendiente' que esperan aprobacion del admin
                    List<usuario> pendientes = dao.listarUsuariosPendientes();
                    req.setAttribute("listaUsuarios", pendientes);
                    // El atributo "vista" permite que el JSP ajuste columnas y botones segun el contexto
                    req.setAttribute("vista", "pendientes");
                    req.getRequestDispatcher("/templates/administrador/gestion_usuarios.jsp")
                       .forward(req, res);
                    return;

                default:
                    // Accion desconocida: cae al caso por defecto que carga todos los usuarios
                    break;
            }
        }

        // Vista por defecto: carga activos e inactivos juntos; el filtrado se delega al cliente via JS
        List<usuario> todos = dao.listarTodosLosUsuarios();
        req.setAttribute("listaUsuarios", todos);
        // "todos" indica al JSP que muestre las opciones de filtrado completo
        req.setAttribute("vista", "todos");
        req.getRequestDispatcher("/templates/administrador/gestion_usuarios.jsp")
           .forward(req, res);
    }

    // ── POST: actualizar datos del usuario ────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        if (!sesionAdminValida(req, res)) return;

        // Fuerza UTF-8 antes de leer parametros para evitar corrupcion en nombres con caracteres especiales
        req.setCharacterEncoding("UTF-8");

        String idStr  = req.getParameter("id");
        // estadoUsuario permite cambiar el estado desde el formulario de edicion inline
        String estado = req.getParameter("estadoUsuario");

        // ID es el minimo indispensable; sin el no hay operacion posible
        if (idStr == null || idStr.isBlank()) {
            res.sendRedirect("ServletUsuario?status=error");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            UsuarioDao dao = new UsuarioDao();
            boolean ok = false;

            // Solo actualiza si se envio un estado valido; permite extender el POST con otros campos sin romper el flujo
            if (estado != null && !estado.isBlank()) {
                ok = dao.actualizarEstado(id, estado.trim());
            }

            // status en la URL permite que la vista muestre retroalimentacion tras el redirect
            res.sendRedirect("ServletUsuario?status=" + (ok ? "success" : "error"));
        } catch (NumberFormatException e) {
            // ID no numerico indica manipulacion del formulario
            res.sendRedirect("ServletUsuario?status=error");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    // Reutilizable para activar, desactivar y aprobar; el estado final lo decide el caller
    private void cambiarEstado(UsuarioDao dao, String idStr, String nuevoEstado,
                               HttpServletResponse res, HttpServletRequest req)
            throws IOException {
        if (idStr != null && !idStr.isBlank()) {
            try {
                dao.actualizarEstado(Integer.parseInt(idStr), nuevoEstado);
            } catch (NumberFormatException ignored) {
                // ID malformado; se redirige sin modificar nada
            }
        }
        // Retorna a la vista de origen para no perder el contexto de navegacion del admin
        String vista = req.getParameter("vista");
        String redirect = "pendientes".equals(vista)
                ? "ServletUsuario?accion=pendientes&status=success"
                : "ServletUsuario?status=success";
        res.sendRedirect(redirect);
    }

    // Compartido entre "denegar" y "eliminar"; ambos resultan en la misma operacion de BD
    private void eliminar(UsuarioDao dao, String idStr,
                          HttpServletResponse res, HttpServletRequest req)
            throws IOException {
        if (idStr != null && !idStr.isBlank()) {
            try {
                dao.eliminarUsuario(Integer.parseInt(idStr));
            } catch (NumberFormatException ignored) {
                // ID malformado; se redirige sin eliminar
            }
        }
        // Preserva el contexto de vista para que el admin regrese a la misma seccion que estaba usando
        String vista = req.getParameter("vista");
        String redirect = "pendientes".equals(vista)
                ? "ServletUsuario?accion=pendientes&status=success"
                : "ServletUsuario?status=success";
        res.sendRedirect(redirect);
    }
}