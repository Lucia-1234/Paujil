package com.paujil.controlador;

import com.paujil.dao.CultivoDao;
import com.paujil.dao.TrabajoDao;
import com.paujil.dao.UsuarioDao;
import com.paujil.modelo.trabajo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

@WebServlet("/ServletTrabajo")
public class ServletTrabajo extends HttpServlet {

    private TrabajoDao trabajoDao = new TrabajoDao();

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

    // ── GET: Listar, Eliminar y Preparar creación ────────────────────────────
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!sesionAdminValida(request, response)) return;

        String accion = request.getParameter("accion");
        if (accion == null) accion = "listar";

        switch (accion) {
            case "listar":
                List<trabajo> lista = trabajoDao.listarTrabajosCompletos();
                request.setAttribute("listaTrabajos", lista);
                request.getRequestDispatcher("/templates/administrador/listar_trabajos.jsp")
                       .forward(request, response);
                break;

            case "eliminar":
                String idStr = request.getParameter("id");
                if (idStr == null || idStr.trim().isEmpty()) {
                    response.sendRedirect("ServletTrabajo?accion=listar&status=error");
                    return;
                }
                try {
                    int id = Integer.parseInt(idStr);
                    boolean eliminado = trabajoDao.eliminarTrabajo(id);
                    response.sendRedirect("ServletTrabajo?accion=listar&status=" + (eliminado ? "eliminado" : "error"));
                } catch (NumberFormatException e) {
                    response.sendRedirect("ServletTrabajo?accion=listar&status=error");
                }
                break;

            case "prepararCreacion":
                CultivoDao cDao = new CultivoDao();
                UsuarioDao uDao = new UsuarioDao();
                request.setAttribute("listaCultivos", cDao.listarCultivos());
                request.setAttribute("listaUsuarios", uDao.listarUsuariosActivos());
                request.getRequestDispatcher("/templates/administrador/asignar_trabajos.jsp")
                       .forward(request, response);
                break;

            default:
                response.sendRedirect("ServletTrabajo?accion=listar");
        }
    }

    // ── POST: Registrar nuevo trabajo ─────────────────────────────────────────
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (!sesionAdminValida(request, response)) return;

        String nombre       = request.getParameter("nombreTrabajo");
        String desc         = request.getParameter("descripcion");
        String fechaStr     = request.getParameter("fechaAsignacion");
        String idCultivoStr = request.getParameter("idCultivo");
        String idUsuarioStr = request.getParameter("idUsuario");

        if (estaVacio(nombre) || estaVacio(desc) || estaVacio(fechaStr)
                || estaVacio(idCultivoStr) || estaVacio(idUsuarioStr)) {
            enviarError("Todos los campos son obligatorios.", request, response);
            return;
        }

        try {
            int idCultivo        = Integer.parseInt(idCultivoStr);
            int idUsuario        = Integer.parseInt(idUsuarioStr);
            Date fechaAsignacion = Date.valueOf(fechaStr);

            trabajo t = new trabajo(nombre.trim(), desc.trim(), fechaAsignacion);
            boolean ok = trabajoDao.registrarTrabajoCompleto(t, idCultivo, idUsuario);

            if (ok) {
                response.sendRedirect("ServletTrabajo?accion=listar&status=success");
            } else {
                enviarError("Error al guardar en base de datos.", request, response);
            }
        } catch (Exception e) {
            enviarError("Datos inválidos: " + e.getMessage(), request, response);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private boolean estaVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private void enviarError(String mensaje, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Recarga los selectores para que el formulario se pueda re-mostrar correctamente
        request.setAttribute("mensajeError", mensaje);
        request.setAttribute("listaCultivos", new CultivoDao().listarCultivos());
        request.setAttribute("listaUsuarios", new UsuarioDao().listarUsuariosActivos());
        request.getRequestDispatcher("/templates/administrador/asignar_trabajos.jsp")
               .forward(request, response);
    }
}