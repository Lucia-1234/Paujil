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

    // ── Guarda de sesión (administrador) ──────────────────────────────────────
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

    // ── Guarda de sesión (cualquier usuario autenticado) ──────────────────────
    private boolean sesionValida(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("idUsuario") == null) {
            response.sendRedirect(request.getContextPath() + "/templates/login.jsp?error=acceso_denegado");
            return false;
        }
        return true;
    }

    // ── GET ───────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("DEBUG: Entrando al Servlet, accion=" + request.getParameter("accion"));

        String accion = request.getParameter("accion");
        if (accion == null) accion = "listar";

        switch (accion) {

            // ── Acciones de ADMINISTRADOR ──────────────────────────────────
            case "listar":
                if (!sesionAdminValida(request, response)) return;
                List<trabajo> lista = trabajoDao.listarTrabajosCompletos();
                request.setAttribute("listaTrabajos", lista);
                request.getRequestDispatcher("/templates/administrador/listar_trabajos.jsp")
                       .forward(request, response);
                break;

            case "eliminar":
                if (!sesionAdminValida(request, response)) return;
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
                if (!sesionAdminValida(request, response)) return;
                CultivoDao cDao = new CultivoDao();
                UsuarioDao uDao = new UsuarioDao();
                request.setAttribute("listaCultivos", cDao.listarCultivos());
                request.setAttribute("listaUsuarios", uDao.listarUsuariosActivos());
                request.getRequestDispatcher("/templates/administrador/asignar_trabajos.jsp")
                       .forward(request, response);
                break;

            // ── Acción de TRABAJADOR ───────────────────────────────────────
            case "misTrabajos":
                if (!sesionValida(request, response)) return;
                HttpSession session = request.getSession(false);
                int idUsuario = (int) session.getAttribute("idUsuario");
                List<trabajo> misTrabajos = trabajoDao.listarTrabajosPorUsuario(idUsuario);

                // DEBUG: Ver si realmente hay datos antes de ir al JSP
                System.out.println("DEBUG: Cantidad de trabajos enviados: " + (misTrabajos != null ? misTrabajos.size() : "NULL"));

                request.setAttribute("listaMisTrabajos", misTrabajos);
                request.getRequestDispatcher("/templates/trabajador/trabajos_asignados.jsp").forward(request, response);
                break;
                
            case "finalizados":
                if (!sesionValida(request, response)) return;
                HttpSession session2 = request.getSession(false);
                int idUserFin = (int) session2.getAttribute("idUsuario");
                // Llamamos al método que filtra solo los terminados
                request.setAttribute("listaFinalizados", trabajoDao.listarTrabajosFinalizadosPorUsuario(idUserFin));
                request.getRequestDispatcher("/templates/trabajador/trabajos_finalizados.jsp").forward(request, response);
                break; 
                
            
        }
    }

    // ── POST ──────────────────────────────────────────────────────────────────
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        if (accion == null) accion = "";

        switch (accion) {

            // ── Admin: registrar nuevo trabajo ─────────────────────────────
            case "registrar":
                if (!sesionAdminValida(request, response)) return;
                registrarTrabajo(request, response);
                break;

            // ── Trabajador: guardar avance o marcar finalizado ─────────────
            case "actualizarEstado":
                if (!sesionValida(request, response)) return;
                actualizarEstado(request, response);
                break;

            default:
                // Compatibilidad: si no viene accion en POST se asume registrar (admin)
                if (!sesionAdminValida(request, response)) return;
                registrarTrabajo(request, response);
        }
    }

    // ── Helper: registrar trabajo (admin) ─────────────────────────────────────
    private void registrarTrabajo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

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

    // ── Helper: actualizar estado (trabajador) ────────────────────────────────
    private void actualizarEstado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idTrabajoStr = request.getParameter("idTrabajo");
        String observaciones = request.getParameter("observaciones");
        String btnAccion     = request.getParameter("btnAccion"); // "guardar" | "finalizar"

        if (estaVacio(idTrabajoStr)) {
            response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=error");
            return;
        }

        try {
            int idTrabajo = Integer.parseInt(idTrabajoStr);
            boolean finalizar = "finalizar".equals(btnAccion);
            boolean ok = trabajoDao.actualizarEstadoTrabajo(idTrabajo, observaciones, finalizar);
            String status = ok ? (finalizar ? "finalizado" : "guardado") : "error";
            response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=" + status);
        } catch (NumberFormatException e) {
            response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=error");
        }
    }

    // ── Helpers generales ──────────────────────────────────────────────────────
    private boolean estaVacio(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private void enviarError(String mensaje, HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("mensajeError", mensaje);
        request.setAttribute("listaCultivos", new CultivoDao().listarCultivos());
        request.setAttribute("listaUsuarios", new UsuarioDao().listarUsuariosActivos());
        request.getRequestDispatcher("/templates/administrador/asignar_trabajos.jsp")
               .forward(request, response);
    }
}