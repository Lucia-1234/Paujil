package com.paujil.controlador;

import com.paujil.dao.UsuarioDao;
import com.paujil.modelo.usuario;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
// Mapea este servlet a una URL especifica sin requerir configuracion en web.xml
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/GestionarRoles")
public class GestionarRoles extends HttpServlet {

    // Guarda de sesion (administrador)

    // Metodo reutilizable que centraliza la logica de autorizacion,
    // evitando duplicarla en cada handler HTTP
    private boolean sesionAdminValida(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        // false previene crear sesion fantasma para usuarios no autenticados
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

    //  GET: cargar la lista de usuarios pendientes 

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Cortocircuita la ejecucion si el solicitante no tiene privilegios de administrador
        if (!sesionAdminValida(request, response)) return;

        UsuarioDao dao = new UsuarioDao();
        // Consulta solo usuarios en estado pendiente de aprobacion, no todos los del sistema
        List<usuario> pendientes = dao.listarUsuariosPendientes();

        // Expone la lista al scope de request para que la JSP la consuma via EL o JSTL
        request.setAttribute("usuariosPendientes", pendientes);

        // Forward mantiene la URL original en el navegador, a diferencia de sendRedirect
        request.getRequestDispatcher("/templates/administrador/asignar_rol.jsp")
               .forward(request, response);
    }

    // POST: aprobar o denegar un usuario pendiente 

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (!sesionAdminValida(request, response)) return;

        String accion = request.getParameter("accion");
        String idStr  = request.getParameter("id_usuario");

        // Validacion temprana: aborta si faltan parametros obligatorios antes de tocar la BD
        if (idStr == null || idStr.trim().isEmpty()
                || accion == null || accion.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath()
                    + "/GestionarRoles?error=parametros_invalidos");
            return;
        }

        int idUsuario;
        try {
            // trim() previene que espacios en blanco rompan el parseo
            idUsuario = Integer.parseInt(idStr.trim());
        } catch (NumberFormatException e) {
            // Captura entradas malformadas o intentos de inyectar valores no numericos
            response.sendRedirect(request.getContextPath()
                    + "/GestionarRoles?error=id_invalido");
            return;
        }

        UsuarioDao dao = new UsuarioDao();
        boolean ok;

        // toLowerCase() hace la comparacion robusta frente a variaciones de capitalizacion del formulario
        switch (accion.trim().toLowerCase()) {
            case "aceptar":
                // Activa la cuenta sin eliminarla, el usuario podra iniciar sesion tras esta operacion
                ok = dao.actualizarEstado(idUsuario, "Activo");
                response.sendRedirect(request.getContextPath()
                        + "/GestionarRoles?status=" + (ok ? "aceptado" : "error"));
                break;
            case "denegar":
                // Elimina el registro por completo, el rechazo es definitivo, no solo un cambio de estado
                ok = dao.eliminarUsuario(idUsuario);
                response.sendRedirect(request.getContextPath()
                        + "/GestionarRoles?status=" + (ok ? "denegado" : "error"));
                break;
            default:
                // Protege contra acciones arbitrarias enviadas manualmente fuera del formulario
                response.sendRedirect(request.getContextPath()
                        + "/GestionarRoles?error=accion_invalida");
        }
    }
}