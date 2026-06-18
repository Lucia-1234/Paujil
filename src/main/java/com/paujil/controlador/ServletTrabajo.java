package com.paujil.controlador;

import com.paujil.dao.CultivoDao;
import com.paujil.dao.TipoTrabajoDao;
import com.paujil.dao.UsuarioDao;
import com.paujil.modelo.tipoTrabajo;
import com.paujil.modelo.trabajo;
import com.paujil.servicio.AsignacionServicio;
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

        // ── CORRECCIÓN CRÍTICA: default al final, nunca antes de los casos ──
        switch (accion) {
            case "registrar":
                if (!verificarSesionAdmin(request, response)) return;
                registrarTrabajo(request, response);
                break;

            case "actualizarEstado":
                if (!verificarSesionUsuario(request, response)) return;
                actualizarEstado(request, response);
                break;

            case "crearTipo":
                if (!verificarSesionAdmin(request, response)) return;
                crearTipo(request, response);
                break;

            case "eliminarTipo":
                if (!verificarSesionAdmin(request, response)) return;
                eliminarTipo(request, response);
                break;

            default:
                response.sendRedirect("ServletTrabajo?accion=listar");
                break;
        }
    }

    // ── Helper: registrar trabajo + asignación ────────────────────────────────
    // nombreTrabajo eliminado — el nombre ya no es un campo del formulario.
    // La tabla trabajos lo sigue teniendo en DB, se rellena con el nombre
    // del tipo de trabajo para mantener la FK sin romper el schema existente.
    private void registrarTrabajo(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String desc             = request.getParameter("descripcion");
        String fechaStr         = request.getParameter("fechaAsignacion");
        String idCultivoStr     = request.getParameter("idCultivo");
        String idUsuarioStr     = request.getParameter("idUsuario");
        String idTipoTrabajoStr = request.getParameter("idTipoTrabajo");

        if (estaVacio(desc) || estaVacio(fechaStr)
                || estaVacio(idCultivoStr) || estaVacio(idUsuarioStr)
                || estaVacio(idTipoTrabajoStr)) {
            enviarError("Todos los campos son obligatorios.", request, response);
            return;
        }

        // Validaciones de longitud
        if (desc.trim().length() < 10) {
            enviarError("La descripción debe tener al menos 10 caracteres.", request, response);
            return;
        }
        if (desc.trim().length() > 500) {
            enviarError("La descripción no puede superar 500 caracteres.", request, response);
            return;
        }

        try {
            int idCultivo        = Integer.parseInt(idCultivoStr);
            int idUsuario        = Integer.parseInt(idUsuarioStr);
            int idTipoTrabajo    = Integer.parseInt(idTipoTrabajoStr);
            Date fechaAsignacion = Date.valueOf(fechaStr);

            // Nombre del trabajo = nombre del tipo (se obtiene en el servicio/DAO)
            trabajo t = new trabajo(idTipoTrabajo);
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

        // Validar longitud de observaciones si vienen rellenas
        if (observaciones != null && observaciones.trim().length() > 500) {
            response.sendRedirect("ServletTrabajo?accion=misTrabajos&status=error_obs");
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

    // ── Helper: crear tipo de trabajo (responde JSON) ─────────────────────────
    private void crearTipo(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        String nombre = request.getParameter("nombreTipo");

        if (estaVacio(nombre)) {
            response.getWriter().write("{\"ok\":false,\"mensaje\":\"El nombre no puede estar vacío.\"}");
            return;
        }
        nombre = nombre.trim();
        if (nombre.length() < 3) {
            response.getWriter().write("{\"ok\":false,\"mensaje\":\"El nombre debe tener al menos 3 caracteres.\"}");
            return;
        }
        if (nombre.length() > 50) {
            response.getWriter().write("{\"ok\":false,\"mensaje\":\"El nombre no puede superar 50 caracteres.\"}");
            return;
        }
        // Solo letras, espacios, tildes y guion
        if (!nombre.matches("[\\p{L}\\s\\-]+")) {
            response.getWriter().write("{\"ok\":false,\"mensaje\":\"El nombre solo puede contener letras, espacios y guiones.\"}");
            return;
        }

        TipoTrabajoDao dao = new TipoTrabajoDao();
        boolean ok = dao.registrarTipo(nombre);
        if (!ok) {
            response.getWriter().write("{\"ok\":false,\"mensaje\":\"Ya existe un tipo con ese nombre o error al guardar.\"}");
            return;
        }

        List<tipoTrabajo> tipos = dao.listarTipos();
        StringBuilder json = new StringBuilder("{\"ok\":true,\"tipos\":[");
        for (int i = 0; i < tipos.size(); i++) {
            if (i > 0) json.append(",");
            json.append("{\"id\":").append(tipos.get(i).getIdTipoTrabajo())
                .append(",\"nombre\":\"").append(escaparJson(tipos.get(i).getNombreTipo()))
                .append("\"}");
        }
        json.append("]}");
        response.getWriter().write(json.toString());
    }

    // ── Helper: eliminar tipo de trabajo (responde JSON) ──────────────────────
    private void eliminarTipo(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        String idStr = request.getParameter("id");
        if (estaVacio(idStr)) {
            response.getWriter().write("{\"ok\":false,\"mensaje\":\"ID inválido.\"}");
            return;
        }
        try {
            boolean ok = new TipoTrabajoDao().eliminarTipo(Integer.parseInt(idStr));
            response.getWriter().write("{\"ok\":" + ok + "}");
        } catch (NumberFormatException e) {
            response.getWriter().write("{\"ok\":false,\"mensaje\":\"ID inválido.\"}");
        }
    }

    private String escaparJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}