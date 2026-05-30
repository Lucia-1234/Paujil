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

    // Centraliza la logica de autorizacion para no repetirla en cada handler HTTP
    private boolean sesionAdminValida(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // false evita crear sesion fantasma si el usuario no esta autenticado
        HttpSession session = request.getSession(false);
        // Triple condicion: sesion existente + usuario identificado + rol correcto
        if (session == null
                || session.getAttribute("idUsuario") == null
                || !"administrador".equalsIgnoreCase((String) session.getAttribute("rolUsuario"))) {
            response.sendRedirect(request.getContextPath() + "/templates/login.jsp?error=acceso_denegado");
            return false;
        }
        return true;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Cortocircuita la ejecucion si el solicitante no tiene privilegios de administrador
        if (!sesionAdminValida(request, response)) return;

        UsuarioDao dao = new UsuarioDao();
        // Consulta solo usuarios en estado pendiente; los activos e inactivos se gestionan en otro servlet
        List<usuario> pendientes = dao.listarUsuariosPendientes();
        // Expone la lista al scope de request para que la JSP la consuma via EL o JSTL
        request.setAttribute("usuariosPendientes", pendientes);
        // Forward preserva la URL original en el navegador, a diferencia de sendRedirect
        request.getRequestDispatcher("/templates/administrador/asignar_rol.jsp")
               .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!sesionAdminValida(request, response)) return;

        String accion = request.getParameter("accion");
        String idStr  = request.getParameter("id_usuario");

        // Validacion temprana: aborta antes de tocar la BD si faltan los parametros minimos
        if (idStr == null || idStr.trim().isEmpty()
                || accion == null || accion.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath()
                    + "/GestionarRoles?error=parametros_invalidos");
            return;
        }

        int idUsuario;
        try {
            // trim() previene que espacios en blanco rompan el parseo del entero
            idUsuario = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            // Valor no numerico indica manipulacion del formulario o error del cliente
            response.sendRedirect(request.getContextPath()
                    + "/GestionarRoles?error=id_invalido");
            return;
        }

        UsuarioDao dao = new UsuarioDao();
        boolean ok;

        // toLowerCase() hace la comparacion robusta frente a variaciones de capitalizacion del formulario
        switch (accion.trim().toLowerCase()) {
            case "aceptar":
                // Activa la cuenta sin eliminarla; el usuario podra iniciar sesion tras esta operacion
                ok = dao.actualizarEstado(idUsuario, "Activo");
                response.sendRedirect(request.getContextPath()
                        + "/GestionarRoles?status=" + (ok ? "aceptado" : "error"));
                break;
            case "denegar":
                // Elimina el registro por completo; el rechazo es definitivo, no un simple cambio de estado
                ok = dao.eliminarUsuario(idUsuario);
                response.sendRedirect(request.getContextPath()
                        + "/GestionarRoles?status=" + (ok ? "denegado" : "error"));
                break;
            default:
                // Protege contra valores de accion arbitrarios enviados fuera del formulario
                response.sendRedirect(request.getContextPath()
                        + "/GestionarRoles?error=accion_invalida");
        }
    }
}