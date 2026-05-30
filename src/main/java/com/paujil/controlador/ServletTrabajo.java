package com.paujil.controlador;

import com.paujil.dao.CultivoDao;
import com.paujil.dao.TrabajoDao;
import com.paujil.dao.UsuarioDao;
import com.paujil.modelo.trabajo;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

import static com.paujil.utils.ServletUtils.estaVacio;
import static com.paujil.utils.ServletUtils.verificarSesionAdmin;
import static com.paujil.utils.ServletUtils.verificarSesionUsuario;

@WebServlet("/ServletTrabajo")
public class ServletTrabajo extends HttpServlet {

    private final TrabajoDao trabajoDao = new TrabajoDao();

    // ── GET ───────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        if (accion == null) accion = "listar";

        switch (accion) {

            // ── Acciones de ADMINISTRADOR ──────────────────────────────────
            case "listar":
                if (!verificarSesionAdmin(request, response)) return;
                List<trabajo> lista = trabajoDao.listarTrabajosCompletos();
                request.setAttribute("listaTrabajos", lista);
                request.getRequestDispatcher("/templates/administrador/listar_trabajos.jsp")
                       .forward(request, response);
                break;

            case "eliminar":
                if (!verificarSesionAdmin(request, response)) return;
                String idStr = request.getParameter("id");
                if (estaVacio(idStr)) {
                    response.sendRedirect("ServletTrabajo?accion=listar&status=error");
                    return;
                }
                try {
                    int id = Integer.parseInt(idStr);
                    boolean eliminado = trabajoDao.eliminarTrabajo(id);
                    response.sendRedirect("ServletTrabajo?accion=listar&status="
                            + (eliminado ? "eliminado" : "error"));
                } catch (NumberFormatException e) {
                    response.sendRedirect("ServletTrabajo?accion=listar&status=error");
                }
                break;

            case "prepararCreacion":
                if (!verificarSesionAdmin(request, response)) return;
                request.setAttribute("listaCultivos", new CultivoDao().listarCultivos());
                request.setAttribute("listaUsuarios", new UsuarioDao().listarUsuariosActivos());
                request.getRequestDispatcher("/templates/administrador/asignar_trabajos.jsp")
                       .forward(request, response);
                break;

            // ── Acciones de TRABAJADOR ─────────────────────────────────────
            case "misTrabajos":
                if (!verificarSesionUsuario(request, response)) return;
                int idUsuario = (int) request.getSession(false).getAttribute("idUsuario");
                request.setAttribute("listaMisTrabajos",
                        trabajoDao.listarTrabajosPorUsuario(idUsuario));
                request.getRequestDispatcher("/templates/trabajador/trabajos_asignados.jsp")
                       .forward(request, response);
                break;

            case "finalizados":
                if (!verificarSesionUsuario(request, response)) return;
                int idUserFin = (int) request.getSession(false).getAttribute("idUsuario");
                request.setAttribute("listaFinalizados",
                        trabajoDao.listarTrabajosFinalizadosPorUsuario(idUserFin));
                request.getRequestDispatcher("/templates/trabajador/trabajos_finalizados.jsp")
                       .forward(request, response);
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

            case "registrar":
                if (!verificarSesionAdmin(request, response)) return;
                registrarTrabajo(request, response);
                break;

            case "actualizarEstado":
                if (!verificarSesionUsuario(request, response)) return;
                actualizarEstado(request, response);
                break;

            default:
                if (!verificarSesionAdmin(request, response)) return;
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

        String idTrabajoStr  = request.getParameter("idTrabajo");
        String observaciones = request.getParameter("observaciones");
        String btnAccion     = request.getParameter("btnAccion");

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

    // ── Helper: reenviar a formulario con error ───────────────────────────────
    private void enviarError(String mensaje, HttpServletRequest request,
                             HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("mensajeError", mensaje);
        request.setAttribute("listaCultivos", new CultivoDao().listarCultivos());
        request.setAttribute("listaUsuarios", new UsuarioDao().listarUsuariosActivos());
        request.getRequestDispatcher("/templates/administrador/asignar_trabajos.jsp")
               .forward(request, response);
    }
}