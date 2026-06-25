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
@WebServlet("/GestionarRoles") // Mapeo de la URL.
public class GestionarRoles extends HttpServlet { // Servlet para gestión de roles.
    // Método privado para validar que el usuario es administrador.
    private boolean sesionAdminValida(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false); // Obtiene sesión existente.
        // Valida si existe sesión, ID y rol administrativo.
        if (session == null || session.getAttribute("idUsuario") == null 
            || !"administrador".equalsIgnoreCase((String) session.getAttribute("rolUsuario"))) {
            response.sendRedirect(request.getContextPath() + "/templates/login.jsp?error=acceso_denegado"); // Redirige si falla.
            return false; // Retorna acceso denegado.
        } // Fin if.
        return true; // Retorna acceso permitido.
    } // Fin método validación.
    @Override // Sobrescribe método GET.
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!sesionAdminValida(request, response)) return; // Valida sesión.
        String accion = request.getParameter("accion"); // Verifica si viene una acción (denegar/aceptar por GET).
        if (accion != null && !accion.trim().isEmpty()) { // Si hay acción, delega al doPost.
            doPost(request, response); // Reutiliza la lógica de POST para no duplicar código.
            return; // Termina flujo GET.
        } // Fin if acción.
        UsuarioDao dao = new UsuarioDao(); // Instancia DAO.
        List<usuario> pendientes = dao.listarUsuariosPendientes(); // Obtiene pendientes.
        request.setAttribute("usuariosPendientes", pendientes); // Pasa a la vista.
        request.getRequestDispatcher("/templates/administrador/asignar_rol.jsp").forward(request, response); // Renderiza vista.
    } // Fin doGet.
    @Override // Sobrescribe método POST.
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!sesionAdminValida(request, response)) return; // Valida sesión.
        String accion = request.getParameter("accion"); // Obtiene acción.
        String idStr = request.getParameter("id_usuario"); // Obtiene ID.
        // Valida parámetros nulos o vacíos.
        if (idStr == null || idStr.trim().isEmpty() || accion == null || accion.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/GestionarRoles?error=parametros_invalidos"); return;
        } // Fin validación.
        int idUsuario;
        try { idUsuario = Integer.parseInt(idStr.trim()); } // Parsea ID.
        catch (NumberFormatException e) { // Manejo formato erróneo.
            response.sendRedirect(request.getContextPath() + "/GestionarRoles?error=id_invalido"); return;
        } // Fin catch.
        UsuarioDao dao = new UsuarioDao(); // Instancia DAO.
        boolean ok; // Resultado de operación.
        switch (accion.trim().toLowerCase()) { // Lógica según acción.
            case "aceptar": // Caso aceptar.
                ok = dao.actualizarEstado(idUsuario, "Activo"); // Cambia a Activo.
                response.sendRedirect(request.getContextPath() + "/GestionarRoles?status=" + (ok ? "aceptado" : "error"));
                break;
            case "denegar": // Caso denegar.
                ok = dao.eliminarUsuario(idUsuario); // Elimina usuario.
                response.sendRedirect(request.getContextPath() + "/GestionarRoles?status=" + (ok ? "denegado" : "error"));
                break;
            default: // Caso error.
                response.sendRedirect(request.getContextPath() + "/GestionarRoles?error=accion_invalida");
        } // Fin switch.
    } // Fin doPost.
} // Fin clase.