package com.paujil.controlador;

import com.paujil.dao.CultivoDao;
import com.paujil.dao.TipoTrabajoDao;
import com.paujil.dao.UsuarioDao;
import com.paujil.modelo.asignacion;
import com.paujil.modelo.trabajo;
import com.paujil.servicio.AsignacionServicio;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.util.List;

import static com.paujil.utils.ServletUtils.estaVacio;
import static com.paujil.utils.ServletUtils.verificarSesionAdmin;
import static com.paujil.utils.ServletUtils.verificarSesionUsuario;

@WebServlet("/ServletTrabajo")
public class ServletTrabajo extends HttpServlet {

    private final AsignacionServicio servicio = new AsignacionServicio();

    // ── GET ───────────────────────────────────────────────────────────────────
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");
        if (accion == null) accion = "listar";

        switch (accion) {

            case "listar":
                if (!verificarSesionAdmin(request, response)) return;
                request.setAttribute("listaAsignaciones", servicio.listarTodas());
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
                    boolean eliminado = servicio.eliminarTrabajo(Integer.parseInt(idStr));
                    response.sendRedirect("ServletTrabajo?accion=listar&status="
                            + (eliminado ? "eliminado" : "error"));
                } catch (NumberFormatException e) {
                    response.sendRedirect("ServletTrabajo?accion=listar&status=error");
                }
                break;

            case "prepararCreacion":
                if (!verificarSesionAdmin(request, response)) return;
                request.setAttribute("listaCultivos",     new CultivoDao().listarCultivos());
                request.setAttribute("listaUsuarios",     new UsuarioDao().listarUsuariosActivos());
                request.setAttribute("listaTiposTrabajo", new TipoTrabajoDao().listarTipos());
                request.getRequestDispatcher("/templates/administrador/asignar_trabajos.jsp")
                       .forward(request, response);
                break;

            case "misTrabajos":
                if (!verificarSesionUsuario(request, response)) return;
                int idUsuario = (int) request.getSession(false).getAttribute("idUsuario");
                request.setAttribute("listaMisTrabajos",
                        servicio.listarPorUsuario(idUsuario, null));
                request.getRequestDispatcher("/templates/trabajador/trabajos_asignados.jsp")
                       .forward(request, response);
                break;

            case "pendientes":
                if (!verificarSesionUsuario(request, response)) return;
                int idUserPend = (int) request.getSession(false).getAttribute("idUsuario");
                request.setAttribute("listaPendientes",
                        servicio.listarPorUsuario(idUserPend, "Pendiente"));
                request.getRequestDispatcher("/templates/trabajador/trabajos_pendientes.jsp")
                       .forward(request, response);
                break;

            case "finalizados":
                if (!verificarSesionUsuario(request, response)) return;
                int idUserFin = (int) request.getSession(false).getAttribute("idUsuario");
                request.setAttribute("listaFinalizados",
                        servicio.listarPorUsuario(idUserFin, "Finalizado"));
                request.getRequestDispatcher("/templates/trabajador/trabajos_finalizados.jsp")
                       .forward(request, response);
                break;

            default:
                response.sendRedirect("ServletTrabajo?accion=listar");
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
                response.sendRedirect("ServletTrabajo?accion=listar");
        }
    }

    // ── Helper: registrar trabajo + asignación ────────────────────────────────
    private void registrarTrabajo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String nombre           = request.getParameter("nombreTrabajo");
        String desc             = request.getParameter("descripcion");
        String fechaStr         = request.getParameter("fechaAsignacion");
        String idCultivoStr     = request.getParameter("idCultivo");
        String idUsuarioStr     = request.getParameter("idUsuario");
        String idTipoTrabajoStr = request.getParameter("idTipoTrabajo");

        if (estaVacio(nombre) || estaVacio(desc) || estaVacio(fechaStr)
                || estaVacio(idCultivoStr) || estaVacio(idUsuarioStr)
                || estaVacio(idTipoTrabajoStr)) {
            enviarError("Todos los campos son obligatorios.", request, response);
            return;
        }

        try {
            int idCultivo        = Integer.parseInt(idCultivoStr);
            int idUsuario        = Integer.parseInt(idUsuarioStr);
            int idTipoTrabajo    = Integer.parseInt(idTipoTrabajoStr);
            Date fechaAsignacion = Date.valueOf(fechaStr);

            trabajo t = new trabajo(nombre.trim(), desc.trim(), idTipoTrabajo);
            boolean ok = servicio.registrarTrabajoCompleto(t, idCultivo, idUsuario, fechaAsignacion);

            if (ok) {
                response.sendRedirect("ServletTrabajo?accion=listar&status=success");
            } else {
                enviarError("Error al guardar en base de datos.", request, response);
            }
        } catch (IllegalArgumentException e) {
            enviarError("Datos inválidos. Verifica todos los campos.", request, response);
        }
    }

    // ── Helper: actualizar estado (trabajador) ────────────────────────────────
    private void actualizarEstado(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idAsignacionStr = request.getParameter("idAsignacion");
        String observaciones   = request.getParameter("observaciones");
        String btnAccion       = request.getParameter("btnAccion");

        if (estaVacio(idAsignacionStr)) {
            response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=error");
            return;
        }

        try {
            int idAsignacion = Integer.parseInt(idAsignacionStr);

            String nuevoEstado;
            switch (btnAccion != null ? btnAccion : "") {
                case "iniciar":   nuevoEstado = "En proceso"; break;
                case "finalizar": nuevoEstado = "Finalizado"; break;
                default:          nuevoEstado = null;          // solo guarda observaciones
            }

            boolean ok = servicio.actualizarEstado(idAsignacion, observaciones, nuevoEstado);

            String status = ok
                    ? (nuevoEstado != null
                        ? nuevoEstado.toLowerCase().replace(" ", "_")
                        : "guardado")
                    : "error";

            response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=" + status);

        } catch (NumberFormatException e) {
            response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=error");
        }
    }

    // ── Helper: reenviar formulario con error ─────────────────────────────────
    private void enviarError(String mensaje, HttpServletRequest request,
                             HttpServletResponse response)
            throws ServletException, IOException {
        request.setAttribute("mensajeError",     mensaje);
        request.setAttribute("listaCultivos",    new CultivoDao().listarCultivos());
        request.setAttribute("listaUsuarios",    new UsuarioDao().listarUsuariosActivos());
        request.setAttribute("listaTiposTrabajo",new TipoTrabajoDao().listarTipos());
        request.getRequestDispatcher("/templates/administrador/asignar_trabajos.jsp")
               .forward(request, response);
    }
}