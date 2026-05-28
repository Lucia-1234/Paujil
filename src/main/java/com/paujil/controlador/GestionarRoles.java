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

@WebServlet("/GestionarRoles")
public class GestionarRoles extends HttpServlet {

    // ── GET: cargar la lista de usuarios pendientes ───────────────────────────
    // El JSP no debería hacer lógica de BD directamente.
    // El servlet carga los datos y los pasa como atributo al JSP.
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UsuarioDao dao = new UsuarioDao();
        List<usuario> pendientes = dao.listarUsuariosPendientes();

        request.setAttribute("usuariosPendientes", pendientes);
        request.getRequestDispatcher(
                "/templates/administrador/asignar_rol.jsp")
                .forward(request, response);
    }

    // ── POST: aprobar o denegar un usuario pendiente ──────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion   = request.getParameter("accion");
        String idStr    = request.getParameter("id_usuario");

        // 1. Validar parámetros
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

        // 2. Ejecutar acción
        UsuarioDao dao = new UsuarioDao();
        boolean ok;

        switch (accion.trim().toLowerCase()) {
            case "aceptar":
                // Cambia el estado a 'Activo' — el usuario ya puede iniciar sesión
                ok = dao.actualizarEstado(idUsuario, "Activo");
                response.sendRedirect(request.getContextPath()
                        + "/GestionarRoles?status=" + (ok ? "aceptado" : "error"));
                break;

            case "denegar":
                // Elimina el registro completo — CASCADE limpia correos, teléfonos y rol
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